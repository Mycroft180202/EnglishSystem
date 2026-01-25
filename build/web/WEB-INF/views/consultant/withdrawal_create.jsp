<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>

<t:layout title="Tạo yêu cầu rút tiền" active="wallet-withdrawals">
    <div class="d-flex flex-wrap gap-2 align-items-center justify-content-between mb-3">
        <div>
            <h3 class="m-0">Tạo yêu cầu rút tiền</h3>
            <div class="text-muted small">Chỉ rút tiền mặt tại trung tâm. Kế toán phải duyệt.</div>
        </div>
        <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/consultant/wallet-withdrawals">Quay lại</a>
    </div>

    <c:if test="${not empty flashSuccess}">
        <div class="alert alert-success"><c:out value="${flashSuccess}"/></div>
    </c:if>
    <c:if test="${not empty flashError}">
        <div class="alert alert-danger"><c:out value="${flashError}"/></div>
    </c:if>

    <form method="post" action="${pageContext.request.contextPath}/consultant/wallet-withdrawals/create" class="card"
          onsubmit="this.querySelector('button[type=submit]').disabled=true;">
        <div class="card-body">
            <input type="hidden" name="formToken" value="${formToken}">

            <div class="row g-3">
                <div class="col-md-6">
                    <label class="form-label">Tìm học viên</label>
                    <input class="form-control" id="studentSearch" placeholder="Nhập tên / SĐT / mã học viên...">
                </div>

                <div class="col-md-6">
                    <label class="form-label">Chọn học viên</label>
                    <select class="form-select" name="studentId" id="studentId" required>
                        <option value="">-- Chọn học viên --</option>
                        <c:forEach items="${students}" var="s">
                            <option value="${s.studentId}" data-balance="${s.balance}" ${selectedStudentId == s.studentId ? 'selected' : ''}>
                                <c:out value="${s.studentId}"/> - <c:out value="${s.fullName}"/><c:if test="${not empty s.phone}"> (<c:out value="${s.phone}"/>)</c:if>
                            </option>
                        </c:forEach>
                    </select>
                    <div class="form-text">Số dư ví: <b id="walletBalanceText">--</b></div>
                </div>

                <div class="col-md-12">
                    <label class="form-label">Hình thức rút</label>
                    <div class="d-flex flex-wrap gap-3">
                        <div class="form-check">
                            <input class="form-check-input" type="radio" name="mode" id="modeAll" value="ALL" checked>
                            <label class="form-check-label" for="modeAll">Rút toàn bộ</label>
                        </div>
                        <div class="form-check">
                            <input class="form-check-input" type="radio" name="mode" id="modeCustom" value="CUSTOM">
                            <label class="form-check-label" for="modeCustom">Rút theo yêu cầu</label>
                        </div>
                    </div>
                </div>

                <div class="col-md-4">
                    <label class="form-label">Số tiền rút</label>
                    <input class="form-control" type="number" min="0" step="0.01" name="amount" id="amount" placeholder="0">
                    <div class="form-text">Nếu chọn “Rút toàn bộ”, hệ thống sẽ tự điền theo số dư ví.</div>
                </div>

                <div class="col-md-8">
                    <label class="form-label">Ghi chú (tùy chọn)</label>
                    <input class="form-control" name="note" maxlength="255">
                </div>
            </div>

            <div class="mt-3">
                <button class="btn btn-primary" type="submit">Gửi cho kế toán duyệt</button>
            </div>
        </div>
    </form>
</t:layout>

<script>
    (function () {
        const searchEl = document.getElementById('studentSearch');
        const selectEl = document.getElementById('studentId');
        const balanceTextEl = document.getElementById('walletBalanceText');
        const modeAllEl = document.getElementById('modeAll');
        const modeCustomEl = document.getElementById('modeCustom');
        const amountEl = document.getElementById('amount');
        if (!selectEl || !balanceTextEl || !modeAllEl || !modeCustomEl || !amountEl) return;

        const options = Array.from(selectEl.options);

        function fmtVnd(n) {
            const x = Number(n || 0);
            try {
                return new Intl.NumberFormat('vi-VN').format(x) + ' VND';
            } catch (e) {
                return x + ' VND';
            }
        }

        function currentBalance() {
            const opt = selectEl.options[selectEl.selectedIndex];
            if (!opt) return 0;
            const b = opt.getAttribute('data-balance');
            return b ? Number(b) : 0;
        }

        function syncBalanceUi() {
            const b = currentBalance();
            balanceTextEl.textContent = fmtVnd(b);

            if (modeAllEl.checked) {
                amountEl.value = b > 0 ? String(b) : '';
                amountEl.disabled = true;
                amountEl.removeAttribute('max');
            } else {
                amountEl.disabled = false;
                if (b > 0) amountEl.max = String(b);
                else amountEl.removeAttribute('max');
            }
        }

        function filterOptions() {
            if (!searchEl) return;
            const q = (searchEl.value || '').trim().toLowerCase();
            options.forEach(opt => {
                if (!opt.value) return;
                const text = (opt.text || '').toLowerCase();
                opt.hidden = !!q && !text.includes(q);
            });
        }

        if (searchEl) searchEl.addEventListener('input', filterOptions);
        selectEl.addEventListener('change', syncBalanceUi);
        modeAllEl.addEventListener('change', syncBalanceUi);
        modeCustomEl.addEventListener('change', syncBalanceUi);

        filterOptions();
        syncBalanceUi();
    })();
</script>
