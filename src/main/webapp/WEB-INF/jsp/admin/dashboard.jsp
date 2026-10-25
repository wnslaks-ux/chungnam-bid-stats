<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="../common/header.jspf" %>
<h2 class="page-title">운영관리<small>${pageContext.request.userPrincipal.name}님</small></h2>
<div class="content">
<div class="row g-2 mb-3 metric-row">
  <div class="col-6 col-md-4"><div class="metric"><div class="lbl">API 모드</div><div class="val">${mockMode ? '데모' : '실데이터'}</div></div></div>
  <div class="col-6 col-md-4"><div class="metric accent"><div class="lbl">API 키 저장 위치</div><div class="val">${keyStatus == 'DB' ? 'DB' : (keyStatus == 'YML' ? 'yml' : '없음')}</div></div></div>
  <div class="col-12 col-md-4"><div class="metric"><div class="lbl">키(마스킹)</div><div class="val small-val">${empty keyMasked ? '-' : keyMasked}</div></div></div>
</div>
<div class="admin-links">
  <a class="region-card" href="/admin/api-key"><b>API 키 설정</b><small>나라장터 인증키 변경</small></a>
  <a class="region-card" href="/admin/password"><b>비밀번호 변경</b><small>관리자 계정</small></a>
  <a class="region-card" href="/settings"><b>검색조건</b><small>저장된 프리셋 관리</small></a>
  <a class="region-card" href="/stats"><b>통계 보기</b><small>시·군별 입찰통계</small></a>
</div>
</div>
<%@ include file="../common/footer.jspf" %>
