/*
  Migration: allow teacher to mark a class session as a test (Test 1/2/Final)
  - Creates dbo.session_assessments
  - Enforces: 1 assessment per session, 1 session per (class, assessment)
*/
SET NOCOUNT ON;
SET XACT_ABORT ON;
GO

USE [EnglishCenterDB];
GO

IF OBJECT_ID(N'dbo.session_assessments', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.session_assessments (
        session_id   int NOT NULL,
        class_id     int NOT NULL,
        assess_id    int NOT NULL,
        assigned_at  datetime2(0) NOT NULL CONSTRAINT DF_session_assessments_assigned_at DEFAULT SYSUTCDATETIME(),
        assigned_by  int NULL,
        CONSTRAINT PK_session_assessments PRIMARY KEY (session_id),
        CONSTRAINT FK_sa_session FOREIGN KEY (session_id) REFERENCES dbo.class_sessions(session_id),
        CONSTRAINT FK_sa_class FOREIGN KEY (class_id) REFERENCES dbo.classes(class_id),
        CONSTRAINT FK_sa_assessment FOREIGN KEY (assess_id) REFERENCES dbo.assessments(assess_id),
        CONSTRAINT FK_sa_user FOREIGN KEY (assigned_by) REFERENCES dbo.users(user_id)
    );

    CREATE UNIQUE INDEX UX_sa_class_assess ON dbo.session_assessments(class_id, assess_id);
    CREATE INDEX IX_sa_assess_id ON dbo.session_assessments(assess_id);

    PRINT 'OK: dbo.session_assessments created.';
END
ELSE
BEGIN
    PRINT 'SKIP: dbo.session_assessments already exists.';
END
GO

