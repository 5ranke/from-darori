(() => {
  // 예시 데이터를 복사해서 사용 (원본은 그대로)
  const newsList = window.NEWS_SAMPLE.map(n => ({ ...n, photos: [...(n.photos || [])] }));
  let selectedCategory = '다로리의 요즘'; // 지금 선택된 카테고리
  let photos = [];                        // 지금 올라간 사진들 [{ url, name }]
  let editingId = null;                   // 수정 중인 소식 id (null = 새로 작성)

  // 카테고리별 내용 안내 문구
  const placeholders = {
    '다로리의 요즘': '다로리의 요즘 소식을 입력하세요.',
    '프로젝트 후속': '어떤 프로젝트였는지, 참여 이후 어떤 변화가 있었는지 적어 주세요.',
    '구인 / 참여': '모집 대상, 역할, 활동 일정, 신청 방법을 적어 주세요.',
    '제철 정보': '무엇이 제철인지, 판매 시기와 구매 방법을 적어 주세요.'
  };

  // 화면 요소 가져오기
  const $ = id => document.getElementById(id);
  const form = $('news-form');
  const formTitle = $('news-form-title');
  const titleInput = $('news-title-input');
  const contentInput = $('news-content-input');
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
  const listEl = $('news-list');
  const countEl = $('news-count');

  // ① 카테고리 선택 (버튼 색 + 안내 문구 + 추가 입력칸)
  function selectCategory(category) {
    selectedCategory = category;
    document.querySelectorAll('.news-chip').forEach(chip => {
      chip.classList.toggle('news-chip--active', chip.dataset.category === category);
    });
    contentInput.placeholder = placeholders[category];
    linkField.hidden = category !== '프로젝트 후속';
    recruitField.hidden = category !== '구인 / 참여';
    buyField.hidden = category !== '제철 정보';
  }

  // ② 저장된 소식 목록 그리기
  function render() {
    listEl.innerHTML = '';
    newsList.forEach(news => {
      const li = document.createElement('li');
      li.className = 'news-item';
      li.dataset.id = news.id;
      li.innerHTML = `
        <div>
          <p class="news-item-title"></p>
          <p class="news-item-meta"></p>
        </div>
        <div class="news-item-actions">
          <button type="button" class="news-small-btn" data-action="delete">삭제</button>
          <button type="button" class="news-small-btn news-small-btn--accent" data-action="edit">수정</button>
        </div>`;
      li.querySelector('.news-item-title').textContent = news.title;
      li.querySelector('.news-item-meta').textContent = `${news.category} | ${news.date}`;
      listEl.append(li);
    });
    countEl.textContent = newsList.length;
  }

  // ③ 사진 미리보기 그리기 (사진마다 × 버튼, 추가 칸은 항상 맨 뒤)
  function renderPhotos() {
    photoGrid.querySelectorAll('.news-photo-wrap').forEach(el => el.remove());
    photos.forEach((photo, index) => {
      const wrap = document.createElement('div');
      wrap.className = 'news-photo-wrap';

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

  // ④ 입력칸을 '새로 작성' 상태로 되돌리기
  function resetForm() {
    form.reset();
    photos = [];
    renderPhotos();
    editingId = null;
    selectCategory('다로리의 요즘');
    formTitle.textContent = '소식 작성';
    saveBtn.textContent = '소식 저장';
    cancelBtn.hidden = true;
    statusEl.hidden = true;
  }

  // ⑤ 수정 시작: 선택한 소식 내용을 입력칸에 채우기
  function startEdit(news) {
    editingId = news.id;
    titleInput.value = news.title;
    contentInput.value = news.content || '';
    selectCategory(news.category);
    linkInput.value = news.link || '';
    deadlineInput.value = news.deadline || '';
    applyInput.value = news.applyLink || '';
    buyInput.value = news.buyLink || '';
    photos = [...(news.photos || [])];
    renderPhotos();

    formTitle.textContent = '소식 수정';
    saveBtn.textContent = '수정 저장';
    cancelBtn.hidden = false;
    statusEl.hidden = false;
    formTitle.scrollIntoView({ behavior: 'smooth', block: 'start' });
  }

  // 오늘 날짜를 2026.11.18 형식으로
  function today() {
    const d = new Date();
    return `${d.getFullYear()}.${String(d.getMonth() + 1).padStart(2, '0')}.${String(d.getDate()).padStart(2, '0')}`;
  }

  // ── 이벤트 연결 ──

  // 카테고리 버튼
  $('news-categories').addEventListener('click', e => {
    const chip = e.target.closest('.news-chip');
    if (chip) selectCategory(chip.dataset.category);
  });

  // 사진 추가 (여러 장 가능)
  photoInput.addEventListener('change', () => {
    [...photoInput.files].forEach(file => {
      photos.push({ url: URL.createObjectURL(file), name: file.name });
    });
    photoInput.value = ''; // 같은 사진을 다시 고를 수 있게
    renderPhotos();
  });

  // 사진 × 버튼 → 빼기
  photoGrid.addEventListener('click', e => {
    const btn = e.target.closest('.news-photo-remove');
    if (!btn) return;
    photos.splice(Number(btn.dataset.index), 1);
    renderPhotos();
  });

  // 저장 (새로 작성 / 수정 둘 다)
  form.addEventListener('submit', e => {
    e.preventDefault(); // 페이지 새로고침 막기
    const title = titleInput.value.trim();
    const content = contentInput.value.trim();
    if (!title || !content) {
      DaroriAdmin.showDialog({ title: '입력 확인', description: '제목과 내용을 입력해 주세요.' });
      return;
    }

    const data = {
      title,
      content,
      category: selectedCategory,
      link: linkInput.value.trim(),
      deadline: deadlineInput.value,
      applyLink: applyInput.value.trim(),
      buyLink: buyInput.value.trim(),
      photos: [...photos]
    };

    if (editingId) {
      // 수정: 기존 소식 내용 바꾸기 (날짜는 그대로)
      const target = newsList.find(n => n.id === editingId);
      Object.assign(target, data);
    } else {
      // 새로 작성: 목록 맨 위에 추가
      newsList.unshift({ id: Date.now(), date: today(), ...data });
    }
    resetForm();
    render();
  });

  // 수정 취소
  cancelBtn.addEventListener('click', resetForm);

  // 목록의 삭제·수정 버튼
  listEl.addEventListener('click', e => {
    const btn = e.target.closest('[data-action]');
    if (!btn) return;
    const id = Number(btn.closest('.news-item').dataset.id);
    const news = newsList.find(n => n.id === id);

    if (btn.dataset.action === 'edit') {
      startEdit(news);
    } else {
      DaroriAdmin.showDialog({
        title: '소식 삭제',
        description: '이 소식을 삭제할까요?',
        confirmLabel: '삭제하기',
        onConfirm: () => {
          newsList.splice(newsList.findIndex(n => n.id === id), 1);
          if (editingId === id) resetForm(); // 수정 중이던 소식이면 입력칸도 비우기
          render();
        }
      });
    }
  });

  // 주민 제보에서 넘어왔으면 제목·내용 자동으로 채우기
  const params = new URLSearchParams(window.location.search);
  if (params.get('title')) {
    titleInput.value = params.get('title');
  }
  if (params.get('content')) {
    contentInput.value = params.get('content');
  }

  render();
})();