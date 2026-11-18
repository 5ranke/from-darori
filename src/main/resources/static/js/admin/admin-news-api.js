/* 소식 작성 화면에서 쓰는 API 모음 */
window.NewsApi = (() => {
  const auth = window.DaroriAdminAuth;
  const storyPath = id => `/api/v1/admin/stories/${encodeURIComponent(id)}`;
  const reportPath = id => `/api/v1/admin/resident-reports/${encodeURIComponent(id)}`;

  // 카테고리 (로그인 필요 없는 API라 일반 fetch 사용)
  async function getCategories() {
    const response = await fetch('/api/v1/categories', {
      headers: { Accept: 'application/json' },
      credentials: 'same-origin',
      cache: 'no-store'
    });
    const data = await response.json().catch(() => null);
    if (!response.ok || !data || data.isSuccess !== true) {
      throw new Error((data && data.message) || '카테고리를 불러오지 못했습니다.');
    }
    return [...data.result].sort((a, b) => a.displayOrder - b.displayOrder);
  }

  // 소식
  const getStories = (page = 0, size = 10) => auth.request(`/api/v1/admin/stories?page=${page}&size=${size}`);
  const getStory = id => auth.request(storyPath(id));
  const createStory = body => auth.request('/api/v1/admin/stories', { method: 'POST', body });
  const updateStory = (id, body) => auth.request(storyPath(id), { method: 'PATCH', body });
  const deleteStory = id => auth.request(storyPath(id), { method: 'DELETE' });

  // 주민 제보 → 소식
  const getReport = id => auth.request(reportPath(id));
  const registerReportStory = (id, body) => auth.request(`${reportPath(id)}/story`, { method: 'POST', body });

  // 사진 업로드 (key 이름: file)
  function uploadImage(file) {
    const form = new FormData();
    form.append('file', file);
    return auth.request('/api/v1/admin/story-images', { method: 'POST', body: form });
  }

  return {
    getCategories, getStories, getStory, createStory, updateStory, deleteStory,
    getReport, registerReportStory, uploadImage
  };
})();