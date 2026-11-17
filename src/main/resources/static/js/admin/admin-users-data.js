/* 실제 계정 API 어댑터. 공개 필드만 반환하며 비밀번호·토큰을 목록에 보관하지 않습니다. */
window.DaroriAdminUsersData = (() => {
  const roles = Object.freeze({ OPERATOR: '운영자', REPRESENTATIVE: '대표' });
  const base = '/api/v1/admin/users';
  const uuid = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
  function invalid() {
    throw new window.DaroriAdminAuth.AuthError('response', '운영자 API 응답 형식을 확인할 수 없습니다.');
  }
  function person(item) {
    if (!item || !uuid.test(item.id) || typeof item.email !== 'string'
        || typeof item.displayName !== 'string' || !Object.hasOwn(roles, item.role)
        || typeof item.createdAt !== 'string' || !Number.isFinite(Date.parse(item.createdAt))) invalid();
    return { id: item.id, email: item.email, display_name: item.displayName, role: item.role, createdAt: item.createdAt };
  }
  function validate(input) {
    const errors = {};
    if (!input.display_name.trim()) errors.name = '이름을 입력해 주세요.';
    else if (input.display_name.length > 50) errors.name = '이름은 최대 50자까지 입력해 주세요.';
    if (!input.email.trim()) errors.email = '이메일을 입력해 주세요.';
    else if (input.email.length > 254) errors.email = '이메일은 최대 254자까지 입력해 주세요.';
    else if (!/^[^\s@]+@[^\s@]+$/.test(input.email)) errors.email = '올바른 이메일 형식을 입력해 주세요.';
    if (!input.password.trim()) errors.password = '비밀번호를 입력해 주세요.';
    else if (input.password.length < 8 || input.password.length > 72) errors.password = '비밀번호는 8~72자로 입력해 주세요.';
    else if (!/^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[^A-Za-z0-9\s])\S+$/.test(input.password)) errors.password = '비밀번호에 영문·숫자·특수문자를 포함하고 공백을 제외해 주세요.';
    else if (new TextEncoder().encode(input.password).length > 72) errors.password = '비밀번호는 UTF-8 기준 72바이트 이하여야 합니다.';
    if (!Object.hasOwn(roles, input.role)) errors.role = '역할을 선택해 주세요.';
    return errors;
  }
  return {
    roles, validate,
    async list(page = 0, size = 4) {
      if (!Number.isSafeInteger(page) || page < 0 || !Number.isSafeInteger(size) || size < 1) invalid();
      const result = await window.DaroriAdminAuth.request(`${base}?page=${page}&size=${size}`);
      if (!result || !Array.isArray(result.users) || !Number.isSafeInteger(result.totalCount) || result.totalCount < 0
          || result.page !== page || result.size !== size || !Number.isSafeInteger(result.totalPages)
          || result.totalPages < 0 || typeof result.hasNext !== 'boolean' || result.users.length > size) invalid();
      return { totalCount: result.totalCount, users: result.users.map(person), page: result.page,
        size: result.size, totalPages: result.totalPages, hasNext: result.hasNext };
    },
    async create(input) {
      const errors = validate(input);
      if (Object.keys(errors).length) { const error = new Error('입력값을 확인해 주세요.'); error.fields = errors; throw error; }
      // 이메일의 대소문자·공백과 비밀번호를 임의 변환하지 않습니다.
      const result = await window.DaroriAdminAuth.request(base, { method: 'POST', body: {
        displayName: input.display_name, email: input.email, password: input.password, role: input.role
      } });
      return person(result);
    },
    async remove(id) {
      if (!uuid.test(id)) invalid();
      const result = await window.DaroriAdminAuth.request(`${base}/${encodeURIComponent(id)}`, { method: 'DELETE' });
      if (!result || result.adminUserId !== id) invalid();
      return { adminUserId: result.adminUserId };
    }
  };
})();
