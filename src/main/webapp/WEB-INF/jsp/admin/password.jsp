<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="../common/header.jspf" %>
<h2 class="page-title">비밀번호 변경</h2>
<div class="content">
<%@ include file="msg.jspf" %>
<div class="panel login-panel">
  <form method="post" action="/admin/password">
    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
    <div class="mb-2"><label class="form-label">현재 비밀번호</label><input type="password" name="current" class="form-control" autocomplete="current-password" required></div>
    <div class="mb-2"><label class="form-label">새 비밀번호 (8자 이상)</label><input type="password" name="next" class="form-control" autocomplete="new-password" minlength="8" required></div>
    <div class="mb-3"><label class="form-label">새 비밀번호 확인</label><input type="password" name="confirm" class="form-control" autocomplete="new-password" required></div>
    <button class="btn btn-primary w-100">변경</button>
  </form>
</div>
</div>
<%@ include file="../common/footer.jspf" %>
