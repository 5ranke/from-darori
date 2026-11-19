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
    if (!response.ok || data?.isSuccess !== true || !Array.isArray(data.result)) {
      throw new Error((data && data.message) || '카테고리를 불러오지 못했습니다.');
    }
    return data.result;
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

  async function uploadImage(file) {
    const form = new FormData();
    form.append('filename', file);
    let result;
    try {
      result = await auth.request('/api/v1/admin/story-images', { method: 'POST', body: form });
    } catch (error) {
      if (error.status === 413) {
        throw new Error('사진 용량이 업로드 제한을 초과했어요. 더 작은 사진을 선택해 주세요.');
      }
      throw error;
    }
    const image = Array.isArray(result) ? result[0] : undefined;
    if (typeof image?.objectKey !== 'string' || !image.objectKey) {
      throw new Error('업로드한 사진의 저장 정보를 확인할 수 없어요.');
    }
    return image;
  }

  return {
    getCategories, getStories, getStory, createStory, updateStory, deleteStory,
    getReport, registerReportStory, uploadImage
  };
})();
