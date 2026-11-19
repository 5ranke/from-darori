/* 운영자 계정 목록·추가·삭제. 인증 요청은 공통 모듈에 위임합니다. */
(async () => {
  const authenticatedUser = await window.DaroriAdminAuthPage.ready;
  if (!authenticatedUser) return;
  const data = window.DaroriAdminUsersData;
  const $ = id => document.getElementById(id);
  const form = $('user-form');
  const dialog = $('user-delete-dialog');
  const pageSize = 4;
  let items = [];
  let totalCount = null;
  let nextPage = 0;
  let hasNext = false;
  let pendingDelete = null;
  let busy = false;
  let retryAction = null;
  const routes = window.DaroriAdmin.routes;
  routes.operators = 'users.html'; routes.notices = 'notices.html'; routes.subscribers = 'subscribers.html';
  document.querySelector('[data-admin-sidebar] [aria-current="page"]').href = routes.operators;
  function status(message) { $('user-status-message').textContent = message; }
  function showErrors(errors) {
    for (const key of ['name', 'email', 'password', 'role']) {
      const error = $(`user-${key}-error`); error.textContent = errors[key] || ''; error.hidden = !errors[key];
      $(`user-${key}`).setAttribute('aria-invalid', String(Boolean(errors[key])));
    }
  }
  function focusError(errors) { $(`user-${Object.keys(errors)[0]}`).focus(); }
  function handleSession(error) {
    if (error.kind !== 'session') return false;
    status('인증이 만료되었습니다. 다시 로그인해 주세요.');
    window.location.replace('login.html?reason=expired'); return true;
  }
  function errorMessage(error) {
    const messages = { USER4031: '본인 계정은 삭제할 수 없습니다.', USER4041: '이미 삭제되었거나 존재하지 않는 운영자입니다.',
      USER4092: '마지막 운영자 계정은 삭제할 수 없습니다.', USER4093: '연관 데이터 제약으로 삭제할 수 없습니다.',
      USER4001: '이름·이메일·비밀번호·역할의 입력값을 확인해 주세요.', USER4091: '이미 등록된 이메일입니다.' };
    return messages[error.code] || error.message || '요청을 처리하지 못했습니다. 다시 시도해 주세요.';
  }
  function controls(disabled) {
    for (const node of document.querySelectorAll('#user-form input, #user-form select, #user-form button, #user-list button, #user-more, #user-retry, #user-delete-dialog button')) {
      node.disabled = disabled || node.dataset.selfDelete === 'true';
    }
  }
  async function run(action) {
    if (busy) return;
    busy = true; $('user-main').setAttribute('aria-busy', 'true'); controls(true);
    try { await action(); }
    finally { busy = false; $('user-main').setAttribute('aria-busy', 'false'); controls(false); }
  }
  function render() {
    $('user-summary').textContent = totalCount === null ? '현재 운영자 수를 확인하지 못했습니다.' : `현재 운영자 ${totalCount.toLocaleString('ko-KR')}명`;
    const list = $('user-list'); list.replaceChildren();
    if (!items.length) { const empty = document.createElement('li'); empty.className = 'user-empty'; empty.textContent = totalCount === null ? '목록을 조회하지 못했습니다.' : '등록된 운영자가 없습니다.'; list.append(empty); }
    items.forEach(item => {
      const row = document.createElement('li'); row.className = 'user-row';
      const person = document.createElement('div'); person.className = 'user-person';
      const role = document.createElement('span'); role.className = 'user-role-badge'; role.textContent = data.roles[item.role];
      const email = document.createElement('p'); email.className = 'user-email'; email.textContent = item.email;
      const name = document.createElement('p'); name.className = 'user-name'; name.textContent = item.display_name;
      person.append(role, email, name);
      const remove = document.createElement('button'); remove.type = 'button'; remove.className = 'admin-button admin-button--outline user-remove'; remove.textContent = '삭제';
      remove.setAttribute('aria-label', `${item.email} 삭제 확인`);
      if (item.id === authenticatedUser.id) { remove.dataset.selfDelete = 'true'; remove.disabled = true; remove.title = '본인 계정은 삭제할 수 없습니다.'; remove.setAttribute('aria-label', `${item.email}: 본인 계정은 삭제할 수 없습니다.`); }
      remove.addEventListener('click', () => openDelete(item)); row.append(person, remove); list.append(row);
    });
    $('user-more').hidden = !hasNext || retryAction !== null;
    $('user-more-label').textContent = `더 보기 (남은 ${Math.max(0, (totalCount ?? 0) - items.length)}명)`;
  }
  function openDelete(item) {
    if (busy || dialog.open || item.id === authenticatedUser.id) return;
    pendingDelete = item;
    $('user-delete-name').textContent = item.display_name;
    $('user-delete-email').textContent = item.email;
    $('user-delete-role').textContent = data.roles[item.role];
    $('user-delete-error').textContent = ''; $('user-delete-error').hidden = true;
    dialog.returnValue = ''; dialog.showModal(); $('user-delete-cancel').focus();
  }
  async function load(append = false, successMessage = '') {
    const page = append ? nextPage : 0;
    retryAction = null; $('user-retry').hidden = true;
    status(successMessage ? `${successMessage} 목록을 확인하는 중입니다.` : '운영자 목록을 불러오는 중입니다.');
    if (!append) { items = []; totalCount = null; nextPage = 0; hasNext = false; $('user-summary').textContent = '운영자 수 확인 중'; $('user-list').replaceChildren(); $('user-more').hidden = true; }
    try {
      const result = await data.list(page, pageSize);
      const merged = append ? [...items, ...result.users] : result.users;
      items = [...new Map(merged.map(item => [item.id, item])).values()];
      totalCount = result.totalCount; nextPage = result.page + 1; hasNext = result.hasNext;
      render(); status(successMessage || '운영자 목록을 불러왔습니다.'); return true;
    } catch (error) {
      if (handleSession(error)) return false;
      retryAction = () => load(append, successMessage);
      render(); $('user-retry').hidden = false;
      status(`${successMessage ? successMessage + ' 다만 목록 재조회에 실패했습니다.' : '목록 조회에 실패했습니다.'} ${errorMessage(error)}`); return false;
    }
  }
  form.addEventListener('submit', event => {
    event.preventDefault(); if (busy) return;
    const input = { display_name: $('user-name').value, email: $('user-email').value,
      password: $('user-password').value, role: $('user-role').value };
    const errors = data.validate(input);
    if (!errors.email && $('user-email').validity.typeMismatch) errors.email = '올바른 이메일 형식을 입력해 주세요.';
    showErrors(errors);
    if (Object.keys(errors).length) { input.password = ''; focusError(errors); return; }
    $('user-password').value = '';
    return run(async () => {
      try {
        await data.create(input); form.reset(); showErrors({});
        await load(false, '운영자 계정을 추가했습니다.'); $('user-list-heading').focus();
      } catch (error) {
        if (handleSession(error)) return;
        if (error.code === 'USER4091') { showErrors({ email: errorMessage(error) }); $('user-email').focus(); }
        status(['network', 'response'].includes(error.kind) || error.status >= 500
          ? '추가 요청의 처리 결과를 확인하지 못했습니다. 다시 추가하기 전에 목록을 재조회해 주세요.' : errorMessage(error));
        retryAction = () => load(); $('user-retry').hidden = false;
      } finally { input.password = ''; }
    });
  });
  window.addEventListener('pagehide', () => { $('user-password').value = ''; });
  $('user-more').addEventListener('click', () => run(() => load(true)));
  $('user-retry').addEventListener('click', () => run(retryAction || (() => load())));
  $('user-delete-confirm').addEventListener('click', () => {
    if (!pendingDelete || pendingDelete.id === authenticatedUser.id) return;
    const id = pendingDelete.id;
    return run(async () => {
      try {
        await data.remove(id); dialog.close('deleted');
        await load(false, '운영자 계정을 삭제했습니다.'); $('user-list-heading').focus();
      } catch (error) {
        if (handleSession(error)) return;
        const message = ['network', 'response'].includes(error.kind) || error.status >= 500
          ? '삭제 요청의 처리 결과를 확인하지 못했습니다. 팝업을 닫고 목록을 재조회해 주세요.' : errorMessage(error);
        $('user-delete-error').textContent = message; $('user-delete-error').hidden = false;
        retryAction = () => load(); $('user-retry').hidden = false;
      }
    });
  });
  dialog.addEventListener('cancel', event => { if (busy) event.preventDefault(); });
  dialog.addEventListener('close', () => { pendingDelete = null; });
  await run(() => load());
})();
