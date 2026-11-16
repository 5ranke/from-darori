/* メモ리 전용 예시 저장소. 서버 DTO/권한 계약이 아닙니다.
 * 비밀번호·해시·토큰은 받거나 저장하지 않습니다. 새로고침 시 초기화됩니다.
 * API 계약이 준비되면 별도 인증 요청 계층에서 연결하고, 목록은 공개 필드만 매핑하세요.
 */
window.DaroriAdminUsersData = (() => {
  const roles = Object.freeze({ OPERATOR: '운영자', REPRESENTATIVE: '대표' });
  let users = [
    { id: '00000000-0000-4000-8000-000000000001', display_name: '서삼열', email: 'ceo@example.com', role: 'REPRESENTATIVE' },
    { id: '00000000-0000-4000-8000-000000000002', display_name: '다로리인 운영팀', email: 'team@example.com', role: 'OPERATOR' }
  ];
  // 중복 이메일의 대소문자 미구분은 미리보기 정책이며 서버 계약은 미확정입니다.
  const emailKey = value => value.trim().toLowerCase();
  function validate(input) {
    const errors = {};
    if (!input.display_name.trim()) errors.name = '이름을 입력해 주세요.';
    else if ([...input.display_name.trim()].length > 50) errors.name = '이름은 최대 50자까지 입력해 주세요.';
    const email = input.email.trim();
    if (!email) errors.email = '이메일을 입력해 주세요.';
    else if ([...email].length > 254) errors.email = '이메일은 최대 254자까지 입력해 주세요.';
    else if (!/^[^\s@]+@[^\s@]+$/.test(email)) errors.email = '올바른 이메일 형식을 입력해 주세요.';
    else if (users.some(item => emailKey(item.email) === emailKey(email))) errors.email = '이미 등록된 이메일입니다. 예시 화면에서는 대소문자를 구분하지 않습니다.';
    if (!Object.hasOwn(roles, input.role)) errors.role = '역할을 선택해 주세요.';
    return errors;
  }
  return {
    roles, validate,
    async list() { return users.map(({ id, display_name, email, role }) => ({ id, display_name, email, role })); },
    async create({ display_name, email, role }) {
      const errors = validate({ display_name, email, role });
      if (Object.keys(errors).length) { const error = new Error(Object.values(errors)[0]); error.fields = errors; throw error; }
      const item = { id: crypto.randomUUID(), display_name: display_name.trim(), email: email.trim(), role };
      users.unshift(item); return { ...item };
    },
    async remove(id) {
      if (!users.some(item => item.id === id)) throw new Error('운영자를 찾을 수 없습니다. 목록을 다시 확인해 주세요.');
      // 예시 동작에만 적용합니다. 실제 자기 계정/대표/마지막 계정 삭제 정책은 미확정입니다.
      users = users.filter(item => item.id !== id);
    }
  };
})();
