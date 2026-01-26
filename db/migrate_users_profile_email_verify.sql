/*
  Migration: user profile fields + email verification
  - Adds: full_name, address, email_verified, email_verify_token, email_verify_expires
*/
SET NOCOUNT ON;
SET XACT_ABORT ON;
GO

USE [EnglishCenterDB];
GO

IF COL_LENGTH('dbo.users', 'full_name') IS NULL
BEGIN
    ALTER TABLE dbo.users ADD full_name nvarchar(150) NULL;
    PRINT 'OK: added dbo.users.full_name';
END
ELSE
BEGIN
    PRINT 'SKIP: dbo.users.full_name already exists';
END
GO

IF COL_LENGTH('dbo.users', 'address') IS NULL
BEGIN
    ALTER TABLE dbo.users ADD address nvarchar(255) NULL;
    PRINT 'OK: added dbo.users.address';
END
ELSE
BEGIN
    PRINT 'SKIP: dbo.users.address already exists';
END
GO

IF COL_LENGTH('dbo.users', 'email_verified') IS NULL
BEGIN
    ALTER TABLE dbo.users
    ADD email_verified bit NOT NULL CONSTRAINT DF_users_email_verified DEFAULT 0;
    PRINT 'OK: added dbo.users.email_verified';
END
ELSE
BEGIN
    PRINT 'SKIP: dbo.users.email_verified already exists';
END
GO

IF COL_LENGTH('dbo.users', 'email_verify_token') IS NULL
BEGIN
    ALTER TABLE dbo.users ADD email_verify_token nvarchar(64) NULL;
    PRINT 'OK: added dbo.users.email_verify_token';
END
ELSE
BEGIN
    PRINT 'SKIP: dbo.users.email_verify_token already exists';
END
GO

IF COL_LENGTH('dbo.users', 'email_verify_expires') IS NULL
BEGIN
    ALTER TABLE dbo.users ADD email_verify_expires datetime2(0) NULL;
    PRINT 'OK: added dbo.users.email_verify_expires';
END
ELSE
BEGIN
    PRINT 'SKIP: dbo.users.email_verify_expires already exists';
END
GO

PRINT 'OK: migrate_users_profile_email_verify applied.';
GO

