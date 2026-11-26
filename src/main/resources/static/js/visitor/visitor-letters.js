(() => {
  // ── 설정 ──
  const LETTER_PAGE = 'letter.html';
  const PAGE_SIZE = 6;
  const PREVIEW = new URLSearchParams(window.location.search).get('preview') === 'sample';

  // 카테고리 code (서버가 code를 안 보내줄 때 이름으로 맞추기)
  const CODE_BY_NAME = {
    '다로리의 요즘': 'VILLAGE_NEWS',
    '프로젝트 후속': 'PROJECT_FOLLOW_UP',
    '구인/참여': 'RECRUITMENT',
    '제철 정보': 'SEASONAL'
  };
  const EMOJI = { PROJECT_FOLLOW_UP: '🌱', RECRUITMENT: '🙋', SEASONAL: '🍊' };

  let tabs = [];            // [{ id: 'ALL' 또는 categoryId, label }]
  let currentTab = 'ALL';
  let year = null;
  let page = 0;
  let hasNext = false;
  let items = [];
  let loading = false;
  let listRequestId = 0;

  const $ = id => document.getElementById(id);
  const tabBox = $('vl-tabs');
  const yearSelect = $('vl-year');
  const grid = $('vl-grid');
  const statusEl = $('vl-status');
  const moreBtn = $('vl-more');
  const dialog = $('vl-dialog');

  // ── 작은 도우미 ──
  const letterUrl = id => `${LETTER_PAGE}?${new URLSearchParams({ newsletterId: id })}`;
  const issueText = (y, m) => `${y} ${m}월호`;
  const withEmoji = name => {
    const emoji = EMOJI[CODE_BY_NAME[name]];
    return emoji ? `${emoji} ${name}` : name;
  };
  function el(tag, className, text) {
    const node = document.createElement(tag);
    if (className) node.className = className;
    if (text !== undefined) node.textContent = text;
    return node;
  }

  // 서버에서 데이터 받기 (로그인 필요 없는 API)
  async function getJson(path) {
    if (PREVIEW) return sampleData(path);
    let response;
    try {
      response = await fetch(path, { headers: { Accept: 'application/json' }, credentials: 'same-origin', cache: 'no-store' });
    } catch {
      throw new Error('서버에 연결할 수 없어요. 인터넷 연결을 확인해 주세요.');
    }
    const json = await response.json().catch(() => null);
    if (!response.ok || !json || json.isSuccess !== true) {
      throw new Error((json && json.message) || '편지를 불러오지 못했어요. 잠시 후 다시 시도해 주세요.');
    }
    return json.result;
  }

  // ── ① 탭 ──
  function renderTabs() {
    tabBox.innerHTML = '';
    tabs.forEach(tab => {
      const btn = el('button', 'vl-tab', tab.label);
      btn.type = 'button';
      btn.dataset.id = tab.id;
      btn.classList.toggle('vl-tab--active', tab.id === currentTab);
      btn.setAttribute('aria-pressed', String(tab.id === currentTab));
      tabBox.append(btn);
    });
  }

  async function loadTabs() {
    tabs = [{ id: 'ALL', label: '전체' }];
    try {
      const categories = await getJson('/api/v1/categories');
      categories
        .map(c => ({ ...c, code: c.code || CODE_BY_NAME[c.name] }))
        .filter(c => c.code !== 'VILLAGE_NEWS') // 명세: 다로리의 요즘은 탭에 없음
        .sort((a, b) => a.displayOrder - b.displayOrder)
        .forEach(c => tabs.push({ id: c.categoryId, label: withEmoji(c.name) }));
    } catch {
      // 카테고리를 못 불러와도 '전체' 탭은 보여줌
    }
    renderTabs();
  }

  // ── ② 연도 ──
  async function loadYears() {
    try {
      const { years } = await getJson('/api/v1/newletters/years');
      yearSelect.innerHTML = '';
      years.forEach(y => {
        const option = el('option', null, `${y} 편지`);
        option.value = y;
        yearSelect.append(option);
      });
      year = years.length ? years[0] : null; // 가장 최근 연도
      yearSelect.hidden = !years.length;
    } catch {
      yearSelect.hidden = true;
    }
  }

  // ── ③ 카드 ──
  function makeCard(item) {
    const card = el('article', 'vl-card');

    if (currentTab === 'ALL') {
      // 전체 탭: 발행호 카드 → 편지 상세 페이지로 이동
      card.append(
        el('span', 'vl-badge', `${issueText(item.issueYear, item.issueMonth)}${item.isLatest ? ' | 최신' : ''}`),
        el('h2', 'vl-card-title', item.title),
        el('p', 'vl-card-text', item.summary || '')
      );
      const link = el('a', 'vl-btn', '편지 읽기 ↗');
      link.href = letterUrl(item.newsletterId);
      card.append(link);
    } else {
      // 소식별 탭: 수록 소식 카드 → 자세히 보기 팝업
      card.append(
        el('span', 'vl-badge', withEmoji(item.categoryName)),
        el('p', 'vl-issue', issueText(item.issueYear, item.issueMonth)),
        el('h2', 'vl-card-title', item.title),
        el('p', 'vl-card-text', item.excerpt || '')
      );
      const btn = el('button', 'vl-btn', '자세히 보기 ↗');
      btn.type = 'button';
      btn.dataset.storyId = item.publishedStoryId;
      card.append(btn);
    }
    return card;
  }

  function render() {
    grid.innerHTML = '';
    items.forEach(item => grid.append(makeCard(item)));

    statusEl.hidden = items.length > 0;
    if (!items.length) {
      statusEl.textContent = currentTab === 'ALL' ? '아직 발행된 편지가 없어요.' : '이 분류에 실린 소식이 아직 없어요.';
    }

    // 더 있으면 ∨(더 보기), 다 펼쳤으면 ∧(접기), 한 페이지뿐이면 숨김
    const expanded = !hasNext && page > 0;
    moreBtn.hidden = !hasNext && page === 0;
    moreBtn.classList.toggle('vl-more--open', expanded);
    moreBtn.setAttribute('aria-label', expanded ? '접기' : '더 보기');
  }

  // reset = true면 처음부터, false면 다음 페이지 이어 붙이기
  async function loadList(reset = true) {
    if (loading && !reset) return;
    const requestId = ++listRequestId;
    loading = true;
    moreBtn.disabled = true;
    if (reset) {
      page = 0;
      grid.innerHTML = '';
    }
    statusEl.hidden = false;
    statusEl.textContent = '불러오는 중이에요…';

    const yearQuery = year ? `&year=${year}` : '';
    const path = currentTab === 'ALL'
      ? `/api/v1/newletters?page=${page}&size=${PAGE_SIZE}${yearQuery}`
      : `/api/v1/published-stories?categoryId=${encodeURIComponent(currentTab)}&page=${page}&size=${PAGE_SIZE}${yearQuery}`;

    try {
      const result = await getJson(path);
      if (requestId !== listRequestId) return;
      items = reset ? result.items : [...items, ...result.items];
      hasNext = result.hasNext;
      render();
    } catch (error) {
      if (requestId !== listRequestId) return;
      statusEl.textContent = error.message;
      moreBtn.hidden = true;
    } finally {
      if (requestId === listRequestId) {
        loading = false;
        moreBtn.disabled = false;
      }
    }
  }

  // ── ④ 자세히 보기 팝업 ──
  async function openStory(id) {
    try {
      const story = await getJson(`/api/v1/published-stories/${encodeURIComponent(id)}`);
      const issue = issueText(story.issueYear, story.issueMonth);

      $('vl-d-badge').textContent = withEmoji(story.categoryName);
      $('vl-d-issue').textContent = issue;
      $('vl-d-title').textContent = story.title;
      $('vl-d-body').textContent = story.body;

      // 사진 (가로 스크롤)
      const images = $('vl-d-images');
      images.innerHTML = '';
      (story.images || []).forEach(image => {
        const img = el('img');
        img.src = image.url;
        img.alt = image.altText || '';
        img.loading = 'lazy';
        images.append(img);
      });
      images.hidden = !images.children.length;

      const deadline = $('vl-d-deadline');
      deadline.hidden = !story.applicationDeadline;
      deadline.textContent = deadline.hidden ? '' : `모집 마감일 · ${story.applicationDeadline.replaceAll('-', '.')}`;

      const action = $('vl-d-action');
      action.hidden = !/^https?:\/\//i.test(story.actionUrl ?? '');
      if (action.hidden) action.removeAttribute('href');
      else action.href = story.actionUrl;

      // 해당 월호 보러가기 → 편지 상세 페이지
      const go = $('vl-d-go');
      $('vl-d-go-label').textContent = `${issue} 보러가기`;
      go.href = letterUrl(story.newsletterId);
      dialog.showModal();
      dialog.scrollTop = 0;
    } catch (error) {
      statusEl.hidden = false;
      statusEl.textContent = error.message;
    }
  }

  // ── 이벤트 연결 ──
  tabBox.addEventListener('click', e => {
    const btn = e.target.closest('.vl-tab');
    if (!btn || btn.dataset.id === currentTab) return;
    currentTab = btn.dataset.id;
    renderTabs();
    loadList();
  });

  yearSelect.addEventListener('change', () => {
    year = Number(yearSelect.value);
    loadList();
  });

  moreBtn.addEventListener('click', () => {
    if (hasNext) {
      page += 1;
      loadList(false);
    } else {
      loadList(true); // 접기
      grid.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
  });

  grid.addEventListener('click', e => {
    const btn = e.target.closest('[data-story-id]');
    if (btn) openStory(btn.dataset.storyId);
  });

  $('vl-d-close').addEventListener('click', () => dialog.close());
  dialog.addEventListener('click', e => {
    if (e.target !== dialog) return;
    const bounds = dialog.getBoundingClientRect();
    if (e.clientX < bounds.left || e.clientX > bounds.right || e.clientY < bounds.top || e.clientY > bounds.bottom) {
      dialog.close();
    }
  });

  // ── 시작 ──
  (async () => {
    await Promise.all([loadTabs(), loadYears()]);
    loadList();
  })();

  // ── 개발 확인용 예시 데이터 (?preview=sample 일 때만 사용) ──
  function sampleData(path) {
    if (path.startsWith('/api/v1/categories')) {
      return [
        { categoryId: 'c1', code: 'VILLAGE_NEWS', name: '다로리의 요즘', displayOrder: 1 },
        { categoryId: 'c2', code: 'PROJECT_FOLLOW_UP', name: '프로젝트 후속', displayOrder: 2 },
        { categoryId: 'c3', code: 'RECRUITMENT', name: '구인/참여', displayOrder: 3 },
        { categoryId: 'c4', code: 'SEASONAL', name: '제철 정보', displayOrder: 4 }
      ];
    }
    if (path.startsWith('/api/v1/newletters/years')) return { years: [2026, 2025] };
    if (path.startsWith('/api/v1/newletters')) {
      return {
        hasNext: false,
        items: [11, 10, 9, 8, 7, 6].map((m, i) => ({
          newsletterId: `n${m}`, issueYear: 2026, issueMonth: m,
          title: `2026년 ${m}월, 다로리에서 보내는 편지`,
          summary: '마을의 안부와 새로운 참여 기회, 제철 소식을 한 통에 담았습니다.',
          isLatest: i === 0
        }))
      };
    }
    if (path.startsWith('/api/v1/published-stories/')) {
      return {
        publishedStoryId: 's11', newsletterId: 'n11', categoryName: '프로젝트 후속',
        issueYear: 2026, issueMonth: 11,
        title: '여러분이 남긴 산책 기록, 마을 사진 지도로 이어졌어요',
        body: '방문자들이 남긴 사진과 메모를 주민들과 함께 정리했습니다.\n\n다음에는 계절별 사진을 더 모아 마을회관에서 볼 수 있도록 준비합니다.',
        actionUrl: null, applicationDeadline: null, images: []
      };
    }
    if (path.startsWith('/api/v1/published-stories')) {
      return {
        hasNext: false,
        items: [11, 10, 9, 8, 7, 6].map(m => ({
          publishedStoryId: `s${m}`, categoryName: '프로젝트 후속', issueYear: 2026, issueMonth: m,
          title: `${m}월호에 실린 프로젝트 후속 이야기`,
          excerpt: '지난 마을길 기록 프로젝트에서 방문자들이 남긴 사진과 메모를 주민들과 함께 정리했습니다.'
        }))
      };
    }
    return {};
  }
})();
