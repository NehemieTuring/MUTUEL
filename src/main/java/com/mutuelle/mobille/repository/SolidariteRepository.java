package com.mutuelle.mobille.repository;

import com.mutuelle.mobille.enums.TransactionType;
import com.mutuelle.mobille.models.Transaction;
import com.mutuelle.mobille.models.Session;
import com.mutuelle.mobille.models.account.AccountMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface SolidariteRepository extends JpaRepository<Transaction, Long> {
    /**
     * Récupère toutes les transactions de solidarité d'un membre
     * 
     * @param type          Le type de transaction (SOLIDARITE)
     * @param accountMember Le compte du membre
     * @return Liste des transactions de solidarité du membre
     */
    List<Transaction> findByTransactionTypeAndAccountMember(
            TransactionType type,
            AccountMember accountMember);

    /**
     * Calcule le total des montants payés en solidarité par un membre
     */
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.transactionType = :type AND t.accountMember = :accountMember")
    BigDecimal sumAmountByTransactionTypeAndAccountMember(
            @Param("type") TransactionType type,
            @Param("accountMember") AccountMember accountMember);

    /**
     * Récupère les transactions de solidarité d'un membre pour une session
     * spécifique
     */
    List<Transaction> findByTransactionTypeAndAccountMemberAndSession(
            TransactionType type,
            AccountMember accountMember,
            Session session);
}