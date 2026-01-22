/*
  Migration: add must_change_password flag to users
  Used to force password change on first login.
*/
SET NOCOUNT ON;
SET XACT_ABORT ON;
GO

USE [EnglishCenterDB];
GO

IF COL_LENGTH('dbo.users', 'must_change_password') IS NULL
BEGIN
    ALTER TABLE dbo.users
    ADD must_change_password bit NOT NULL
        CONSTRAINT DF_users_must_change_password DEFAULT 0;
    PRINT 'OK: added dbo.users.must_change_password';
END
ELSE
BEGIN
    PRINT 'SKIP: dbo.users.must_change_password already exists';
END
GO

