/*
  English Center Management System - SQL Server schema
  - Creates database (if missing) and all tables + constraints
  - Target: Microsoft SQL Server 2016+
*/

SET NOCOUNT ON;
SET XACT_ABORT ON;
GO

/* If you want a different DB name, change both occurrences of [EnglishCenterDB] in this script. */
IF DB_ID(N'EnglishCenterDB') IS NULL
    CREATE DATABASE [EnglishCenterDB];
GO

USE [EnglishCenterDB];
GO

/*
  Set to 1 if you want to DROP existing tables and recreate the schema.
  Default is 0 (safe for first-time creation).
*/
DECLARE @Recreate bit = 0;

IF @Recreate = 1
BEGIN
    /* Drop in reverse dependency order */
    IF OBJECT_ID(N'dbo.audit_logs', N'U') IS NOT NULL DROP TABLE dbo.audit_logs;

    IF OBJECT_ID(N'dbo.scores', N'U') IS NOT NULL DROP TABLE dbo.scores;
    IF OBJECT_ID(N'dbo.attendance', N'U') IS NOT NULL DROP TABLE dbo.attendance;

    IF OBJECT_ID(N'dbo.payments', N'U') IS NOT NULL DROP TABLE dbo.payments;
    IF OBJECT_ID(N'dbo.invoices', N'U') IS NOT NULL DROP TABLE dbo.invoices;

    IF OBJECT_ID(N'dbo.class_sessions', N'U') IS NOT NULL DROP TABLE dbo.class_sessions;
    IF OBJECT_ID(N'dbo.assessments', N'U') IS NOT NULL DROP TABLE dbo.assessments;

    IF OBJECT_ID(N'dbo.enrollments', N'U') IS NOT NULL DROP TABLE dbo.enrollments;

    IF OBJECT_ID(N'dbo.class_schedules', N'U') IS NOT NULL DROP TABLE dbo.class_schedules;
    IF OBJECT_ID(N'dbo.classes', N'U') IS NOT NULL DROP TABLE dbo.classes;

    IF OBJECT_ID(N'dbo.teacher_certificates', N'U') IS NOT NULL DROP TABLE dbo.teacher_certificates;
    IF OBJECT_ID(N'dbo.teachers', N'U') IS NOT NULL DROP TABLE dbo.teachers;
    IF OBJECT_ID(N'dbo.students', N'U') IS NOT NULL DROP TABLE dbo.students;

    IF OBJECT_ID(N'dbo.time_slots', N'U') IS NOT NULL DROP TABLE dbo.time_slots;
    IF OBJECT_ID(N'dbo.rooms', N'U') IS NOT NULL DROP TABLE dbo.rooms;
    IF OBJECT_ID(N'dbo.courses', N'U') IS NOT NULL DROP TABLE dbo.courses;

    IF OBJECT_ID(N'dbo.user_roles', N'U') IS NOT NULL DROP TABLE dbo.user_roles;
    IF OBJECT_ID(N'dbo.users', N'U') IS NOT NULL DROP TABLE dbo.users;
    IF OBJECT_ID(N'dbo.roles', N'U') IS NOT NULL DROP TABLE dbo.roles;
END

/* =========================
   Security / Users
   ========================= */

CREATE TABLE dbo.roles (
    role_id   int IDENTITY(1,1) NOT NULL CONSTRAINT PK_roles PRIMARY KEY,
    role_code nvarchar(50) NOT NULL CONSTRAINT UQ_roles_role_code UNIQUE,
    role_name nvarchar(100) NOT NULL
);

CREATE TABLE dbo.users (
    user_id       int IDENTITY(1,1) NOT NULL CONSTRAINT PK_users PRIMARY KEY,
    username      nvarchar(50)  NOT NULL CONSTRAINT UQ_users_username UNIQUE,
    password_hash nvarchar(255) NOT NULL,
    email         nvarchar(255) NULL,
    phone         nvarchar(30)  NULL,
    teacher_id    int NULL,
    student_id    int NULL,
    must_change_password bit NOT NULL CONSTRAINT DF_users_must_change_password DEFAULT 0,
    status        nvarchar(20)  NOT NULL CONSTRAINT DF_users_status DEFAULT N'ACTIVE',
    created_at    datetime2(0)  NOT NULL CONSTRAINT DF_users_created_at DEFAULT SYSUTCDATETIME(),
    CONSTRAINT CK_users_status CHECK (status IN (N'ACTIVE', N'LOCKED', N'DISABLED'))
);

