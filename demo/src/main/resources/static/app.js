async function api(url, options = {}) {
  const jwt = localStorage.getItem('jwt');
  const headers = Object.assign({ 'Content-Type': 'application/json' }, options.headers || {});
  if (jwt) headers['Authorization'] = jwt;
  const res = await fetch(url, Object.assign({}, options, { headers }));
  const text = await res.text();
  let body;
  try { body = JSON.parse(text); } catch { body = text; }
  if (!res.ok) throw { status: res.status, body };
  return body;
}

document.addEventListener('DOMContentLoaded', () => {
  const form = document.getElementById('loginForm');
  const statusEl = document.getElementById('status');
  const out = document.getElementById('output');
  const btnProducts = document.getElementById('testProducts');
  const btnOrders = document.getElementById('testMyOrders');
  const btnLogout = document.getElementById('logoutBtn');

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    const email = document.getElementById('email').value.trim();
    const password = document.getElementById('password').value;
    try {
      const resp = await api('/api/auth/login', { method: 'POST', body: JSON.stringify({ email, password }) });
      localStorage.setItem('jwt', 'Bearer ' + resp.token);
      statusEl.textContent = 'Durum: Oturum açık';
      out.textContent = 'Giriş başarılı';
    } catch (err) {
      out.textContent = 'Giriş başarısız: ' + JSON.stringify(err);
    }
  });

  btnLogout.addEventListener('click', () => {
    localStorage.removeItem('jwt');
    statusEl.textContent = 'Durum: Oturum kapalı';
    out.textContent = 'Çıkış yapıldı';
  });

  btnProducts.addEventListener('click', async () => {
    try {
      const resp = await api('/api/products');
      out.textContent = JSON.stringify(resp, null, 2);
    } catch (err) {
      out.textContent = 'Hata: ' + JSON.stringify(err, null, 2);
    }
  });

  btnOrders.addEventListener('click', async () => {
    try {
      const resp = await api('/api/orders');
      out.textContent = JSON.stringify(resp, null, 2);
    } catch (err) {
      out.textContent = 'Hata: ' + JSON.stringify(err, null, 2);
    }
  });
});


