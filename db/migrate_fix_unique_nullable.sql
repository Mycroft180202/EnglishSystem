/*
  Migration: replace UNIQUE constraints on nullable email/phone with filtered unique indexes
  Fixes SQL Server behavior where UNIQUE allows only one NULL.
*/
SET NOCOUNT ON;
SET XACT_ABORT ON;
GO

USE [EnglishCenterDB];
GO

/* ===== users ===== */
IF EXISTS (SELECT 1 FROM sys.key_constraints WHERE name = 'UQ_users_email')
    ALTER TABLE dbo.users DROP CONSTRAINT UQ_users_email;
IF EXISTS (SELECT 1 FROM sys.key_constraints WHERE name = 'UQ_users_phone')
    ALTER TABLE dbo.users DROP CONSTRAINT UQ_users_phone;
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'UX_users_email_notnull' AND object_id = OBJECT_ID('dbo.users'))
    CREATE UNIQUE INDEX UX_users_email_notnull ON dbo.users(email) WHERE email IS NOT NULL;
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'UX_users_phone_notnull' AND object_id = OBJECT_ID('dbo.users'))
    CREATE UNIQUE INDEX UX_users_phone_notnull ON dbo.users(phone) WHERE phone IS NOT NULL;
IF COL_LENGTH('dbo.users', 'teacher_id') IS NOT NULL
    IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'UX_users_teacher_id_notnull' AND object_id = OBJECT_ID('dbo.users'))
        CREATE UNIQUE INDEX UX_users_teacher_id_notnull ON dbo.users(teacher_id) WHERE teacher_id IS NOT NULL;
IF COL_LENGTH('dbo.users', 'student_id') IS NOT NULL
    IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'UX_users_student_id_notnull' AND object_id = OBJECT_ID('dbo.users'))
        CREATE UNIQUE INDEX UX_users_student_id_notnull ON dbo.users(student_id) WHERE student_id IS NOT NULL;
GO

/* ===== students ===== */
IF EXISTS (SELECT 1 FROM sys.key_constraints WHERE name = 'UQ_students_email')
    ALTER TABLE dbo.students DROP CONSTRAINT UQ_students_email;
IF EXISTS (SELECT 1 FROM sys.key_constraints WHERE name = 'UQ_students_phone')
    ALTER TABLE dbo.students DROP CONSTRAINT UQ_students_phone;
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'UX_students_email_notnull' AND object_id = OBJECT_ID('dbo.students'))
    CREATE UNIQUE INDEX UX_students_email_notnull ON dbo.students(email) WHERE email IS NOT NULL;
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'UX_students_phone_notnull' AND object_id = OBJECT_ID('dbo.students'))
    CREATE UNIQUE INDEX UX_students_phone_notnull ON dbo.students(phone) WHERE phone IS NOT NULL;
GO

/* ===== teachers ===== */
IF EXISTS (SELECT 1 FROM sys.key_constraints WHERE name = 'UQ_teachers_email')
    ALTER TABLE dbo.teachers DROP CONSTRAINT UQ_teachers_email;
IF EXISTS (SELECT 1 FROM sys.key_constraints WHERE name = 'UQ_teachers_phone')
    ALTER TABLE dbo.teachers DROP CONSTRAINT UQ_teachers_phone;
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'UX_teachers_email_notnull' AND object_id = OBJECT_ID('dbo.teachers'))
    CREATE UNIQUE INDEX UX_teachers_email_notnull ON dbo.teachers(email) WHERE email IS NOT NULL;
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'UX_teachers_phone_notnull' AND object_id = OBJECT_ID('dbo.teachers'))
    CREATE UNIQUE INDEX UX_teachers_phone_notnull ON dbo.teachers(phone) WHERE phone IS NOT NULL;
GO

PRINT 'OK: filtered unique indexes applied for nullable email/phone.';
GO

