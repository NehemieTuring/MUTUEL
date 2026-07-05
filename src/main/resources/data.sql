-- Version compatible Spring Boot + MySQL (aucun SET, aucun SELECT)

-- 1. Créer l'admin dans la table admins (seulement s'il n'existe pas)
INSERT IGNORE INTO admins (full_name, is_active)
SELECT 'Administrateur Principal', true
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM admins LIMIT 1);

-- 2. Créer l'utilisateur admin seulement s'il n'existe pas
INSERT IGNORE INTO auth_users (email, password, user_type, user_ref_id)
SELECT
    'admin@mutuelle.com',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', -- admin123
    'ADMIN',
    (SELECT id FROM admins ORDER BY id ASC LIMIT 1)
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM auth_users WHERE email = 'admin@mutuelle.com');


