/* 페이지별 화면 초기화는 ready가 성공한 뒤 시작합니다. */
window.DaroriAdminAuthPage = (() => {
  const isLogin = document.body.dataset.adminPage === 'login';
  const auth = window.DaroriAdminAuth;
  let user = null;
  const moveToLogin = () => window.location.replace('login.html?reason=expired');
  function reveal(account) {
    user = account;
    document.documentElement.classList.remove('admin-auth-pending');
    const name = document.getElementById('admin-name');
    if (name) name.textContent = account.displayName;
    else {
      const greeting = document.querySelector('.admin-greeting');
      if (greeting) greeting.textContent = `안녕하세요, ${account.displayName}님`;
    }
    const logout = document.getElementById('admin-logout');
    if (logout) logout.hidden = false;
  }
  async function check() {
    try {
      const account = await auth.restore();
      if (isLogin) { window.location.replace('newsletter.html'); return null; }
      reveal(account); return account;
    } catch (error) {
      if (isLogin) {
        if (error.kind !== 'session') document.getElementById('admin-login-status').textContent = error.message;
        return null;
      }
      if (error.kind === 'session') { moveToLogin(); return null; }
      const panel = document.createElement('section');
      panel.className = 'admin-container admin-auth-status';
      panel.setAttribute('role', 'alert');
      const message = document.createElement('p'); message.textContent = error.message;
      const retry = document.createElement('button'); retry.type = 'button';
      retry.className = 'admin-button admin-button--outline'; retry.textContent = '다시 시도';
      retry.addEventListener('click', () => window.location.reload());
      panel.append(message, retry); document.body.append(panel);
      return null;
    }
  }
  document.addEventListener('click', async event => {
    const button = event.target.closest('#admin-logout');
    if (!button || button.disabled) return;
    button.disabled = true;
    try { await auth.logout(); window.location.replace('login.html'); }
    catch (error) {
      window.DaroriAdmin.showDialog({ title: '로그아웃 실패', description: `${error.message} 로그아웃이 완료되지 않았습니다. 다시 시도해 주세요.` });
      button.disabled = false;
    }
  });
  window.addEventListener('pagehide', () => {
    auth.clear();
    if (!isLogin) document.documentElement.classList.add('admin-auth-pending');
  });
  window.addEventListener('pageshow', event => { if (event.persisted) window.location.reload(); });
  const ready = check();
  return { ready, get user() { return user; } };
})();
