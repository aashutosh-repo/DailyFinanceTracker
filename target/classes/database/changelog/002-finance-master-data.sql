INSERT INTO users
(
    user_id, username, email, password_hash, full_name, phone, country_code, currency, profile_pic_url,
    bio, date_of_birth, is_active, last_login, email_verified, phone_verified, two_factor_enabled, created_at, updated_at )
VALUES
    (
        'U1001','aashutosh', 'aashutosh@gmail.com', '$2a$12$TpjweLt8QQBONazNo0UO9OPb7bhXQtfEbl3tQtA8pUpyGd8wVImV6',
        'John Doe', '9876543210','USA','USD','https://example.com/john.jpg',
        'Finance enthusiast','1995-05-20',true,NOW(),true,true,false,
        NOW(),
        NOW()
    ),
    (
        'U1002','admin','admin@gmail.com','$2a$13$V3xg6uQRArQmZB2D2cNECeS6bUE6G9TJsHyPhpnz6Drtg5eYY14dq',
        'Admin User','9876543211','USA','USD',NULL,'System Administrator','1990-01-10',true,
        NOW(),true,true,true,NOW(),NOW()
    );
INSERT INTO roles (role_id,name, description)
VALUES
    (1,'USER', 'Regular application user'),
    (2,'ADMIN', 'System administrator'),
    (3,'MODERATOR', 'Moderator with limited admin privileges');

INSERT INTO user_roles (user_id, role_id)
VALUES
    (1, 1),   -- john -> USER
    (2, 1),   -- admin -> USER
    (2, 2);   -- admin -> ADMIN
