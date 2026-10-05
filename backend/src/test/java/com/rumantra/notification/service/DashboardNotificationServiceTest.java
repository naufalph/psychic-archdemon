package com.rumantra.notification.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.LongStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.rumantra.bidding.domain.Bid;
import com.rumantra.bidding.repository.BidRepository;
import com.rumantra.client.domain.Project;
import com.rumantra.client.domain.ProjectStatus;
import com.rumantra.client.repository.ProjectRepository;
import com.rumantra.notification.domain.DashboardNotification;
import com.rumantra.notification.domain.NotificationType;
import com.rumantra.notification.dto.NotificationPageResponse;
import com.rumantra.notification.dto.NotificationResponse;
import com.rumantra.notification.repository.DashboardNotificationRepository;
import com.rumantra.project.domain.ProjectPhase;
import com.rumantra.project.repository.ProjectPhaseRepository;
import com.rumantra.security.UserPrincipal;
import com.rumantra.shared.exception.ResourceNotFoundException;
import com.rumantra.user.domain.User;
import com.rumantra.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class DashboardNotificationServiceTest {

  private static final Long PROJECT_ID = 42L;
  private static final Long USER_ID = 5L;
  private static final Long OTHER_USER_ID = 6L;

  @Mock private DashboardNotificationRepository notificationRepository;
  @Mock private UserRepository userRepository;
  @Mock private ProjectRepository projectRepository;
  @Mock private BidRepository bidRepository;
  @Mock private ProjectPhaseRepository projectPhaseRepository;

  @InjectMocks private DashboardNotificationService service;

  @BeforeEach
  void authenticate() {
    UserPrincipal principal = new UserPrincipal(USER_ID, "user@test", null, List.of());
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(principal, null, List.of()));
  }

  @AfterEach
  void clearAuthentication() {
    SecurityContextHolder.clearContext();
  }

  private DashboardNotification notification(long id, Long ownerId) {
    return DashboardNotification.builder()
        .id(id)
        .user(User.builder().id(ownerId).build())
        .type(NotificationType.BID_RECEIVED)
        .title("t")
        .message("m")
        .isRead(false)
        .createdAt(LocalDateTime.of(2026, 10, 1, 12, 0).minusMinutes(id))
        .build();
  }

  private List<DashboardNotification> rows(int count) {
    return LongStream.rangeClosed(1, count).mapToObj(id -> notification(id, USER_ID)).toList();
  }

  private void stubCounts() {
    when(notificationRepository.countUnreadByUserId(USER_ID)).thenReturn(3L);
    when(notificationRepository.countByUserId(USER_ID)).thenReturn(9L);
  }

  @Test
  void emptyPageHasNoCursor() {
    stubCounts();
    when(notificationRepository.findFirstPage(eq(USER_ID), eq(false), any(Pageable.class)))
        .thenReturn(List.of());

    NotificationPageResponse page = service.getNotificationPage(null, 20, false);

    assertTrue(page.getItems().isEmpty());
    assertNull(page.getNextCursor());
    assertEquals(3L, page.getUnreadCount());
    assertEquals(9L, page.getTotalCount());
  }

  @Test
  void exactLimitPageHasNoCursor() {
    stubCounts();
    when(notificationRepository.findFirstPage(eq(USER_ID), eq(false), any(Pageable.class)))
        .thenReturn(rows(2));

    NotificationPageResponse page = service.getNotificationPage(null, 2, false);

    assertEquals(2, page.getItems().size());
    assertNull(page.getNextCursor());
  }

  @Test
  void overfullPageIsTrimmedAndPointsAtItsLastRow() {
    stubCounts();
    when(notificationRepository.findFirstPage(eq(USER_ID), eq(false), any(Pageable.class)))
        .thenReturn(rows(3));

    NotificationPageResponse page = service.getNotificationPage(null, 2, false);

    assertEquals(
        List.of(1L, 2L), page.getItems().stream().map(NotificationResponse::getId).toList());
    assertEquals(2L, page.getNextCursor());
  }

  @Test
  void limitIsCappedAndOneExtraRowIsRequested() {
    stubCounts();
    when(notificationRepository.findFirstPage(eq(USER_ID), eq(true), any(Pageable.class)))
        .thenReturn(List.of());

    service.getNotificationPage(null, 500, true);

    verify(notificationRepository)
        .findFirstPage(
            USER_ID, true, PageRequest.of(0, DashboardNotificationService.MAX_PAGE_SIZE + 1));
  }

  @Test
  void cursorContinuesAfterTheAnchorRow() {
    stubCounts();
    DashboardNotification anchor = notification(4, USER_ID);
    when(notificationRepository.findById(4L)).thenReturn(Optional.of(anchor));
    when(notificationRepository.findPageAfter(
            eq(USER_ID), eq(true), eq(anchor.getCreatedAt()), eq(4L), any(Pageable.class)))
        .thenReturn(List.of(notification(5, USER_ID)));

    NotificationPageResponse page = service.getNotificationPage(4L, 20, true);

    assertEquals(1, page.getItems().size());
    assertNull(page.getNextCursor());
  }

  @Test
  void cursorOwnedByAnotherUserYieldsAnEmptyPage() {
    stubCounts();
    when(notificationRepository.findById(4L))
        .thenReturn(Optional.of(notification(4, OTHER_USER_ID)));

    NotificationPageResponse page = service.getNotificationPage(4L, 20, false);

    assertTrue(page.getItems().isEmpty());
    verify(notificationRepository, never())
        .findPageAfter(any(), anyBoolean(), any(), any(), any(Pageable.class));
  }

  @Test
  void markingAnotherUsersNotificationIsNotFound() {
    when(notificationRepository.findById(4L))
        .thenReturn(Optional.of(notification(4, OTHER_USER_ID)));

    assertThrows(ResourceNotFoundException.class, () -> service.markAsRead(4L));
    verify(notificationRepository, never()).save(any());
  }

  @Test
  void markAsReadIsIdempotent() {
    DashboardNotification unread = notification(4, USER_ID);
    when(notificationRepository.findById(4L)).thenReturn(Optional.of(unread));
    when(notificationRepository.save(unread)).thenReturn(unread);

    assertTrue(service.markAsRead(4L).getIsRead());
    assertTrue(service.markAsRead(4L).getIsRead());

    verify(notificationRepository, times(1)).save(unread);
  }

  @Test
  void unreadCountComesFromTheStoreAfterMarkRead() {
    DashboardNotification unread = notification(4, USER_ID);
    when(notificationRepository.findById(4L)).thenReturn(Optional.of(unread));
    when(notificationRepository.save(unread)).thenReturn(unread);
    when(notificationRepository.countUnreadByUserId(USER_ID)).thenReturn(3L, 2L);
    when(notificationRepository.countByUserId(USER_ID)).thenReturn(9L);
    when(notificationRepository.findFirstPage(eq(USER_ID), eq(false), any(Pageable.class)))
        .thenReturn(List.of());

    assertEquals(3L, service.getNotificationPage(null, 20, false).getUnreadCount());
    service.markAsRead(4L);
    assertEquals(2L, service.getNotificationPage(null, 20, false).getUnreadCount());
  }

  @Test
  void projectsAreBatchLoadedOnceWithNameAndStatus() {
    stubCounts();
    DashboardNotification first = notification(1, USER_ID);
    DashboardNotification second = notification(2, USER_ID);
    first.setProjectId(PROJECT_ID);
    second.setProjectId(PROJECT_ID);
    Project project = project();
    project.setTitle("Kosan Premium");
    project.setStatus(ProjectStatus.OPEN);
    when(notificationRepository.findFirstPage(eq(USER_ID), eq(false), any(Pageable.class)))
        .thenReturn(List.of(first, second));
    when(projectRepository.findAllById(List.of(PROJECT_ID))).thenReturn(List.of(project));

    NotificationPageResponse page = service.getNotificationPage(null, 20, false);

    assertEquals("Kosan Premium", page.getItems().get(0).getProjectName());
    assertEquals("OPEN", page.getItems().get(1).getProjectStatus());
    verify(projectRepository, times(1)).findAllById(any());
  }

  private Project project() {
    Project project = new Project();
    project.setId(PROJECT_ID);
    return project;
  }

  @Test
  void projectReferenceIsItsOwnProject() {
    assertEquals(PROJECT_ID, service.resolveProjectId("PROJECT", PROJECT_ID));
    verifyNoInteractions(bidRepository, projectPhaseRepository);
  }

  @Test
  void bidReferenceResolvesThroughBidProject() {
    Bid bid = new Bid();
    bid.setProject(project());
    when(bidRepository.findById(7L)).thenReturn(Optional.of(bid));

    assertEquals(PROJECT_ID, service.resolveProjectId("BID", 7L));
  }

  @Test
  void phaseReferenceResolvesThroughPhaseProject() {
    ProjectPhase phase = new ProjectPhase();
    phase.setProject(project());
    when(projectPhaseRepository.findById(9L)).thenReturn(Optional.of(phase));

    assertEquals(PROJECT_ID, service.resolveProjectId("PHASE", 9L));
  }

  @Test
  void missingBidOrPhaseResolvesToNull() {
    when(bidRepository.findById(7L)).thenReturn(Optional.empty());
    when(projectPhaseRepository.findById(9L)).thenReturn(Optional.empty());

    assertNull(service.resolveProjectId("BID", 7L));
    assertNull(service.resolveProjectId("PHASE", 9L));
  }

  @Test
  void nonProjectReferencesResolveToNull() {
    assertNull(service.resolveProjectId("SUPPORT_CONVERSATION", 3L));
    assertNull(service.resolveProjectId(null, null));
    assertNull(service.resolveProjectId("PROJECT", null));
  }
}
