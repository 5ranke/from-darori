# From. 다로리 관리 영역

공통 레이아웃과 대시보드·공지사항 관리·구독자 관리·운영자 계정 관리 화면은 develop에 반영되어 있습니다. 운영자 로그인·인증 API 호출 코드를 연결했습니다. 실제 서버 인증 성공은 아직 검증하지 않았습니다. 대시보드는 실제 조회 API를 사용합니다. 공지사항·구독자·운영자 계정 관리 데이터는 계속 예시 데이터입니다.

## 미리 보기

VSCode에서 프로젝트 폴더를 연 뒤 메뉴의 **터미널 → 새 터미널**을 선택하세요.
프로젝트 최상위 폴더에서 다음 명령을 실행합니다.

```sh
python3 -m http.server 5500 --bind 127.0.0.1 --directory src/main/resources/static
```

이 정적 서버로는 실제 인증 API를 호출할 수 없습니다. 인증 연동 이후 관리 화면은 인증 확인을 거치므로 실제 동작 검사는 아래 Spring Boot 실행 환경이 필요합니다.
서버를 종료할 때는 터미널에서 Control + C를 누릅니다.
이 서버는 프론트엔드 화면 확인용이며 Spring이나 데이터베이스 실행이 필요하지 않습니다.
VSCode Live Server를 사용한다면 dashboard.html을 우클릭하고 Open with Live Server로 열어도 됩니다.

## 파일 역할

- `admin/dashboard.html`: 대시보드 뼈대, 스크립트·스타일 연결, 공통 팝업.
- `css/admin/admin-common.css`: 관리 전용 색상, 헤더, 탭, 메뉴, 푸터, 버튼, 카드, 입력창, 선택 버튼, 팝업 스타일.
- `css/admin/admin-dashboard.css`: 통계 카드, 주민 제보 영역, 최근 소식 목록 배치.
- `js/admin/admin-common.js`: 재사용할 헤더·메뉴·푸터 생성, 페이지 경로 설정, 공통 팝업.
- `js/admin/admin-dashboard-data.js`: 인증 모듈로 실제 대시보드 API를 호출하고 응답을 화면 모델로 변환합니다.
- `js/admin/admin-dashboard.js`: 조회·로딩·빈 목록·오류·재시도 및 미연동 동작 안내를 처리합니다.

기존의 빈 `css/common.css`, `js/common.js`는 보존했으며 이 페이지에서 사용하지 않습니다.
모든 스타일은 `.admin-page` 내부에 한정됩니다. 새 관리 화면도 body에 이 클래스를 붙이고 공통 CSS·JS를 연결하면 됩니다.
`data-admin-page`에 페이지 키를 지정하면 해당 사이드 메뉴가 선택됩니다.

## 화면에서 확인할 내용

- 관리 탭과 대시보드 메뉴가 선택되어 있습니다.
- 기존 통계 카드 3개와 주민 제보 확인 영역에 서버 집계값 4개가 표시됩니다. 소식 수는 뉴스레터에 연결되지 않은 소식, 구독자는 ACTIVE, 발행은 PUBLISHED, 제보는 PENDING 기준입니다.
- 최근 등록순 소식 최대 3개를 표시합니다. 날짜는 Asia/Seoul 기준입니다. 제목·수정·삭제 버튼은 미연동 안내를 표시합니다.
- 대시보드에서 서버 소식이나 화면 항목을 삭제하지 않습니다. 조회 실패 시 수치는 —로 표시하고 예시 데이터나 0으로 대체하지 않습니다.
- 미구현 페이지 버튼은 연결 준비 안내 팝업을 표시합니다.
- 팝업은 Esc와 닫기/취소로 닫을 수 있습니다.
- 좁은 창에서는 사이드 메뉴가 위로 이동하고 통계 카드는 세로로 배치됩니다.

## 다음 단계 연결 지점

`admin-common.js`의 `routes`에서 null을 확정된 HTML 경로로 바꾸면 버튼을 연결할 수 있습니다. 경로는 현재 admin HTML 기준 상대 경로나 사이트 루트 기준 절대 경로로 지정합니다. 방문자 페이지와 구독 해지 경로도 확정 후 지정합니다. 푸터 이메일은 시안의 예시 문구입니다.

인증 API는 `admin-auth.js`를 사용합니다. 대시보드는 `GET /api/v1/admin/dashboard`에 연결되어 있으며 다른 관리 화면의 데이터 API 연동은 별도 작업입니다.
뉴스레터 제작 담당 화면 및 실제 소식 수정·삭제 기능은 구현하지 않았습니다.

## 운영자 로그인 화면

실제 인증 검사는 Spring Boot와 같은 출처의 `/admin/login.html`에서 진행하세요.

- `admin/login.html`: 인사말·상단 탭·사이드 메뉴가 없는 로그인 전용 화면입니다. 기존 공통 헤더와 푸터를 재사용합니다.
- `css/admin/admin-login.css`: 로그인 카드, 입력창, 체크박스, 화면 높이 및 모바일 배치입니다. 로그인 body 클래스 내부에만 적용됩니다.
- `js/admin/admin-login.js`: 이메일·비밀번호 검증, 로그인 요청, 오류 안내, 성공 후 대시보드 이동을 처리합니다.

