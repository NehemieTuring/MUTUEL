package com.mutuelle.mobille.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Migration PostgreSQL locale : statuts NON_A_JOUR, nouveaux champs account_members et config.
 */
@Component
@Profile("dev-postgres")
@RequiredArgsConstructor
@Slf4j
public class MemberComplianceMigration implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        try {
            migrateMemberStatusConstraint();
            migrateAccountMemberColumns();
            migrateMutuelleConfigColumns();
            migrateRenfoulementColumns();
            migrateInsolvableToNonAJour();
            log.info("Migration conformité membres terminée");
        } catch (Exception e) {
            log.warn("Migration conformité membres ignorée : {}", e.getMessage());
        }
    }

    private void migrateMemberStatusConstraint() {
        jdbcTemplate.execute("ALTER TABLE members DROP CONSTRAINT IF EXISTS members_status_check");
        jdbcTemplate.execute("""
                ALTER TABLE members ADD CONSTRAINT members_status_check
                CHECK (status IN ('PENDING', 'ACTIF', 'NON_A_JOUR', 'INACTIF'))
                """);
    }

    private void migrateAccountMemberColumns() {
        jdbcTemplate.execute("""
                ALTER TABLE accounts_member
                ADD COLUMN IF NOT EXISTS first_registration_date DATE
                """);
        jdbcTemplate.execute("""
                ALTER TABLE accounts_member
                ADD COLUMN IF NOT EXISTS historical_registration_paid NUMERIC(12,2)
                """);
        jdbcTemplate.execute("""
                ALTER TABLE accounts_member
                ADD COLUMN IF NOT EXISTS registration_configured BOOLEAN NOT NULL DEFAULT FALSE
                """);
        jdbcTemplate.execute("""
                ALTER TABLE accounts_member
                ADD COLUMN IF NOT EXISTS sessions_in_non_a_jour INTEGER NOT NULL DEFAULT 0
                """);
        jdbcTemplate.execute("""
                ALTER TABLE accounts_member
                ADD COLUMN IF NOT EXISTS last_penalty_session_id BIGINT
                """);
        jdbcTemplate.execute("""
                ALTER TABLE accounts_member
                ADD COLUMN IF NOT EXISTS assistance_blocked_sessions_remaining INTEGER NOT NULL DEFAULT 0
                """);

        jdbcTemplate.update("""
                UPDATE accounts_member
                SET registration_configured = TRUE
                WHERE unpaid_registration_amount IS NOT NULL
                  AND unpaid_registration_amount = 0
                  AND registration_configured = FALSE
                """);
    }

    private void migrateMutuelleConfigColumns() {
        jdbcTemplate.execute("""
                ALTER TABLE mutuelle_config
                ADD COLUMN IF NOT EXISTS loan_penalty_rate_percent NUMERIC(5,2) NOT NULL DEFAULT 3.00
                """);
        jdbcTemplate.execute("""
                ALTER TABLE mutuelle_config
                ADD COLUMN IF NOT EXISTS default_agape_amount NUMERIC(14,2) NOT NULL DEFAULT 5000.00
                """);
    }

    private void migrateRenfoulementColumns() {
        jdbcTemplate.execute("""
                ALTER TABLE renfoulement
                ADD COLUMN IF NOT EXISTS management_fees_amount NUMERIC(15,2) NOT NULL DEFAULT 0.00
                """);
    }

    private void migrateInsolvableToNonAJour() {
        int updated = jdbcTemplate.update("UPDATE members SET status = 'NON_A_JOUR' WHERE status = 'INSOLVABLE'");
        if (updated > 0) {
            log.info("Migration statuts : {} membre(s) INSOLVABLE → NON_A_JOUR", updated);
        }
    }
}
