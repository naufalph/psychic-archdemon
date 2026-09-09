package com.rumantra.project.service;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rumantra.bidding.domain.Bid;
import com.rumantra.bidding.domain.BidPaymentPhase;
import com.rumantra.bidding.domain.BidStatus;
import com.rumantra.bidding.repository.BidPaymentPhaseRepository;
import com.rumantra.bidding.repository.BidRepository;
import com.rumantra.client.domain.Project;
import com.rumantra.client.repository.ProjectRepository;
import com.rumantra.project.domain.ContractParty;
import com.rumantra.project.domain.ProjectContract;
import com.rumantra.project.domain.ProjectContractAcceptance;
import com.rumantra.project.dto.AcceptContractRequest;
import com.rumantra.project.dto.ContractAcceptanceResponse;
import com.rumantra.project.dto.ContractAcceptanceStatusResponse;
import com.rumantra.project.dto.ContractDocumentResponse;
import com.rumantra.project.dto.ContractTermsSnapshot;
import com.rumantra.project.repository.ProjectContractAcceptanceRepository;
import com.rumantra.project.repository.ProjectContractRepository;
import com.rumantra.shared.HashUtils;
import com.rumantra.shared.exception.BusinessException;
import com.rumantra.shared.exception.ExceptionConstants;
import com.rumantra.user.domain.User;

import lombok.RequiredArgsConstructor;

/**
 * Write side of the project contract: snapshot the accepted bid once, then record each party's
 * signature against it.
 */
@Service
@RequiredArgsConstructor
public class ContractDocumentService {

  private static final int MIN_SIGNATURE_LENGTH = 3;

  private final ProjectRepository projectRepository;
  private final BidRepository bidRepository;
  private final BidPaymentPhaseRepository bidPaymentPhaseRepository;
  private final ProjectContractRepository contractRepository;
  private final ProjectContractAcceptanceRepository acceptanceRepository;
  private final ContractTemplateRenderer renderer;
  private final ObjectMapper objectMapper;

  @Transactional
  public ContractDocumentResponse getDocument(Long projectId, Long userId) {
    Project project = loadProject(projectId);
    ContractParty party = resolveParty(userId, project);
    ProjectContract contract = getOrCreate(project);
    return toDocument(contract, party);
  }

  @Transactional
  public ContractAcceptanceStatusResponse getStatus(Long projectId, Long userId) {
    Project project = loadProject(projectId);
    ContractParty party = resolveParty(userId, project);
    ProjectContract contract = getOrCreate(project);

    List<ProjectContractAcceptance> rows =
        acceptanceRepository.findByContractIdOrderByAcceptedAtAsc(contract.getId());
    ProjectContractAcceptance client = pick(rows, ContractParty.CLIENT);
    ProjectContractAcceptance architect = pick(rows, ContractParty.ARCHITECT);

    return ContractAcceptanceStatusResponse.builder()
        .contractId(contract.getId())
        .contentHash(contract.getContentHash())
        .myParty(party.name())
        .expectedSignatureName(expectedName(project, party, contract))
        .clientAccepted(client != null)
        .architectAccepted(architect != null)
        .myAcceptance(toAcceptance(party == ContractParty.CLIENT ? client : architect))
        .clientAcceptance(toAcceptance(client))
        .architectAcceptance(toAcceptance(architect))
        .build();
  }

  @Transactional
  public ContractAcceptanceStatusResponse accept(
      Long projectId, Long userId, AcceptContractRequest request, String ip, String userAgent) {
    Project project = loadProject(projectId);
    ContractParty party = resolveParty(userId, project);
    ProjectContract contract = getOrCreate(project);

    // The signer echoes back the hash of what they were shown; a mismatch means the tab predates
    // the contract they are agreeing to. Same replay guard AgreementService uses for the T&C.
    if (!contract.getContentHash().equals(request.getContentHash())) {
      throw new BusinessException(ExceptionConstants.CONTRACT_STALE);
    }
    if (acceptanceRepository.existsByProjectIdAndParty(projectId, party)) {
      throw new BusinessException(ExceptionConstants.CONTRACT_ALREADY_SIGNED);
    }

    verifySignatureName(request.getSignatureName(), expectedName(project, party, contract));

    acceptanceRepository.save(
        ProjectContractAcceptance.builder()
            .contractId(contract.getId())
            .projectId(projectId)
            .userId(userId)
            .party(party)
            .signatureName(request.getSignatureName().trim())
            .contentHash(contract.getContentHash())
            .lang("id".equalsIgnoreCase(request.getLang()) ? "id" : "en")
            .ipAddress(ip)
            .userAgent(userAgent)
            .build());

    return getStatus(projectId, userId);
  }

