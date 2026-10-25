<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="../common/header.jspf" %>
<h2 class="page-title">API 키 설정</h2>
<div class="content">
<%@ include file="msg.jspf" %>
<div class="panel"><h5>현재 상태</h5>
  <p class="mb-1">상태: <b>${keyStatus == 'DB' ? '관리자 화면에서 저장됨(DB)' : (keyStatus == 'YML' ? 'application.yml / 환경변수' : '미설정 (데모 모드)')}</b></p>
  <p class="mb-0">키: <code>${empty keyMasked ? '-' : keyMasked}</code></p>
</div>
<div class="panel"><h5>새 키 저장</h5>
  <form method="post" action="/admin/api-key">
    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
    <label class="form-label">공공데이터포털 인증키 (Encoding 또는 Decoding)</label>
    <input type="password" name="serviceKey" class="form-control mb-2" autocomplete="off" placeholder="인증키 붙여넣기">
    <div class="btn-row">
      <button class="btn btn-primary" name="action" value="save">저장</button>
      <button class="btn btn-outline-danger" name="action" value="clear" onclick="return confirm('DB에 저장된 키를 삭제할까요?')">저장된 키 삭제</button>
    </div>
    <p class="text-muted small mt-2 mb-0">DB(app_config)에 저장되어 재시작 후에도 유지되며 즉시 적용됩니다. DB 값이 application.yml 값보다 우선합니다.</p>
  </form>
</div>
</div>
<%@ include file="../common/footer.jspf" %>
