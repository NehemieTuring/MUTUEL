package com.mutuelle.mobille.repository;

import com.mutuelle.mobille.models.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    // Méthodes existantes
    List<Member> findByIsActive(Boolean isActive);
    List<Member> findByIsActiveTrue();
    List<Member> findByIsActiveFalse();

    Optional<Member> findByPhone(String phone);
    Optional<Member> findByPhoneAndIsActiveTrue(String phone);
    boolean existsByPhone(String phone);

    @Query("SELECT m FROM Member m WHERE LOWER(m.phone) = LOWER(:phone)")
    Optional<Member> findByPhoneCaseInsensitive(@Param("phone") String phone);

    List<Member> findByLastnameContainingIgnoreCaseAndFirstnameContainingIgnoreCase(
            String lastname, String firstname);

    List<Member> findByLastnameContainingIgnoreCaseOrFirstnameContainingIgnoreCase(
            String search, String search2);

    @Query("SELECT m FROM Member m LEFT JOIN FETCH m.accountMember WHERE m.id = :id")
    Optional<Member> findByIdWithAccount(@Param("id") Long id);

    @Query("SELECT m FROM Member m LEFT JOIN FETCH m.accountMember WHERE m.isActive = true")
    List<Member> findAllActiveWithAccount();

    @Query("SELECT m FROM Member m LEFT JOIN FETCH m.accountMember WHERE m.phone = :phone")
    Optional<Member> findByPhoneWithAccount(@Param("phone") String phone);

    Page<Member> findAll(Specification<Member> spec, Pageable pageable);

    boolean existsByPhoneAndIdNot(String phone, Long id);

    @Query("SELECT m FROM Member m WHERE m.isActive = true")
    List<Member> findAllActiveMembers();
}