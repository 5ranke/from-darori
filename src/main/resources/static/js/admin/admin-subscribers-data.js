/* 메모리 전용 예시 저장소입니다. 실제 API DTO나 삭제 계약이 아닙니다.
 * 새로고침하면 초기화됩니다. 네트워크 요청/브라우저 영구 저장은 하지 않습니다.
 * 계약 확정 후 이 계층에서 서버 응답·오류와 화면 모델을 변환하세요.
 */
window.DaroriAdminSubscribersData = (() => {
  let subscribers = [
    { id: '00000000-0000-4000-8000-000000000001', email: 'taemin.han@example.com', name: '한태민', status: 'ACTIVE' },
    { id: '00000000-0000-4000-8000-000000000002', email: 'seoyeon.yoon@example.com', name: '윤서연', status: 'ACTIVE' },
    { id: '00000000-0000-4000-8000-000000000003', email: 'hyunwoo.kang@example.com', name: '강현우', status: 'ACTIVE' },
    { id: '00000000-0000-4000-8000-000000000004', email: 'yuna.jung@example.com', name: '정유나', status: 'ACTIVE' },
    { id: '00000000-0000-4000-8000-000000000005', email: 'previous.reader@example.com', name: null, status: 'UNSUBSCRIBED' }
  ];
  // 대소문자 미구분 중복 확인은 미리보기 정책입니다. 서버의 정규화 규칙은 추후 확인합니다.
  const emailKey = email => email.trim().toLowerCase();
  function validate(input) {
    const errors = {};
    const email = input.email.trim();
    if ([...input.name.trim()].length > 100) errors.name = '이름은 최대 100자까지 입력해 주세요.';
    if (!email) errors.email = '이메일을 입력해 주세요.';
    else if ([...email].length > 254) errors.email = '이메일은 최대 254자까지 입력해 주세요.';
    else if (!/^[^\s@]+@[^\s@]+$/.test(email)) errors.email = '올바른 이메일 형식을 입력해 주세요.';
    else if (subscribers.some(item => emailKey(item.email) === emailKey(email))) errors.email = '이미 등록된 이메일입니다. 예시 화면에서는 대소문자를 구분하지 않습니다.';
    return errors;
  }
  return {
    validate,
    // 화면에는 현재 구독자만 반환합니다. 숨겨진 해지 항목은 원본에 보존합니다.
    // 실제 API 연결 시 이 경계에서 백엔드 응답 계약에 맞게 목록을 변환하세요.
    async list() { return subscribers.filter(item => item.status === 'ACTIVE').map(item => ({ ...item })); },
    async create(input) {
      const errors = validate(input);
      if (Object.keys(errors).length) {
        const error = new Error(Object.values(errors)[0]); error.fields = errors; throw error;
      }
      // 새 예시 항목만 ACTIVE로 생성합니다. 구독 해지/재구독 처리는 구현하지 않습니다.
      const item = { id: crypto.randomUUID(), name: input.name.trim() || null, email: input.email.trim(), status: 'ACTIVE' };
      subscribers.unshift(item);
      return { ...item };
    },
    async remove(id) {
      if (!subscribers.some(item => item.id === id)) throw new Error('항목을 찾을 수 없습니다. 목록을 다시 확인해 주세요.');
      // 예시 목록에서만 제거합니다. 상태 변경(구독 해지)을 수행하지 않습니다.
      subscribers = subscribers.filter(item => item.id !== id);
    }
  };
})();