ALTER TABLE dbo.users WITH CHECK ADD CONSTRAINT FK_users_teachers FOREIGN KEY (teacher_id) REFERENCES dbo.teachers(teacher_id);
ALTER TABLE dbo.users WITH CHECK ADD CONSTRAINT FK_users_students FOREIGN KEY (student_id) REFERENCES dbo.students(student_id);

/* Allow multiple NULLs while still enforcing uniqueness when provided */
CREATE UNIQUE INDEX UX_users_email_notnull ON dbo.users(email) WHERE email IS NOT NULL;
CREATE UNIQUE INDEX UX_users_phone_notnull ON dbo.users(phone) WHERE phone IS NOT NULL;
CREATE UNIQUE INDEX UX_users_teacher_id_notnull ON dbo.users(teacher_id) WHERE teacher_id IS NOT NULL;
CREATE UNIQUE INDEX UX_users_student_id_notnull ON dbo.users(student_id) WHERE student_id IS NOT NULL;

CREATE TABLE dbo.user_roles (
    user_id int NOT NULL,
    role_id int NOT NULL,
    CONSTRAINT PK_user_roles PRIMARY KEY (user_id, role_id),
    CONSTRAINT FK_user_roles_users FOREIGN KEY (user_id) REFERENCES dbo.users(user_id),
    CONSTRAINT FK_user_roles_roles FOREIGN KEY (role_id) REFERENCES dbo.roles(role_id)
);

/* =========================
   Core master data
   ========================= */

CREATE TABLE dbo.students (
    student_id  int IDENTITY(1,1) NOT NULL CONSTRAINT PK_students PRIMARY KEY,
    full_name   nvarchar(150) NOT NULL,
    dob         date NULL,
    gender      nchar(1) NULL,
    email       nvarchar(255) NULL,
    phone       nvarchar(30)  NULL,
    address     nvarchar(255) NULL,
    input_level nvarchar(50)  NULL,
    status      nvarchar(20)  NOT NULL CONSTRAINT DF_students_status DEFAULT N'ACTIVE',
    created_at  datetime2(0)  NOT NULL CONSTRAINT DF_students_created_at DEFAULT SYSUTCDATETIME(),
    CONSTRAINT CK_students_gender CHECK (gender IS NULL OR gender IN (N'M', N'F', N'O')),
    CONSTRAINT CK_students_status CHECK (status IN (N'ACTIVE', N'INACTIVE'))
);

CREATE UNIQUE INDEX UX_students_email_notnull ON dbo.students(email) WHERE email IS NOT NULL;
CREATE UNIQUE INDEX UX_students_phone_notnull ON dbo.students(phone) WHERE phone IS NOT NULL;

CREATE TABLE dbo.teachers (
    teacher_id  int IDENTITY(1,1) NOT NULL CONSTRAINT PK_teachers PRIMARY KEY,
    full_name   nvarchar(150) NOT NULL,
    dob         date NULL,
    email       nvarchar(255) NULL,
    phone       nvarchar(30)  NULL,
    level       nvarchar(100) NULL,
    status      nvarchar(20)  NOT NULL CONSTRAINT DF_teachers_status DEFAULT N'ACTIVE',
    created_at  datetime2(0)  NOT NULL CONSTRAINT DF_teachers_created_at DEFAULT SYSUTCDATETIME(),
    CONSTRAINT CK_teachers_status CHECK (status IN (N'ACTIVE', N'INACTIVE'))
);

CREATE UNIQUE INDEX UX_teachers_email_notnull ON dbo.teachers(email) WHERE email IS NOT NULL;
CREATE UNIQUE INDEX UX_teachers_phone_notnull ON dbo.teachers(phone) WHERE phone IS NOT NULL;

CREATE TABLE dbo.teacher_certificates (
    cert_id     int IDENTITY(1,1) NOT NULL CONSTRAINT PK_teacher_certificates PRIMARY KEY,
    teacher_id  int NOT NULL,
    name        nvarchar(150) NOT NULL,
    issuer      nvarchar(150) NULL,
    issued_date date NULL,
    CONSTRAINT FK_teacher_certificates_teachers FOREIGN KEY (teacher_id) REFERENCES dbo.teachers(teacher_id)
);