이메일은 필수·이메일 형식·최대 254자, 비밀번호는 공백만 입력하지 않도록 검사합니다. 비밀번호는 전송 전에 공백을 제거하지 않습니다. 로그인 유지 체크박스는 최초에 선택되지 않으며 입력 오류도 최초에는 표시되지 않습니다.

## 인증 모듈과 동작

- `js/admin/admin-auth.js`: 로그인·me·refresh·logout, 공통 인증 요청, 401 재발급 후 최대 한 번 재시도, 페이지 내부 동시 재발급 통합. 네트워크/시간 초과, 인증 만료, 자격 증명 오류, 권한 오류, 서버 응답 오류를 구분합니다.
- `js/admin/admin-auth-guard.js`: 페이지 진입 시 `refresh → me`, 비로그인 관리 화면 접근 제한, 인증된 이름 표시, 로그아웃 및 뒤로가기 재확인.
- `css/admin/admin-auth.css`: 인증 확인 전 관리 콘텐츠 숨김, 헤더 로그아웃 버튼 배치, 연결 장애 재시도 안내. 기존 공통 디자인은 유지합니다.
- 관리 페이지 JS는 인증 확인 성공 후 화면을 초기화합니다. 대시보드는 실제 조회, 다른 관리 페이지는 기존 예시 기능을 사용합니다. 인증 장애가 발생하면 새로고침 방식으로 재시도하며 예시 기능은 초기화됩니다.

Access Token은 클로저 메모리에만 보관하며 localStorage/sessionStorage/URL에 저장하지 않습니다. Refresh Token은 서버의 HttpOnly 쿠키만 사용합니다. 비밀번호를 데이터 저장소나 로그에 보관하지 않습니다. 모든 요청은 같은 출처와 `credentials: 'same-origin'`을 사용합니다.

서버 구현상 로그인 유지 미선택은 Refresh Token 7일, 선택은 30일입니다. 미선택도 지속 쿠키이며 브라우저 종료 시 로그아웃을 뜻하지 않습니다. 로그아웃은 Refresh Token 무효화와 쿠키 삭제를 수행하며 이미 발급된 Access Token 자체를 블랙리스트 처리하지는 않습니다. 클라이언트는 성공 시 메모리 토큰을 지웁니다. 실패하면 완료 안내나 강제 이동 없이 재시도를 안내합니다.

## 실제 API 테스트 준비

프로젝트의 Java 17 toolchain, Gradle 의존성, 스키마가 준비된 PostgreSQL과 DB 접속 설정이 필요합니다. `DB_USER`, `DB_PASSWORD`, `JWT_SECRET`을 실행 환경에 설정하세요. JWT_SECRET은 Base64 디코딩 시 32바이트 이상이어야 하며 저장소에 커밋하지 마세요. 테스트 운영자 계정에는 BCrypt 비밀번호 해시가 필요합니다. 자동 테스트 계정 생성 코드는 확인되지 않았습니다.

로컬 HTTP 환경에서는 Refresh 쿠키의 Secure 설정을 확인해야 합니다. 필요하면 실행 환경변수 `JWT_COOKIE_SECURE=false`를 로컬에서만 사용하세요. 운영 HTTPS 환경은 Secure=true를 유지합니다. 백엔드 파일을 바꾸지 않고 설정합니다.

환경이 준비되면 프로젝트 최상위에서 `./gradlew bootRun`을 실행하고 해당 Spring 서버의 `/admin/login.html`을 여세요. 이번 작업에서는 서버를 실행하지 않았습니다. 실제 로그인, 브라우저 쿠키 발급/전송, DB 인증과 토큰 재발급·로그아웃은 서버 환경에서 추가 검증해야 합니다. DOM 모의 검사와 요청 모의 검사는 실제 인증 성공을 증명하지 않습니다.

## 대시보드 API 연동

`DaroriAdminAuthPage.ready` 성공 후 `DaroriAdminAuth.request('/api/v1/admin/dashboard')`를 호출합니다. 인증 모듈이 응답의 result를 반환하며 Bearer Token과 재발급을 처리합니다. 대시보드가 토큰을 별도로 저장하거나 직접 재발급하지 않습니다. 인증 모듈의 최종 session 오류는 로그인 화면 이동으로, 네트워크·서버·권한 오류는 화면 안내와 재시도로 처리합니다.

`statistics`의 storyCount/subscriberCount/publishedNewsletterCount/pendingResidentReportCount를 사용합니다. `recentStories`의 storyId/title/categoryId/categoryName/sourceType/createdAt을 검증하고 표시용으로 변환합니다. 잘못된 응답은 조회 실패로 처리합니다. 실제 0은 0으로, 조회 중·실패는 —로 구분합니다.

소식 본문은 이 응답에 없습니다. 상세·수정·삭제·주민 제보 및 편지 관리 기능은 아직 미연동이므로 버튼을 유지하고 안내만 표시합니다. 대시보드의 사이드 메뉴는 기존 notices.html/subscribers.html/users.html로 연결하며 구독자 통계 버튼도 subscribers.html로 이동합니다. 다른 화면의 데이터 기능은 변경하지 않습니다.

대시보드의 디자인·반응형 레이아웃은 유지합니다. 빈 목록·로딩·재시도 상태만 추가합니다. 실제 서버 로그인·DB 조회·브라우저 쿠키 동작은 서버 환경에서 별도 검증이 필요합니다.
