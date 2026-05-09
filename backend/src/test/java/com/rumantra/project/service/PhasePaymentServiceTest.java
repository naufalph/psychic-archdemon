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
import org.mockito.junit.jupiter.MockitoExtension;

import com.rumantra.architect.repository.ArchitectRepository;
import com.rumantra.bidding.domain.BidStatus;
import com.rumantra.bidding.repository.BidRepository;
import com.rumantra.client.domain.*;
import com.rumantra.client.repository.ClientRepository;
import com.rumantra.client.repository.ProjectRepository;
import com.rumantra.integration.xendit.XenditService;
import com.rumantra.integration.xendit.dto.XenditInvoiceResponse;
import com.rumantra.integration.xendit.dto.XenditInvoiceWebhook;
import com.rumantra.payment.domain.PhasePayment;
import com.rumantra.payment.domain.PhasePaymentStatus;
import com.rumantra.payment.repository.PhasePaymentRepository;
import com.rumantra.project.domain.*;
import com.rumantra.project.repository.*;
import com.rumantra.shared.exception.BusinessException;
import com.rumantra.shared.exception.ExceptionConstants;
import com.rumantra.user.domain.User;
import com.rumantra.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class PhasePaymentServiceTest {

  @InjectMocks private PhasePaymentService service;

  @Mock private ProjectPhaseRepository projectPhaseRepository;
  @Mock private PhaseDisbursementRepository phaseDisbursementRepository;
  @Mock private PhaseDeliverableRepository phaseDeliverableRepository;
  @Mock private PhaseProcessLogRepository phaseProcessLogRepository;
  @Mock private PhasePaymentRepository phasePaymentRepository;
  @Mock private ProjectRepository projectRepository;
  @Mock private ClientRepository clientRepository;
  @Mock private ArchitectRepository architectRepository;
  @Mock private BidRepository bidRepository;
  @Mock private UserRepository userRepository;
  @Mock private XenditService xenditService;

  private Project project;
  private Client client;
  private User clientUser;
  private ProjectPhase phase;
  private PhasePayment payment;

  @BeforeEach
  void setUp() {
    clientUser = new User();
    clientUser.setId(1L);

    client = new Client();
    client.setId(10L);
    client.setUser(clientUser);

    project = new Project();
    project.setId(100L);
    project.setClient(client);
    project.setStatus(ProjectStatus.IN_PROGRESS);

    phase =
        ProjectPhase.builder()
            .id(200L)
            .project(project)
            .phaseNumber(1)
            .title("Design Phase")
            .amount(new BigDecimal("5000000"))
            .status(PhaseStatus.BILLED)
            .build();

    payment =
        PhasePayment.builder()
            .id(300L)
            .projectPhase(phase)
            .project(project)
            .client(client)
            .amount(new BigDecimal("5000000"))
            .xenditInvoiceId("inv_123")
            .xenditReferenceId("proj_phase_200_1234")
            .status(PhasePaymentStatus.PENDING)
            .build();
  }

  @Test
  void handlePaymentWebhook_isIdempotent() {
    payment.setStatus(PhasePaymentStatus.COMPLETED);

    XenditInvoiceWebhook webhook = new XenditInvoiceWebhook();
    webhook.setExternalId("proj_phase_200_1234");
    webhook.setStatus("PAID");
    webhook.setId("inv_123");

    when(phasePaymentRepository.findByXenditReferenceId("proj_phase_200_1234"))
        .thenReturn(Optional.of(payment));

    service.handlePaymentWebhook(webhook);

    // No save should occur — already COMPLETED
    verify(phasePaymentRepository, never()).save(any());
    verify(projectPhaseRepository, never()).save(any());
  }

  @Test
  void approveDeliverable_failsIfNotDelivered() {
    phase.setStatus(PhaseStatus.BILLED);

    when(projectPhaseRepository.findById(200L)).thenReturn(Optional.of(phase));
    when(clientRepository.findByUserId(1L)).thenReturn(Optional.of(client));
    when(projectRepository.findById(100L)).thenReturn(Optional.of(project));

    BusinessException ex =
        assertThrows(BusinessException.class, () -> service.approveDeliverable(200L, 1L));

    assertEquals(ExceptionConstants.PHASE_WRONG_STATUS, ex.getExceptionCode());
  }

  @Test
  void advanceProjectOrClose_billsNextPhaseIfExists() {
    ProjectPhase nextPhase =
        ProjectPhase.builder()
            .id(201L)
            .project(project)
            .phaseNumber(2)
            .title("Construction Phase")
            .amount(new BigDecimal("3000000"))
            .status(PhaseStatus.PENDING)
            .build();

    when(projectPhaseRepository.findFirstByProjectIdAndStatusOrderByPhaseNumberAsc(
            100L, PhaseStatus.PENDING))
        .thenReturn(Optional.of(nextPhase));

    when(projectPhaseRepository.findById(201L)).thenReturn(Optional.of(nextPhase));
    when(clientRepository.findByUserId(1L)).thenReturn(Optional.of(client));
    when(projectRepository.findById(100L)).thenReturn(Optional.of(project));

    XenditInvoiceResponse xenditResp = new XenditInvoiceResponse();
    xenditResp.setId("inv_next");
    xenditResp.setInvoiceUrl("https://checkout.xendit.co/next");
    xenditResp.setExpiryDate("2026-05-02T00:00:00+07:00");

    when(xenditService.createInvoice(any())).thenReturn(xenditResp);
    when(phasePaymentRepository.findByProjectPhaseId(201L)).thenReturn(Optional.empty());
    when(bidRepository.findByProjectIdAndStatus(100L, BidStatus.ACCEPTED)).thenReturn(List.of());
    when(phasePaymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    when(projectPhaseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    when(phaseProcessLogRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    com.rumantra.integration.xendit.dto.XenditPayoutCallback callback =
        new com.rumantra.integration.xendit.dto.XenditPayoutCallback();
    callback.setEvent("payout.succeeded");
    callback.setId("payout_abc");
    callback.setReferenceId("phase_payout_200_1234");

    PhaseDisbursement disbursement =
        PhaseDisbursement.builder()
            .id(400L)
            .phase(phase)
            .amount(new BigDecimal("5000000"))
            .status(DisbursementStatus.ACCEPTED)
            .xenditPayoutId("payout_abc")
            .xenditReferenceId("phase_payout_200_1234")
            .build();

    when(phaseDisbursementRepository.findByXenditPayoutId("payout_abc"))
        .thenReturn(Optional.of(disbursement));
    when(phaseDisbursementRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    when(userRepository.findById(any())).thenReturn(Optional.empty());

    service.handlePayoutCallback(callback);

    // Verify invoice was created for next phase
    verify(xenditService).createInvoice(any());
  }

  @Test
  void advanceProjectOrClose_closesProjectIfNoMorePhases() {
    when(projectPhaseRepository.findFirstByProjectIdAndStatusOrderByPhaseNumberAsc(
            100L, PhaseStatus.PENDING))
        .thenReturn(Optional.empty());
    when(projectRepository.findById(100L)).thenReturn(Optional.of(project));
    when(projectRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    com.rumantra.integration.xendit.dto.XenditPayoutCallback callback =
        new com.rumantra.integration.xendit.dto.XenditPayoutCallback();
    callback.setEvent("payout.succeeded");
    callback.setId("payout_xyz");
    callback.setReferenceId("phase_payout_200_9999");

    PhaseDisbursement disbursement =
        PhaseDisbursement.builder()
            .id(401L)
            .phase(phase)
            .amount(new BigDecimal("5000000"))
            .status(DisbursementStatus.ACCEPTED)
            .xenditPayoutId("payout_xyz")
            .xenditReferenceId("phase_payout_200_9999")
            .build();

    when(phaseDisbursementRepository.findByXenditPayoutId("payout_xyz"))
        .thenReturn(Optional.of(disbursement));
    when(phaseDisbursementRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    when(projectPhaseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    when(phaseProcessLogRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    service.handlePayoutCallback(callback);

    // Project should be set to COMPLETED
    assertEquals(ProjectStatus.COMPLETED, project.getStatus());
    verify(projectRepository).save(project);
  }
}