CREATE TABLE dbo.courses (
    course_id     int IDENTITY(1,1) NOT NULL CONSTRAINT PK_courses PRIMARY KEY,
    course_code   nvarchar(50)  NOT NULL CONSTRAINT UQ_courses_course_code UNIQUE,
    course_name   nvarchar(150) NOT NULL,
    description   nvarchar(max) NULL,
    level         nvarchar(50)  NULL,
    duration_weeks int NOT NULL,
    standard_fee  decimal(18,2) NOT NULL,
    status        nvarchar(20)  NOT NULL CONSTRAINT DF_courses_status DEFAULT N'ACTIVE',
    CONSTRAINT CK_courses_duration CHECK (duration_weeks > 0),
    CONSTRAINT CK_courses_fee CHECK (standard_fee >= 0),
    CONSTRAINT CK_courses_status CHECK (status IN (N'ACTIVE', N'INACTIVE'))
);

CREATE TABLE dbo.rooms (
    room_id    int IDENTITY(1,1) NOT NULL CONSTRAINT PK_rooms PRIMARY KEY,
    room_code  nvarchar(50)  NOT NULL CONSTRAINT UQ_rooms_room_code UNIQUE,
    room_name  nvarchar(100) NOT NULL,
    capacity   int NOT NULL,
    status     nvarchar(20) NOT NULL CONSTRAINT DF_rooms_status DEFAULT N'ACTIVE',
    CONSTRAINT CK_rooms_capacity CHECK (capacity > 0),
    CONSTRAINT CK_rooms_status CHECK (status IN (N'ACTIVE', N'INACTIVE'))
);

CREATE TABLE dbo.time_slots (
    slot_id    int IDENTITY(1,1) NOT NULL CONSTRAINT PK_time_slots PRIMARY KEY,
    name       nvarchar(50) NOT NULL,
    start_time time(0) NOT NULL,
    end_time   time(0) NOT NULL,
    CONSTRAINT CK_time_slots_time CHECK (start_time < end_time),
    CONSTRAINT UQ_time_slots UNIQUE (start_time, end_time)
);

/* =========================
   Classes / Scheduling
   ========================= */

CREATE TABLE dbo.classes (
    class_id    int IDENTITY(1,1) NOT NULL CONSTRAINT PK_classes PRIMARY KEY,
    course_id   int NOT NULL,
    class_code  nvarchar(50)  NOT NULL CONSTRAINT UQ_classes_class_code UNIQUE,
    class_name  nvarchar(150) NOT NULL,
    teacher_id  int NULL,
    room_id     int NULL,
    capacity    int NOT NULL,
    start_date  date NOT NULL,
    end_date    date NULL,
    status      nvarchar(20) NOT NULL CONSTRAINT DF_classes_status DEFAULT N'DRAFT',
    CONSTRAINT FK_classes_courses FOREIGN KEY (course_id) REFERENCES dbo.courses(course_id),
    CONSTRAINT FK_classes_teachers FOREIGN KEY (teacher_id) REFERENCES dbo.teachers(teacher_id),
    CONSTRAINT FK_classes_rooms FOREIGN KEY (room_id) REFERENCES dbo.rooms(room_id),
    CONSTRAINT CK_classes_capacity CHECK (capacity > 0),
    CONSTRAINT CK_classes_dates CHECK (end_date IS NULL OR end_date >= start_date),
    CONSTRAINT CK_classes_status CHECK (status IN (N'DRAFT', N'OPEN', N'CLOSED', N'CANCELLED'))
);

CREATE INDEX IX_classes_course_id ON dbo.classes(course_id);
CREATE INDEX IX_classes_teacher_id ON dbo.classes(teacher_id);

