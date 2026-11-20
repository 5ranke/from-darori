/* 로그인 입력 검증과 인증 모듈 연결. 비밀번호는 저장하지 않습니다. */
(() => {
  const form = document.getElementById('admin-login-form');
  const email = document.getElementById('admin-login-email');
  const password = document.getElementById('admin-login-password');
  const submit = document.getElementById('admin-login-submit');
  const status = document.getElementById('admin-login-status');
  const fields = [email, password];
  let busy = true;
  window.DaroriAdminAuthPage.ready.then(() => { busy = false; updateButton(); });

  function getError(field) {
    if (field === email) {
      if (!field.value.trim()) return '이메일을 입력해 주세요.';
      if (field.value.length > 254) return '이메일은 최대 254자까지 입력해 주세요.';
      if (field.validity.typeMismatch) return '올바른 이메일 형식을 입력해 주세요.';
      return '';
    }
    // 비밀번호 앞뒤 공백을 임의로 제거하지 않습니다.
    return field.value.trim().length ? '' : '비밀번호를 입력해 주세요.';
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
    submit.disabled = busy || fields.some(field => Boolean(getError(field)));
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
  form.addEventListener('submit', async event => {
    event.preventDefault();
    if (busy) return;
    const results = fields.map(validate);
    if (results.includes(false)) {
      fields[results.indexOf(false)].focus();
      return;
    }
    busy = true; updateButton();
    form.setAttribute('aria-busy', 'true');
    status.textContent = '로그인 중입니다.';
    try {
      await window.DaroriAdminAuth.login({ email: email.value, password: password.value,
        rememberMe: document.getElementById('admin-login-remember').checked });
      password.value = '';
      window.location.replace('newsletter.html');
    } catch (error) {
      status.textContent = error.message;
      password.value = '';
    } finally {
      busy = false; form.removeAttribute('aria-busy'); updateButton();
    }
  });
  window.addEventListener('pageshow', updateButton);
  updateButton();
})();
