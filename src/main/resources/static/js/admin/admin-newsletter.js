(() => {
  const api = window.NewsletterApi;
  const PAGE_SIZE = 30;

  let stories = [];     // 소식 후보
  let page = 0;
  let hasNext = false;
  let draft = null;     // AI가 만든 초안 (칸에서 고치면 여기에 바로 반영)
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
  const draftBox = $('nl-draft');
  const saveBtn = $('nl-save');

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
      // 아직 뉴스레터에 안 들어간 소식만 후보로
      const fresh = result.stories.filter(s => !s.publicationStatus || s.publicationStatus === 'UNPUBLISHED');
      stories = reset ? fresh : [...stories, ...fresh];
      hasNext = result.hasNext;
      renderNews();
    } catch (error) {
      listStatus.textContent = '소식을 불러오지 못했어요.';
      handleError(error, '소식 불러오기 실패');
    }
  }

  // ── ② 초안 편집 칸 ──
  // 입력칸 하나 만들기 (고치면 onInput으로 draft에 바로 반영)
  function makeField(labelText, value, onInput, multiline = false) {
    const wrap = document.createElement('label');
    wrap.className = 'nl-draft-field';
    const label = document.createElement('span');
    label.className = 'nl-draft-label';
    label.textContent = labelText;
    const input = document.createElement(multiline ? 'textarea' : 'input');
    input.className = 'nl-input';
    if (!multiline) input.type = 'text';
    input.value = value || '';
    input.addEventListener('input', () => onInput(input.value));
    wrap.append(label, input);
    return wrap;
  }

  function renderDraft() {
    draftBox.innerHTML = '';
    if (!draft) {
      const empty = document.createElement('p');
      empty.className = 'nl-draft-empty';
      empty.textContent = '뉴스레터를 만들면 여기에 초안이 나타나요.';
      draftBox.append(empty);
      return;
    }

    draftBox.append(
      makeField('소개 (지난 편지 목록에 보이는 한 줄)', draft.summary, v => { draft.summary = v; }),
      makeField('인사말', draft.intro, v => { draft.intro = v; }, true)
    );

    // 소식별
    draft.stories.forEach((story, i) => {
      const section = document.createElement('div');
      section.className = 'nl-draft-story';
      const head = document.createElement('p');
      head.className = 'nl-draft-story-head';
      head.textContent = `소식 ${i + 1}`;
      const badge = document.createElement('span');
      badge.className = 'nl-draft-badge';
      badge.textContent = story.categoryName;
      head.append(badge);
      section.append(
        head,
        makeField('제목', story.title, v => { story.title = v; }),
        makeField('본문', story.body, v => { story.body = v; }, true)
      );
      draftBox.append(section);
    });

    draftBox.append(makeField('맺음말', draft.closing, v => { draft.closing = v; }, true));

    // 퀴즈 (질문·해설만 수정 가능, 보기·정답은 보기만)
    if (draft.quiz) {
      const quiz = draft.quiz;
      const section = document.createElement('div');
      section.className = 'nl-draft-story';
      const head = document.createElement('p');
      head.className = 'nl-draft-story-head';
      head.textContent = '📝 이번 호 퀴즈';
      section.append(head, makeField('질문', quiz.question, v => { quiz.question = v; }));

      const options = document.createElement('ol');
      options.className = 'nl-quiz-options';
      (quiz.options || []).forEach((option, i) => {
        const li = document.createElement('li');
        li.className = 'nl-quiz-option';
        li.textContent = `${i + 1}. ${option}`;
        if (i === quiz.answer) {
          li.classList.add('nl-quiz-option--answer');
          const tag = document.createElement('span');
          tag.className = 'nl-quiz-answer-tag';
          tag.textContent = '✔ 정답';
          li.append(tag);
        }
        options.append(li);
      });
      section.append(options, makeField('해설', quiz.explanation, v => { quiz.explanation = v; }, true));
      draftBox.append(section);
    }
  }

  // ── ③ AI 초안 생성 ──
  async function generate(title, storyIds) {
    generating = true;
    generateBtn.disabled = true;
    generateText.textContent = '뉴스레터 만드는 중… (조금 걸릴 수 있어요)';
    try {
      draft = await api.generate({ title, storyIds });
      renderDraft();
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
        description: '지금 초안을 새 초안으로 바꿀까요? 직접 고친 내용은 사라져요.',
        confirmLabel: '다시 만들기',
        onConfirm: () => generate(title, storyIds)
      });
    } else {
      generate(title, storyIds);
    }
  });

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
      renderDraft();
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
  renderDraft();
  window.DaroriAdminAuthPage.ready.then(account => {
    if (account) loadStories();
  });
})();