CREATE TABLE dbo.class_schedules (
    schedule_id  int IDENTITY(1,1) NOT NULL CONSTRAINT PK_class_schedules PRIMARY KEY,
    class_id     int NOT NULL,
    day_of_week  tinyint NOT NULL, /* 1=Mon ... 7=Sun */
    slot_id      int NOT NULL,
    room_id      int NOT NULL,
    teacher_id   int NOT NULL,
    CONSTRAINT FK_class_schedules_classes FOREIGN KEY (class_id) REFERENCES dbo.classes(class_id),
    CONSTRAINT FK_class_schedules_slots FOREIGN KEY (slot_id) REFERENCES dbo.time_slots(slot_id),
    CONSTRAINT FK_class_schedules_rooms FOREIGN KEY (room_id) REFERENCES dbo.rooms(room_id),
    CONSTRAINT FK_class_schedules_teachers FOREIGN KEY (teacher_id) REFERENCES dbo.teachers(teacher_id),
    CONSTRAINT CK_class_schedules_dow CHECK (day_of_week BETWEEN 1 AND 7),
    CONSTRAINT UQ_class_schedules_class UNIQUE (class_id, day_of_week, slot_id),
    CONSTRAINT UQ_class_schedules_room UNIQUE (day_of_week, slot_id, room_id),
    CONSTRAINT UQ_class_schedules_teacher UNIQUE (day_of_week, slot_id, teacher_id)
);

/* =========================
   Enrollment / Fees / Payments
   ========================= */

CREATE TABLE dbo.enrollments (
    enroll_id   int IDENTITY(1,1) NOT NULL CONSTRAINT PK_enrollments PRIMARY KEY,
    student_id  int NOT NULL,
    class_id    int NOT NULL,
    enrolled_at datetime2(0) NOT NULL CONSTRAINT DF_enrollments_enrolled_at DEFAULT SYSUTCDATETIME(),
    status      nvarchar(20) NOT NULL CONSTRAINT DF_enrollments_status DEFAULT N'PENDING',
    CONSTRAINT FK_enrollments_students FOREIGN KEY (student_id) REFERENCES dbo.students(student_id),
    CONSTRAINT FK_enrollments_classes FOREIGN KEY (class_id) REFERENCES dbo.classes(class_id),
    CONSTRAINT UQ_enrollments_student_class UNIQUE (student_id, class_id),
    CONSTRAINT CK_enrollments_status CHECK (status IN (N'PENDING', N'ACTIVE', N'CANCELLED', N'COMPLETED'))
);

CREATE INDEX IX_enrollments_class_id ON dbo.enrollments(class_id);
CREATE INDEX IX_enrollments_student_id ON dbo.enrollments(student_id);

CREATE TABLE dbo.invoices (
    invoice_id      int IDENTITY(1,1) NOT NULL CONSTRAINT PK_invoices PRIMARY KEY,
    enroll_id       int NOT NULL,
    invoice_code    nvarchar(50) NOT NULL CONSTRAINT UQ_invoices_invoice_code UNIQUE,
    total_amount    decimal(18,2) NOT NULL,
    discount_amount decimal(18,2) NOT NULL CONSTRAINT DF_invoices_discount DEFAULT (0),
    paid_amount     decimal(18,2) NOT NULL CONSTRAINT DF_invoices_paid DEFAULT (0),
    status          nvarchar(20) NOT NULL CONSTRAINT DF_invoices_status DEFAULT N'UNPAID',
    issued_at       datetime2(0) NOT NULL CONSTRAINT DF_invoices_issued_at DEFAULT SYSUTCDATETIME(),
    issued_by       int NULL,
    CONSTRAINT FK_invoices_enrollments FOREIGN KEY (enroll_id) REFERENCES dbo.enrollments(enroll_id),
    CONSTRAINT FK_invoices_users FOREIGN KEY (issued_by) REFERENCES dbo.users(user_id),
    CONSTRAINT CK_invoices_amounts CHECK (total_amount >= 0 AND discount_amount >= 0 AND paid_amount >= 0 AND discount_amount <= total_amount),
    CONSTRAINT CK_invoices_status CHECK (status IN (N'UNPAID', N'PARTIAL', N'PAID', N'VOID'))
);

CREATE INDEX IX_invoices_enroll_id ON dbo.invoices(enroll_id);

CREATE TABLE dbo.payments (
    payment_id   int IDENTITY(1,1) NOT NULL CONSTRAINT PK_payments PRIMARY KEY,
    invoice_id   int NOT NULL,
    amount       decimal(18,2) NOT NULL,
    method       nvarchar(20) NOT NULL,
    txn_ref      nvarchar(100) NULL,
    paid_at      datetime2(0) NOT NULL CONSTRAINT DF_payments_paid_at DEFAULT SYSUTCDATETIME(),
    received_by  int NULL,
    CONSTRAINT FK_payments_invoices FOREIGN KEY (invoice_id) REFERENCES dbo.invoices(invoice_id),
    CONSTRAINT FK_payments_users FOREIGN KEY (received_by) REFERENCES dbo.users(user_id),
    CONSTRAINT CK_payments_amount CHECK (amount > 0),
    CONSTRAINT CK_payments_method CHECK (method IN (N'CASH', N'TRANSFER', N'CARD', N'WALLET', N'PAYOS'))
);

