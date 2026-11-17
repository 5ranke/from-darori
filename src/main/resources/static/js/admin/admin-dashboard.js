/* 대시보드 조회와 화면 상태 처리. 소식 상세·수정·삭제 API는 아직 연결하지 않습니다. */
(async () => {
  const authenticatedUser = await window.DaroriAdminAuthPage.ready;
  if (!authenticatedUser) return;
  const { routes, showDialog } = window.DaroriAdmin;
  Object.assign(routes, { notices: 'notices.html', subscribers: 'subscribers.html', operators: 'users.html' });
  const $ = id => document.getElementById(id);
  const panel = $('admin-content');
  const stats = $('admin-stats');
  const list = $('admin-news-list');
  const status = $('admin-dashboard-status');
  const retry = $('admin-dashboard-retry');
  const count = $('admin-report-count');
  const values = [];
  let busy = false;
  $('admin-name').textContent = authenticatedUser.displayName;
  function unlinked(label) {
    showDialog({ title: `${label} 미연동`, description: '이 동작은 아직 연결되지 않았습니다. 서버 데이터는 변경하지 않습니다.', confirmLabel: '닫기' });
  }
  // 기존 3개 카드 + 주민 제보 확인 영역에 API의 집계값 4개를 표시합니다.
  for (const stat of [
    { label: '소식', route: 'news' }, { label: '구독자', route: 'subscribers' }, { label: '편지 발행', route: 'letters' }
  ]) {
    const card = document.createElement('article'); card.className = 'admin-stat admin-card';
    const top = document.createElement('div'); top.className = 'admin-stat-top';
    const title = document.createElement('h3'); title.textContent = stat.label;
    const button = document.createElement('button'); button.type = 'button';
    button.className = 'admin-button admin-button--primary admin-stat-link'; button.textContent = '↗';
    button.setAttribute('aria-label', `${stat.label} 관리로 이동`); button.dataset.adminRoute = stat.route;
    if (stat.route !== 'subscribers') button.addEventListener('click', event => { event.stopPropagation(); unlinked(`${stat.label} 관리`); });
    const value = document.createElement('strong'); value.className = 'admin-stat-value'; value.textContent = '—';
    values.push(value); top.append(title, button); card.append(top, value); stats.append(card);
  }
  document.querySelector('[data-admin-route="reports"]').addEventListener('click', event => {
    event.stopPropagation(); unlinked('주민 제보 조회');
  });
  function listMessage(message) {
    list.replaceChildren();
    const item = document.createElement('li'); item.className = 'admin-news-item'; item.textContent = message; list.append(item);
  }
  function render(data) {
    data.statistics.forEach((stat, i) => { values[i].textContent = `${stat.count.toLocaleString('ko-KR')}${stat.unit}`; });
    count.textContent = `${data.pendingReportCount.toLocaleString('ko-KR')}건`;
    list.replaceChildren();
    if (!data.recentNews.length) { listMessage('최근 등록한 소식이 없습니다.'); return; }
    data.recentNews.forEach(news => {
      const item = document.createElement('li'); item.className = 'admin-news-item';
      const copy = document.createElement('div'); copy.className = 'admin-news-copy';
      const title = document.createElement('button'); title.type = 'button'; title.className = 'admin-news-title'; title.textContent = news.title;
      title.addEventListener('click', () => unlinked('소식 상세 조회'));
      const meta = document.createElement('p'); meta.className = 'admin-news-meta'; meta.textContent = `${news.category} | ${news.date}`;
      copy.append(title, meta);
      const actions = document.createElement('div'); actions.className = 'admin-news-actions';
      const remove = document.createElement('button'); remove.type = 'button'; remove.className = 'admin-button admin-button--outline'; remove.textContent = '삭제';
      remove.setAttribute('aria-label', `${news.title} 삭제`); remove.addEventListener('click', () => unlinked('소식 삭제'));
      const edit = document.createElement('button'); edit.type = 'button'; edit.className = 'admin-button admin-button--primary'; edit.textContent = '수정';
      edit.setAttribute('aria-label', `${news.title} 수정`); edit.addEventListener('click', () => unlinked('소식 수정'));
      actions.append(remove, edit); item.append(copy, actions); list.append(item);
    });
  }
  async function load() {
    if (busy) return;
    busy = true; panel.setAttribute('aria-busy', 'true'); retry.hidden = true; retry.disabled = true;
    values.forEach(value => { value.textContent = '—'; }); count.textContent = '—';
    listMessage('소식을 불러오는 중입니다.'); status.textContent = '대시보드를 불러오는 중입니다.';
    try {
      const data = await window.DaroriAdminDashboardData.getDashboard();
      render(data); status.textContent = '대시보드를 불러왔습니다.';
    } catch (error) {
      listMessage('소식을 조회하지 못했습니다.');
      if (error.kind === 'session') {
        status.textContent = '인증 상태가 만료되었습니다. 로그인 화면으로 이동합니다.';
        window.location.replace('login.html?reason=expired'); return;
      }
      status.textContent = error.kind === 'network' ? error.message
        : error.kind === 'forbidden' ? '대시보드 조회 권한이 없습니다.'
          : '대시보드를 조회하지 못했습니다. 다시 시도해 주세요.';
      retry.hidden = false;
    } finally { busy = false; retry.disabled = false; panel.setAttribute('aria-busy', 'false'); }
  }
  retry.addEventListener('click', load);
  await load();
})();
