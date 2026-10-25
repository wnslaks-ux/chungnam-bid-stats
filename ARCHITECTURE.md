# 아키텍처

## 계층
1. **JSP (View)** `src/main/webapp/WEB-INF/jsp` — 화면. 공통 머리/꼬리는 `common/header.jspf`, `footer.jspf`, 검색폼 `searchForm.jspf`, 결과표 `resultTable.jspf`.
2. **Controller** — 요청 파라미터를 `SearchFormDto`/`SearchPresetVo`로 받아 Service 호출 후 JSP로 전달.
   - `/` 홈, `/region/{code}` 지역 통계 이동, `/api/regions` 지역 JSON
   - `/search` 검색 (`run=1` 이면 조회, `presetId` 로 저장조건 실행)
   - `/settings` 조건 목록·등록·수정(`/settings/save`)·삭제(`/settings/delete/{id}`)
   - `/stats` 통계 (검색과 같은 파라미터)
3. **Service** — 업무 로직
   - `G2bApiClient`: 나라장터 API 호출 + JSON→`BidNoticeVo` 변환, 키 없으면 Mock
   - `BidSearchService`: 금액·키워드 필터, 실데이터는 `bid_cache` 저장
   - `StatsService`: 건수/합계/평균/최대/최소, 월별 합계·건수
   - `SettingsService`: 저장 조건 CRUD
4. **Mapper (MyBatis)** — 인터페이스 `mapper/*.java` + SQL `resources/mapper/*.xml`
5. **DB (H2 파일)** — `schema.sql`(region, search_preset, bid_cache), `data.sql`(지역 시드). MariaDB 전환 시 `application.yml` datasource 와 pom 드라이버만 교체.

## 자주 바꾸는 곳
- **지역 추가/변경**: `config/Regions.java` + `resources/data.sql` 의 region MERGE 문 (두 곳 모두)
- **API 필드 매핑**: `G2bApiClient.map()` (예: 기초금액 `bdgtAmt`, 개찰일 `opengDt` 추가 시 `BidNoticeVo`에 필드 추가)
- **API 파라미터**: `G2bApiClient.fetchConstructionBids()` (기간·페이지 수, `numOfRows`는 application.yml)
- **용역/물품으로 확장**: 오퍼레이션명 `getBidPblancListInfoServcPPSSrch`, `getBidPblancListInfoThngPPSSrch` 로 메서드 추가
- **통계 항목 추가**: `StatsService` + `StatsSummaryVo` + `stats.jsp`
