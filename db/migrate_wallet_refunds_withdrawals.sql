/*
  Migration: wallet refunds + withdrawals
  - Extend dbo.wallet_transactions.txn_type to include REFUND and WITHDRAWAL
  - Add dbo.wallet_withdrawal_requests for consultant->accountant approval workflow
*/
SET NOCOUNT ON;
SET XACT_ABORT ON;
GO

USE [EnglishCenterDB];
GO

/* ===== wallet_transactions.txn_type ===== */
IF EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = 'CK_wallet_tx_type' AND parent_object_id = OBJECT_ID('dbo.wallet_transactions'))
BEGIN
    ALTER TABLE dbo.wallet_transactions DROP CONSTRAINT CK_wallet_tx_type;
    PRINT 'OK: dropped CK_wallet_tx_type';
END
GO

ALTER TABLE dbo.wallet_transactions
ADD CONSTRAINT CK_wallet_tx_type CHECK (
    txn_type IN (N'TOPUP', N'ENROLLMENT_FEE', N'REFUND', N'WITHDRAWAL', N'ADJUSTMENT')
);
GO

/* ===== wallet_withdrawal_requests ===== */
IF OBJECT_ID(N'dbo.wallet_withdrawal_requests', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.wallet_withdrawal_requests (
        request_id  int IDENTITY(1,1) NOT NULL CONSTRAINT PK_wallet_withdrawal_requests PRIMARY KEY,
        student_id  int NOT NULL,
        amount      decimal(18,2) NOT NULL,
        status      nvarchar(20) NOT NULL CONSTRAINT DF_withdraw_req_status DEFAULT N'PENDING',
        note        nvarchar(255) NULL,
        created_at  datetime2(0) NOT NULL CONSTRAINT DF_withdraw_req_created_at DEFAULT SYSUTCDATETIME(),
        created_by  int NULL,
        decided_at  datetime2(0) NULL,
        decided_by  int NULL,
        CONSTRAINT FK_withdraw_req_student FOREIGN KEY (student_id) REFERENCES dbo.students(student_id),
        CONSTRAINT FK_withdraw_req_created_by FOREIGN KEY (created_by) REFERENCES dbo.users(user_id),
        CONSTRAINT FK_withdraw_req_decided_by FOREIGN KEY (decided_by) REFERENCES dbo.users(user_id),
        CONSTRAINT CK_withdraw_req_amount CHECK (amount > 0),
        CONSTRAINT CK_withdraw_req_status CHECK (status IN (N'PENDING', N'APPROVED', N'REJECTED'))
    );

    CREATE INDEX IX_withdraw_req_status_created ON dbo.wallet_withdrawal_requests(status, created_at);
    CREATE INDEX IX_withdraw_req_student_id ON dbo.wallet_withdrawal_requests(student_id);
    PRINT 'OK: dbo.wallet_withdrawal_requests created.';
END
ELSE
BEGIN
    PRINT 'SKIP: dbo.wallet_withdrawal_requests already exists.';
END
GO

