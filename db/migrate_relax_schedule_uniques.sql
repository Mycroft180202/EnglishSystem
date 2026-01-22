/*
  Migration: relax class_schedules uniqueness for room/teacher
  Goal: allow reusing same room/teacher slot across different classes as long as their date ranges don't overlap.

  We drop:
    - UQ_class_schedules_room (day_of_week, slot_id, room_id)
    - UQ_class_schedules_teacher (day_of_week, slot_id, teacher_id)

  And add non-unique indexes to keep lookups fast.
*/
SET NOCOUNT ON;
SET XACT_ABORT ON;
GO

USE [EnglishCenterDB];
GO

IF EXISTS (SELECT 1 FROM sys.key_constraints WHERE name = 'UQ_class_schedules_room')
BEGIN
    ALTER TABLE dbo.class_schedules DROP CONSTRAINT UQ_class_schedules_room;
    PRINT 'OK: dropped UQ_class_schedules_room';
END
ELSE
BEGIN
    PRINT 'SKIP: UQ_class_schedules_room not found';
END
GO

IF EXISTS (SELECT 1 FROM sys.key_constraints WHERE name = 'UQ_class_schedules_teacher')
BEGIN
    ALTER TABLE dbo.class_schedules DROP CONSTRAINT UQ_class_schedules_teacher;
    PRINT 'OK: dropped UQ_class_schedules_teacher';
END
ELSE
BEGIN
    PRINT 'SKIP: UQ_class_schedules_teacher not found';
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_class_schedules_room_dow_slot' AND object_id = OBJECT_ID('dbo.class_schedules'))
BEGIN
    CREATE INDEX IX_class_schedules_room_dow_slot ON dbo.class_schedules(room_id, day_of_week, slot_id);
    PRINT 'OK: created IX_class_schedules_room_dow_slot';
END
ELSE
BEGIN
    PRINT 'SKIP: IX_class_schedules_room_dow_slot already exists';
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_class_schedules_teacher_dow_slot' AND object_id = OBJECT_ID('dbo.class_schedules'))
BEGIN
    CREATE INDEX IX_class_schedules_teacher_dow_slot ON dbo.class_schedules(teacher_id, day_of_week, slot_id);
    PRINT 'OK: created IX_class_schedules_teacher_dow_slot';
END
ELSE
BEGIN
    PRINT 'SKIP: IX_class_schedules_teacher_dow_slot already exists';
END
GO

