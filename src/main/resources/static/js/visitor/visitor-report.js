(() => {
  const $ = id => document.getElementById(id);
  const form = $('vr-form');
  const titleInput = $('vr-title-input');
  const bodyInput = $('vr-body-input');
  const nameInput = $('vr-name-input');
  const emailInput = $('vr-email-input');
  const submitBtn = $('vr-submit');
  const errorEl = $('vr-error');
  const formSection = $('vr-form-section');
  const doneSection = $('vr-done');
  const doneTitle = $('vr-done-title');

  const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  let sending = false;

  // ① 제목·내용이 다 있어야 버튼 활성화
  function updateButton() {
    submitBtn.disabled = sending || !titleInput.value.trim() || !bodyInput.value.trim();
  }

  function showError(message) {
    errorEl.textContent = message;
    errorEl.hidden = false;
  }
  function hideError() {
    errorEl.hidden = true;
  }

  // ② 완료 화면으로 바꾸기
  function showDone() {
    formSection.hidden = true;
    doneSection.hidden = false;
    window.scrollTo(0, 0);
    doneTitle.focus();
  }

  // ③ 서버로 제보 보내기 (로그인 필요 없는 API)
  async function sendReport(data) {
    let response;
    try {
      response = await fetch('/api/v1/resident-reports', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
        credentials: 'same-origin',
        body: JSON.stringify(data)
      });
    } catch {
      throw new Error('서버에 연결할 수 없어요. 인터넷 연결을 확인해 주세요.');
    }
    const json = await response.json().catch(() => null);
    if (!response.ok || !json || json.isSuccess !== true) {
      throw new Error((json && json.message) || '제보를 보내지 못했어요. 잠시 후 다시 시도해 주세요.');
    }
    return json.result;
  }

  // ── 이벤트 연결 ──
  [titleInput, bodyInput].forEach(input => {
    input.addEventListener('input', () => { updateButton(); hideError(); });
  });
  [nameInput, emailInput].forEach(input => input.addEventListener('input', hideError));

  form.addEventListener('submit', async e => {
    e.preventDefault(); // 페이지 새로고침 막기
    if (sending) return;

    const data = {
      title: titleInput.value.trim(),
      body: bodyInput.value.trim(),
      reporterName: nameInput.value.trim() || null,   // 비우면 null (명세 규칙)
      reporterEmail: emailInput.value.trim() || null
    };

    if (!data.title || !data.body) {
      showError('제목과 내용을 입력해 주세요.');
      return;
    }
    if (data.reporterEmail && !EMAIL_PATTERN.test(data.reporterEmail)) {
      showError('이메일 형식을 확인해 주세요.');
      emailInput.focus();
      return;
    }

    sending = true;
    submitBtn.textContent = '보내는 중…';
    updateButton();
    try {
      await sendReport(data);
      showDone();
    } catch (error) {
      showError(error.message);
    } finally {
      sending = false;
      submitBtn.textContent = '제보하기';
      updateButton();
    }
  });

  // 개발 확인용: 주소 뒤에 ?preview=done 을 붙이면 완료 화면을 바로 볼 수 있어요
  if (new URLSearchParams(window.location.search).get('preview') === 'done') showDone();

  updateButton();
})();