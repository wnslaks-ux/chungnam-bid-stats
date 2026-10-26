<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="common/header.jspf" %>
<c:if test="${not empty errorMessage}"><div class="alert alert-danger">${errorMessage}</div></c:if>
<c:if test="${not empty requestScope.g2bWarning}"><div class="alert alert-warning">${requestScope.g2bWarning}</div></c:if>
<h2 class="page-title">${summary.regionName} 공사 입찰 통계 <c:if test="${ran}"><small>${form.dateFrom} ~ ${form.dateTo}</small></c:if></h2>
<div class="content">
<div class="alert alert-info py-2 small mb-3">메뉴·시·군은 바로 열립니다. 기간을 고른 뒤 <b>검색</b>을 누르면 나라장터에서 통계를 불러옵니다. 기본 기간은 최근 1개월입니다.</div>

<c:set var="formAction" value="/stats"/>
<%@ include file="common/searchForm.jspf" %>

<c:if test="${not ran}">
  <div class="text-muted small mb-3">시·군과 기간을 확인한 뒤 <b>검색</b>을 누르면 공고 건수·금액 통계와 목록이 표시됩니다.</div>
</c:if>

<c:if test="${ran}">
<div class="row g-3 mb-4">
  <div class="col-6 col-md-3"><div class="metric accent"><div class="lbl">공고 건수</div><div class="val">${summary.count}건</div></div></div>
  <div class="col-6 col-md-3"><div class="metric accent"><div class="lbl">추정금액 합계</div><div class="val"><fmt:formatNumber value="${summary.totalPrice}" pattern="#,###"/>원</div></div></div>
  <div class="col-6 col-md-3"><div class="metric accent"><div class="lbl">평균 추정금액</div><div class="val"><fmt:formatNumber value="${summary.avgPrice}" pattern="#,###"/>원</div></div></div>
  <div class="col-6 col-md-3"><div class="metric accent"><div class="lbl">최대 / 최소</div><div class="val" style="font-size:15px"><fmt:formatNumber value="${summary.maxPrice}" pattern="#,###"/> / <fmt:formatNumber value="${summary.minPrice}" pattern="#,###"/>원</div></div></div>
</div>
<div class="panel"><h5>월별 추정금액 합계 / 건수</h5><div class="chart-box"><canvas id="monthly"></canvas></div></div>
<div class="panel" id="bid-list-panel"><h5>공고 목록</h5><%@ include file="common/resultTable.jspf" %></div>
<script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.1/dist/chart.umd.min.js"></script>
<script>
new Chart(document.getElementById('monthly'), {
  data: {
    labels: ${chartLabels},
    datasets: [
      {type: 'bar', label: '추정금액 합계(원)', data: ${chartTotals}, yAxisID: 'y', backgroundColor: 'rgba(21,101,192,.7)'},
      {type: 'line', label: '건수', data: ${chartCounts}, yAxisID: 'y1', borderColor: '#f57c00'}
    ]
  },
  options: {responsive: true, maintainAspectRatio: false, scales: {y: {position: 'left'}, y1: {position: 'right', grid: {drawOnChartArea: false}}}}
});
</script>
</c:if>
</div>
<%@ include file="common/footer.jspf" %>
