/* 인증 정보는 이 클로저의 메모리에만 둡니다. Refresh Token은 HttpOnly 쿠키입니다. */
window.DaroriAdminAuth = (() => {
  const base = '/api/v1/admin/auth';
  let accessToken = null;
  let refreshPromise = null;
  let revision = 0;
  class AuthError extends Error {
    constructor(kind, message, status = 0, code = '') {
      super(message); this.kind = kind; this.status = status; this.code = code;
    }
  }
  function clear() { accessToken = null; revision += 1; }
  async function send(path, { method = 'GET', body, token } = {}) {
    const controller = new AbortController();
    const timeout = setTimeout(() => controller.abort(), 15000);
    try {
      const headers = { Accept: 'application/json' };
      if (body !== undefined) headers['Content-Type'] = 'application/json';
      if (token) headers.Authorization = `Bearer ${token}`;
      let response;
      try {
        response = await fetch(path, { method, headers, credentials: 'same-origin', cache: 'no-store',
          signal: controller.signal, redirect: 'error',
          ...(body === undefined ? {} : { body: JSON.stringify(body) }) });
      } catch (error) {
        throw new AuthError('network', error.name === 'AbortError'
          ? '서버 응답이 지연되고 있습니다. 다시 시도해 주세요.'
          : '서버에 연결할 수 없습니다. 연결 상태를 확인해 주세요.');
      }
      let data;
      try { data = await response.json(); }
      catch { throw new AuthError('server', '서버 응답 형식을 확인할 수 없습니다.', response.status); }
      if (!data || typeof data !== 'object' || typeof data.isSuccess !== 'boolean') {
        throw new AuthError('server', '서버 응답 형식을 확인할 수 없습니다.', response.status);
      }
      if (!response.ok || data.isSuccess !== true) {
        const code = data.code || '';
        const kind = code === 'AUTH4011' ? 'credentials' : response.status === 401 ? 'session'
          : response.status === 403 ? 'forbidden' : 'server';
        throw new AuthError(kind, kind === 'session' ? '로그인 상태가 없거나 만료되었습니다. 다시 로그인해 주세요.'
          : data.message || '요청을 처리하지 못했습니다.', response.status, code);
      }
      return data.result;
    } finally { clearTimeout(timeout); }
  }
  function accept(result) {
    if (!result || result.tokenType !== 'Bearer' || typeof result.accessToken !== 'string' || !result.accessToken) {
      throw new AuthError('server', '인증 응답 형식을 확인할 수 없습니다.');
    }
    accessToken = result.accessToken;
    return result;
  }
  function refresh() {
    if (!refreshPromise) {
      const started = revision;
      refreshPromise = send(`${base}/refresh`, { method: 'POST' }).then(result => {
        if (revision !== started) throw new AuthError('session', '인증 상태가 변경되었습니다. 다시 로그인해 주세요.');
        accept(result);
      }).catch(error => { if (error.kind === 'session') clear(); throw error; })
        .finally(() => { refreshPromise = null; });
    }
    return refreshPromise;
  }
  async function request(path, options = {}) {
    // Authorization 헤더가 다른 출처로 전달되지 않도록 내부 API로 한정합니다.
    if (!path.startsWith('/api/v1/admin/') || path.includes('..') || path.includes('\\')) {
      throw new AuthError('server', '허용되지 않은 API 경로입니다.');
    }
    if (!accessToken) await refresh();
    const token = accessToken;
    try { return await send(path, { ...options, token }); }
    catch (error) {
      if (error.kind !== 'session') throw error;
      if (error.code === 'AUTH4016') { clear(); throw error; }
      if (accessToken === token) await refresh();
      try { return await send(path, { ...options, token: accessToken }); }
      catch (retryError) { if (retryError.kind === 'session') clear(); throw retryError; }
    }
  }
  async function restore() {
    await refresh();
    return request(`${base}/me`);
  }
  async function login({ email, password, rememberMe }) {
    const result = await send(`${base}/login`, { method: 'POST', body: { email, password, rememberMe } });
    revision += 1;
    accept(result);
    return request(`${base}/me`);
  }
  async function logout() {
    // 진행 중 재발급을 마친 뒤 쿠키를 무효화하여 응답 순서로 인한 쿠키 재생성을 방지합니다.
    if (refreshPromise) await refreshPromise.catch(() => {});
    await send(`${base}/logout`, { method: 'POST' });
    clear();
  }
  return { login, restore, request, logout, clear, AuthError };
})();
