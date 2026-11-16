/* 구독자 폼·목록·삭제 팝업. 데이터 접근은 예시 저장소로 분리합니다. */
(() => {
  const data = window.DaroriAdminSubscribersData;
  const $ = id => document.getElementById(id);
  const form = $('subscriber-form');
  const dialog = $('subscriber-delete-dialog');
  const pageSize = 4;
  let visibleCount = pageSize;
  let pendingDelete = null;
  let busy = false;
  const routes = window.DaroriAdmin.routes;
  routes.subscribers = 'subscribers.html';
  routes.notices = 'notices.html';
  document.querySelector('[data-admin-sidebar] [aria-current="page"]').href = routes.subscribers;
  function status(message) { $('subscriber-status-message').textContent = message; }
  function showErrors(errors) {
    for (const key of ['name', 'email']) {
      const error = $(`subscriber-${key}-error`);
      error.textContent = errors[key] || ''; error.hidden = !errors[key];
      $(`subscriber-${key}`).setAttribute('aria-invalid', String(Boolean(errors[key])));
    }
  }
  function focusError(errors) { $(`subscriber-${Object.keys(errors)[0]}`).focus(); }
  async function run(action, errorTarget) {
    if (busy) return;
    busy = true;
    const controls = [...document.querySelectorAll('#subscriber-form button, #subscriber-list button, #subscriber-more, #subscriber-delete-dialog button')];
    const disabled = controls.map(node => node.disabled);
    controls.forEach(node => { node.disabled = true; });
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
    $('subscriber-delete-name').textContent = item.name || '이름 미입력';
    $('subscriber-delete-email').textContent = item.email;
    $('subscriber-delete-error').textContent = ''; $('subscriber-delete-error').hidden = true;
    dialog.returnValue = '';
    dialog.showModal(); $('subscriber-delete-cancel').focus();
  }
  async function render() {
    const items = await data.list();
    // list()는 ACTIVE 목록을 반환하므로 인원수와 더 보기도 같은 목록으로 계산합니다.
    $('subscriber-summary').textContent = `현재 구독자 ${items.length}명`;
    const list = $('subscriber-list'); list.replaceChildren();
    if (!items.length) {
      const empty = document.createElement('li'); empty.className = 'subscriber-empty'; empty.textContent = '현재 구독 중인 구독자가 없습니다.'; list.append(empty);
    }
    items.slice(0, visibleCount).forEach(item => {
      const row = document.createElement('li'); row.className = 'subscriber-row';
      const person = document.createElement('div'); person.className = 'subscriber-person';
      const email = document.createElement('p'); email.className = 'subscriber-email'; email.textContent = item.email;
      const meta = document.createElement('div'); meta.className = 'subscriber-person-meta';
      const name = document.createElement('span'); name.className = 'subscriber-name'; name.textContent = item.name || '이름 미입력';
      const badge = document.createElement('span'); badge.className = `subscriber-state${item.status === 'UNSUBSCRIBED' ? ' subscriber-state--unsubscribed' : ''}`;
      badge.textContent = item.status === 'ACTIVE' ? '구독 중' : '구독 해지';
      meta.append(name, badge); person.append(email, meta);
      const remove = document.createElement('button'); remove.type = 'button'; remove.className = 'admin-button admin-button--outline subscriber-remove'; remove.textContent = '삭제';
      remove.setAttribute('aria-label', `${item.email} 삭제 확인`); remove.addEventListener('click', () => openDelete(item));
      row.append(person, remove); list.append(row);
    });
    const remaining = Math.max(0, items.length - visibleCount);
    $('subscriber-more').hidden = remaining === 0;
    $('subscriber-more-label').textContent = `더 보기 (남은 ${remaining}명)`;
  }
  form.addEventListener('submit', event => {
    event.preventDefault(); if (busy) return;
    const input = { name: $('subscriber-name').value, email: $('subscriber-email').value };
    const errors = data.validate(input);
    if (!errors.email && $('subscriber-email').validity.typeMismatch) errors.email = '올바른 이메일 형식을 입력해 주세요.';
    showErrors(errors);
    if (Object.keys(errors).length) { focusError(errors); return; }
    run(async () => {
      await data.create(input); form.reset(); showErrors({}); visibleCount = pageSize; await render();
      status('예시 구독자를 추가했습니다. 실제 서버에는 저장되지 않습니다.'); $('subscriber-list-heading').focus();
    });
  });
  $('subscriber-more').addEventListener('click', () => run(async () => {
    const previousCount = visibleCount; visibleCount += pageSize; await render();
    $('subscriber-list').children[previousCount]?.querySelector('button')?.focus();
  }));
  $('subscriber-delete-confirm').addEventListener('click', () => {
    if (!pendingDelete) return;
    const id = pendingDelete.id;
    run(async () => {
      await data.remove(id); await render(); dialog.close('deleted');
      status('예시 목록에서 항목을 삭제했습니다. 구독 해지 처리는 하지 않았습니다.'); $('subscriber-list-heading').focus();
    }, $('subscriber-delete-error'));
  });
  dialog.addEventListener('cancel', event => { if (busy) event.preventDefault(); });
  dialog.addEventListener('close', () => { pendingDelete = null; });
  run(render);
})();
