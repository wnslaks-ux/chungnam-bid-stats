<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="common/header.jspf" %>
<h2 class="page-title">관리자 로그인</h2>
<div class="content"><div class="panel login-panel">
  <c:if test="${param.error != null}"><div class="alert alert-danger py-2">아이디 또는 비밀번호가 올바르지 않습니다.</div></c:if>
  <form method="post" action="/login">
    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
    <div class="mb-2"><label class="form-label">아이디</label><input name="username" class="form-control" autocomplete="username" required autofocus></div>
    <div class="mb-3"><label class="form-label">비밀번호</label><input type="password" name="password" class="form-control" autocomplete="current-password" required></div>
    <button class="btn btn-primary w-100">로그인</button>
  </form>
</div></div>
<%@ include file="common/footer.jspf" %>
