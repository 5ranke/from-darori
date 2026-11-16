/* 메모리 전용 예시 저장소. 네트워크 요청/브라우저 영구 저장을 하지 않습니다.
 * 아래 객체는 화면용 모델이며 서버 DTO가 아닙니다.
 * API 명세 확정 후 이 계층에서 요청/응답을 변환하세요.
 */
window.DaroriAdminNoticeData = (() => {
  const categories = Object.freeze({ GUIDE: '안내', NEWS: '소식', PROGRAM: '프로그램', CLOSED: '마감' });
  const titles = ['2026 행복농촌만들기 콘테스트 금상, 그 이후의 이야기', '다로리 마을 홈페이지가 새롭게 문을 열었습니다', '카페 다로리 이용 안내', '교육돌봄 프로그램 참여 안내', '협업 및 방문 문의 안내', '가을 마을 행사 접수 마감', '마을회관 이용 안내'];
  let notices = titles.map((title, index) => ({
    id: `00000000-0000-4000-8000-${String(index + 1).padStart(12, '0')}`,
    title,
    category: ['NEWS', 'NEWS', 'GUIDE', 'PROGRAM', 'GUIDE', 'CLOSED', 'GUIDE'][index],
    body: ['다로리 마을의 새로운 이야기를 전합니다.\n\n주민 여러분의 관심과 참여에 감사드립니다.', '다로리 마을 홈페이지가 새롭게 문을 열었습니다.\n마을 소식과 안내를 이곳에서 확인하실 수 있습니다.', '카페 다로리를 방문해 주세요.\n이용 일정은 운영팀에 문의해 주세요.', '교육돌봄 프로그램에 함께할 주민 여러분을 기다립니다.', '협업과 마을 방문에 관한 문의는 운영팀에 전달해 주세요.', '가을 마을 행사 신청이 마감되었습니다.\n참여해 주셔서 감사합니다.', '마을회관을 이용하실 때는 서로 배려해 주세요.'][index],
    is_pinned: index < 2,
    created_by: null,
    created_at: `2026-11-${String(10 - index).padStart(2, '0')}T09:00:00+09:00`,
    updated_at: `2026-11-${String(10 - index).padStart(2, '0')}T09:00:00+09:00`
  }));
  function validate(input) {
    const errors = {};
    if (!input.title.trim()) errors.title = '제목을 입력해 주세요.';
    else if ([...input.title].length > 80) errors.title = '제목은 최대 80자까지 입력할 수 있습니다.';
    if (!Object.hasOwn(categories, input.category)) errors.category = '카테고리를 선택해 주세요.';
    if (!input.body.trim()) errors.body = '내용을 입력해 주세요.';
    return errors;
  }
  function payload(input) {
    if (Object.keys(validate(input)).length) throw new Error('필수 항목과 제목 길이를 확인해 주세요.');
    return { title: input.title.trim(), category: input.category, body: input.body, is_pinned: Boolean(input.is_pinned) };
  }
  function find(id) {
    const notice = notices.find(item => item.id === id);
    if (!notice) throw new Error('공지를 찾을 수 없습니다. 목록을 다시 확인해 주세요.');
    return notice;
  }
  return {
    categories, validate,
    async list({ query = '', page = 1, pageSize = 5 } = {}) {
      const filtered = notices.filter(item => item.title.toLocaleLowerCase('ko-KR').includes(query.trim().toLocaleLowerCase('ko-KR')))
        .sort((a, b) => Number(b.is_pinned) - Number(a.is_pinned) || Date.parse(b.created_at) - Date.parse(a.created_at) || a.id.localeCompare(b.id));
      const pages = Math.max(1, Math.ceil(filtered.length / pageSize));
      const currentPage = Math.max(1, Math.min(page, pages));
      return { items: filtered.slice((currentPage - 1) * pageSize, currentPage * pageSize).map(item => ({ ...item })), total: notices.length, matched: filtered.length, page: currentPage, pages };
    },
    async get(id) { return { ...find(id) }; },
    async create(input) {
      const values = payload(input);
      const now = new Date().toISOString();
      const notice = { id: crypto.randomUUID(), ...values, created_by: null, created_at: now, updated_at: now };
      notices.push(notice);
      return { ...notice };
    },
    async update(id, input) { const values = payload(input); const notice = find(id); Object.assign(notice, values, { updated_at: new Date().toISOString() }); return { ...notice }; },
    async setPinned(id, pinned) { const notice = find(id); notice.is_pinned = Boolean(pinned); notice.updated_at = new Date().toISOString(); },
    async remove(id) { find(id); notices = notices.filter(item => item.id !== id); }
  };
})();
