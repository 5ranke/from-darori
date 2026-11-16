/* 화면 전용 예시 데이터입니다. 실제 API 응답 구조를 가정하지 않습니다.
 * 추후 API 명세를 확인한 뒤 getDashboard 내부에서 응답을 이 화면 모델로 변환합니다.
 * 인증/API 요청 및 토큰 저장은 이번 단계에 포함하지 않습니다.
 */
window.DaroriAdminDashboardData = {
  async getDashboard() {
    return {
      operatorName: 'OOO',
      statistics: [
        { label: '소식', count: 6, unit: '건', route: 'news' },
        { label: '구독자', count: 100, unit: '명', route: 'subscribers' },
        { label: '편지 발행', count: 6, unit: '편', route: 'letters' }
      ],
      pendingReportCount: 2,
      recentNews: [
        { id: 'demo-1', title: '2026 행복농촌만들기 콘테스트 금상, 그 이후의 이야기', category: '프로젝트 후속', date: '2026.11.10', content: '행복농촌만들기 콘테스트 이후 다로리 마을에서 이어지고 있는 이야기를 전합니다. 주민들과 함께 다음 프로젝트를 준비하고 있습니다.' },
        { id: 'demo-2', title: '카페 다로리에 놀러 오세요', category: '다로리의 요즘', date: '2026.11.10', content: '마을의 이야기가 모이는 카페 다로리에 놀러 오세요. 따뜻한 차 한 잔과 함께 다로리의 일상을 만나 보세요.' },
        { id: 'demo-3', title: '교육돌봄 프로그램 참여 안내', category: '구인/참여', date: '2026.11.10', content: '다로리 교육돌봄 프로그램에 함께할 주민 여러분을 기다립니다. 자세한 일정은 추후 안내할 예정입니다.' }
      ]
    };
  }
};
