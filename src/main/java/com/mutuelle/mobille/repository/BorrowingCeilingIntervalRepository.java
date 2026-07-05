package com.mutuelle.mobille.repository;

import com.mutuelle.mobille.models.BorrowingCeilingInterval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BorrowingCeilingIntervalRepository extends JpaRepository<BorrowingCeilingInterval, Long> {

    List<BorrowingCeilingInterval> findAllByOrderByMinEpargneAsc();
}