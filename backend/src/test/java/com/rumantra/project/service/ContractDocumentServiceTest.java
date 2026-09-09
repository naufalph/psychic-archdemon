package com.rumantra.project.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rumantra.architect.domain.Architect;
import com.rumantra.bidding.domain.Bid;
import com.rumantra.bidding.domain.BidPaymentPhase;
import com.rumantra.bidding.domain.BidStatus;
import com.rumantra.bidding.repository.BidPaymentPhaseRepository;
import com.rumantra.bidding.repository.BidRepository;
import com.rumantra.client.domain.Client;
import com.rumantra.client.domain.Project;
import com.rumantra.client.repository.ProjectRepository;
import com.rumantra.project.domain.ContractParty;
import com.rumantra.project.domain.ProjectContract;
import com.rumantra.project.domain.ProjectContractAcceptance;
import com.rumantra.project.dto.AcceptContractRequest;
import com.rumantra.project.dto.ContractDocumentResponse;
import com.rumantra.project.repository.ProjectContractAcceptanceRepository;
import com.rumantra.project.repository.ProjectContractRepository;
import com.rumantra.shared.exception.BusinessException;
import com.rumantra.shared.exception.ExceptionConstants;
import com.rumantra.user.domain.User;

@ExtendWith(MockitoExtension.class)
class ContractDocumentServiceTest {

  private static final Long PROJECT_ID = 7L;
  private static final Long CLIENT_USER_ID = 100L;
  private static final Long ARCHITECT_USER_ID = 200L;
  private static final Long STRANGER_USER_ID = 300L;

  @Mock private ProjectRepository projectRepository;
  @Mock private BidRepository bidRepository;
  @Mock private BidPaymentPhaseRepository bidPaymentPhaseRepository;
  @Mock private ProjectContractRepository contractRepository;
  @Mock private ProjectContractAcceptanceRepository acceptanceRepository;

  @Spy private ContractTemplateRenderer renderer = new ContractTemplateRenderer();
  @Spy private ObjectMapper objectMapper = new ObjectMapper();

  @InjectMocks private ContractDocumentService service;

  private Project project;
  private Bid bid;

  @BeforeEach
  void setUp() {
    User clientUser =
        User.builder().id(CLIENT_USER_ID).firstName("Rina").lastName("Wijaya").build();
    Client client = Client.builder().id(1L).user(clientUser).build();

    project =
        Project.builder()
            .id(PROJECT_ID)
            .client(client)
            .title("Rumah Kayu Bandung")
            .city("Bandung")
            .scopeOfWork("Full design services")
            .build();

    User architectUser =
        User.builder().id(ARCHITECT_USER_ID).firstName("Budi").lastName("Santoso").build();
    Architect architect =
        Architect.builder()
            .id(2L)
            .user(architectUser)
            .companyName("Studio Kayu")
            .city("Bandung")
            .build();

    bid =
        Bid.builder()
            .id(50L)
            .project(project)
            .architect(architect)
            .bidAmount(new BigDecimal("120000000"))
            .proposedTimelineDays(90)
            .build();
  }

