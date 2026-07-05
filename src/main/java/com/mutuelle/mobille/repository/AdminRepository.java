package com.mutuelle.mobille.repository;

import com.mutuelle.mobille.models.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {

    List<Admin> findByIsActive(Boolean isActive);

    List<Admin> findByIsActiveTrue();

    Optional<Admin> findByIdAndIsActiveTrue(Long id);

    boolean existsByIdAndIsActiveTrue(Long id);

    List<Admin> findByFullNameContainingIgnoreCaseAndIsActiveTrue(String fullName);
}