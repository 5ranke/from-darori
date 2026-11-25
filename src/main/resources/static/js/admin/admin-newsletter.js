(() => {
  const api = window.NewsletterApi;
  const PAGE_SIZE = 30;

  let stories = [];     // 소식 후보
  let page = 0;
  let hasNext = false;
  let draft = null;
  let generating = false;
  let saving = false;

  const $ = id => document.getElementById(id);
  const form = $('nl-form');
  const titleInput = $('nl-title-input');
  const listEl = $('nl-news-list');
  const listStatus = $('nl-list-status');
  const moreBtn = $('nl-more');
  const generateBtn = $('nl-generate');
  const generateText = $('nl-generate-text');
  const draftEmpty = $('nl-draft-empty');
  const saveBtn = $('nl-save');
  const previewSection = $('nl-email-preview');
  const previewStatus = $('nl-preview-status');
  const previewFrame = $('nl-preview-frame');
  let previewTimer;
  let previewRevision = 0;

  // 에러 처리: 로그인 만료면 로그인 화면으로, 아니면 팝업
  function handleError(error, title = '요청 실패') {
    if (error.kind === 'session') {
      window.location.replace('login.html?reason=expired');
      return;
    }
    DaroriAdmin.showDialog({ title, description: error.message || '요청을 처리하지 못했습니다.' });
  }

  const formatDate = iso => (iso ? iso.slice(0, 10).replaceAll('-', '.') : '');

  // ── ① 소식 후보 (체크박스) ──
  function getSelectedIds() {
    return [...listEl.querySelectorAll('input:checked')].map(cb => cb.value);
  }

  function renderNews() {
    const checked = getSelectedIds(); // 다시 그려도 체크 유지
    listEl.innerHTML = '';
    stories.forEach(story => {
      const li = document.createElement('li');
      li.className = 'nl-news-item';

      const label = document.createElement('label');
      label.className = 'nl-news-label';

      const checkbox = document.createElement('input');
      checkbox.type = 'checkbox';
      checkbox.value = story.storyId;
      checkbox.checked = checked.includes(story.storyId);

      const text = document.createElement('span');
      const title = document.createElement('span');
      title.className = 'nl-news-title';
      title.textContent = story.title;
      const meta = document.createElement('span');
      meta.className = 'nl-news-meta';
      meta.textContent = `${story.categoryName} | ${formatDate(story.createdAt)}`;

      text.append(title, meta);
      label.append(checkbox, text);
      li.append(label);
      listEl.append(li);
    });
    listStatus.hidden = stories.length > 0;
    if (!stories.length) listStatus.textContent = '뉴스레터에 담을 소식이 없어요. 소식 작성에서 먼저 소식을 저장해 주세요.';
    moreBtn.hidden = !hasNext;
  }

  async function loadStories(reset = true) {
    if (reset) page = 0;
    listStatus.hidden = false;
    listStatus.textContent = '소식을 불러오는 중이에요…';
    try {
      const result = await api.getStories(page, PAGE_SIZE);
      stories = reset ? result.stories : [...stories, ...result.stories];
      hasNext = result.hasNext;
      renderNews();
    } catch (error) {
      listStatus.textContent = '소식을 불러오지 못했어요.';
      handleError(error, '소식 불러오기 실패');
    }
  }

  // ── ③ AI 초안 생성 ──
  async function generate(title, storyIds) {
    generating = true;
    generateBtn.disabled = true;
    generateText.textContent = '뉴스레터 만드는 중… (조금 걸릴 수 있어요)';
    try {
      draft = await api.generate({ title, storyIds });
      queuePreview();
    } catch (error) {
      handleError(error, '뉴스레터 생성 실패');
    } finally {
      generating = false;
      generateBtn.disabled = false;
      generateText.textContent = 'GPT로 뉴스레터 만들기';
    }
  }

  generateBtn.addEventListener('click', () => {
    if (generating) return;
    const title = titleInput.value.trim();
    const storyIds = getSelectedIds();
    if (!title) {
      DaroriAdmin.showDialog({ title: '입력 확인', description: '뉴스레터 제목을 먼저 입력해 주세요.' });
      return;
    }
    if (!storyIds.length) {
      DaroriAdmin.showDialog({ title: '소식 선택', description: '편지에 담을 소식을 하나 이상 선택해 주세요.' });
      return;
    }
    if (draft) {
      DaroriAdmin.showDialog({
        title: '초안 다시 만들기',
        description: '지금 미리보기를 새 초안으로 바꿀까요?',
        confirmLabel: '다시 만들기',
        onConfirm: () => generate(title, storyIds)
      });
    } else {
      generate(title, storyIds);
    }
  });

  function queuePreview() {
    const revision = ++previewRevision;
    window.clearTimeout(previewTimer);
    previewSection.hidden = !draft;
    draftEmpty.hidden = Boolean(draft);
    const title = titleInput.value.trim();
    if (!draft || !title) {
      previewFrame.hidden = true;
      previewFrame.removeAttribute('srcdoc');
      previewStatus.textContent = draft ? '뉴스레터 제목을 입력해 주세요.' : '';
      return;
    }
    previewStatus.textContent = '미리보기를 업데이트하는 중입니다.';
    previewTimer = window.setTimeout(async () => {
      try {
        const { html } = await api.preview({ ...draft, title });
        if (revision !== previewRevision) return;
        previewFrame.srcdoc = html;
        previewFrame.hidden = false;
        previewStatus.textContent = '';
      } catch (error) {
        if (revision !== previewRevision) return;
        previewFrame.hidden = true;
        if (error.kind === 'session') handleError(error);
        else previewStatus.textContent = error.message || '미리보기를 불러오지 못했습니다.';
      }
    }, 400);
  }
  titleInput.addEventListener('input', queuePreview);

  // ── ④ 발행 내역에 저장 ──
  form.addEventListener('submit', async e => {
    e.preventDefault(); // 페이지 새로고침 막기
    if (saving) return;

    const title = titleInput.value.trim();
    if (!title) {
      DaroriAdmin.showDialog({ title: '입력 확인', description: '뉴스레터 제목을 입력해 주세요.' });
      return;
    }
    if (!draft) {
      DaroriAdmin.showDialog({ title: '입력 확인', description: '뉴스레터 초안을 먼저 만들어 주세요.' });
      return;
    }
    const emptyStory = draft.stories.some(s => !s.title.trim() || !s.body.trim());
    if (!draft.intro.trim() || !draft.closing.trim() || emptyStory) {
      DaroriAdmin.showDialog({ title: '입력 확인', description: '인사말, 맺음말, 소식 제목·본문은 비워 둘 수 없어요.' });
      return;
    }

    // 서버에 보낼 데이터 (명세 모양 그대로)
    const body = {
      title,
      summary: (draft.summary || '').trim() || null,
      intro: draft.intro.trim(),
      stories: draft.stories.map(s => ({
        storyId: s.storyId,
        displayOrder: s.displayOrder,
        categoryId: s.categoryId,
        title: s.title.trim(),
        body: s.body.trim(),
        categoryName: s.categoryName,
        actionUrl: s.actionUrl,
        applicationDeadline: s.applicationDeadline,
        images: (s.images || []).map(img => ({ objectKey: img.objectKey, displayOrder: img.displayOrder }))
      })),
      closing: draft.closing.trim(),
      quiz: draft.quiz
    };

    saving = true;
    saveBtn.disabled = true;
    try {
      await api.save(body);
      form.reset();
      draft = null;
      queuePreview();
      await loadStories(); // 저장한 소식은 후보에서 빠질 수 있어서 새로 불러오기
      DaroriAdmin.showDialog({ title: '저장 완료', description: '발행 내역에 저장했어요. 발행 예약에서 예약할 수 있어요.' });
    } catch (error) {
      handleError(error, '저장 실패');
    } finally {
      saving = false;
      saveBtn.disabled = false;
    }
  });

  moreBtn.addEventListener('click', () => {
    page += 1;
    loadStories(false);
  });

  // ── 시작: 로그인 확인이 끝난 뒤 ──
  queuePreview();
  window.DaroriAdminAuthPage.ready.then(account => {
    if (account) loadStories();
  });
})();