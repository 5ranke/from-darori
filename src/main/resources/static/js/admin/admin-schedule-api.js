/* 발행 예약 화면에서 쓰는 API 모음 */
window.ScheduleApi = (() => {
  const auth = window.DaroriAdminAuth;
  const reservationPath = id => `/api/v1/admin/newsletters/${encodeURIComponent(id)}/reservation`;

  // 뉴스레터 목록 (상태 필터 없이 전부 → 화면에서 저장됨/예약됨만 보여줌)
  const getNewsletters = (page = 0, size = 50) => auth.request(`/api/v1/admin/newsletters?page=${page}&size=${size}`);

  // 발행 주기 조회 / 저장
  const getSchedule = () => auth.request('/api/v1/admin/newsletter-schedule');
  const saveSchedule = body => auth.request('/api/v1/admin/newsletter-schedule', { method: 'POST', body });

  // 뉴스레터 예약 / 예약 취소 (보낼 데이터 없음)
  const reserve = id => auth.request(reservationPath(id), { method: 'PATCH' });
  const cancelReservation = id => auth.request(reservationPath(id), { method: 'DELETE' });

  return { getNewsletters, getSchedule, saveSchedule, reserve, cancelReservation };
})();