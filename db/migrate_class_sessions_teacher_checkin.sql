/*
  Migration: teacher check-in for class_sessions
  - Adds teacher_checkin_at, teacher_checkin_by to dbo.class_sessions
  - Used for teacher to confirm present in their session
*/
SET NOCOUNT ON;
SET XACT_ABORT ON;
GO

USE [EnglishCenterDB];
GO

IF COL_LENGTH('dbo.class_sessions', 'teacher_checkin_at') IS NULL
BEGIN
    ALTER TABLE dbo.class_sessions
    ADD teacher_checkin_at datetime2(0) NULL;
    PRINT 'OK: added dbo.class_sessions.teacher_checkin_at';
END
ELSE
BEGIN
    PRINT 'SKIP: dbo.class_sessions.teacher_checkin_at already exists';
END
GO

IF COL_LENGTH('dbo.class_sessions', 'teacher_checkin_by') IS NULL
BEGIN
    ALTER TABLE dbo.class_sessions
    ADD teacher_checkin_by int NULL;
    PRINT 'OK: added dbo.class_sessions.teacher_checkin_by';
END
ELSE
BEGIN
    PRINT 'SKIP: dbo.class_sessions.teacher_checkin_by already exists';
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_class_sessions_teacher_checkin_by')
BEGIN
    ALTER TABLE dbo.class_sessions
    ADD CONSTRAINT FK_class_sessions_teacher_checkin_by FOREIGN KEY (teacher_checkin_by) REFERENCES dbo.users(user_id);
    PRINT 'OK: added FK_class_sessions_teacher_checkin_by';
END
ELSE
BEGIN
    PRINT 'SKIP: FK_class_sessions_teacher_checkin_by already exists';
END
GO

