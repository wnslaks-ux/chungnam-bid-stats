<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="common/header.jspf" %>
<h2 class="page-title">검색조건 설정</h2>
<div class="content">
<div class="panel"><h5>${empty edit.id ? '새 검색조건' : '검색조건 수정'}</h5>
<form class="mb-0" method="post" action="/settings/save">
  <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
  <input type="hidden" name="id" value="${edit.id}">
  <div class="row g-2">
    <div class="col-md-3"><label class="form-label">조건 이름</label><input name="presetName" value="${edit.presetName}" class="form-control" required></div>
    <div class="col-md-2"><label class="form-label">시·군</label>
      <select name="regionCode" class="form-select">
        <c:forEach var="r" items="${regions}"><option value="${r.regionCode}" ${r.regionCode == edit.regionCode ? 'selected' : ''}>${r.regionName}</option></c:forEach>
      </select></div>
    <div class="col-md-2"><label class="form-label">시작일</label><input type="date" name="dateFrom" value="${edit.dateFrom}" class="form-control"></div>
    <div class="col-md-2"><label class="form-label">종료일</label><input type="date" name="dateTo" value="${edit.dateTo}" class="form-control"></div>
    <div class="col-md-3"><label class="form-label">키워드</label><input name="keyword" value="${edit.keyword}" class="form-control"></div>
    <div class="col-md-3"><label class="form-label">최소 추정가격(원)</label><input type="number" name="minPrice" value="${edit.minPrice}" class="form-control"></div>
    <div class="col-md-3"><label class="form-label">최대 추정가격(원)</label><input type="number" name="maxPrice" value="${edit.maxPrice}" class="form-control"></div>
  </div>
  <div class="mt-3"><button class="btn btn-primary">${empty edit.id ? '저장' : '수정'}</button>
    <c:if test="${not empty edit.id}"><a href="/settings" class="btn btn-link">취소</a></c:if></div>
</form></div>
<div class="panel"><h5>저장된 검색조건</h5>
<div class="table-responsive"><table class="table table-sm table-bid mb-0">
  <thead><tr><th>이름</th><th>시·군</th><th>기간</th><th>금액대</th><th>키워드</th><th></th></tr></thead>
  <tbody>
  <c:forEach var="p" items="${presets}">
    <tr><td>${p.presetName}</td><td>${p.regionName}</td><td>${p.dateFrom} ~ ${p.dateTo}</td>
      <td><fmt:formatNumber value="${p.minPrice}" pattern="#,###"/> ~ <fmt:formatNumber value="${p.maxPrice}" pattern="#,###"/></td>
      <td>${p.keyword}</td>
      <td class="text-nowrap">
        <a class="btn btn-sm btn-primary" href="/search?presetId=${p.id}">검색</a>
        <a class="btn btn-sm btn-outline-secondary" href="/settings?editId=${p.id}">수정</a>
        <form method="post" action="/settings/delete/${p.id}" class="d-inline" onsubmit="return confirm('삭제할까요?')"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"><button class="btn btn-sm btn-outline-danger">삭제</button></form>
      </td></tr>
  </c:forEach>
  </tbody>
</table></div></div>
</div>
<%@ include file="common/footer.jspf" %>