CREATE INDEX IX_payments_invoice_id ON dbo.payments(invoice_id);
CREATE INDEX IX_payments_paid_at ON dbo.payments(paid_at);

/* =========================
   Wallet / Payment Requests
   ========================= */

CREATE TABLE dbo.student_wallets (
    student_id int NOT NULL CONSTRAINT PK_student_wallets PRIMARY KEY,
    balance    decimal(18,2) NOT NULL CONSTRAINT DF_student_wallets_balance DEFAULT (0),
    updated_at datetime2(0) NOT NULL CONSTRAINT DF_student_wallets_updated_at DEFAULT SYSUTCDATETIME(),
    CONSTRAINT FK_student_wallets_students FOREIGN KEY (student_id) REFERENCES dbo.students(student_id),
    CONSTRAINT CK_student_wallets_balance CHECK (balance >= 0)
);

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

/* =========================
   VietQR Payment Intents
   ========================= */

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

/*
   PayOS Payment Intents
*/
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

/*
   PayOS Wallet Top-ups
*/
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

/* =========================
   Sessions / Attendance / Scores
   ========================= */

CREATE TABLE dbo.class_sessions (
    session_id   int IDENTITY(1,1) NOT NULL CONSTRAINT PK_class_sessions PRIMARY KEY,
    class_id     int NOT NULL,
    session_date date NOT NULL,
    slot_id      int NOT NULL,
    room_id      int NOT NULL,
    teacher_id   int NOT NULL,
    status       nvarchar(20) NOT NULL CONSTRAINT DF_class_sessions_status DEFAULT N'SCHEDULED',
    CONSTRAINT FK_class_sessions_classes FOREIGN KEY (class_id) REFERENCES dbo.classes(class_id),
    CONSTRAINT FK_class_sessions_slots FOREIGN KEY (slot_id) REFERENCES dbo.time_slots(slot_id),
    CONSTRAINT FK_class_sessions_rooms FOREIGN KEY (room_id) REFERENCES dbo.rooms(room_id),
    CONSTRAINT FK_class_sessions_teachers FOREIGN KEY (teacher_id) REFERENCES dbo.teachers(teacher_id),
    CONSTRAINT CK_class_sessions_status CHECK (status IN (N'SCHEDULED', N'COMPLETED', N'CANCELLED')),
    CONSTRAINT UQ_class_sessions UNIQUE (class_id, session_date, slot_id)
);

CREATE INDEX IX_class_sessions_class_date ON dbo.class_sessions(class_id, session_date);

CREATE TABLE dbo.attendance (
    att_id     int IDENTITY(1,1) NOT NULL CONSTRAINT PK_attendance PRIMARY KEY,
    session_id int NOT NULL,
    enroll_id  int NOT NULL,
    status     nvarchar(20) NOT NULL,
    note       nvarchar(255) NULL,
    marked_at  datetime2(0) NOT NULL CONSTRAINT DF_attendance_marked_at DEFAULT SYSUTCDATETIME(),
    marked_by  int NULL,
    CONSTRAINT FK_attendance_sessions FOREIGN KEY (session_id) REFERENCES dbo.class_sessions(session_id),
    CONSTRAINT FK_attendance_enrollments FOREIGN KEY (enroll_id) REFERENCES dbo.enrollments(enroll_id),
    CONSTRAINT FK_attendance_users FOREIGN KEY (marked_by) REFERENCES dbo.users(user_id),
    CONSTRAINT UQ_attendance UNIQUE (session_id, enroll_id),
    CONSTRAINT CK_attendance_status CHECK (status IN (N'ATTENDED', N'ABSENT', N'EXCUSED'))
);

CREATE INDEX IX_attendance_enroll_id ON dbo.attendance(enroll_id);

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

