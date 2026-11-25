/* 발행 예약 화면에서 쓰는 API 모음 */
window.ScheduleApi = (() => {
  const auth = window.DaroriAdminAuth;
  const reservationPath = id => `/api/v1/admin/newsletters/${encodeURIComponent(id)}/reservation`;

  const getNewsletters = (status, page = 0, size = 50) => {
    const query = new URLSearchParams({ status, page, size });
    return auth.request(`/api/v1/admin/newsletters?${query}`);
  };

  const getNewsletter = id => auth.request(`/api/v1/admin/newsletters/${encodeURIComponent(id)}`);
  const previewNewsletter = body => auth.request('/api/v1/admin/newsletters/preview', { method: 'POST', body });

  // 발행 주기 조회 / 저장
  const getSchedule = () => auth.request('/api/v1/admin/newsletter-schedule');
  const saveSchedule = body => auth.request('/api/v1/admin/newsletter-schedule', { method: 'POST', body });

  // 뉴스레터 예약 / 예약 취소 (보낼 데이터 없음)
  const reserve = id => auth.request(reservationPath(id), { method: 'PATCH' });
  const withdrawNewsletter = id => auth.request(`/api/v1/admin/newsletters/${encodeURIComponent(id)}`, { method: 'DELETE' });
  const cancelReservation = id => auth.request(reservationPath(id), { method: 'DELETE' });

  return { getNewsletters, getNewsletter, previewNewsletter, getSchedule, saveSchedule, reserve, cancelReservation, withdrawNewsletter };
})();