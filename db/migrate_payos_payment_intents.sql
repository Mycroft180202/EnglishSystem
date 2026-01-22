/*
  Migration: PayOS payment intents (auto payment)
  - Creates dbo.payos_payment_intents
*/
SET NOCOUNT ON;
SET XACT_ABORT ON;
GO

USE [EnglishCenterDB];
GO

IF OBJECT_ID(N'dbo.payos_payment_intents', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.payos_payment_intents (
        intent_id    bigint IDENTITY(1,1) NOT NULL CONSTRAINT PK_payos_payment_intents PRIMARY KEY,
        invoice_id   int NOT NULL,
        enroll_id    int NOT NULL,
        amount       decimal(18,2) NOT NULL,
        order_code   bigint NOT NULL CONSTRAINT UQ_payos_order_code UNIQUE,
        status       nvarchar(20) NOT NULL CONSTRAINT DF_payos_status DEFAULT N'PENDING',
        created_at   datetime2(0) NOT NULL CONSTRAINT DF_payos_created_at DEFAULT SYSUTCDATETIME(),
        paid_at      datetime2(0) NULL,
        txn_ref      nvarchar(100) NULL,
        raw_payload  nvarchar(max) NULL,
        CONSTRAINT FK_payos_invoice FOREIGN KEY (invoice_id) REFERENCES dbo.invoices(invoice_id),
        CONSTRAINT FK_payos_enroll FOREIGN KEY (enroll_id) REFERENCES dbo.enrollments(enroll_id),
        CONSTRAINT CK_payos_amount CHECK (amount > 0),
        CONSTRAINT CK_payos_status CHECK (status IN (N'PENDING', N'PAID', N'EXPIRED', N'FAILED'))
    );

    CREATE INDEX IX_payos_invoice_id ON dbo.payos_payment_intents(invoice_id);
    CREATE INDEX IX_payos_status_created ON dbo.payos_payment_intents(status, created_at);
    PRINT 'OK: dbo.payos_payment_intents created.';
END
ELSE
BEGIN
    PRINT 'SKIP: dbo.payos_payment_intents already exists.';
END
GO

