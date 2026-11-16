/* 화면 입력 검증 전용입니다. 실제 로그인/API/토큰 저장은 하지 않습니다. */
(() => {
  const form = document.getElementById('admin-login-form');
  const email = document.getElementById('admin-login-email');
  const password = document.getElementById('admin-login-password');
  const submit = document.getElementById('admin-login-submit');
  const status = document.getElementById('admin-login-status');
  const fields = [email, password];

  function getError(field) {
    if (field === email) {
      if (!field.value.trim()) return '이메일을 입력해 주세요.';
      // type="email"의 브라우저 형식 검증을 사용합니다. 서버 요청 필드 계약은 아닙니다.
      if (field.validity.typeMismatch) return '올바른 이메일 형식을 입력해 주세요.';
      return '';
    }
    // 비밀번호 앞뒤 공백을 임의로 제거하지 않습니다.
    return field.value.length ? '' : '비밀번호를 입력해 주세요.';
  }
  function validate(field) {
    const error = document.getElementById(`${field.id}-error`);
    const message = getError(field);
    field.setAttribute('aria-invalid', String(Boolean(message)));
    error.textContent = message;
    error.hidden = !message;
    return !message;
  }
  function updateButton() {
    submit.disabled = fields.some(field => Boolean(getError(field)));
  }
  fields.forEach(field => {
    field.addEventListener('blur', () => validate(field));
    field.addEventListener('input', () => {
      if (field.hasAttribute('aria-invalid')) validate(field);
      status.textContent = '';
      updateButton();
    });
    // 브라우저 자동 완성으로 값이 바뀌었을 때도 반영합니다.
    field.addEventListener('change', updateButton);
  });
  form.addEventListener('submit', event => {
    event.preventDefault();
    const results = fields.map(validate);
    if (results.includes(false)) {
      fields[results.indexOf(false)].focus();
      return;
    }
    status.textContent = '입력값을 확인했습니다. 현재는 화면 미리보기 단계로, 실제 로그인과 로그인 유지는 API 연결 후 사용할 수 있습니다.';
    // 추후 명세 확인 후 별도 인증 모듈을 호출할 연결 지점입니다.
    // 비밀번호·이메일·로그인 유지 선택은 현재 저장하거나 전송하지 않습니다.
  });
  window.addEventListener('pageshow', updateButton);
  updateButton();
})();
