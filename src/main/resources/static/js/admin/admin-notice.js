/* 공지사항 화면 동작. 데이터 접근은 예시 저장소에 위임합니다. */
(async () => {
  const authenticatedUser = await window.DaroriAdminAuthPage.ready;
  if (!authenticatedUser) return;
  const data = window.DaroriAdminNoticeData;
  const { routes, showDialog } = window.DaroriAdmin;
  // 이 페이지에서만 공지 경로를 연결합니다. 기존 공통/대시보드 파일은 유지합니다.
  routes.notices = 'notices.html';
  document.querySelector('[data-admin-sidebar] [aria-current="page"]').href = routes.notices;
  const $ = id => document.getElementById(id);
  const form = $('notice-form');
  let editingId = null;
  let page = 1;
  let query = '';
  let busy = false;
  let renderVersion = 0;
  const dateFormat = new Intl.DateTimeFormat('ko-KR', { year: 'numeric', month: '2-digit', day: '2-digit', timeZone: 'Asia/Seoul' });
  const formatDate = value => dateFormat.format(new Date(value));
  function status(message) { $('notice-status').textContent = message; }
  function button(label, className, action) {
    const node = document.createElement('button'); node.type = 'button'; node.textContent = label; node.className = className;
    node.addEventListener('click', () => run(action));
    return node;
  }
  // 하나의 변경 요청만 실행하며 실패 시 화면에 안내합니다.
  async function run(action) {
    if (busy) return;
    busy = true;
    const controls = [...document.querySelectorAll('#notice-form button, #notice-list button, #notice-pagination button, #notice-search-form button')];
    const disabled = controls.map(node => node.disabled);
    controls.forEach(node => { node.disabled = true; });
    try { await action(); }
    catch (error) { status(error.message || '처리하지 못했습니다. 다시 시도해 주세요.'); }
    finally { controls.forEach((node, index) => { node.disabled = disabled[index]; }); busy = false; }
  }
  function values() {
    return { title: $('notice-title').value, category: form.querySelector('[name="category"]:checked')?.value || '', body: $('notice-body').value, is_pinned: $('notice-pinned').checked };
  }
  function showErrors(errors) {
    for (const key of ['title', 'category', 'body']) {
      const node = $(`notice-${key}-error`);
      node.textContent = errors[key] || ''; node.hidden = !errors[key];
      const inputs = key === 'category' ? form.querySelectorAll('[name="category"]') : [$(`notice-${key}`)];
      inputs.forEach(input => input.setAttribute('aria-invalid', String(Boolean(errors[key]))));
    }
  }
  function countTitle() { $('notice-title-count').textContent = `${[...$('notice-title').value].length} / 80자`; }
  function resetEditor() {
    editingId = null; form.reset(); showErrors({}); countTitle();
    $('notice-form-heading').textContent = '공지사항 작성'; $('notice-save').textContent = '공지 등록'; $('notice-cancel-edit').hidden = true;
  }
  function badges(item, target) {
    if (item.is_pinned) { const badge = document.createElement('span'); badge.className = 'notice-badge'; badge.textContent = '고정'; target.append(badge); }
    const category = document.createElement('span'); category.className = 'notice-badge'; category.textContent = data.categories[item.category]; target.append(category);
  }
  async function detail(id) {
    const item = await data.get(id);
    $('notice-detail-title').textContent = item.title;
    $('notice-detail-body').textContent = item.body;
    $('notice-detail-date').textContent = `등록 ${formatDate(item.created_at)} · 수정 ${formatDate(item.updated_at)}`;
    $('notice-detail-badges').replaceChildren(); badges(item, $('notice-detail-badges'));
    $('notice-detail').showModal();
  }
  async function edit(id) {
    const item = await data.get(id); editingId = id;
    $('notice-title').value = item.title; $('notice-body').value = item.body; $('notice-pinned').checked = item.is_pinned;
    form.querySelectorAll('[name="category"]').forEach(input => { input.checked = input.value === item.category; });
    showErrors({}); countTitle(); $('notice-form-heading').textContent = '공지사항 수정'; $('notice-save').textContent = '수정 저장'; $('notice-cancel-edit').hidden = false;
    status('선택한 예시 공지를 수정하고 있습니다.');
    $('notice-form-heading').scrollIntoView({ behavior: 'smooth', block: 'center' }); $('notice-title').focus({ preventScroll: true });
  }
  async function render() {
    const version = ++renderVersion;
    const result = await data.list({ query, page, pageSize: 5 });
    if (version !== renderVersion) return;
    page = result.page;
    $('notice-total').textContent = `(${result.total})`;
    $('notice-list-summary').textContent = `${query ? `제목 검색 결과 ${result.matched}건 · ` : ''}고정 공지 우선 · 그룹별 최신 등록순 · ${page} / ${result.pages}페이지`;
    const list = $('notice-list'); list.replaceChildren();
    if (!result.items.length) { const empty = document.createElement('li'); empty.className = 'notice-empty'; empty.textContent = query ? '검색 결과가 없습니다. 다른 제목으로 검색해 주세요.' : '등록된 공지가 없습니다.'; list.append(empty); }
    result.items.forEach(item => {
      const row = document.createElement('li'); row.className = 'notice-row';
      const copy = document.createElement('div'); copy.className = 'notice-copy';
      const tags = document.createElement('div'); tags.className = 'notice-badges'; badges(item, tags);
      const title = button(item.title, 'notice-title', () => detail(item.id));
      const date = document.createElement('p'); date.className = 'notice-meta'; date.textContent = formatDate(item.created_at);
      copy.append(tags, title, date);
      const actions = document.createElement('div'); actions.className = 'notice-row-actions';
      const pin = button(item.is_pinned ? '고정 해제' : '고정', 'admin-button admin-button--outline', async () => { await data.setPinned(item.id, !item.is_pinned); await render(); status('예시 공지의 고정 설정을 변경했습니다.'); });
      const remove = button('삭제', 'admin-button admin-button--outline', () => showDialog({
        title: '공지를 삭제하시겠습니까?', description: `아래 예시 공지가 목록에서 삭제됩니다. 새로고침하면 초기화됩니다.\n\n${item.title}`,
        confirmLabel: '삭제하기', onConfirm: () => run(async () => { await data.remove(item.id); if (editingId === item.id) resetEditor(); await render(); status('예시 공지를 삭제했습니다.'); $('notice-list-heading').focus(); })
      }));
      const change = button('수정', 'admin-button admin-button--primary', () => edit(item.id));
      for (const control of [pin, remove, change]) control.setAttribute('aria-label', `${item.title} ${control.textContent}`);
      actions.append(pin, remove, change); row.append(copy, actions); list.append(row);
    });
    const pagination = $('notice-pagination'); pagination.replaceChildren();
    function pageButton(label, target, disabled = false, current = false) {
      const control = button(label, 'notice-page-button', async () => { page = target; await render(); $('notice-list-heading').focus(); });
      control.disabled = disabled;
      if (current) control.setAttribute('aria-current', 'page');
      pagination.append(control);
    }
    pageButton('이전', page - 1, page === 1);
    for (let number = Math.max(1, page - 2); number <= Math.min(result.pages, page + 2); number++) pageButton(String(number), number, false, number === page);
    pageButton('다음', page + 1, page === result.pages);
  }
  form.addEventListener('submit', event => {
    event.preventDefault();
    if (busy) return;
    const input = values(); const errors = data.validate(input); showErrors(errors);
    if (Object.keys(errors).length) {
      const key = Object.keys(errors)[0];
      (key === 'category' ? form.querySelector('[name="category"]') : $(`notice-${key}`)).focus();
      return;
    }
    run(async () => {
      const wasEditing = Boolean(editingId);
      if (wasEditing) await data.update(editingId, input); else await data.create(input);
      resetEditor(); query = ''; $('notice-search').value = ''; page = 1; await render();
      status(wasEditing ? '예시 공지를 수정했습니다.' : '예시 공지를 등록했습니다.'); $('notice-list-heading').focus();
    });
  });
  $('notice-title').addEventListener('input', countTitle);
  $('notice-cancel-edit').addEventListener('click', () => { if (!busy) { resetEditor(); status('수정을 취소했습니다.'); $('notice-title').focus(); } });
  $('notice-search-form').addEventListener('submit', event => { event.preventDefault(); run(async () => { query = $('notice-search').value.trim(); page = 1; await render(); }); });
  $('notice-search-reset').addEventListener('click', () => run(async () => { query = ''; $('notice-search').value = ''; page = 1; await render(); }));
  resetEditor(); run(render);
})();
