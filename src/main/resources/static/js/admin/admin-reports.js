(() => {
  const reports = [...window.REPORT_SAMPLE]; // 받은 제보들
  const PREVIEW = 4;                          // 처음에 보여줄 개수
  let expanded = false;                       // 펼쳤는지 여부
  let deletingId = null;                      // 삭제하려는 제보 id

  const $ = id => document.getElementById(id);
  const listEl = $('report-list');
  const countEl = $('report-count');
  const moreBtn = $('report-more');

  // 팝업 요소
  const detailDialog = $('report-detail-dialog');
  const detailTitle = $('report-detail-title');
  const detailBody = $('report-detail-body');
  const deleteDialog = $('report-delete-dialog');
  const deleteTarget = $('report-delete-target');

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
          <button type="button" class="report-item-title" data-action="detail"></button>
          <p class="report-item-meta"></p>
        </div>
        <div class="report-item-actions">
          <button type="button" class="report-btn" data-action="delete">삭제</button>
          <button type="button" class="report-btn report-btn--dark" data-action="register">소식으로 등록</button>
        </div>`;
      li.querySelector('.report-item-title').textContent = report.title;
      li.querySelector('.report-item-meta').textContent = `${report.name} | ${report.date}`;
      listEl.append(li);
    });
    countEl.textContent = reports.length;

    // 4개 이하면 화살표 숨기기, 펼쳤으면 화살표 위로
    moreBtn.hidden = reports.length <= PREVIEW;
    moreBtn.classList.toggle('report-more--open', expanded);
    moreBtn.setAttribute('aria-label', expanded ? '제보 접기' : '제보 더 보기');
  }

  // ② 상세 팝업 열기
  function openDetail(report) {
    detailTitle.textContent = report.title;
    detailBody.textContent = report.content;
    detailDialog.showModal();
  }

  // ③ 삭제 팝업 열기
  function openDelete(report) {
    deletingId = report.id;
    deleteTarget.textContent = report.title;
    deleteDialog.showModal();
    $('report-delete-cancel').focus();
  }

  // ── 이벤트 연결 ──

  // 화살표: 펼치기/접기
  moreBtn.addEventListener('click', () => {
    expanded = !expanded;
    render();
  });

  // 목록 버튼들 (제목 / 삭제 / 소식으로 등록)
  listEl.addEventListener('click', e => {
    const btn = e.target.closest('[data-action]');
    if (!btn) return;
    const id = Number(btn.closest('.report-item').dataset.id);
    const report = reports.find(r => r.id === id);
    const action = btn.dataset.action;

    if (action === 'detail') {
      openDetail(report);
    } else if (action === 'delete') {
      openDelete(report);
    } else if (action === 'register') {
      DaroriAdmin.showDialog({
        title: '소식으로 등록',
        description: '이 제보 내용으로 소식 작성 화면을 열까요?',
        confirmLabel: '등록하러 가기',
        onConfirm: () => {
          // 제목·본문을 주소에 실어서 소식 작성 화면으로 보내기
          const params = new URLSearchParams({ title: report.title, content: report.content });
          window.location.href = `news.html?${params}`;
        }
      });
    }
  });

  // 상세 팝업 닫기
  $('report-detail-close').addEventListener('click', () => detailDialog.close());

  // 삭제 팝업: 취소 / 삭제하기
  $('report-delete-cancel').addEventListener('click', () => deleteDialog.close());
  $('report-delete-confirm').addEventListener('click', () => {
    reports.splice(reports.findIndex(r => r.id === deletingId), 1);
    deletingId = null;
    deleteDialog.close();
    render();
  });

  // 팝업 바깥(어두운 부분) 누르면 닫기
  [detailDialog, deleteDialog].forEach(dialog => {
    dialog.addEventListener('click', e => {
      if (e.target === dialog) dialog.close();
    });
  });

  render();
})();