/* 운영자 관리 화면. 실제 계정 생성/권한 처리/로그인 기능은 포함하지 않습니다. */
(() => {
  const data = window.DaroriAdminUsersData;
  const $ = id => document.getElementById(id);
  const form = $('user-form');
  const dialog = $('user-delete-dialog');
  const pageSize = 4;
  let visibleCount = pageSize;
  let pendingDelete = null;
  let busy = false;
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
  async function run(action, errorTarget) {
    if (busy) return;
    busy = true;
    const controls = [...document.querySelectorAll('#user-form button, #user-list button, #user-more, #user-delete-dialog button')];
    const disabled = controls.map(node => node.disabled); controls.forEach(node => { node.disabled = true; });
    try { await action(); }
    catch (error) {
      if (error.fields) { showErrors(error.fields); focusError(error.fields); }
      else if (errorTarget) { errorTarget.textContent = error.message || '처리하지 못했습니다. 다시 시도해 주세요.'; errorTarget.hidden = false; }
      else status(error.message || '처리하지 못했습니다. 다시 시도해 주세요.');
    } finally { controls.forEach((node, index) => { node.disabled = disabled[index]; }); busy = false; }
  }
  function openDelete(item) {
    if (busy || dialog.open) return;
    pendingDelete = item;
    $('user-delete-name').textContent = item.display_name;
    $('user-delete-email').textContent = item.email;
    $('user-delete-role').textContent = data.roles[item.role];
    $('user-delete-error').textContent = ''; $('user-delete-error').hidden = true;
    dialog.returnValue = ''; dialog.showModal(); $('user-delete-cancel').focus();
  }
  async function render() {
    const items = await data.list();
    $('user-summary').textContent = `현재 운영자 ${items.length}명`;
    const list = $('user-list'); list.replaceChildren();
    if (!items.length) { const empty = document.createElement('li'); empty.className = 'user-empty'; empty.textContent = '등록된 운영자가 없습니다.'; list.append(empty); }
    items.slice(0, visibleCount).forEach(item => {
      const row = document.createElement('li'); row.className = 'user-row';
      const person = document.createElement('div'); person.className = 'user-person';
      const role = document.createElement('span'); role.className = 'user-role-badge'; role.textContent = data.roles[item.role];
      const email = document.createElement('p'); email.className = 'user-email'; email.textContent = item.email;
      const name = document.createElement('p'); name.className = 'user-name'; name.textContent = item.display_name;
      person.append(role, email, name);
      const remove = document.createElement('button'); remove.type = 'button'; remove.className = 'admin-button admin-button--outline user-remove'; remove.textContent = '삭제';
      remove.setAttribute('aria-label', `${item.email} 삭제 확인`); remove.addEventListener('click', () => openDelete(item));
      row.append(person, remove); list.append(row);
    });
    const remaining = Math.max(0, items.length - visibleCount);
    $('user-more').hidden = remaining === 0; $('user-more-label').textContent = `더 보기 (남은 ${remaining}명)`;
  }
  form.addEventListener('submit', event => {
    event.preventDefault(); if (busy) return;
    // 비밀번호는 화면에서 입력 여부만 확인합니다. 예시 저장소로 전달하지 않습니다.
    const input = { display_name: $('user-name').value, email: $('user-email').value, role: $('user-role').value };
    const errors = data.validate(input);
    if (!errors.email && $('user-email').validity.typeMismatch) errors.email = '올바른 이메일 형식을 입력해 주세요.';
    if (!$('user-password').value.length) errors.password = '비밀번호를 입력해 주세요.';
    showErrors(errors);
    if (Object.keys(errors).length) { focusError(errors); return; }
    $('user-password').value = '';
    run(async () => {
      await data.create(input); form.reset(); showErrors({}); visibleCount = pageSize; await render();
      status('예시 운영자를 추가했습니다. 실제 계정은 생성되지 않으며 비밀번호는 저장·전송하지 않았습니다.'); $('user-list-heading').focus();
    });
  });
  // 뒤로 가기 캐시에 입력값을 남기지 않도록 페이지를 떠날 때도 비웁니다.
  window.addEventListener('pagehide', () => { $('user-password').value = ''; });
  $('user-more').addEventListener('click', () => run(async () => {
    const previousCount = visibleCount; visibleCount += pageSize; await render();
    $('user-list').children[previousCount]?.querySelector('button')?.focus();
  }));
  $('user-delete-confirm').addEventListener('click', () => {
    if (!pendingDelete) return;
    const id = pendingDelete.id;
    run(async () => {
      await data.remove(id); await render(); dialog.close('deleted');
      status('예시 목록에서 운영자를 삭제했습니다. 실제 서버 계정에는 영향이 없습니다.'); $('user-list-heading').focus();
    }, $('user-delete-error'));
  });
  dialog.addEventListener('cancel', event => { if (busy) event.preventDefault(); });
  dialog.addEventListener('close', () => { pendingDelete = null; });
  run(render);
})();
