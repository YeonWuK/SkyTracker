package com.skytracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRoom extends JpaRepository<ChatRoom, Long> {
}
