/*
  Migration: PayOS wallet top-up intents
  - Creates dbo.payos_wallet_topups
*/
SET NOCOUNT ON;
SET XACT_ABORT ON;
GO

USE [EnglishCenterDB];
GO

IF OBJECT_ID(N'dbo.payos_wallet_topups', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.payos_wallet_topups (
        intent_id    bigint IDENTITY(1,1) NOT NULL CONSTRAINT PK_payos_wallet_topups PRIMARY KEY,
        student_id   int NOT NULL,
        amount       decimal(18,2) NOT NULL,
        order_code   bigint NOT NULL CONSTRAINT UQ_payos_wallet_order_code UNIQUE,
        status       nvarchar(20) NOT NULL CONSTRAINT DF_payos_wallet_status DEFAULT N'PENDING',
        created_at   datetime2(0) NOT NULL CONSTRAINT DF_payos_wallet_created_at DEFAULT SYSUTCDATETIME(),
        paid_at      datetime2(0) NULL,
        txn_ref      nvarchar(100) NULL,
        raw_payload  nvarchar(max) NULL,
        CONSTRAINT FK_payos_wallet_students FOREIGN KEY (student_id) REFERENCES dbo.students(student_id),
        CONSTRAINT CK_payos_wallet_amount CHECK (amount > 0),
        CONSTRAINT CK_payos_wallet_status CHECK (status IN (N'PENDING', N'PAID', N'EXPIRED', N'FAILED'))
    );

    CREATE INDEX IX_payos_wallet_student_id ON dbo.payos_wallet_topups(student_id);
    CREATE INDEX IX_payos_wallet_status_created ON dbo.payos_wallet_topups(status, created_at);
    PRINT 'OK: dbo.payos_wallet_topups created.';
END
ELSE
BEGIN
    PRINT 'SKIP: dbo.payos_wallet_topups already exists.';
END
GO

