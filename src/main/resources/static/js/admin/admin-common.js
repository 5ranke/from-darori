/* 경로가 확정되면 null을 실제 HTML 경로로 교체하세요. API 주소가 아닙니다. */
window.DaroriAdmin = (() => {
  const routes = {
    dashboard: 'dashboard.html',
    visitor: null,
    newsletter: 'newsletter.html',
    notices: null,
    subscribers: 'subscribers.html',
    operators: 'users.html',
    news: 'news.html',
    reports: 'reports.html',
    schedule: 'schedule.html',
    letters: null,
    unsubscribe: null
  };
  const labels = {
    visitor: '방문자 페이지', newsletter: '뉴스레터 제작', notices: '공지사항 작성',
    subscribers: '구독자 관리', operators: '운영자 관리', news: '소식 작성',
    reports: '주민 제보', schedule: '발행 예약', letters: '편지 발행', unsubscribe: '구독 해지'
  };
  const header = document.querySelector('[data-admin-header]');
  if (header) header.innerHTML = `<header class="admin-header"><div class="admin-container admin-header-inner"><a class="admin-logo" href="dashboard.html">From. 다로리</a><div class="admin-header-actions"><button type="button" class="admin-button admin-button--dark" data-admin-route="visitor">방문자 페이지로</button>${document.body.dataset.adminPage === 'login' ? '' : '<button type="button" class="admin-button admin-button--outline" id="admin-logout" hidden>로그아웃</button>'}</div></div></header>`;
  const sidebar = document.querySelector('[data-admin-sidebar]');
  if (sidebar) {
    // 탭별 메뉴 묶음
    const menuGroups = {
      manage: [['dashboard', '대시보드'], ['notices', '공지사항 작성'], ['subscribers', '구독자 관리'], ['operators', '운영자 관리']],
      newsletter: [['news', '소식 작성'], ['reports', '주민 제보'], ['newsletter', '뉴스레터 제작'], ['schedule', '발행 예약']]
    };
    const page = document.body.dataset.adminPage;
    const isNewsletterTab = menuGroups.newsletter.some(([key]) => key === page);
    const items = isNewsletterTab ? menuGroups.newsletter : menuGroups.manage;
    items.forEach(([key, label]) => {
      const active = page === key;
      const item = document.createElement(active ? 'a' : 'button');
      item.className = `admin-menu${active ? ' admin-menu--active' : ''}`;
      item.textContent = label;
      if (active) { item.href = routes[key]; item.setAttribute('aria-current', 'page'); }
      else { item.type = 'button'; item.dataset.adminRoute = key; }
      sidebar.append(item);
    });
  }
  const footer = document.querySelector('[data-admin-footer]');
  if (footer) footer.innerHTML = `<footer class="admin-footer"><div class="admin-container admin-footer-inner"><a class="admin-logo" href="dashboard.html">From. 다로리</a><div class="admin-footer-info"><span>문의 | example@gmail.com</span><button type="button" class="admin-footer-action" data-admin-route="unsubscribe">구독해지</button></div></div></footer>`;

  // dialog는 브라우저의 기본 초점 관리, Esc 닫기, 배경 접근 차단을 사용합니다.
  const dialog = document.getElementById('admin-dialog');
  function showDialog({ title, description, confirmLabel = '확인', onConfirm }) {
    if (dialog.open) return;
    document.getElementById('admin-dialog-title').textContent = title;
    document.getElementById('admin-dialog-description').textContent = description;
    document.getElementById('admin-dialog-confirm').textContent = confirmLabel;
    const cancel = document.getElementById('admin-dialog-cancel');
    cancel.hidden = !onConfirm;
    dialog.returnValue = '';
    dialog.addEventListener('close', () => {
      if (dialog.returnValue === 'confirm' && onConfirm) onConfirm();
    }, { once: true });
    dialog.showModal();
    (onConfirm ? cancel : document.getElementById('admin-dialog-confirm')).focus();
  }
  function navigate(key) {
    if (routes[key]) { window.location.assign(routes[key]); return; }
    showDialog({ title: `${labels[key] || '페이지'} 연결 준비 중`, description: '아직 연결할 페이지가 준비되지 않았습니다. 페이지가 완성되면 연결될 예정입니다.' });
  }
  document.addEventListener('click', event => {
    const button = event.target.closest('[data-admin-route]');
    if (button) navigate(button.dataset.adminRoute);
  });
  return { routes, navigate, showDialog };
})();
