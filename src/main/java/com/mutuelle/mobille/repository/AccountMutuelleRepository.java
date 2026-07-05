package com.mutuelle.mobille.repository;

import com.mutuelle.mobille.models.account.AccountMutuelle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountMutuelleRepository extends JpaRepository<AccountMutuelle, Long> {
    boolean existsById(Long id); // ou simplement utiliser count() > 0
}