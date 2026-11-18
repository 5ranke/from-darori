/* 주민 제보 화면에서 쓰는 API 모음 */
window.ReportsApi = (() => {
  const auth = window.DaroriAdminAuth;
  const reportPath = id => `/api/v1/admin/resident-reports/${encodeURIComponent(id)}`;

  const getReports = (page = 0, size = 4) => auth.request(`/api/v1/admin/resident-reports?page=${page}&size=${size}`);
  const getReport = id => auth.request(reportPath(id));
  const deleteReport = id => auth.request(reportPath(id), { method: 'DELETE' });

  return { getReports, getReport, deleteReport };
})();