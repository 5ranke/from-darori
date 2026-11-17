(() => {
  const newsList = [...window.NEWS_SAMPLE]; // 화면에 보여줄 소식들
  let selectedCategory = '다로리의 요즘';    // 지금 선택된 카테고리

  // 카테고리별 내용 안내 문구
  const placeholders = {
    '다로리의 요즘': '다로리의 요즘 소식을 작성해 주세요.',
    '프로젝트 후속': '어떤 프로젝트였는지, 참여 이후 어떤 변화가 있었는지 적어 주세요.',
    '구인 / 참여': '모집 대상, 역할, 활동 일정, 신청 방법을 적어 주세요.',
    '제철 정보': '무엇이 제철인지, 판매 시기와 구매 방법을 적어 주세요.'
  };

  // 카테고리별로 나타났다 사라지는 입력칸
  const linkField = document.getElementById('news-link-field');
  const recruitField = document.getElementById('news-recruit-field');
  const buyField = document.getElementById('news-buy-field');

  const listEl = document.getElementById('news-list');
  const countEl = document.getElementById('news-count');
  const form = document.getElementById('news-form');
  const titleInput = document.getElementById('news-title-input');
  const contentInput = document.getElementById('news-content-input');
  const photoInput = document.getElementById('news-photo-input');
  const photoText = document.getElementById('news-photo-text');

  // ① 저장된 소식 목록을 화면에 그리기
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
          <button type="button" class="news-small-btn news-small-btn--dark" data-action="edit">수정</button>
          <button type="button" class="news-small-btn" data-action="delete">삭제</button>
        </div>`;
      li.querySelector('.news-item-title').textContent = news.title;
      li.querySelector('.news-item-meta').textContent = `${news.category} | ${news.date}`;
      listEl.append(li);
    });
    countEl.textContent = newsList.length;
  }

  // ② 카테고리 버튼 누르면 그것만 검정으로 + 입력칸 바꾸기
  document.getElementById('news-categories').addEventListener('click', e => {
    const chip = e.target.closest('.news-chip');
    if (!chip) return;
    document.querySelectorAll('.news-chip').forEach(c => c.classList.remove('news-chip--active'));
    chip.classList.add('news-chip--active');
    selectedCategory = chip.dataset.category;
    contentInput.placeholder = placeholders[selectedCategory];
    linkField.hidden = selectedCategory !== '프로젝트 후속';
    recruitField.hidden = selectedCategory !== '구인 / 참여';
    buyField.hidden = selectedCategory !== '제철 정보';
  });

  // ③ 사진 고르면 파일 이름 보여주기
  photoInput.addEventListener('change', () => {
    const file = photoInput.files[0];
    photoText.textContent = file ? file.name : '추가할 사진을 선택해주세요';
  });

  // ④ 소식 저장 → 목록 맨 위에 추가
  form.addEventListener('submit', e => {
    e.preventDefault(); // 페이지 새로고침 막기
    const title = titleInput.value.trim();
    const content = contentInput.value.trim();
    if (!title || !content) {
      DaroriAdmin.showDialog({ title: '입력 확인', description: '제목과 내용을 입력해 주세요.' });
      return;
    }
    const d = new Date();
    const date = `${d.getFullYear()}.${String(d.getMonth() + 1).padStart(2, '0')}.${String(d.getDate()).padStart(2, '0')}`;
    newsList.unshift({ id: Date.now(), title, category: selectedCategory, date });
    form.reset();
    photoText.textContent = '추가할 사진을 선택해주세요';
    render();
  });

  // ⑤ 수정·삭제 버튼
  listEl.addEventListener('click', e => {
    const btn = e.target.closest('[data-action]');
    if (!btn) return;
    const id = Number(btn.closest('.news-item').dataset.id);
    if (btn.dataset.action === 'delete') {
      DaroriAdmin.showDialog({
        title: '소식 삭제',
        description: '이 소식을 삭제할까요?',
        confirmLabel: '삭제하기',
        onConfirm: () => {
          newsList.splice(newsList.findIndex(n => n.id === id), 1);
          render();
        }
      });
    } else {
      DaroriAdmin.showDialog({ title: '수정 준비 중', description: '수정 기능은 다음 단계에서 연결할 예정입니다.' });
    }
  });

  render();
})();