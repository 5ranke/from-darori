(() => {
  const api = window.NewsApi;
  const PAGE_SIZE = 10;

  // 카테고리 code별 설정 (이름이 바뀌어도 code는 그대로라 안전)
  const CATEGORY_UI = {
    VILLAGE_NEWS: { emoji: '', placeholder: '다로리의 요즘 소식을 입력하세요.' },
    PROJECT_FOLLOW_UP: { emoji: '🌱 ', placeholder: '어떤 프로젝트였는지, 참여 이후 어떤 변화가 있었는지 적어 주세요.' },
    RECRUITMENT: { emoji: '🙋 ', placeholder: '모집 대상, 역할, 활동 일정, 신청 방법을 적어 주세요.' },
    SEASONAL: { emoji: '🍑 ', placeholder: '무엇이 제철인지, 판매 시기와 구매 방법을 적어 주세요.' }
  };

  let categories = [];   // 서버에서 받은 카테고리
  let selected = null;   // 선택된 카테고리
  let photos = [];       // [{ imageId?, objectKey?, url, name, uploading }]
  let editingId = null;  // 수정 중인 소식 id
  let reportId = null;   // 주민 제보에서 넘어온 경우 제보 id
  let stories = [];      // 화면에 보이는 소식 목록
  let page = 0;
  let hasNext = false;
  let totalCount = 0;
  let saving = false;

  // 화면 요소
  const $ = id => document.getElementById(id);
  const form = $('news-form');
  const formTitle = $('news-form-title');
  const titleInput = $('news-title-input');
  const contentInput = $('news-content-input');
  const categoryBox = $('news-categories');
  const linkField = $('news-link-field');
  const recruitField = $('news-recruit-field');
  const buyField = $('news-buy-field');
  const linkInput = $('news-link-input');
  const deadlineInput = $('news-deadline-input');
  const applyInput = $('news-apply-input');
  const buyInput = $('news-buy-input');
  const photoInput = $('news-photo-input');
  const photoGrid = $('news-photos');
  const addTile = photoGrid.querySelector('.news-photo-add');
  const saveBtn = $('news-save');
  const cancelBtn = $('news-cancel');
  const statusEl = $('news-status');
  const reportStatus = $('news-report-status');
  const listEl = $('news-list');
  const countEl = $('news-count');
  const listStatus = $('news-list-status');
  const moreBtn = $('news-more');

  // 에러 처리: 로그인 만료면 로그인 화면으로, 아니면 팝업
  function handleError(error, title = '요청 실패') {
    if (error.kind === 'session') {
      window.location.replace('login.html?reason=expired');
      return;
    }
    DaroriAdmin.showDialog({ title, description: error.message || '요청을 처리하지 못했습니다.' });
  }

  // '2026-11-10T14:00:00+09:00' → '2026.11.10'
  const formatDate = iso => (iso ? iso.slice(0, 10).replaceAll('-', '.') : '');

  // ── ① 카테고리 ──
  function renderCategories(emptyMessage) {
    categoryBox.innerHTML = '';
    if (!categories.length) {
      const empty = document.createElement('p');
      empty.className = 'news-empty';
      empty.textContent = emptyMessage;
      categoryBox.append(empty);
      return;
    }
    categories.forEach(category => {
      const chip = document.createElement('button');
      chip.type = 'button';
      chip.className = 'news-chip';
      chip.dataset.id = category.categoryId;
      chip.textContent = `${CATEGORY_UI[category.code]?.emoji || ''}${category.name}`;
      categoryBox.append(chip);
    });
  }

  function selectCategory(categoryId) {
    selected = categories.find(c => c.categoryId === categoryId) || null;
    categoryBox.querySelectorAll('.news-chip').forEach(chip => {
      chip.classList.toggle('news-chip--active', chip.dataset.id === categoryId);
    });
    const code = selected?.code;
    contentInput.placeholder = CATEGORY_UI[code]?.placeholder || '내용을 입력하세요.';
    linkField.hidden = code !== 'PROJECT_FOLLOW_UP';
    recruitField.hidden = code !== 'RECRUITMENT';
    buyField.hidden = code !== 'SEASONAL';
  }

  async function loadCategories() {
    let emptyMessage = '등록된 카테고리가 없어요.';
    try {
      categories = await api.getCategories();
    } catch {
      categories = [];
      emptyMessage = '카테고리를 불러오지 못했어요. 새로고침해 주세요.';
    }
    renderCategories(emptyMessage);
    selectCategory(categories[0]?.categoryId);
  }
  // ── ② 저장된 소식 목록 ──
  function renderList() {
    listEl.innerHTML = '';
    stories.forEach(story => {
      const li = document.createElement('li');
      li.className = 'news-item';
      li.dataset.id = story.storyId;
      li.innerHTML = `
        <div>
          <p class="news-item-title"></p>
          <p class="news-item-meta"></p>
        </div>
        <div class="news-item-actions">
          <button type="button" class="news-small-btn" data-action="delete">삭제</button>
          <button type="button" class="news-small-btn news-small-btn--accent" data-action="edit">수정</button>
        </div>`;
      li.querySelector('.news-item-title').textContent = story.title;
      li.querySelector('.news-item-meta').textContent = `${story.categoryName} | ${formatDate(story.createdAt)}`;
      listEl.append(li);
    });
    countEl.textContent = totalCount;
    listStatus.hidden = stories.length > 0;
    if (!stories.length) listStatus.textContent = '아직 저장된 소식이 없어요.';
    moreBtn.hidden = !hasNext;
  }

  // reset = true면 처음부터, false면 다음 페이지 이어 붙이기
  async function loadStories(reset = true) {
    if (reset) page = 0;
    listStatus.hidden = false;
    listStatus.textContent = '소식을 불러오는 중이에요…';
    try {
      const result = await api.getStories(page, PAGE_SIZE);
      stories = reset ? result.stories : [...stories, ...result.stories];
      totalCount = result.totalCount;
      hasNext = result.hasNext;
      renderList();
    } catch (error) {
      listStatus.textContent = '소식을 불러오지 못했어요.';
      handleError(error, '목록 불러오기 실패');
    }
  }

  // ── ③ 사진 ──
  function renderPhotos() {
    photoGrid.querySelectorAll('.news-photo-wrap').forEach(el => el.remove());
    photos.forEach((photo, index) => {
      const wrap = document.createElement('div');
      wrap.className = 'news-photo-wrap';
      wrap.classList.toggle('news-photo-wrap--uploading', photo.uploading);

      const img = document.createElement('img');
      img.className = 'news-photo-item';
      img.src = photo.url;
      img.alt = photo.name;

      const remove = document.createElement('button');
      remove.type = 'button';
      remove.className = 'news-photo-remove';
      remove.textContent = '×';
      remove.dataset.index = index;
      remove.setAttribute('aria-label', `${photo.name} 사진 빼기`);

      wrap.append(img, remove);
      photoGrid.insertBefore(wrap, addTile);
    });
  }

  // 사진 고르면 바로 서버에 업로드 → objectKey 기억
  photoInput.addEventListener('change', () => {
    const files = [...photoInput.files];
    photoInput.value = ''; // 같은 사진 다시 고를 수 있게
    files.forEach(async file => {
      const photo = { url: URL.createObjectURL(file), name: file.name, uploading: true };
      photos.push(photo);
      renderPhotos();
      try {
        const result = await api.uploadImage(file);
        photo.objectKey = result.objectKey;
        photo.uploading = false;
      } catch (error) {
        photos = photos.filter(p => p !== photo); // 실패한 사진은 빼기
        handleError(error, '사진 업로드 실패');
      }
      renderPhotos();
    });
  });

  // × 버튼 → 사진 빼기
  photoGrid.addEventListener('click', e => {
    const btn = e.target.closest('.news-photo-remove');
    if (!btn) return;
    photos.splice(Number(btn.dataset.index), 1);
    renderPhotos();
  });

  // ── ④ 주민 제보 모드 ──
  function leaveReportMode() {
    reportId = null;
    reportStatus.hidden = true;
    window.history.replaceState(null, '', 'news.html'); // 주소에서 reportId 지우기
  }

  async function startReportMode(id) {
    try {
      const report = await api.getReport(id);
      reportId = report.reportId;
      titleInput.value = report.title;
      contentInput.value = report.body;
      saveBtn.textContent = '소식으로 등록';
      cancelBtn.textContent = '등록 취소';
      cancelBtn.hidden = false;
      reportStatus.hidden = false;
    } catch (error) {
      leaveReportMode();
      handleError(error, '제보 불러오기 실패');
    }
  }

  // ── ⑤ 작성 / 수정 ──
  function resetForm() {
    form.reset();
    photos = [];
    renderPhotos();
    editingId = null;
    if (reportId) leaveReportMode();
    if (categories.length) selectCategory(categories[0].categoryId);
    formTitle.textContent = '소식 작성';
    saveBtn.textContent = '소식 저장';
    cancelBtn.textContent = '수정 취소';
    cancelBtn.hidden = true;
    statusEl.hidden = true;
  }

  async function startEdit(storyId) {
    try {
      const story = await api.getStory(storyId);
      if (reportId) leaveReportMode(); // 제보 등록 중이었으면 빠져나오기
      editingId = story.storyId;
      titleInput.value = story.title;
      contentInput.value = story.body || '';
      selectCategory(story.categoryId);

      // 링크는 카테고리에 맞는 칸에 넣기
      linkInput.value = '';
      applyInput.value = '';
      buyInput.value = '';
      const code = selected?.code;
      if (code === 'PROJECT_FOLLOW_UP') linkInput.value = story.actionUrl || '';
      if (code === 'RECRUITMENT') applyInput.value = story.actionUrl || '';
      if (code === 'SEASONAL') buyInput.value = story.actionUrl || '';
      deadlineInput.value = story.applicationDeadline || '';

      // 기존 사진은 imageId로 기억
      photos = (story.images || []).map(img => ({
        imageId: img.imageId, url: img.imageUrl, name: '기존 사진', uploading: false
      }));
      renderPhotos();

      formTitle.textContent = '소식 수정';
      saveBtn.textContent = '수정 저장';
      cancelBtn.textContent = '수정 취소';
      cancelBtn.hidden = false;
      statusEl.hidden = false;
      formTitle.scrollIntoView({ behavior: 'smooth', block: 'start' });
    } catch (error) {
      handleError(error, '소식 불러오기 실패');
    }
  }

  // 지금 카테고리에 맞는 링크 값 (없으면 null)
  function getActionUrl() {
    const code = selected?.code;
    const value = code === 'PROJECT_FOLLOW_UP' ? linkInput.value
      : code === 'RECRUITMENT' ? applyInput.value
      : code === 'SEASONAL' ? buyInput.value
      : '';
    return value.trim() || null;
  }

  // 저장 (새로 작성 / 수정 / 제보로 등록)
  form.addEventListener('submit', async e => {
    e.preventDefault(); // 페이지 새로고침 막기
    if (saving) return;

    const title = titleInput.value.trim();
    const content = contentInput.value.trim();
    if (!selected) {
      DaroriAdmin.showDialog({ title: '입력 확인', description: '카테고리를 선택해 주세요.' });
      return;
    }
    if (!title || !content) {
      DaroriAdmin.showDialog({ title: '입력 확인', description: '제목과 내용을 입력해 주세요.' });
      return;
    }
    if (photos.some(p => p.uploading)) {
      DaroriAdmin.showDialog({ title: '잠시만요', description: '사진을 업로드하고 있어요. 끝난 뒤 다시 저장해 주세요.' });
      return;
    }

    // 서버에 보낼 데이터 (세 경우 모두 같은 모양)
    const body = {
      categoryId: selected.categoryId,
      title,
      body: content,
      actionUrl: getActionUrl(),
      applicationDeadline: selected.code === 'RECRUITMENT' ? (deadlineInput.value || null) : null,
      // 기존 사진은 imageId, 새 사진은 objectKey. 순서는 화면 순서대로
      images: photos.map((p, i) => (p.imageId
        ? { imageId: p.imageId, displayOrder: i + 1 }
        : { objectKey: p.objectKey, displayOrder: i + 1 }))
    };

    const mode = editingId ? 'edit' : reportId ? 'report' : 'create';
    const doneMessage = {
      edit: { title: '수정 완료', description: '소식을 수정했어요.' },
      report: { title: '등록 완료', description: '주민 제보를 소식으로 등록했어요.' },
      create: { title: '저장 완료', description: '소식을 저장했어요.' }
    }[mode];

    saving = true;
    saveBtn.disabled = true;
    try {
      if (mode === 'edit') await api.updateStory(editingId, body);
      else if (mode === 'report') await api.registerReportStory(reportId, body);
      else await api.createStory(body);
      resetForm();
      await loadStories();
      DaroriAdmin.showDialog(doneMessage);
    } catch (error) {
      handleError(error, '저장 실패');
    } finally {
      saving = false;
      saveBtn.disabled = false;
    }
  });

  // ── 이벤트 연결 ──
  categoryBox.addEventListener('click', e => {
    const chip = e.target.closest('.news-chip');
    if (chip) selectCategory(chip.dataset.id);
  });

  cancelBtn.addEventListener('click', resetForm);

  moreBtn.addEventListener('click', () => {
    page += 1;
    loadStories(false);
  });

  // 목록의 삭제·수정 버튼
  listEl.addEventListener('click', e => {
    const btn = e.target.closest('[data-action]');
    if (!btn) return;
    const id = btn.closest('.news-item').dataset.id;

    if (btn.dataset.action === 'edit') {
      startEdit(id);
    } else {
      DaroriAdmin.showDialog({
        title: '소식 삭제',
        description: '이 소식을 삭제할까요?',
        confirmLabel: '삭제하기',
        onConfirm: async () => {
          try {
            await api.deleteStory(id);
            if (editingId === id) resetForm();
            await loadStories();
          } catch (error) {
            handleError(error, '삭제 실패');
          }
        }
      });
    }
  });

  // ── 시작: 로그인 확인이 끝난 뒤 ──
  async function init() {
    await Promise.all([loadCategories(), loadStories()]);

    // 주민 제보에서 넘어왔으면 (news.html?reportId=...) 제보 내용 불러오기
    const fromReport = new URLSearchParams(window.location.search).get('reportId');
    if (fromReport) await startReportMode(fromReport);
  }

  window.DaroriAdminAuthPage.ready.then(account => {
    if (account) init();
  });
})();
