-- V5__add_role_to_users.sql
ALTER TABLE users
  ADD COLUMN role TEXT NOT NULL DEFAULT 'USER',
  ADD CONSTRAINT users_role_chk CHECK (role IN ('USER','ADMIN'));