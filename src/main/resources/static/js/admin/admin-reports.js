(() => {
  const api = window.ReportsApi;
  const PAGE_SIZE = 4; // 시안대로 4개씩

  let reports = [];   // 화면에 보이는 제보
  let page = 0;
  let hasNext = false;
  let total = 0;
  let deletingId = null;
  let deleting = false;

  const $ = id => document.getElementById(id);
  const listEl = $('report-list');
  const countEl = $('report-count');
  const listStatus = $('report-list-status');
  const moreBtn = $('report-more');
  const detailDialog = $('report-detail-dialog');
  const detailTitle = $('report-detail-title');
  const detailBody = $('report-detail-body');
  const deleteDialog = $('report-delete-dialog');
  const deleteTarget = $('report-delete-target');
  const deleteConfirm = $('report-delete-confirm');

  // 에러 처리: 로그인 만료면 로그인 화면으로, 아니면 팝업
  function handleError(error, title = '요청 실패') {
    if (error.kind === 'session') {
      window.location.replace('login.html?reason=expired');
      return;
    }
    DaroriAdmin.showDialog({ title, description: error.message || '요청을 처리하지 못했습니다.' });
  }

  // '2026-11-10T10:00:00+09:00' → '2026.11.10'
  const formatDate = iso => (iso ? iso.slice(0, 10).replaceAll('-', '.') : '');

  // ① 제보 목록 그리기
  function render() {
    listEl.innerHTML = '';
    reports.forEach(report => {
      const li = document.createElement('li');
      li.className = 'report-item';
      li.dataset.id = report.reportId;
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
      li.querySelector('.report-item-meta').textContent = `${report.reporterName || '익명'} | ${formatDate(report.createdAt)}`;
      listEl.append(li);
    });
    countEl.textContent = total;

    listStatus.hidden = reports.length > 0;
    if (!reports.length) listStatus.textContent = '확인할 제보가 없어요.';

    // 더 있으면 ∨(더 보기), 다 펼쳤으면 ∧(접기), 한 페이지뿐이면 숨김
    const expanded = !hasNext && page > 0;
    moreBtn.hidden = !hasNext && page === 0;
    moreBtn.classList.toggle('report-more--open', expanded);
    moreBtn.setAttribute('aria-label', expanded ? '제보 접기' : '제보 더 보기');
  }

  // ② 제보 불러오기 (reset = true면 처음부터, false면 이어 붙이기)
  async function load(reset = true) {
    if (reset) page = 0;
    listStatus.hidden = false;
    listStatus.textContent = '제보를 불러오는 중이에요…';
    try {
      const result = await api.getReports(page, PAGE_SIZE);
      reports = reset ? result.reports : [...reports, ...result.reports];
      total = result.totalElements;
      hasNext = result.hasNext;
      render();
    } catch (error) {
      listStatus.textContent = '제보를 불러오지 못했어요.';
      handleError(error, '제보 불러오기 실패');
    }
  }

  // ③ 상세 팝업 (서버에서 본문 불러오기)
  async function openDetail(id) {
    try {
      const report = await api.getReport(id);
      detailTitle.textContent = report.title;
      detailBody.textContent = report.body;
      detailDialog.showModal();
    } catch (error) {
      handleError(error, '제보 불러오기 실패');
    }
  }

  // ④ 삭제 팝업
  function openDelete(report) {
    deletingId = report.reportId;
    deleteTarget.textContent = report.title;
    deleteDialog.showModal();
    $('report-delete-cancel').focus();
  }

  // ── 이벤트 연결 ──

  // 화살표: 더 있으면 다음 페이지, 다 펼쳤으면 접기
  moreBtn.addEventListener('click', () => {
    if (hasNext) {
      page += 1;
      load(false);
    } else {
      load(true);
    }
  });

  // 목록 버튼들
  listEl.addEventListener('click', e => {
    const btn = e.target.closest('[data-action]');
    if (!btn) return;
    const id = btn.closest('.report-item').dataset.id;
    const report = reports.find(r => r.reportId === id);
    const action = btn.dataset.action;

    if (action === 'detail') {
      openDetail(id);
    } else if (action === 'delete') {
      openDelete(report);
    } else if (action === 'register') {
      DaroriAdmin.showDialog({
        title: '소식으로 등록',
        description: '이 제보 내용으로 소식 작성 화면을 열까요?',
        confirmLabel: '등록하러 가기',
        onConfirm: () => {
          // 제보 ID만 넘기고, 소식 작성 화면에서 내용을 불러와요
          window.location.href = `news.html?reportId=${encodeURIComponent(id)}`;
        }
      });
    }
  });

  // 상세 팝업 닫기
  $('report-detail-close').addEventListener('click', () => detailDialog.close());

  // 삭제 팝업: 취소 / 삭제하기
  $('report-delete-cancel').addEventListener('click', () => deleteDialog.close());
  deleteConfirm.addEventListener('click', async () => {
    if (deleting || !deletingId) return;
    deleting = true;
    deleteConfirm.disabled = true;
    try {
      await api.deleteReport(deletingId);
      deleteDialog.close();
      await load(true);
    } catch (error) {
      deleteDialog.close();
      handleError(error, '삭제 실패'); // 이미 소식으로 등록된 제보(409) 등
    } finally {
      deletingId = null;
      deleting = false;
      deleteConfirm.disabled = false;
    }
  });

  // 팝업 바깥(어두운 부분) 누르면 닫기
  [detailDialog, deleteDialog].forEach(dialog => {
    dialog.addEventListener('click', e => {
      if (e.target === dialog) dialog.close();
    });
  });

  // ── 시작: 로그인 확인이 끝난 뒤 ──
  window.DaroriAdminAuthPage.ready.then(account => {
    if (account) load();
  });
})();