CREATE TABLE dbo.assessments (
    assess_id  int IDENTITY(1,1) NOT NULL CONSTRAINT PK_assessments PRIMARY KEY,
    course_id  int NOT NULL,
    name       nvarchar(150) NOT NULL,
    weight     decimal(5,2) NOT NULL,
    max_score  decimal(8,2) NOT NULL,
    type       nvarchar(20) NOT NULL,
    CONSTRAINT FK_assessments_courses FOREIGN KEY (course_id) REFERENCES dbo.courses(course_id),
    CONSTRAINT CK_assessments_weight CHECK (weight >= 0 AND weight <= 100),
    CONSTRAINT CK_assessments_max CHECK (max_score > 0),
    CONSTRAINT CK_assessments_type CHECK (type IN (N'TEST1', N'TEST2', N'FINAL', N'QUIZ', N'MIDTERM')),
    CONSTRAINT UQ_assessments UNIQUE (course_id, type)
);

CREATE INDEX IX_assessments_course_id ON dbo.assessments(course_id);

CREATE TABLE dbo.scores (
    score_id    int IDENTITY(1,1) NOT NULL CONSTRAINT PK_scores PRIMARY KEY,
    assess_id   int NOT NULL,
    enroll_id   int NOT NULL,
    score_value decimal(8,2) NULL,
    note        nvarchar(255) NULL,
    graded_at   datetime2(0) NOT NULL CONSTRAINT DF_scores_graded_at DEFAULT SYSUTCDATETIME(),
    graded_by   int NULL,
    CONSTRAINT FK_scores_assessments FOREIGN KEY (assess_id) REFERENCES dbo.assessments(assess_id),
    CONSTRAINT FK_scores_enrollments FOREIGN KEY (enroll_id) REFERENCES dbo.enrollments(enroll_id),
    CONSTRAINT FK_scores_users FOREIGN KEY (graded_by) REFERENCES dbo.users(user_id),
    CONSTRAINT UQ_scores UNIQUE (assess_id, enroll_id),
    CONSTRAINT CK_scores_value CHECK (score_value IS NULL OR score_value >= 0)
);

CREATE INDEX IX_scores_enroll_id ON dbo.scores(enroll_id);

/* =========================
   Session Assessments (test schedule)
   ========================= */

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

/* =========================
   Audit
   ========================= */

CREATE TABLE dbo.audit_logs (
    audit_id       bigint IDENTITY(1,1) NOT NULL CONSTRAINT PK_audit_logs PRIMARY KEY,
    actor_user_id  int NULL,
    action         nvarchar(50) NOT NULL,
    entity         nvarchar(50) NOT NULL,
    entity_id      nvarchar(64) NULL,
    data_json      nvarchar(max) NULL,
    created_at     datetime2(0) NOT NULL CONSTRAINT DF_audit_logs_created_at DEFAULT SYSUTCDATETIME(),
    ip             nvarchar(45) NULL,
    CONSTRAINT FK_audit_logs_users FOREIGN KEY (actor_user_id) REFERENCES dbo.users(user_id)
);

CREATE INDEX IX_audit_logs_created_at ON dbo.audit_logs(created_at);

/* =========================
   Seed roles
   ========================= */

IF NOT EXISTS (SELECT 1 FROM dbo.roles WHERE role_code = N'ADMIN')
    INSERT INTO dbo.roles(role_code, role_name) VALUES (N'ADMIN', N'Quản trị hệ thống');
IF NOT EXISTS (SELECT 1 FROM dbo.roles WHERE role_code = N'CONSULTANT')
    INSERT INTO dbo.roles(role_code, role_name) VALUES (N'CONSULTANT', N'Nhân viên tư vấn');
IF NOT EXISTS (SELECT 1 FROM dbo.roles WHERE role_code = N'TEACHER')
    INSERT INTO dbo.roles(role_code, role_name) VALUES (N'TEACHER', N'Giáo viên');
IF NOT EXISTS (SELECT 1 FROM dbo.roles WHERE role_code = N'STUDENT')
    INSERT INTO dbo.roles(role_code, role_name) VALUES (N'STUDENT', N'Học viên');
IF NOT EXISTS (SELECT 1 FROM dbo.roles WHERE role_code = N'ACCOUNTANT')
    INSERT INTO dbo.roles(role_code, role_name) VALUES (N'ACCOUNTANT', N'Kế toán');

/* Quick sanity: list tables */
SELECT name AS table_name
FROM sys.tables
ORDER BY name;
GO
