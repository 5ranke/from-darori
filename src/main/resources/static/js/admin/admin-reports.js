(() => {
  const reports = [...window.REPORT_SAMPLE]; // 받은 제보들
  const PREVIEW = 4;                          // 처음에 보여줄 개수
  let expanded = false;                       // 펼쳤는지 여부

  const listEl = document.getElementById('report-list');
  const countEl = document.getElementById('report-count');
  const moreBtn = document.getElementById('report-more');

  // ① 제보 목록 그리기
  function render() {
    listEl.innerHTML = '';
    const visible = expanded ? reports : reports.slice(0, PREVIEW);
    visible.forEach(report => {
      const li = document.createElement('li');
      li.className = 'report-item';
      li.dataset.id = report.id;
      li.innerHTML = `
        <div>
          <p class="report-item-title"></p>
          <p class="report-item-meta"></p>
        </div>
        <div class="report-item-actions">
          <button type="button" class="report-btn report-btn--dark" data-action="register">소식으로 등록</button>
          <button type="button" class="report-btn" data-action="delete">삭제</button>
        </div>`;
      li.querySelector('.report-item-title').textContent = report.content;
      li.querySelector('.report-item-meta').textContent = `${report.name} | ${report.date}`;
      listEl.append(li);
    });
    countEl.textContent = reports.length;

    // 4개 이하면 화살표 숨기기, 펼쳤으면 화살표 위로
    moreBtn.hidden = reports.length <= PREVIEW;
    moreBtn.classList.toggle('report-more--open', expanded);
    moreBtn.setAttribute('aria-label', expanded ? '제보 접기' : '제보 더 보기');
  }

  // ② 화살표 누르면 펼치기/접기
  moreBtn.addEventListener('click', () => {
    expanded = !expanded;
    render();
  });

  // ③ 소식으로 등록 / 삭제
  listEl.addEventListener('click', e => {
    const btn = e.target.closest('[data-action]');
    if (!btn) return;
    const id = Number(btn.closest('.report-item').dataset.id);
    const report = reports.find(r => r.id === id);

    if (btn.dataset.action === 'register') {
      DaroriAdmin.showDialog({
        title: '소식으로 등록',
        description: '이 제보 내용으로 소식 작성 화면을 열까요?',
        confirmLabel: '등록하러 가기',
        onConfirm: () => {
          // 제보 내용을 주소에 실어서 소식 작성 화면으로 보내기
          const params = new URLSearchParams({ content: report.content });
          window.location.href = `news.html?${params}`;
        }
      });
    } else {
      DaroriAdmin.showDialog({
        title: '제보 삭제',
        description: '이 제보를 삭제할까요?',
        confirmLabel: '삭제하기',
        onConfirm: () => {
          reports.splice(reports.findIndex(r => r.id === id), 1);
          render();
        }
      });
    }
  });

  render();
})();