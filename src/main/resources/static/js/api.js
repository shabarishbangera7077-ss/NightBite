async function apiFetch(url, options = {}) {
  const headers = new Headers(options.headers || {});
  const token = localStorage.getItem('token');

  if (!headers.has('Content-Type') && !(options.body instanceof FormData)) {
    headers.set('Content-Type', 'application/json');
  }

  if (token) {
    headers.set('Authorization', `Bearer ${token}`);
  }

  const response = await fetch(url, { ...options, headers });

  if (response.status === 401) {
    localStorage.clear();
    window.location.href = 'login.html';
    return null;
  }

  const contentType = response.headers.get('content-type') || '';
  const isJson = contentType.includes('application/json');
  const payload = isJson ? await response.json() : await response.text();

  if (!response.ok) {
    throw new Error(payload.message || payload.error || 'Request failed');
  }

  return payload;
}

function logout() {
  localStorage.clear();
  window.location.href = 'login.html';
}
