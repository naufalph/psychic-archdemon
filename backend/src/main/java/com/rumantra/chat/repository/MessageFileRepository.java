package com.rumantra.chat.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rumantra.chat.domain.MessageFile;

@Repository
public interface MessageFileRepository extends JpaRepository<MessageFile, Long> {

  Optional<MessageFile> findByMessageId(Long messageId);
}
