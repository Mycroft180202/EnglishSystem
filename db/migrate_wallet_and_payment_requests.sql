/*
  Migration: wallet + payment request workflow
  - Adds WALLET to dbo.payments.method constraint
  - Creates dbo.student_wallets, dbo.wallet_transactions, dbo.payment_requests
*/
SET NOCOUNT ON;
SET XACT_ABORT ON;
GO

USE [EnglishCenterDB];
GO

/* payments.method allow WALLET */
IF EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = 'CK_payments_method' AND parent_object_id = OBJECT_ID('dbo.payments'))
    ALTER TABLE dbo.payments DROP CONSTRAINT CK_payments_method;
GO

ALTER TABLE dbo.payments
ADD CONSTRAINT CK_payments_method CHECK (method IN (N'CASH', N'TRANSFER', N'CARD', N'WALLET', N'VIETQR', N'PAYOS'));
GO

/* student_wallets */
IF OBJECT_ID(N'dbo.student_wallets', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.student_wallets (
        student_id int NOT NULL CONSTRAINT PK_student_wallets PRIMARY KEY,
        balance    decimal(18,2) NOT NULL CONSTRAINT DF_student_wallets_balance DEFAULT (0),
        updated_at datetime2(0) NOT NULL CONSTRAINT DF_student_wallets_updated_at DEFAULT SYSUTCDATETIME(),
        CONSTRAINT FK_student_wallets_students FOREIGN KEY (student_id) REFERENCES dbo.students(student_id),
        CONSTRAINT CK_student_wallets_balance CHECK (balance >= 0)
    );
    PRINT 'OK: dbo.student_wallets created.';
END
GO

/* wallet_transactions */
IF OBJECT_ID(N'dbo.wallet_transactions', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.wallet_transactions (
        txn_id     bigint IDENTITY(1,1) NOT NULL CONSTRAINT PK_wallet_transactions PRIMARY KEY,
        student_id int NOT NULL,
        amount     decimal(18,2) NOT NULL,
        txn_type   nvarchar(30) NOT NULL,
        enroll_id  int NULL,
        note       nvarchar(255) NULL,
        created_at datetime2(0) NOT NULL CONSTRAINT DF_wallet_transactions_created_at DEFAULT SYSUTCDATETIME(),
        created_by int NULL,
        CONSTRAINT FK_wallet_tx_students FOREIGN KEY (student_id) REFERENCES dbo.students(student_id),
        CONSTRAINT FK_wallet_tx_enroll FOREIGN KEY (enroll_id) REFERENCES dbo.enrollments(enroll_id),
        CONSTRAINT FK_wallet_tx_user FOREIGN KEY (created_by) REFERENCES dbo.users(user_id),
        CONSTRAINT CK_wallet_tx_amount CHECK (amount <> 0),
        CONSTRAINT CK_wallet_tx_type CHECK (txn_type IN (N'TOPUP', N'ENROLLMENT_FEE', N'ADJUSTMENT'))
    );
    CREATE INDEX IX_wallet_tx_student_id ON dbo.wallet_transactions(student_id);
    CREATE INDEX IX_wallet_tx_created_at ON dbo.wallet_transactions(created_at);
    PRINT 'OK: dbo.wallet_transactions created.';
END
GO

/* payment_requests */
IF OBJECT_ID(N'dbo.payment_requests', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.payment_requests (
        request_id  int IDENTITY(1,1) NOT NULL CONSTRAINT PK_payment_requests PRIMARY KEY,
        invoice_id  int NOT NULL,
        enroll_id   int NOT NULL,
        amount      decimal(18,2) NOT NULL,
        method      nvarchar(20) NOT NULL CONSTRAINT DF_payment_requests_method DEFAULT N'CASH',
        status      nvarchar(20) NOT NULL CONSTRAINT DF_payment_requests_status DEFAULT N'PENDING',
        note        nvarchar(255) NULL,
        created_at  datetime2(0) NOT NULL CONSTRAINT DF_payment_requests_created_at DEFAULT SYSUTCDATETIME(),
        created_by  int NULL,
        decided_at  datetime2(0) NULL,
        decided_by  int NULL,
        CONSTRAINT FK_payreq_invoice FOREIGN KEY (invoice_id) REFERENCES dbo.invoices(invoice_id),
        CONSTRAINT FK_payreq_enroll FOREIGN KEY (enroll_id) REFERENCES dbo.enrollments(enroll_id),
        CONSTRAINT FK_payreq_created_by FOREIGN KEY (created_by) REFERENCES dbo.users(user_id),
        CONSTRAINT FK_payreq_decided_by FOREIGN KEY (decided_by) REFERENCES dbo.users(user_id),
        CONSTRAINT CK_payreq_amount CHECK (amount > 0),
        CONSTRAINT CK_payreq_method CHECK (method IN (N'CASH', N'TRANSFER')),
        CONSTRAINT CK_payreq_status CHECK (status IN (N'PENDING', N'APPROVED', N'REJECTED'))
    );
    CREATE INDEX IX_payreq_status_created ON dbo.payment_requests(status, created_at);
    PRINT 'OK: dbo.payment_requests created.';
END
GO