  /** Guard for the negotiation confirm: neither side may confirm terms they have not signed. */
  @Transactional(readOnly = true)
  public void verifyAccepted(Long projectId, ContractParty party) {
    if (!acceptanceRepository.existsByProjectIdAndParty(projectId, party)) {
      throw new BusinessException(ExceptionConstants.CONTRACT_NOT_ACCEPTED);
    }
  }

  /**
   * The contract is snapshotted the first time either party opens it, which is why a read path
   * writes. Freezing it at that moment is the point: both parties then sign the same terms even if
   * the underlying bid rows are touched afterwards.
   */
  private ProjectContract getOrCreate(Project project) {
    return contractRepository.findByProjectId(project.getId()).orElseGet(() -> generate(project));
  }

  private ProjectContract generate(Project project) {
    Bid bid =
        bidRepository.findByProjectIdAndStatus(project.getId(), BidStatus.ACCEPTED).stream()
            .findFirst()
            .orElseThrow(() -> new BusinessException(ExceptionConstants.CONTRACT_NOT_AVAILABLE));

    List<BidPaymentPhase> bidPhases =
        bidPaymentPhaseRepository.findByBidIdOrderByPhaseNumber(bid.getId());
    ContractTermsSnapshot terms = snapshot(project, bid, bidPhases);

    ProjectContract contract =
        ProjectContract.builder()
            .projectId(project.getId())
            .bidId(bid.getId())
            .templateVersion(ContractTemplateRenderer.TEMPLATE_VERSION)
            .termsSnapshot(terms)
            .bodyEn(renderer.render("en", terms))
            .bodyId(renderer.render("id", terms))
            .contentHash(hash(terms))
            .build();

    try {
      return contractRepository.saveAndFlush(contract);
    } catch (DataIntegrityViolationException e) {
      // Both parties opened the contract at once; the unique constraint on project_id picked a
      // winner and that row is the contract.
      return contractRepository
          .findByProjectId(project.getId())
          .orElseThrow(() -> new BusinessException(ExceptionConstants.CONTRACT_NOT_AVAILABLE));
    }
  }

  private ContractTermsSnapshot snapshot(
      Project project, Bid bid, List<BidPaymentPhase> bidPhases) {
    User clientUser = project.getClient().getUser();
    User architectUser = bid.getArchitect().getUser();

    return ContractTermsSnapshot.builder()
        .projectId(project.getId())
        .projectTitle(project.getTitle())
        .projectCity(project.getCity())
        .projectCategory(project.getProjectCategory())
        .scopeOfWork(project.getScopeOfWork())
        .clientUserId(clientUser.getId())
        .clientName(fullName(clientUser))
        .bidId(bid.getId())
        .architectId(bid.getArchitect().getId())
        .architectUserId(architectUser.getId())
        .architectName(fullName(architectUser))
        .architectCompany(bid.getArchitect().getCompanyName())
        .architectCity(bid.getArchitect().getCity())
        .totalFee(bid.getBidAmount())
        .timelineDays(bid.getProposedTimelineDays())
        .phases(
            bidPhases.stream()
                .map(
                    phase ->
                        ContractTermsSnapshot.Phase.builder()
                            .phaseNumber(phase.getPhaseNumber())
                            .title(phase.getTitle())
                            .amount(phase.getAmount())
                            .estimatedDays(phase.getEstimatedDays())
                            .revisionRounds(phase.getRevisionRounds())
                            .deliverables(phase.getDeliverables())
                            .build())
                .collect(Collectors.toList()))
        .build();
  }

