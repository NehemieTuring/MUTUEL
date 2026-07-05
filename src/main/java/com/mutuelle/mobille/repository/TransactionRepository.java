package com.mutuelle.mobille.repository;

import com.mutuelle.mobille.enums.TransactionDirection;
import com.mutuelle.mobille.enums.TransactionType;
import com.mutuelle.mobille.models.Session;
import com.mutuelle.mobille.models.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.criteria.Predicate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {

    List<Transaction> findByTransactionType(TransactionType type);

    List<Transaction> findByTransactionDirection(TransactionDirection direction);

    List<Transaction> findByTransactionTypeAndTransactionDirection(TransactionType type, TransactionDirection direction);

    default Page<Transaction> findFiltered(
            TransactionType type,
            List<TransactionType> types,
            TransactionDirection direction,
            Long sessionId,
            Long exerciceId,
            Long accountMemberId,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Pageable pageable) {

        org.springframework.data.jpa.domain.Specification<Transaction> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isNull(root.get("parentTransaction")));
            if (types != null && !types.isEmpty()) {
                predicates.add(root.get("transactionType").in(types));
            } else if (type != null) {
                predicates.add(cb.equal(root.get("transactionType"), type));
            }
            if (direction != null) predicates.add(cb.equal(root.get("transactionDirection"), direction));
            if (sessionId != null) predicates.add(cb.equal(root.get("session").get("id"), sessionId));
            if (exerciceId != null) predicates.add(cb.equal(root.get("session").get("exercice").get("id"), exerciceId));
            if (accountMemberId != null) predicates.add(cb.equal(root.get("accountMember").get("id"), accountMemberId));
            if (fromDate != null) predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), fromDate));
            if (toDate != null) predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), toDate));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return findAll(spec, pageable);
    }

    List<Transaction> findBySessionAndTransactionType(Session session, TransactionType type);

    @Query("SELECT t FROM Transaction t WHERE t.session.id = :sessionId AND t.transactionType = :type")
    List<Transaction> findBySessionIdAndTransactionType(@Param("sessionId") Long sessionId, @Param("type") TransactionType type);

    // ── Sommes par session ────────────────────────────────────────────────────

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.session.id = :sessionId AND t.transactionType = com.mutuelle.mobille.enums.TransactionType.ASSISTANCE")
    BigDecimal sumAssistanceAmountBySessionId(@Param("sessionId") Long sessionId);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.session.id = :sessionId AND t.transactionType = com.mutuelle.mobille.enums.TransactionType.AGAPE")
    BigDecimal sumAgapeAmountBySessionId(@Param("sessionId") Long sessionId);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.session.id = :sessionId")
    Long countBySessionId(@Param("sessionId") Long sessionId);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.session.id = :sessionId AND t.transactionType IN (com.mutuelle.mobille.enums.TransactionType.ASSISTANCE, com.mutuelle.mobille.enums.TransactionType.AGAPE)")
    BigDecimal sumTotalExpensesBySessionId(@Param("sessionId") Long sessionId);

    @Query("SELECT t FROM Transaction t WHERE t.session.id = :sessionId AND t.transactionType = com.mutuelle.mobille.enums.TransactionType.SOLIDARITE")
    List<Transaction> findSolidarityContributionsBySession(@Param("sessionId") Long sessionId);

    @Query("SELECT COUNT(DISTINCT t.accountMember) FROM Transaction t WHERE t.session.id = :sessionId AND t.transactionType = com.mutuelle.mobille.enums.TransactionType.SOLIDARITE")
    Long countMembersWithSolidarityContribution(@Param("sessionId") Long sessionId);

    // ── Agrégation par session + type + direction (pour SessionHistory et MemberSessionBilan) ──

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.session.id = :sessionId AND t.transactionType = :type AND t.transactionDirection = :direction")
    BigDecimal sumBySessionAndTypeAndDirection(
            @Param("sessionId") Long sessionId,
            @Param("type") TransactionType type,
            @Param("direction") TransactionDirection direction);

    @Query("SELECT COUNT(t) FROM Transaction t " +
           "WHERE t.session.id = :sessionId AND t.transactionType = :type AND t.transactionDirection = :direction")
    Long countBySessionAndTypeAndDirection(
            @Param("sessionId") Long sessionId,
            @Param("type") TransactionType type,
            @Param("direction") TransactionDirection direction);

    // ── Agrégation par session + member + type + direction (pour MemberSessionBilan) ──

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.session.id = :sessionId AND t.accountMember.id = :accountMemberId " +
           "AND t.transactionType = :type AND t.transactionDirection = :direction")
    BigDecimal sumBySessionAndAccountMemberAndTypeAndDirection(
            @Param("sessionId") Long sessionId,
            @Param("accountMemberId") Long accountMemberId,
            @Param("type") TransactionType type,
            @Param("direction") TransactionDirection direction);

    // ── Agrégation par exercice + type + direction (pour ExerciceHistory) ─────

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.session.exercice.id = :exerciceId AND t.transactionType = :type AND t.transactionDirection = :direction")
    BigDecimal sumByExerciceAndTypeAndDirection(
            @Param("exerciceId") Long exerciceId,
            @Param("type") TransactionType type,
            @Param("direction") TransactionDirection direction);

    // ── Agrégation par exercice + member + type + direction (pour MemberExerciceBilan) ──

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.session.exercice.id = :exerciceId AND t.accountMember.id = :accountMemberId " +
           "AND t.transactionType = :type AND t.transactionDirection = :direction")
    BigDecimal sumByExerciceAndAccountMemberAndTypeAndDirection(
            @Param("exerciceId") Long exerciceId,
            @Param("accountMemberId") Long accountMemberId,
            @Param("type") TransactionType type,
            @Param("direction") TransactionDirection direction);

    // ── Méthodes temporelles globales ─────────────────────────────────────────

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.createdAt >= :from AND t.createdAt <= :to AND t.transactionType = :type")
    BigDecimal sumAmountByTypeAndPeriod(@Param("type") TransactionType type, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.accountMember.id = :accountMemberId")
    Long countByAccountMemberId(@Param("accountMemberId") Long accountMemberId);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.accountMember.member.id = :memberId")
    Long countByMemberId(@Param("memberId") Long memberId);

    @Query("SELECT COUNT(t) FROM Transaction t")
    Long countTotalTransactions();

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.transactionType = :type")
    BigDecimal sumAmountByType(@Param("type") TransactionType type);

    // ── Requêtes pour rollback de réouverture de session ─────────────────────

    @Query("SELECT t FROM Transaction t WHERE t.session.id = :sessionId " +
           "AND t.transactionType = :type AND t.transactionDirection = :direction " +
           "AND t.accountMember IS NOT NULL")
    List<Transaction> findBySessionIdAndTypeAndDirectionWithMember(
            @Param("sessionId") Long sessionId,
            @Param("type") TransactionType type,
            @Param("direction") TransactionDirection direction);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.session.id = :sessionId AND t.transactionType = :type " +
           "AND t.transactionDirection = :direction " +
           "AND t.accountMember IS NULL AND t.parentTransaction IS NULL")
    BigDecimal sumBySessionAndTypeAndDirectionWithoutMemberNoParent(
            @Param("sessionId") Long sessionId,
            @Param("type") TransactionType type,
            @Param("direction") TransactionDirection direction);

    @Query("SELECT t FROM Transaction t WHERE t.session.id = :sessionId " +
           "AND t.transactionType = :type AND t.transactionDirection = :direction " +
           "AND t.accountMember IS NULL AND t.parentTransaction IS NULL")
    List<Transaction> findBySessionIdAndTypeAndDirectionWithoutMemberNoParent(
            @Param("sessionId") Long sessionId,
            @Param("type") TransactionType type,
            @Param("direction") TransactionDirection direction);

    @Query("SELECT t FROM Transaction t WHERE t.parentTransaction.id = :parentId")
    List<Transaction> findByParentTransactionId(@Param("parentId") Long parentId);
}
