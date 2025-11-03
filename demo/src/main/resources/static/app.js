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

  // NEDEN: İsteği ağ üzerinden gönderiyoruz
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


  try {
    const raw = getJwt();
    if (raw && /^Bearer\s+[^.]+\.[^.]+\.[^.]+$/.test(raw)) {
      const token = raw.replace(/^Bearer\s+/i, '');
      const payload = JSON.parse(atob(token.split('.')[1] || ''));
      const now = Math.floor(Date.now() / 1000);
      if (payload && payload.exp && payload.exp > now) {
        const role = (payload.role || payload.authorities || '').toString();
        const isAdmin = /ADMIN/i.test(role);
        window.location.replace(isAdmin ? '/admin.html' : '/store.html');
        return;
      } else {
        
        clearJwt();
      }
    }
  } catch { clearJwt(); }

  
  renderStatus('Durum: Oturum kapalı');

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

      try {
        const payload = JSON.parse(atob(resp.token.split('.')[1] || ''));
        const role = (payload.role || payload.authorities || '').toString();
        const isAdmin = /ADMIN/i.test(role);
        window.location.replace(isAdmin ? '/admin.html' : '/store.html');
      } catch {
        window.location.replace('/store.html');
      }
    } catch (err) {
     
      renderOutput('Giriş başarısız: ' + JSON.stringify(err, null, 2));
    }
  });

  
  btnLogout.addEventListener('click', () => {
    clearJwt();
    
    window.location.replace('/login.html');
  });

  
  btnProducts.addEventListener('click', async () => {
    try {
      const resp = await jsonFetch('/api/products');
      renderOutput(resp);
    } catch (err) {
      renderOutput('Hata: ' + JSON.stringify(err, null, 2));
    }
  });

  
  btnOrders.addEventListener('click', async () => {
    try {
      const resp = await jsonFetch('/api/orders');
      renderOutput(resp);
    } catch (err) {
      renderOutput('Hata: ' + JSON.stringify(err, null, 2));
    }
  });
});



