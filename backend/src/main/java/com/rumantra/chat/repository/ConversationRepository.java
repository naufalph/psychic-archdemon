package com.rumantra.chat.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.rumantra.chat.domain.Conversation;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

  Optional<Conversation> findByBidId(Long bidId);

  List<Conversation> findByArchitectIdOrderByLastMessageAtDesc(Long architectId);

  List<Conversation> findByClientIdOrderByLastMessageAtDesc(Long clientId);

  @Query(
      "SELECT c FROM Conversation c WHERE c.architectId = ?1 OR c.clientId = ?2 ORDER BY"
          + " c.lastMessageAt DESC")
  List<Conversation> findByUserIdOrderByLastMessageAtDesc(Long architectId, Long clientId);
}
