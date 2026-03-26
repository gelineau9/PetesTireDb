-- PetesTireDb Schema
-- Run this file against your MySQL database after creating it:
--   CREATE DATABASE petestiredb;
--   USE petestiredb;
--   SOURCE schema.sql;

-- ============================================================
-- Table: newtire
-- Stores current new tire inventory.
-- Insert logic uses upsert: duplicate (brand, Rnum, size, label)
-- increments quantity rather than creating a second row.
-- ============================================================
CREATE TABLE IF NOT EXISTS newtire (
    brand       VARCHAR(64)     NOT NULL,
    Rnum        INT             NOT NULL,
    size        VARCHAR(32)     NOT NULL,
    label       VARCHAR(64)     NOT NULL,
    quantity    INT             NOT NULL DEFAULT 0,
    extratag    VARCHAR(128),
    PRIMARY KEY (brand, Rnum, size, label)
);

-- ============================================================
-- Table: usedtire
-- Tracks individual used tires by a unique ID.
-- ============================================================
CREATE TABLE IF NOT EXISTS usedtire (
    tireID      INT             NOT NULL AUTO_INCREMENT,
    Rnum        INT             NOT NULL,
    size        VARCHAR(32)     NOT NULL,
    dateAquired DATE            NOT NULL,   -- note: column name preserved as-is from original inventory
    monthsUsed  INT             NOT NULL DEFAULT 0,
    PRIMARY KEY (tireID)
);

-- ============================================================
-- Table: users
-- Application users with role-based access levels (0-4).
-- Access levels:
--   0 = Guest    (login only)
--   1 = Read     (search)
--   2 = Staff    (insert / delete tires)
--   3 = Manager  (Excel import / export)
--   4 = Admin    (user management)
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
    userName    VARCHAR(64)     NOT NULL,
    pass        VARCHAR(255)    NOT NULL,
    accessLevel INT             NOT NULL DEFAULT 0,
    PRIMARY KEY (userName)
);

-- ============================================================
-- Seed: demo user
-- Access level 0 (guest) — can log in but cannot modify data.
-- IMPORTANT: Change or remove this user before deploying to
-- a production or internet-accessible environment.
-- ============================================================
INSERT IGNORE INTO users (userName, pass, accessLevel)
VALUES ('demo', 'demoPass', 0);