  /**
   * Hashed over the terms rather than either rendering, so the English and Indonesian bodies of one
   * contract share a hash and the two parties provably sign the same deal.
   */
  private String hash(ContractTermsSnapshot terms) {
    try {
      return HashUtils.sha256Hex(
          ContractTemplateRenderer.TEMPLATE_VERSION
              + "\n"
              + objectMapper.writeValueAsString(terms));
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Unable to serialise contract terms", e);
    }
  }

  private ContractParty resolveParty(Long userId, Project project) {
    if (project.getClient().getUser().getId().equals(userId)) {
      return ContractParty.CLIENT;
    }
    boolean isWinningArchitect =
        bidRepository.findByProjectIdAndStatus(project.getId(), BidStatus.ACCEPTED).stream()
            .anyMatch(b -> b.getArchitect().getUser().getId().equals(userId));
    if (isWinningArchitect) {
      return ContractParty.ARCHITECT;
    }
    throw new BusinessException(ExceptionConstants.UNAUTHORIZED_PHASE_ACCESS);
  }

  private String expectedName(Project project, ContractParty party, ProjectContract contract) {
    ContractTermsSnapshot terms = contract.getTermsSnapshot();
    if (party == ContractParty.CLIENT) {
      return terms != null && terms.getClientName() != null
          ? terms.getClientName()
          : fullName(project.getClient().getUser());
    }
    return terms == null ? null : terms.getArchitectName();
  }

  /**
   * Compared on a normalised copy so casing and spacing do not matter; what the signer typed is
   * stored untouched, because the signature is the thing they wrote.
   */
  private void verifySignatureName(String typed, String expected) {
    String given = normalise(typed);
    if (given.length() < MIN_SIGNATURE_LENGTH) {
      throw new BusinessException(ExceptionConstants.CONTRACT_SIGNATURE_MISMATCH);
    }
    String target = normalise(expected);
    if (target.isEmpty()) {
      return;
    }
    if (!given.equals(target)) {
      throw new BusinessException(ExceptionConstants.CONTRACT_SIGNATURE_MISMATCH);
    }
  }

  private String normalise(String value) {
    if (value == null) {
      return "";
    }
    String stripped =
        Normalizer.normalize(value, Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "")
            .toLowerCase(Locale.ROOT);
    return stripped.replaceAll("[^a-z0-9 ]", " ").replaceAll("\\s+", " ").trim();
  }

  private String fullName(User user) {
    String name =
        Stream.of(user.getFirstName(), user.getLastName())
            .filter(part -> part != null && !part.isBlank())
            .collect(Collectors.joining(" "));
    return name.isBlank() ? user.getEmail() : name;
  }

  private Project loadProject(Long projectId) {
    return projectRepository
        .findById(projectId)
        .orElseThrow(() -> new BusinessException(ExceptionConstants.PROJECT_NOT_FOUND));
  }

  private ProjectContractAcceptance pick(
      List<ProjectContractAcceptance> rows, ContractParty party) {
    return rows.stream().filter(r -> r.getParty() == party).findFirst().orElse(null);
  }

  private ContractDocumentResponse toDocument(ProjectContract contract, ContractParty party) {
    return ContractDocumentResponse.builder()
        .contractId(contract.getId())
        .projectId(contract.getProjectId())
        .templateVersion(contract.getTemplateVersion())
        .contentHash(contract.getContentHash())
        .generatedAt(contract.getGeneratedAt())
        .bodyEn(contract.getBodyEn())
        .bodyId(contract.getBodyId())
        .terms(contract.getTermsSnapshot())
        .myParty(party.name())
        .acceptances(
            acceptanceRepository.findByContractIdOrderByAcceptedAtAsc(contract.getId()).stream()
                .map(this::toAcceptance)
                .collect(Collectors.toList()))
        .build();
  }

  private ContractAcceptanceResponse toAcceptance(ProjectContractAcceptance row) {
    if (row == null) {
      return null;
    }
    return ContractAcceptanceResponse.builder()
        .party(row.getParty().name())
        .userId(row.getUserId())
        .signatureName(row.getSignatureName())
        .lang(row.getLang())
        .acceptedAt(row.getAcceptedAt())
        .build();
  }
}
