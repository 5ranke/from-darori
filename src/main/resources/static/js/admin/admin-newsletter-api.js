/* 뉴스레터 제작 화면에서 쓰는 API 모음 */
window.NewsletterApi = (() => {
  const auth = window.DaroriAdminAuth;

  // 소식 후보 목록
  const getStories = (page = 0, size = 30) => auth.request(`/api/v1/admin/stories?page=${page}&size=${size}&publicationStatus=AVAILABLE`);

  // AI 초안·퀴즈 생성 (오래 걸릴 수 있어서 60초까지 기다림)
  const generate = body => auth.request('/api/v1/admin/newsletters/generate', { method: 'POST', body, timeoutMs: 135000 });

  // 발행 내역에 저장
  const preview = body => auth.request('/api/v1/admin/newsletters/preview', { method: 'POST', body });
  const save = body => auth.request('/api/v1/admin/newsletters', { method: 'POST', body });

  return { getStories, generate, preview, save };
})();