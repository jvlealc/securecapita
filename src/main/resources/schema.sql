/*
 *  MySQL - DDL & DML
 *  Author: João Vitor Leal de Castro
 */
CREATE SCHEMA IF NOT EXISTS securecapita;

USE securecapita;

ALTER DATABASE securecapita
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

SET NAMES 'utf8mb4';
SET TIME_ZONE = '-03:00'; -- America/Sao_Paulo
SET FOREIGN_KEY_CHECKS = 0;

-- Limpeza
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS roles;
DROP TABLE IF EXISTS user_roles;
DROP TABLE IF EXISTS `events`;
DROP TABLE IF EXISTS user_events;
DROP TABLE IF EXISTS account_verifications;
DROP TABLE IF EXISTS reset_password_verifications;
DROP TABLE IF EXISTS mfa_verifications;

SET FOREIGN_KEY_CHECKS = 1;

-- Tabela de usuários
CREATE TABLE IF NOT EXISTS users
(
    id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(40)     NOT NULL,
    last_name  VARCHAR(40)     NOT NULL,
    email      VARCHAR(100)    NOT NULL,
    password   VARCHAR(255)    DEFAULT NULL,
    phone      VARCHAR(30)     DEFAULT NULL,
    address    VARCHAR(255)    DEFAULT NULL,
    title      VARCHAR(50)     DEFAULT NULL,
    bio        VARCHAR(500)    DEFAULT NULL,
    enabled    BOOLEAN         DEFAULT FALSE,
    non_locked BOOLEAN         DEFAULT TRUE,
    using_mfa  BOOLEAN         DEFAULT FALSE,
    mfa_type   VARCHAR(10)     NOT NULL DEFAULT 'EMAIL',
    image_url  VARCHAR(255)    DEFAULT 'https://cdn-icons-png.flaticon.com/512/3033/3033143.png',
    created_at DATETIME        DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT chk_users_mfa_type CHECK (mfa_type IN ('EMAIL', 'SMS'))
) ENGINE = InnoDB;

-- Tabela de grupos de usuários e permissões
CREATE TABLE IF NOT EXISTS roles
(
    id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(50)     NOT NULL,
    permission VARCHAR(255)    NOT NULL,

    CONSTRAINT uq_roles_name UNIQUE (name)
) ENGINE = InnoDB;

-- Pivot, tabela relacional entre users e roles
CREATE TABLE IF NOT EXISTS user_roles
(
    id      BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    role_id BIGINT UNSIGNED NOT NULL,

    CONSTRAINT fk_user_roles_users FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_user_roles_roles FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT uq_user_roles_user_id UNIQUE (user_id),
    KEY idx_user_roles_users (user_id),
    KEY idx_user_roles_roles (role_id)
) ENGINE = InnoDB;

-- Catálogo de tipos de eventos
CREATE TABLE IF NOT EXISTS `events`
(
    id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    type        VARCHAR(50)     NOT NULL,
    description VARCHAR(300)    NOT NULL,

    CONSTRAINT uq_events_type UNIQUE (type),
    CONSTRAINT chk_events_type CHECK (type IN (
               'LOGIN_ATTEMPT', 'LOGIN_ATTEMPT_FAILURE', 'LOGIN_ATTEMPT_SUCCESS',
               'PROFILE_UPDATE', 'PROFILE_PICTURE_UPDATE', 'ROLE_UPDATE',
               'ACCOUNT_SETTINGS_UPDATE', 'PASSWORD_UPDATE', 'MFA_UPDATE'
    ))
) ENGINE = InnoDB;

-- Log e Auditoria
CREATE TABLE IF NOT EXISTS user_events
(
    id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id    BIGINT UNSIGNED NOT NULL,
    event_id   BIGINT UNSIGNED NOT NULL,
    device     VARCHAR(100) DEFAULT NULL,
    ip_address VARCHAR(100) DEFAULT NULL,
    created_at DATETIME     DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_user_events_users FOREIGN KEY (user_id) REFERENCES `users` (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_user_events_events FOREIGN KEY (event_id) REFERENCES `events` (id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB;

-- Tabelas de verificação
CREATE TABLE IF NOT EXISTS account_verifications
(
    id      BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    url     VARCHAR(255)    NOT NULL,

    CONSTRAINT fk_account_verifications_users FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT uq_account_verifications_user_id UNIQUE (user_id),
    CONSTRAINT uq_account_verifications_url UNIQUE (url)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS reset_password_verifications
(
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT UNSIGNED NOT NULL,
    url             VARCHAR(255)    NOT NULL,
    expiration_date DATETIME        NOT NULL,

    CONSTRAINT fk_reset_password_verifications_users FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT uq_reset_password_verifications_user_id UNIQUE (user_id),
    CONSTRAINT uq_reset_password_verifications_url UNIQUE (url)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS mfa_verifications
(
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT UNSIGNED NOT NULL,
    code            VARCHAR(10)     NOT NULL,
    expiration_date DATETIME        NOT NULL,

    CONSTRAINT fk_mfa_verifications_users FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT uq_mfa_verifications_user_id UNIQUE (user_id),
    CONSTRAINT uq_mfa_verifications_code UNIQUE (code)
) ENGINE = InnoDB;

INSERT INTO roles (name, permission)
VALUES ('ROLE_USER', 'READ:USER, READ:CUSTOMER'),
       ('ROLE_MANAGER', 'READ:USER, READ:CUSTOMER, UPDATE:USER, UPDATE:CUSTOMER'),
       ('ROLE_ADMIN', 'READ:USER, READ:CUSTOMER, CREATE:USER, CREATE:CUSTOMER, UPDATE:USER, UPDATE:CUSTOMER'),
       ('ROLE_SYSADMIN', 'READ:USER, READ:CUSTOMER, CREATE:USER, CREATE:CUSTOMER, UPDATE:USER, UPDATE:CUSTOMER, DELETE:USER, DELETE:CUSTOMER');





