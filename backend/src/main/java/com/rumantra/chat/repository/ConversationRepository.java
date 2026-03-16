package com.rumantra.chat.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.rumantra.chat.domain.Conversation;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

  @Query("SELECT c FROM Conversation c WHERE c.bidId = ?1 AND c.requesterUserId IS NULL")
  Optional<Conversation> findProjectConversationByBidId(Long bidId);

  @Query(
      "SELECT c FROM Conversation c WHERE c.architectId = ?1"
          + " AND c.requesterUserId IS NULL ORDER BY c.lastMessageAt DESC")
  List<Conversation> findProjectConversationsByArchitectId(Long architectId);

  @Query(
      "SELECT c FROM Conversation c WHERE c.clientId = ?1"
          + " AND c.requesterUserId IS NULL ORDER BY c.lastMessageAt DESC")
  List<Conversation> findProjectConversationsByClientId(Long clientId);

  @Query(
      "SELECT c FROM Conversation c WHERE (c.architectId = ?1 OR c.clientId = ?2)"
          + " AND c.requesterUserId IS NULL ORDER BY c.lastMessageAt DESC")
  List<Conversation> findProjectConversationsByUserIdOrderByLastMessageAtDesc(
      Long architectId, Long clientId);

  List<Conversation> findByRequesterUserIdOrderByLastMessageAtDesc(Long requesterUserId);

  Optional<Conversation> findByRequesterUserIdAndBidId(Long requesterUserId, Long bidId);

  @Query(
      "SELECT c FROM Conversation c WHERE c.requesterUserId IS NOT NULL ORDER BY c.lastMessageAt DESC")
  List<Conversation> findAllSupportConversationsOrderByLastMessageAtDesc();

  @Query(
      "SELECT c FROM Conversation c WHERE c.itSupportRequested = true ORDER BY c.lastMessageAt DESC")
  List<Conversation> findProjectConversationsWithSupportRequestedOrderByLastMessageAtDesc();
}
