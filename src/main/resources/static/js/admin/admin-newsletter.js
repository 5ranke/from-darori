(() => {
  const newsList = window.NEWS_SAMPLE; // 소식 작성 화면과 같은 예시 소식

  const $ = id => document.getElementById(id);
  const form = $('nl-form');
  const titleInput = $('nl-title-input');
  const listEl = $('nl-news-list');
  const generateBtn = $('nl-generate');
  const generateText = $('nl-generate-text');
  const draftInput = $('nl-draft');

  // ① 소식 목록을 체크박스로 그리기
  function renderNews() {
    listEl.innerHTML = '';
    newsList.forEach(news => {
      const li = document.createElement('li');
      li.className = 'nl-news-item';

      const label = document.createElement('label');
      label.className = 'nl-news-label';

      const checkbox = document.createElement('input');
      checkbox.type = 'checkbox';
      checkbox.value = news.id;

      const text = document.createElement('span');
      const title = document.createElement('span');
      title.className = 'nl-news-title';
      title.textContent = news.title;
      const meta = document.createElement('span');
      meta.className = 'nl-news-meta';
      meta.textContent = `${news.category} | ${news.date}`;

      text.append(title, meta);
      label.append(checkbox, text);
      li.append(label);
      listEl.append(li);
    });
  }

  // ② 체크한 소식만 가져오기
  function getSelectedNews() {
    const ids = [...listEl.querySelectorAll('input:checked')].map(cb => Number(cb.value));
    return newsList.filter(news => ids.includes(news.id));
  }

  // ③ 임시 초안 만들기
  //    ※ AI 연결 전 예시예요. API 연결할 때 이 함수를 서버 요청으로 바꾸면 돼요.
  function makeDraft(picked) {
    const lines = ['안녕하세요, 다로리에서 편지를 보내요.', ''];
    picked.forEach((news, i) => {
      lines.push(`${i + 1}. ${news.title}`);
      lines.push(news.content || '');
      lines.push('');
    });
    lines.push('📝 이번 호 퀴즈');
    lines.push(`Q. '${picked[0].title}' 소식은 어떤 카테고리일까요?`);
    lines.push(`정답: ${picked[0].category}`);
    lines.push('');
    lines.push('다음 편지에서 또 만나요. From. 다로리');
    return lines.join('\n');
  }

  // ④ 초안 생성 (만드는 중 표시 → 초안 채우기)
  function generate(picked) {
    generateBtn.disabled = true;
    generateText.textContent = '뉴스레터 만드는 중…';
    setTimeout(() => {
      draftInput.value = makeDraft(picked);
      generateBtn.disabled = false;
      generateText.textContent = 'GPT로 뉴스레터 만들기';
      draftInput.focus();
    }, 1200);
  }

  // ── 이벤트 연결 ──

  // GPT 버튼
  generateBtn.addEventListener('click', () => {
    const picked = getSelectedNews();
    if (!picked.length) {
      DaroriAdmin.showDialog({ title: '소식 선택', description: '편지에 담을 소식을 하나 이상 선택해 주세요.' });
      return;
    }
    // 이미 초안이 있으면 덮어쓸지 확인
    if (draftInput.value.trim()) {
      DaroriAdmin.showDialog({
        title: '초안 다시 만들기',
        description: '지금 초안을 새 초안으로 바꿀까요? 직접 고친 내용은 사라져요.',
        confirmLabel: '다시 만들기',
        onConfirm: () => generate(picked)
      });
    } else {
      generate(picked);
    }
  });

  // 발행 내역에 저장
  form.addEventListener('submit', e => {
    e.preventDefault(); // 페이지 새로고침 막기
    if (!titleInput.value.trim()) {
      DaroriAdmin.showDialog({ title: '입력 확인', description: '뉴스레터 제목을 입력해 주세요.' });
      return;
    }
    if (!draftInput.value.trim()) {
      DaroriAdmin.showDialog({ title: '입력 확인', description: '뉴스레터 초안을 먼저 만들어 주세요.' });
      return;
    }
    DaroriAdmin.showDialog({ title: '저장 완료', description: '발행 내역에 저장했어요. 발행 예약에서 예약할 수 있어요.' });
    form.reset();
  });

  renderNews();
})();