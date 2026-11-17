/* 대시보드의 화면 표시와 예시 상호작용만 담당합니다. */
(async () => {
  const authenticatedUser = await window.DaroriAdminAuthPage.ready;
  if (!authenticatedUser) return;
  const { showDialog, navigate } = window.DaroriAdmin;
  const data = await window.DaroriAdminDashboardData.getDashboard();
  document.getElementById('admin-name').textContent = authenticatedUser.displayName;
  document.getElementById('admin-report-count').textContent = `${data.pendingReportCount}건`;
  const stats = document.getElementById('admin-stats');
  data.statistics.forEach(stat => {
    const card = document.createElement('article');
    card.className = 'admin-stat admin-card';
    const top = document.createElement('div');
    top.className = 'admin-stat-top';
    const title = document.createElement('h3');
    title.textContent = stat.label;
    const button = document.createElement('button');
    button.type = 'button';
    button.className = 'admin-button admin-button--primary admin-stat-link';
    button.textContent = '↗';
    button.setAttribute('aria-label', `${stat.label} 관리로 이동`);
    button.dataset.adminRoute = stat.route;
    const value = document.createElement('strong');
    value.className = 'admin-stat-value';
    value.textContent = `${stat.count.toLocaleString('ko-KR')}${stat.unit}`;
    top.append(title, button);
    card.append(top, value);
    stats.append(card);
  });
  const list = document.getElementById('admin-news-list');
  data.recentNews.forEach(news => {
    const item = document.createElement('li');
    item.className = 'admin-news-item';
    const copy = document.createElement('div');
    copy.className = 'admin-news-copy';
    const title = document.createElement('button');
    title.type = 'button';
    title.className = 'admin-news-title';
    title.textContent = news.title;
    title.addEventListener('click', () => showDialog({ title: news.title, description: `${news.category} | ${news.date}\n\n${news.content}`, confirmLabel: '닫기' }));
    const meta = document.createElement('p');
    meta.className = 'admin-news-meta';
    meta.textContent = `${news.category} | ${news.date}`;
    copy.append(title, meta);
    const actions = document.createElement('div');
    actions.className = 'admin-news-actions';
    const remove = document.createElement('button');
    remove.type = 'button';
    remove.className = 'admin-button admin-button--outline';
    remove.textContent = '삭제';
    remove.setAttribute('aria-label', `${news.title} 삭제`);
    remove.addEventListener('click', () => showDialog({
      title: '소식을 삭제하시겠습니까?',
      description: `예시 목록에서만 삭제됩니다. 새로고침하면 다시 표시됩니다.\n\n${news.title}`,
      confirmLabel: '삭제하기',
      onConfirm: () => { item.remove(); if (!list.children.length) { const empty = document.createElement('li'); empty.className = 'admin-news-item'; empty.textContent = '최근 등록한 소식이 없습니다.'; list.append(empty); } }
    }));
    const edit = document.createElement('button');
    edit.type = 'button';
    edit.className = 'admin-button admin-button--primary';
    edit.textContent = '수정';
    edit.setAttribute('aria-label', `${news.title} 수정`);
    edit.addEventListener('click', () => navigate('news'));
    actions.append(remove, edit);
    item.append(copy, actions);
    list.append(item);
  });
})();
