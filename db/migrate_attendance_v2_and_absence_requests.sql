/*
  Migration:
  - Update attendance statuses to ATTENDED/ABSENT/EXCUSED
  - Add absence_requests table for student leave requests
*/
SET NOCOUNT ON;
SET XACT_ABORT ON;
GO

USE [EnglishCenterDB];
GO

/* ===== attendance status values ===== */
IF EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = 'CK_attendance_status')
    ALTER TABLE dbo.attendance DROP CONSTRAINT CK_attendance_status;
GO

UPDATE dbo.attendance
SET status = N'ATTENDED'
WHERE status IN (N'PRESENT', N'LATE');
GO

ALTER TABLE dbo.attendance
ADD CONSTRAINT CK_attendance_status CHECK (status IN (N'ATTENDED', N'ABSENT', N'EXCUSED'));
GO

/* ===== absence_requests ===== */
IF OBJECT_ID(N'dbo.absence_requests', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.absence_requests (
        request_id   int IDENTITY(1,1) NOT NULL CONSTRAINT PK_absence_requests PRIMARY KEY,
        session_id   int NOT NULL,
        enroll_id    int NOT NULL,
        reason       nvarchar(500) NOT NULL,
        status       nvarchar(20) NOT NULL CONSTRAINT DF_absence_requests_status DEFAULT N'PENDING',
        created_at   datetime2(0) NOT NULL CONSTRAINT DF_absence_requests_created_at DEFAULT SYSUTCDATETIME(),
        created_by   int NULL,
        CONSTRAINT FK_absreq_session FOREIGN KEY (session_id) REFERENCES dbo.class_sessions(session_id),
        CONSTRAINT FK_absreq_enroll FOREIGN KEY (enroll_id) REFERENCES dbo.enrollments(enroll_id),
        CONSTRAINT FK_absreq_user FOREIGN KEY (created_by) REFERENCES dbo.users(user_id),
        CONSTRAINT UQ_absreq UNIQUE (session_id, enroll_id),
        CONSTRAINT CK_absreq_status CHECK (status IN (N'PENDING', N'APPROVED', N'REJECTED'))
    );
    CREATE INDEX IX_absreq_enroll_id ON dbo.absence_requests(enroll_id);
    CREATE INDEX IX_absreq_session_id ON dbo.absence_requests(session_id);
END
GO

PRINT 'OK: attendance status updated + absence_requests created.';
GO

