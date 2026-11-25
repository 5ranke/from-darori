(() => {
  const $ = id => document.getElementById(id);
  const form = $('vu-form');
  const emailInput = $('vu-email-input');
  const reasonSelect = $('vu-reason');
  const detailInput = $('vu-detail');
  const submitBtn = $('vu-submit');
  const errorEl = $('vu-error');
  const formSection = $('vu-form-section');
  const doneSection = $('vu-done');
  const doneTitle = $('vu-done-title');

  const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  let sending = false;

  // ① 이메일·사유가 다 있어야 버튼 활성화
  function updateButton() {
    submitBtn.disabled = sending || !emailInput.value.trim() || !reasonSelect.value;
  }
  // '선택해 주세요' 상태면 회색 글씨
  function updateSelectColor() {
    reasonSelect.classList.toggle('vu-select--empty', !reasonSelect.value);
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

  // ③ 서버로 해지 요청 (로그인 필요 없는 API)
  async function sendUnsubscribe(data) {
    let response;
    try {
      response = await fetch('/api/v1/subscriptions/unsubscribe', {
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
      throw new Error((json && json.message) || '구독 해지를 처리하지 못했어요. 잠시 후 다시 시도해 주세요.');
    }
    return json.result;
  }

  // ── 이벤트 연결 ──
  emailInput.addEventListener('input', () => { updateButton(); hideError(); });
  reasonSelect.addEventListener('change', () => { updateButton(); updateSelectColor(); hideError(); });
  detailInput.addEventListener('input', hideError);

  form.addEventListener('submit', async e => {
    e.preventDefault(); // 페이지 새로고침 막기
    if (sending) return;

    const email = emailInput.value.trim();
    if (!EMAIL_PATTERN.test(email)) {
      showError('이메일 형식을 확인해 주세요.');
      emailInput.focus();
      return;
    }
    if (!reasonSelect.value) {
      showError('해지 사유를 선택해 주세요.');
      return;
    }

    // ※ 노션 명세의 필드 이름과 다르면 여기만 고치면 돼요
    const data = {
      email,
      reasonCode: reasonSelect.value,
      detail: detailInput.value.trim() || null
    };

    sending = true;
    submitBtn.textContent = '처리 중…';
    updateButton();
    try {
      await sendUnsubscribe(data);
      showDone();
    } catch (error) {
      showError(error.message);
    } finally {
      sending = false;
      submitBtn.textContent = '해지하기';
      updateButton();
    }
  });

  // 개발 확인용: 주소 뒤에 ?preview=done 을 붙이면 완료 화면을 바로 볼 수 있어요
  if (new URLSearchParams(window.location.search).get('preview') === 'done') showDone();

  updateButton();
  updateSelectColor();
})();