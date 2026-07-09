package com.mutuelle.mobille.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Corrige les rôles auth des membres sur PostgreSQL local.
 * Certaines bases créées avec une contrainte role <= 4 stockent les membres avec le rôle 4
 * au lieu de MEMBER (5), ce qui casse la liste /api/members.
 */
@Component
@Profile("dev-postgres")
@RequiredArgsConstructor
@Slf4j
public class AuthUserRoleMigration implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        try {
            jdbcTemplate.execute("ALTER TABLE auth_users DROP CONSTRAINT IF EXISTS auth_users_role_check");
            jdbcTemplate.execute("ALTER TABLE auth_users ADD CONSTRAINT auth_users_role_check CHECK (role >= 0 AND role <= 5)");

            jdbcTemplate.execute("ALTER TABLE members DROP CONSTRAINT IF EXISTS members_status_check");
            jdbcTemplate.execute("""
                    ALTER TABLE members ADD CONSTRAINT members_status_check
                    CHECK (status IN ('PENDING', 'ACTIF', 'NON_A_JOUR', 'INACTIF'))
                    """);

            int updated = jdbcTemplate.update("""
                    UPDATE auth_users au
                    SET role = 5
                    FROM members m
                    WHERE au.user_ref_id = m.id
                      AND au.role = 4
                      AND lower(au.email) NOT IN ('cac@mutuelle.com')
                    """);

            log.info("Migration PostgreSQL : {} compte(s) membre corrigé(s) vers le rôle MEMBER", updated);
        } catch (Exception e) {
            log.warn("Migration des rôles auth ignorée : {}", e.getMessage());
        }
    }
}
