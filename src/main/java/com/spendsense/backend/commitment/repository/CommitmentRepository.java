package com.spendsense.backend.commitment.repository;

import com.spendsense.backend.commitment.entity.Commitment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommitmentRepository extends JpaRepository<Commitment, Long> {
    List<Commitment> findByUserIdOrderByDueDayAsc(Long userId);
}
