/*
  Migration: VietQR payment intents (auto payment)
  - Creates dbo.vietqr_payment_intents
*/
SET NOCOUNT ON;
SET XACT_ABORT ON;
GO

USE [EnglishCenterDB];
GO

IF OBJECT_ID(N'dbo.vietqr_payment_intents', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.vietqr_payment_intents (
        intent_id   bigint IDENTITY(1,1) NOT NULL CONSTRAINT PK_vietqr_payment_intents PRIMARY KEY,
        invoice_id  int NOT NULL,
        enroll_id   int NOT NULL,
        amount      decimal(18,2) NOT NULL,
        qr_ref      nvarchar(40) NOT NULL CONSTRAINT UQ_vietqr_qr_ref UNIQUE,
        status      nvarchar(20) NOT NULL CONSTRAINT DF_vietqr_status DEFAULT N'PENDING',
        created_at  datetime2(0) NOT NULL CONSTRAINT DF_vietqr_created_at DEFAULT SYSUTCDATETIME(),
        paid_at     datetime2(0) NULL,
        txn_ref     nvarchar(100) NULL,
        raw_payload nvarchar(max) NULL,
        CONSTRAINT FK_vietqr_invoice FOREIGN KEY (invoice_id) REFERENCES dbo.invoices(invoice_id),
        CONSTRAINT FK_vietqr_enroll FOREIGN KEY (enroll_id) REFERENCES dbo.enrollments(enroll_id),
        CONSTRAINT CK_vietqr_amount CHECK (amount > 0),
        CONSTRAINT CK_vietqr_status CHECK (status IN (N'PENDING', N'PAID', N'EXPIRED', N'FAILED'))
    );

    CREATE INDEX IX_vietqr_invoice_id ON dbo.vietqr_payment_intents(invoice_id);
    CREATE INDEX IX_vietqr_status_created ON dbo.vietqr_payment_intents(status, created_at);
    PRINT 'OK: dbo.vietqr_payment_intents created.';
END
ELSE
BEGIN
    PRINT 'SKIP: dbo.vietqr_payment_intents already exists.';
END
GO

