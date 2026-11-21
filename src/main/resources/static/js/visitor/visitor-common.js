/* 사용법은 visitor/dev/common-preview.html 참고. defer로 불러옵니다.
 * 방문자 페이지 경로는 아직 미확정이므로 null입니다. 팀 합의 후 routes로 전달하세요.
 * 예: DaroriVisitor.render({ page: 'about', routes: { about: '../about.html' } });
 * 경로는 해당 HTML 기준입니다. 없는 페이지를 생성하거나 API를 호출하지 않습니다.
 */
(() => {
  if (!document.body.classList.contains('visitor-page')) return;
  const script = document.currentScript;
  const staticRoot = new URL('../../', script.src);
  const routes = {
    home: null, about: null, letters: null, notices: null,
    subscribe: null, report: null, unsubscribe: null,
    admin: new URL('admin/login.html', staticRoot).href
  };
  // Figma 푸터의 임시 값입니다. 운영용 이메일 확정 전까지 메일 발송 링크로 연결하지 않습니다.
  const placeholderEmail = 'example@gmail.com';
  let currentPage = document.body.dataset.visitorPage || 'home';

  function element(tag, className, text) {
    const node = document.createElement(tag);
    if (className) node.className = className;
    if (text) node.textContent = text;
    return node;
  }
  function item(key, label, className, active = false) {
    const node = element(routes[key] ? 'a' : 'button', className, label);
    if (routes[key]) node.href = routes[key];
    else {
      node.type = 'button';
      node.setAttribute('aria-disabled', 'true');
      node.title = '페이지 연결 준비 중';
      node.addEventListener('click', () => {
        const status = document.querySelector('[data-visitor-status]');
        if (status) status.textContent = `${label}: 연결할 페이지 경로가 아직 정해지지 않았습니다.`;
      });
    }
    if (active) node.setAttribute('aria-current', 'page');
    return node;
  }
  function render(options = {}) {
    if (options.routes) Object.assign(routes, options.routes);
    if (options.page) currentPage = options.page;
    const headerHost = document.querySelector('[data-visitor-header]');
    if (headerHost) {
      const header = element('header', 'visitor-header');
      const inner = element('div', 'visitor-container visitor-header-inner');
      inner.append(item('home', 'From. 다로리', 'visitor-logo'));
      const nav = element('nav', 'visitor-navigation');
      nav.setAttribute('aria-label', '방문자 주 메뉴');
      for (const [key, label] of [['about', '마을 소개'], ['letters', '지난 편지'], ['notices', '공지사항'], ['subscribe', '구독하기']]) {
        nav.append(item(key, label, 'visitor-menu', currentPage === key));
      }
      const actions = element('div', 'visitor-header-actions');
      actions.append(item('report', '제보하기', 'visitor-action'), item('admin', '운영자', 'visitor-action visitor-action--admin'));
      nav.append(actions); inner.append(nav);
      const status = element('p', 'visitor-container visitor-status');
      status.setAttribute('data-visitor-status', ''); status.setAttribute('role', 'status');
      header.append(inner, status); headerHost.replaceChildren(header);
    }
    const footerHost = document.querySelector('[data-visitor-footer]');
    if (footerHost) {
      const footer = element('footer', 'visitor-footer');
      const inner = element('div', 'visitor-container visitor-footer-inner');
      const info = element('div', 'visitor-footer-info');
      const contact = element('span', 'visitor-contact', `문의 | ${placeholderEmail}`);
      contact.dataset.visitorPlaceholder = 'true';
      info.append(contact, item('unsubscribe', '구독해지', 'visitor-footer-action'));
      inner.append(item('home', 'From. 다로리', 'visitor-logo'), info);
      footer.append(inner); footerHost.replaceChildren(footer);
    }
  }
  window.DaroriVisitor = { render };
  render(window.DaroriVisitorConfig || {});
})();
