<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="common/header.jspf" %>
<h2 class="page-title">충남 건설입찰 통계 <small>대상 시·군을 선택하세요</small></h2>
<div class="content">
<div class="region-grid">
<c:forEach var="r" items="${regions}">
  <a href="/region/${r.regionCode}" class="region-card"><b>${r.regionName}</b><small>통계 보기 &rarr;</small></a>
</c:forEach>
</div>
<div class="panel">
  <h5>사용 방법</h5>
  <ol class="mb-0">
    <li><b>설정</b>에서 자주 쓰는 검색조건(지역·기간·금액·키워드)을 저장합니다.</li>
    <li><b>검색</b>에서 조건을 입력하거나 저장된 조건을 선택한 뒤 <b>검색</b> 버튼을 누릅니다.</li>
    <li><b>통계</b>에서 건수·추정금액 합계·평균과 월별 차트를 확인합니다.</li>
  </ol>
</div>
</div>
<%@ include file="common/footer.jspf" %>