  private void projectFound() {
    when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));
  }

  private void acceptedBidFound() {
    when(bidRepository.findByProjectIdAndStatus(PROJECT_ID, BidStatus.ACCEPTED))
        .thenReturn(List.of(bid));
    when(bidPaymentPhaseRepository.findByBidIdOrderByPhaseNumber(50L))
        .thenReturn(
            List.of(
                BidPaymentPhase.builder()
                    .id(1L)
                    .phaseNumber(1)
                    .title("Schematic Design")
                    .amount(new BigDecimal("40000000"))
                    .estimatedDays(30)
                    .revisionRounds(2)
                    .deliverables(List.of("SITE_PLAN", "FLOOR_PLAN"))
                    .build()));
  }

  private void noContractYet() {
    when(contractRepository.findByProjectId(PROJECT_ID)).thenReturn(Optional.empty());
    when(contractRepository.saveAndFlush(any(ProjectContract.class)))
        .thenAnswer(
            invocation -> {
              ProjectContract saved = invocation.getArgument(0);
              saved.setId(900L);
              return saved;
            });
  }

  @Test
  void snapshotsTheAcceptedBidOnFirstRead() {
    projectFound();
    acceptedBidFound();
    noContractYet();
    when(acceptanceRepository.findByContractIdOrderByAcceptedAtAsc(900L)).thenReturn(List.of());

    ContractDocumentResponse doc = service.getDocument(PROJECT_ID, CLIENT_USER_ID);

    assertEquals("CLIENT", doc.getMyParty());
    assertEquals(PROJECT_ID, doc.getProjectId());
    assertEquals(new BigDecimal("120000000"), doc.getTerms().getTotalFee());
    assertEquals(90, doc.getTerms().getTimelineDays());
    assertEquals(1, doc.getTerms().getPhases().size());
    assertEquals(
        List.of("SITE_PLAN", "FLOOR_PLAN"), doc.getTerms().getPhases().get(0).getDeliverables());
    // Both renderings carry the commercial terms, and share one hash because it covers the
    // snapshot rather than either body.
    assertTrue(doc.getBodyEn().contains("Schematic Design"));
    assertTrue(doc.getBodyId().contains("Schematic Design"));
    assertTrue(doc.getBodyEn().contains("120,000,000"));
    assertEquals(64, doc.getContentHash().length());
  }

  @Test
  void reusesAnExistingContractRatherThanRegenerating() {
    projectFound();
    when(bidRepository.findByProjectIdAndStatus(PROJECT_ID, BidStatus.ACCEPTED))
        .thenReturn(List.of(bid));
    ProjectContract existing = existingContract();
    when(contractRepository.findByProjectId(PROJECT_ID)).thenReturn(Optional.of(existing));
    when(acceptanceRepository.findByContractIdOrderByAcceptedAtAsc(900L)).thenReturn(List.of());

    ContractDocumentResponse doc = service.getDocument(PROJECT_ID, ARCHITECT_USER_ID);

    assertEquals("ARCHITECT", doc.getMyParty());
    assertEquals("hash-abc", doc.getContentHash());
    verify(contractRepository, never()).saveAndFlush(any());
  }

  @Test
  void refusesWhenNoBidHasBeenAccepted() {
    projectFound();
    when(bidRepository.findByProjectIdAndStatus(PROJECT_ID, BidStatus.ACCEPTED))
        .thenReturn(List.of());

    BusinessException e =
        assertThrows(
            BusinessException.class, () -> service.getDocument(PROJECT_ID, CLIENT_USER_ID));
    assertEquals(ExceptionConstants.CONTRACT_NOT_AVAILABLE, e.getExceptionCode());
  }

  @Test
  void refusesAnyoneWhoIsNeitherParty() {
    projectFound();
    when(bidRepository.findByProjectIdAndStatus(PROJECT_ID, BidStatus.ACCEPTED))
        .thenReturn(List.of(bid));

    BusinessException e =
        assertThrows(
            BusinessException.class, () -> service.getDocument(PROJECT_ID, STRANGER_USER_ID));
    assertEquals(ExceptionConstants.UNAUTHORIZED_PHASE_ACCESS, e.getExceptionCode());
  }

  @Test
  void rejectsASignatureCarryingAStaleHash() {
    signingSetup();

    BusinessException e =
        assertThrows(
            BusinessException.class,
            () ->
                service.accept(
                    PROJECT_ID, CLIENT_USER_ID, request("stale", "Rina Wijaya"), null, null));
    assertEquals(ExceptionConstants.CONTRACT_STALE, e.getExceptionCode());
  }

  @Test
  void rejectsASignatureThatIsNotTheSignersName() {
    signingSetup();
    when(acceptanceRepository.existsByProjectIdAndParty(PROJECT_ID, ContractParty.CLIENT))
        .thenReturn(false);

    BusinessException e =
        assertThrows(
            BusinessException.class,
            () ->
                service.accept(
                    PROJECT_ID, CLIENT_USER_ID, request("hash-abc", "Someone Else"), null, null));
    assertEquals(ExceptionConstants.CONTRACT_SIGNATURE_MISMATCH, e.getExceptionCode());
  }

  @Test
  void acceptsTheSignersOwnNameInAnyCasingAndStoresItAsTyped() {
    signingSetup();
    when(acceptanceRepository.existsByProjectIdAndParty(PROJECT_ID, ContractParty.CLIENT))
        .thenReturn(false);
    when(acceptanceRepository.findByContractIdOrderByAcceptedAtAsc(900L)).thenReturn(List.of());

    service.accept(
        PROJECT_ID, CLIENT_USER_ID, request("hash-abc", "  rina   WIJAYA "), "1.2.3.4", "UA");

    verify(acceptanceRepository)
        .save(
            argThat(
                (ProjectContractAcceptance row) ->
                    "rina   WIJAYA".equals(row.getSignatureName())
                        && row.getParty() == ContractParty.CLIENT
                        && row.getProjectId().equals(PROJECT_ID)
                        && row.getContractId().equals(900L)
                        && "1.2.3.4".equals(row.getIpAddress())));
  }

  @Test
  void refusesASecondSignatureFromTheSameParty() {
    signingSetup();
    when(acceptanceRepository.existsByProjectIdAndParty(PROJECT_ID, ContractParty.CLIENT))
        .thenReturn(true);

    BusinessException e =
        assertThrows(
            BusinessException.class,
            () ->
                service.accept(
                    PROJECT_ID, CLIENT_USER_ID, request("hash-abc", "Rina Wijaya"), null, null));
    assertEquals(ExceptionConstants.CONTRACT_ALREADY_SIGNED, e.getExceptionCode());
  }

  @Test
  void verifyAcceptedThrowsUntilThePartyHasSigned() {
    when(acceptanceRepository.existsByProjectIdAndParty(PROJECT_ID, ContractParty.ARCHITECT))
        .thenReturn(false);

    BusinessException e =
        assertThrows(
            BusinessException.class,
            () -> service.verifyAccepted(PROJECT_ID, ContractParty.ARCHITECT));
    assertEquals(ExceptionConstants.CONTRACT_NOT_ACCEPTED, e.getExceptionCode());

    when(acceptanceRepository.existsByProjectIdAndParty(PROJECT_ID, ContractParty.ARCHITECT))
        .thenReturn(true);
    assertDoesNotThrow(() -> service.verifyAccepted(PROJECT_ID, ContractParty.ARCHITECT));
  }

  /** The signing tests all act as the client, whom resolveParty matches without touching bids. */
  private void signingSetup() {
    projectFound();
    when(contractRepository.findByProjectId(PROJECT_ID))
        .thenReturn(Optional.of(existingContract()));
  }

  private ProjectContract existingContract() {
    return ProjectContract.builder()
        .id(900L)
        .projectId(PROJECT_ID)
        .bidId(50L)
        .templateVersion(ContractTemplateRenderer.TEMPLATE_VERSION)
        .termsSnapshot(
            com.rumantra.project.dto.ContractTermsSnapshot.builder()
                .clientName("Rina Wijaya")
                .architectName("Budi Santoso")
                .build())
        .bodyEn("body")
        .bodyId("body")
        .contentHash("hash-abc")
        .build();
  }

  private AcceptContractRequest request(String hash, String name) {
    return AcceptContractRequest.builder().contentHash(hash).signatureName(name).lang("en").build();
  }
}
