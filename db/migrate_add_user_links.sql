/*
  Migration: link users to teachers/students for role-specific views
*/
SET NOCOUNT ON;
SET XACT_ABORT ON;
GO

USE [EnglishCenterDB];
GO

IF COL_LENGTH('dbo.users', 'teacher_id') IS NULL
    ALTER TABLE dbo.users ADD teacher_id int NULL;
IF COL_LENGTH('dbo.users', 'student_id') IS NULL
    ALTER TABLE dbo.users ADD student_id int NULL;
GO

IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_users_teachers')
    ALTER TABLE dbo.users WITH CHECK ADD CONSTRAINT FK_users_teachers FOREIGN KEY (teacher_id) REFERENCES dbo.teachers(teacher_id);
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_users_students')
    ALTER TABLE dbo.users WITH CHECK ADD CONSTRAINT FK_users_students FOREIGN KEY (student_id) REFERENCES dbo.students(student_id);
GO

PRINT 'OK: users.teacher_id and users.student_id added (if missing).';
GO

