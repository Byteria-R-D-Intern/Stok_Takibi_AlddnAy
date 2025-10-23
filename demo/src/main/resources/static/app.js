// -----------------------------------------------------------------------------
// Minimal fetch wrapper with JWT support
// -----------------------------------------------------------------------------
function getJwt() { return localStorage.getItem('jwt'); }
function setJwt(token) { localStorage.setItem('jwt', token); }
function clearJwt() { localStorage.removeItem('jwt'); }

async function jsonFetch(url, { method = 'GET', headers = {}, body } = {}) {
  const finalHeaders = { 'Content-Type': 'application/json', ...headers };
  const jwt = getJwt();
  if (jwt) finalHeaders['Authorization'] = jwt;

  const response = await fetch(url, { method, headers: finalHeaders, body });
  const text = await response.text();
  let parsed; try { parsed = JSON.parse(text); } catch { parsed = text; }
  if (!response.ok) {
    throw { status: response.status, body: parsed };
  }
  return parsed;
}

// -----------------------------------------------------------------------------
// UI helpers
// -----------------------------------------------------------------------------
function $(id) { return document.getElementById(id); }
function renderStatus(message) { $('status').textContent = message; }
function renderOutput(data) {
  const pretty = typeof data === 'string' ? data : JSON.stringify(data, null, 2);
  $('output').textContent = pretty;
}

// -----------------------------------------------------------------------------
// App bootstrap
// -----------------------------------------------------------------------------
document.addEventListener('DOMContentLoaded', () => {
  const form = $('loginForm');
  const btnProducts = $('testProducts');
  const btnOrders = $('testMyOrders');
  const btnLogout = $('logoutBtn');

  // If already logged in, go to store
  if (getJwt()) {
    // JWT içinden role'ü çöz ve uygun panele yönlendir
    try {
      const token = getJwt().replace(/^Bearer\s+/i, '');
      const payload = JSON.parse(atob(token.split('.')[1] || ''));
      const role = (payload.role || payload.authorities || '').toString();
      const isAdmin = /ADMIN/i.test(role);
      window.location.href = isAdmin ? '/admin.html' : '/store.html';
      return;
    } catch {
      window.location.href = '/store.html';
      return;
    }
  }

  // Initialize status for login page
  renderStatus('Durum: Oturum kapalı');

  // Login submit
  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    const email = $('email').value.trim();
    const password = $('password').value;
    try {
      const resp = await jsonFetch('/api/auth/login', {
        method: 'POST',
        body: JSON.stringify({ email, password })
      });
      setJwt('Bearer ' + resp.token);
      // JWT role'e göre yönlendir
      try {
        const payload = JSON.parse(atob(resp.token.split('.')[1] || ''));
        const role = (payload.role || payload.authorities || '').toString();
        const isAdmin = /ADMIN/i.test(role);
        window.location.href = isAdmin ? '/admin.html' : '/store.html';
      } catch {
        window.location.href = '/store.html';
      }
    } catch (err) {
      renderOutput('Giriş başarısız: ' + JSON.stringify(err, null, 2));
    }
  });

  // Logout
  btnLogout.addEventListener('click', () => {
    clearJwt();
    renderStatus('Durum: Oturum kapalı');
    renderOutput('Çıkış yapıldı');
  });

  // Public products test
  btnProducts.addEventListener('click', async () => {
    try {
      const resp = await jsonFetch('/api/products');
      renderOutput(resp);
    } catch (err) {
      renderOutput('Hata: ' + JSON.stringify(err, null, 2));
    }
  });

  // Auth-required orders test
  btnOrders.addEventListener('click', async () => {
    try {
      const resp = await jsonFetch('/api/orders');
      renderOutput(resp);
    } catch (err) {
      renderOutput('Hata: ' + JSON.stringify(err, null, 2));
    }
  });
});



