/* 서버의 result를 화면 모델로 변환합니다. 인증·재발급은 공통 인증 모듈에 위임합니다. */
window.DaroriAdminDashboardData = (() => {
  const dateFormat = new Intl.DateTimeFormat('ko-KR', {
    year: 'numeric', month: '2-digit', day: '2-digit', timeZone: 'Asia/Seoul'
  });
  const uuid = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
  function invalidResponse() {
    throw new window.DaroriAdminAuth.AuthError('server', '대시보드 응답 형식을 확인할 수 없습니다. 다시 시도해 주세요.');
  }
  function toViewModel(result) {
    if (!result || !result.statistics || !Array.isArray(result.recentStories)) invalidResponse();
    const s = result.statistics;
    for (const key of ['storyCount', 'subscriberCount', 'publishedNewsletterCount', 'pendingResidentReportCount']) {
      if (!Number.isSafeInteger(s[key]) || s[key] < 0) invalidResponse();
    }
    const recentNews = result.recentStories.map(story => {
      if (!story || !uuid.test(story.storyId) || !uuid.test(story.categoryId)
          || typeof story.title !== 'string' || typeof story.categoryName !== 'string'
          || !['MANUAL', 'RESIDENT_REPORT'].includes(story.sourceType)
          || typeof story.createdAt !== 'string' || !Number.isFinite(Date.parse(story.createdAt))) invalidResponse();
      const parts = dateFormat.formatToParts(new Date(story.createdAt));
      const part = type => parts.find(p => p.type === type).value;
      return { id: story.storyId, title: story.title, category: story.categoryName,
        date: `${part('year')}.${part('month')}.${part('day')}` };
    });
    return {
      statistics: [
        { label: '소식', count: s.storyCount, unit: '건', route: 'news' },
        { label: '구독자', count: s.subscriberCount, unit: '명', route: 'subscribers' },
        { label: '편지 발행', count: s.publishedNewsletterCount, unit: '편', route: 'letters' }
      ],
      pendingReportCount: s.pendingResidentReportCount,
      recentNews
    };
  }
  return {
    async getDashboard() {
      const result = await window.DaroriAdminAuth.request('/api/v1/admin/dashboard');
      return toViewModel(result);
    }
  };
})();
