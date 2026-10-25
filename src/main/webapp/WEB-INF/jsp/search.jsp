<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="common/header.jspf" %>
<c:if test="${not empty errorMessage}"><div class="alert alert-danger">${errorMessage}</div></c:if>
<h2 class="page-title">입찰공고 검색 <small>공사</small></h2>
<div class="content">
<div class="alert alert-info py-2 small mb-3">나라장터는 <b>한 번에 약 1개월</b> 단위로 조회합니다. 기본값은 최근 1개월이며, 기간을 늘리면 월별로 나눠 합산합니다. 시·군은 수요기관명 기준입니다.</div>

<c:if test="${not empty presets}">
<div class="mb-3 small">저장된 조건:
  <c:forEach var="p" items="${presets}">
    <a class="btn btn-sm btn-outline-secondary me-1" href="/search?presetId=${p.id}">${p.presetName}</a>
  </c:forEach>
</div>
</c:if>
<c:set var="formAction" value="/search"/>
<%@ include file="common/searchForm.jspf" %>
<c:if test="${results != null}">
  <div class="d-flex justify-content-between align-items-center mb-2">
    <span>검색 결과 <b>${totalCount}</b>건 <small class="text-muted">(${form.dateFrom} ~ ${form.dateTo})</small></span>
    <a class="btn btn-sm btn-success" href="/stats?${statsQuery}&run=1">이 조건으로 통계 보기</a>
  </div>
  <c:if test="${totalCount == 0}">
    <div class="alert alert-warning">조회된 공사 입찰공고가 없습니다. 기간을 넓히거나 다른 시·군을 선택해 보세요.</div>
  </c:if>
  <%@ include file="common/resultTable.jspf" %>
</c:if>
<c:if test="${results == null}">
  <div class="text-muted small">조건을 선택한 뒤 <b>검색</b>을 누르면 나라장터 공사 입찰공고와 통계용 목록이 표시됩니다.</div>
</c:if>
</div>
<%@ include file="common/footer.jspf" %>
