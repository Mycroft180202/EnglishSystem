/*
  Migration: Assessments v2 (TEST1/TEST2/FINAL)
  - Allow new assessment types in CHECK constraint
  - Canonicalize legacy types: QUIZ -> TEST1, MIDTERM -> TEST2
  - Try to enforce one assessment per (course_id, type)
*/
SET NOCOUNT ON;
SET XACT_ABORT ON;
GO

USE [EnglishCenterDB];
GO

/* Update check constraint on dbo.assessments.type */
IF EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = 'CK_assessments_type' AND parent_object_id = OBJECT_ID('dbo.assessments'))
    ALTER TABLE dbo.assessments DROP CONSTRAINT CK_assessments_type;
GO

ALTER TABLE dbo.assessments
ADD CONSTRAINT CK_assessments_type CHECK (type IN (N'TEST1', N'TEST2', N'FINAL', N'QUIZ', N'MIDTERM'));
GO

/* Canonicalize existing data */
UPDATE dbo.assessments SET type = N'TEST1' WHERE type = N'QUIZ';
UPDATE dbo.assessments SET type = N'TEST2' WHERE type = N'MIDTERM';
GO

/* Normalize weights to match grading scheme */
UPDATE dbo.assessments SET weight = 20 WHERE type = N'TEST1';
UPDATE dbo.assessments SET weight = 30 WHERE type = N'TEST2';
UPDATE dbo.assessments SET weight = 40 WHERE type = N'FINAL';
GO

/* Drop old UNIQUE (course_id, name, type) if present */
IF EXISTS (SELECT 1 FROM sys.key_constraints WHERE name = 'UQ_assessments' AND parent_object_id = OBJECT_ID('dbo.assessments'))
    ALTER TABLE dbo.assessments DROP CONSTRAINT UQ_assessments;
GO

/*
  Enforce uniqueness per course+type.
  If duplicates exist, skip index creation and print a message so you can clean them up.
*/
IF EXISTS (
    SELECT 1
    FROM dbo.assessments
    GROUP BY course_id, type
    HAVING COUNT(*) > 1
)
BEGIN
    PRINT 'WARN: Duplicate (course_id, type) rows exist in dbo.assessments. Unique index not created.';
END
ELSE
BEGIN
    IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'UX_assessments_course_type' AND object_id = OBJECT_ID('dbo.assessments'))
        CREATE UNIQUE INDEX UX_assessments_course_type ON dbo.assessments(course_id, type);
    PRINT 'OK: assessments v2 applied.';
END
GO
