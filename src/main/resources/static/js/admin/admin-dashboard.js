/* 대시보드 조회, 소식 상세·삭제 및 관리 화면 연결. */
(async () => {
  const authenticatedUser = await window.DaroriAdminAuthPage.ready;
  if (!authenticatedUser) return;
  const { routes, showDialog } = window.DaroriAdmin;
  const $ = id => document.getElementById(id);
  const panel = $('admin-content');
  const stats = $('admin-stats');
  const list = $('admin-news-list');
  const status = $('admin-dashboard-status');
  const retry = $('admin-dashboard-retry');
  const count = $('admin-report-count');
  const values = [];
  let busy = false;
  let uncertainDeletion = false;
  function setBusy(value) {
    busy = value; panel.setAttribute('aria-busy', String(value));
    retry.disabled = value;
    for (const button of list.querySelectorAll('button')) {
      button.disabled = value || (uncertainDeletion && button.dataset.dashboardDelete === 'true');
    }
  }
  function sessionExpired(error) {
    if (error.kind !== 'session') return false;
    status.textContent = '인증 상태가 만료되었습니다. 로그인 화면으로 이동합니다.';
    window.location.replace('login.html?reason=expired'); return true;
  }
  async function openStory(news) {
    if (busy || $('admin-dialog').open) return;
    setBusy(true); status.textContent = '소식 상세를 불러오는 중입니다.';
    try {
      const story = await window.DaroriAdminDashboardData.getStory(news.id);
      showDialog({ title: story.title, description: `${story.category} | ${story.date}\n\n${story.body}`, confirmLabel: '닫기' });
      status.textContent = '소식 상세를 불러왔습니다.';
    } catch (error) {
      if (!sessionExpired(error)) {
        status.textContent = '소식 상세를 불러오지 못했습니다.';
        showDialog({ title: '소식 상세 조회 실패', description: error.message || '다시 시도해 주세요.', confirmLabel: '닫기' });
      }
    } finally { setBusy(false); }
  }
  function confirmDelete(news) {
    if (busy || uncertainDeletion || $('admin-dialog').open) return;
    showDialog({ title: '소식을 삭제하시겠습니까?',
      description: `서버에 저장된 아래 소식을 삭제합니다. 삭제한 소식은 복구할 수 없습니다.\n\n${news.title}`,
      confirmLabel: '삭제하기', onConfirm: () => deleteStory(news.id) });
  }
  async function deleteStory(id) {
    if (busy || uncertainDeletion) return;
    setBusy(true); status.textContent = '소식을 삭제하는 중입니다.';
    try {
      await window.DaroriAdminDashboardData.deleteStory(id);
      // 삭제 성공과 목록 재조회 실패를 구분합니다. 삭제 요청은 재시도하지 않습니다.
      setBusy(false); await load('소식을 삭제했습니다.');
    } catch (error) {
      if (sessionExpired(error)) return;
      const knownFailure = error.status >= 400 && error.status < 500;
      uncertainDeletion = !knownFailure;
      status.textContent = knownFailure
        ? (error.message || '소식을 삭제하지 못했습니다. 목록을 다시 조회해 주세요.')
        : '삭제 요청의 처리 결과를 확인하지 못했습니다. 다시 삭제하기 전에 목록을 재조회해 주세요.';
      retry.hidden = false;
    } finally { setBusy(false); }
  }
  $('admin-name').textContent = authenticatedUser.displayName;
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
    const value = document.createElement('strong'); value.className = 'admin-stat-value'; value.textContent = '—';
    values.push(value); top.append(title, button); card.append(top, value); stats.append(card);
  }
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
      title.addEventListener('click', () => openStory(news));
      const meta = document.createElement('p'); meta.className = 'admin-news-meta'; meta.textContent = `${news.category} | ${news.date}`;
      copy.append(title, meta);
      const actions = document.createElement('div'); actions.className = 'admin-news-actions';
      const remove = document.createElement('button'); remove.type = 'button'; remove.className = 'admin-button admin-button--outline'; remove.textContent = '삭제';
      remove.dataset.dashboardDelete = 'true';
      remove.setAttribute('aria-label', `${news.title} 삭제`); remove.addEventListener('click', () => confirmDelete(news));
      const edit = document.createElement('button'); edit.type = 'button'; edit.className = 'admin-button admin-button--primary'; edit.textContent = '수정';
      edit.setAttribute('aria-label', `${news.title} 수정`); edit.addEventListener('click', () => {
        if (busy) return;
        window.location.assign(`${routes.news}?${new URLSearchParams({ storyId: news.id })}`);
      });
      actions.append(remove, edit); item.append(copy, actions); list.append(item);
    });
  }
  async function load(successMessage = '') {
    if (busy) return;
    setBusy(true); retry.hidden = true;
    values.forEach(value => { value.textContent = '—'; }); count.textContent = '—';
    listMessage('소식을 불러오는 중입니다.'); status.textContent = successMessage ? `${successMessage} 목록을 다시 조회하는 중입니다.` : '대시보드를 불러오는 중입니다.';
    try {
      const data = await window.DaroriAdminDashboardData.getDashboard();
      uncertainDeletion = false; render(data); status.textContent = successMessage || '대시보드를 불러왔습니다.';
    } catch (error) {
      listMessage('소식을 조회하지 못했습니다.');
      if (sessionExpired(error)) return;
      const message = error.kind === 'network' ? error.message
        : error.kind === 'forbidden' ? '대시보드 조회 권한이 없습니다.'
          : '대시보드를 조회하지 못했습니다. 다시 시도해 주세요.';
      status.textContent = successMessage ? `${successMessage} 다만 목록 재조회에 실패했습니다. ${message}` : message;
      retry.hidden = false;
    } finally { setBusy(false); }
  }
  retry.addEventListener('click', () => load());
  await load();
})();
