# 충남 건설입찰 통계 (chungnam-bid-stats)

충남 예산군·홍성군·서산시·태안군·부여군·논산시·청양군의 **공사 입찰공고**를 나라장터 Open API로 조회하고,
시·군별 건수 / 추정금액 합계 / 평균 / 월별 차트를 보여주는 Spring Boot + JSP + MyBatis 웹 앱입니다.

## 1. 실행 방법
필요: Java 17 이상, Maven 3.8+
```bash
mvn spring-boot:run
# 또는
mvn -DskipTests package
java -jar target/chungnam-bid-stats-0.1.0.war
```
브라우저에서 http://localhost:8080 접속. H2 콘솔: http://localhost:8080/h2-console (JDBC URL `jdbc:h2:file:./data/bidstats`, 사용자 `sa`).

API 키가 없으면 **데모(Mock) 모드**로 지역별 가짜 공고 40건이 표시됩니다.

## 2. 나라장터 API 키(무료) 발급
1. https://www.data.go.kr 회원가입/로그인
2. "조달청_나라장터 입찰공고정보서비스" 검색 → https://www.data.go.kr/data/15129394/openapi.do → **활용신청** (개발계정 자동승인)
3. 마이페이지 > 개발계정에서 **일반 인증키(Encoding)** 복사
4. 설정 (둘 중 하나)
   - 환경변수: `export G2B_SERVICE_KEY=발급받은키` (Windows: `set G2B_SERVICE_KEY=...`)
   - `src/main/resources/application.yml` 의 `g2b.service-key` 에 직접 입력
5. 재시작하면 상단 "데모 모드" 안내가 사라지고 실제 데이터를 조회합니다.

사용 오퍼레이션: `getBidPblancListInfoCnstwkPPSSrch` (공사 입찰공고 조건검색), 수요기관명(`dminsttNm`)에 시·군 이름을 넣어 조회합니다.
> 참고: 인코딩 키 기준으로 작성되어 있습니다. 키 오류(SERVICE_KEY_IS_NOT_REGISTERED 등)가 나면 Decoding 키로 바꿔보세요.

## 3. 무료 호스팅
- **Render**: Web Service → Docker 또는 Build `mvn -DskipTests package`, Start `java -jar target/chungnam-bid-stats-0.1.0.war`, 환경변수 `G2B_SERVICE_KEY`. 무료 티어는 유휴 시 잠들고, 디스크가 초기화되므로 H2 파일 데이터(저장 조건)가 사라질 수 있음 → 오래 쓰려면 무료 MySQL/PostgreSQL로 교체.
- **Railway**: GitHub 연결 후 자동 빌드, 같은 Start 명령. (무료 크레딧 한도 확인)
- **집/사무실 PC**: `java -jar` 실행 + Cloudflare Tunnel 로 외부 공개 (완전 무료).
- 포트는 `PORT` 환경변수 사용 시 `server.port: ${PORT:8080}` 로 바꾸세요.

## 4. 패키지 구조 (AI/GPT로 수정할 때 참고)
```
com.chungnam.bidstats
 ├ BidStatsApplication     실행 진입점
 ├ config/  WebMvcConfig(JSP 뷰, RestTemplate), Regions(대상 시·군 상수)
 ├ controller/ Home, Region, Search, Settings, Stats  (URL ↔ 화면)
 ├ service/ BidSearchService(검색+필터), StatsService(집계), SettingsService(조건 CRUD), G2bApiClient(나라장터 호출/Mock)
 ├ mapper/  SearchPresetMapper, BidCacheMapper (MyBatis 인터페이스)
 ├ vo/      RegionVo, SearchPresetVo, BidNoticeVo, StatsSummaryVo
 └ dto/     SearchFormDto (검색 폼)
resources/  application.yml, schema.sql, data.sql, mapper/*.xml (SQL)
webapp/WEB-INF/jsp/ home, search, settings, stats + common/*.jspf
```
GPT에 요청할 때는 "`StatsService`에 연도별 집계 추가해줘"처럼 **파일 이름을 지정**하면 정확하게 수정됩니다.

## 5. 대상 지역
| 코드 | 이름 |
|---|---|
| YESAN | 예산군 |
| HONGSEONG | 홍성군 |
| SEOSAN | 서산시 |
| TAEAN | 태안군 |
| BUYEO | 부여군 |
| NONSAN | 논산시 |
| CHEONGYANG | 청양군 |

## 관리자 로그인 / 운영 설정
- 로그인: `http://localhost:8080/login` → 관리자 화면 `/admin`
- 기본 계정: **admin / admin123** (최초 실행 시 H2 `admin_user` 테이블에 BCrypt로 저장). 
  환경변수 `ADMIN_USERNAME`, `ADMIN_PASSWORD`로 최초 값 변경 가능. **로그인 후 반드시 `/admin/password`에서 비밀번호를 바꾸세요.**
- API 키: `/admin/api-key` 에서 저장 → H2 `app_config` 테이블에 저장, 재시작 없이 즉시 적용, `application.yml` 값보다 우선.
- 검색·통계·시군 페이지는 로그인 없이 공개, `/admin/**`, `/h2-console` 은 로그인 필요.
- 비밀번호 분실 시: 서버 종료 후 H2에서 `DELETE FROM admin_user` 실행 → 재시작하면 기본값으로 다시 생성.

## 휴대폰에서 보기 (반응형)
- 화면은 768px 이하에서 모바일 레이아웃(햄버거 메뉴, 세로 폼, 가로 스크롤 표)으로 바뀝니다.
- 같은 와이파이: `http://PC의IP:8080` (방화벽 8080 허용 필요)
- 외부(LTE 등)에서 보려면 터널(예: Cloudflare Tunnel, ngrok)로 공개 주소를 만들어야 합니다. 공개 전 관리자 비밀번호를 꼭 변경하세요.

## Render 무료 배포 (PC 꺼도 동작)
1. 이 폴더를 GitHub 저장소에 올립니다 (`application.yml`에 실제 키 넣지 말 것, `data/`는 제외).
2. Render → New → Blueprint → 저장소 선택 (`render.yaml` 자동 인식, Docker 빌드).
3. 환경변수 입력: `G2B_SERVICE_KEY`(나라장터 Encoding 키), `ADMIN_PASSWORD`(관리자 비번), `ADMIN_USERNAME`(기본 admin).
4. 배포 완료 후 `https://<서비스명>.onrender.com` 으로 PC·모바일 접속.

주의: 무료 플랜 디스크는 임시(재시작 시 H2 `/tmp/bidstats-data` 초기화)입니다. 관리자 화면에서 바꾼 설정은 사라질 수 있으니 키·비번은 반드시 환경변수로 설정하세요. 무료 플랜은 15분 미사용 시 잠들고 첫 접속이 30초~1분 걸립니다. cloud 프로필에서는 H2 콘솔이 꺼집니다.
