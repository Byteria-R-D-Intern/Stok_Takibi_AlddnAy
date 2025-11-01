function $(id) {
  return document.getElementById(id);
}

function isValidBearer(raw) {
  return /^Bearer\s+[^.]+\.[^.]+\.[^.]+$/.test(raw || '');
}

function decodeJwt(raw) {
  try {
    const token = raw.replace(/^Bearer\s+/i, '');
    return JSON.parse(atob(token.split('.')[1] || ''));
  } catch { return null; }
}

function getJwt() {
  return localStorage.getItem('jwt');
}

function setJwt(token) {
  localStorage.setItem('jwt', token);
}

function clearJwt() {
  localStorage.removeItem('jwt');
}

async function jsonFetch(url, { method = 'GET', headers = {}, body } = {}) {
  const finalHeaders = { 'Content-Type': 'application/json', ...headers };

  const jwt = getJwt();
  if (jwt) {
    finalHeaders['Authorization'] = jwt;
  }

  const res = await fetch(url, { method, headers: finalHeaders, body });
  const text = await res.text();

  let parsed;
  try {
    parsed = JSON.parse(text);
  } catch {
    parsed = text;
  }

  if (!res.ok) {
    // NEDEN: Yetkisiz/engelli isteklerde kullanıcıyı login'e döndürmek
    if (res.status === 401 || res.status === 403) {
      clearJwt();
      try { window.location.href = '/login.html'; } catch {}
    }
    throw { status: res.status, body: parsed };
  }

  return parsed;
}

// productId -> { name, price, qty }
const cart = new Map();

function renderStatus() {
  $('status').textContent = getJwt() ? 'Durum: Oturum açık' : 'Durum: Oturum kapalı';
}

function renderOutput(data) {
  $('output').textContent = typeof data === 'string' ? data : JSON.stringify(data, null, 2);
}

function renderCart() {
  const tbody = $('cart');
  tbody.innerHTML = '';

  let total = 0;
  let has = false;

  cart.forEach((item, id) => {
    has = true;
    const line = item.price * item.qty;
    total += line;

    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td>${item.name}</td>
      <td>${item.qty}</td>
      <td>${item.price.toFixed(2)}</td>
      <td>${line.toFixed(2)}</td>
      <td>
        <button data-id="${id}" class="rm">Sil</button>
      </td>
    `;
    tbody.appendChild(tr);
  });

  $('grandTotal').textContent = total.toFixed(2);
  $('cartEmpty').style.display = has ? 'none' : '';
  $('cartTable').style.display = has ? '' : 'none';
}

async function loadProducts() {
  try {
    const list = await jsonFetch('/api/products');
    const tbody = $('products');
    tbody.innerHTML = '';

    list.forEach((p) => {
      const tr = document.createElement('tr');
      tr.innerHTML = `
        <td>${p.name}</td>
        <td>${Number(p.price).toFixed(2)}</td>
        <td>${p.stock ?? '-'}</td>
        <td>
          <input
            type="number"
            min="1"
            value="1"
            style="width:80px"
            id="qty_${p.id}"
          />
        </td>
        <td>
          <button
            data-id="${p.id}"
            data-name="${p.name}"
            data-price="${p.price}"
            class="add"
          >Ekle</button>
        </td>
      `;
      tbody.appendChild(tr);
    });
  } catch (err) {
    renderOutput(err);
  }
}

function uuid() {
  return (crypto.randomUUID ? crypto.randomUUID() : Math.random().toString(36).slice(2));
}

// -----------------------------------------------------------------------------
// Tokenization helpers (frontend validation + debounce)
// -----------------------------------------------------------------------------
function luhn(pan) {
  if (!/^[0-9]{12,19}$/.test(pan)) return false;
  let sum = 0, alt = false;
  for (let i = pan.length - 1; i >= 0; i--) {
    let n = pan.charCodeAt(i) - 48; // '0' = 48
    if (alt) { n *= 2; if (n > 9) n -= 9; }
    sum += n; alt = !alt;
  }
  return (sum % 10) === 0;
}

function debounce(fn, delay) {
  let t; return (...args) => { clearTimeout(t); t = setTimeout(() => fn(...args), delay); };
}

let lastCardFingerprint = null; // pan|m|y

// Tarihi okunur biçimde göstermek için küçük yardımcı
function formatDate(iso) {
  try {
    const d = new Date(iso);
    if (isNaN(d.getTime())) return String(iso ?? '');
    return d.toLocaleString('tr-TR');
  } catch {
    return String(iso ?? '');
  }
}

document.addEventListener('DOMContentLoaded', () => {
  // Guard: JWT yoksa veya geçersiz/expired ise login'e yönlendir
  const raw = getJwt();
  if (!isValidBearer(raw)) { clearJwt(); window.location.href = '/login.html'; return; }
  const payload = decodeJwt(raw);
  const now = Math.floor(Date.now() / 1000);
  if (!payload || (payload.exp && payload.exp < now)) { clearJwt(); window.location.href = '/login.html'; return; }

  renderStatus();
  loadProducts();

  // Kullanıcı rolünü JWT'den çöz (payload.role varsa)
  try {
    const token = getJwt().replace(/^Bearer\s+/i, '');
    const payload = JSON.parse(atob(token.split('.')[1] || ''));
    const role = (payload.role || payload.authorities || '').toString();
    const isAdmin = /ADMIN/i.test(role);
    $('roleBadge').textContent = isAdmin ? 'Rol: ADMIN' : 'Rol: CUSTOMER';
    // Admin panelini role göre göster/gizle (eleman yoksa atla)
    const ap = $('adminPanel');
    if (ap) ap.style.display = isAdmin ? '' : 'none';
  } catch {}

  $('logoutBtn').addEventListener('click', () => {
    clearJwt();
    window.location.replace('/login.html');
  });

  $('products').addEventListener('click', (e) => {
    const btn = e.target.closest('button.add');
    if (!btn) return;
    const id = btn.getAttribute('data-id');
    const name = btn.getAttribute('data-name');
    const price = Number(btn.getAttribute('data-price'));
    const qty = Number($('qty_' + id).value || 1);
    const curr = cart.get(id) || { name, price, qty: 0 };
    curr.qty += qty;
    cart.set(id, curr);
    renderCart();
  });

  $('cart').addEventListener('click', (e) => {
    const btn = e.target.closest('button.rm');
    if (!btn) return;
    const id = btn.getAttribute('data-id');
    cart.delete(id);
    renderCart();
  });

  $('checkoutBtn').addEventListener('click', async () => {
    if (!getJwt()) { renderOutput('Önce giriş yapınız'); return; }

    const items = Array.from(cart.entries())
      .map(([id, it]) => ({ productId: Number(id), quantity: it.qty }));

    if (items.length === 0) { renderOutput('Sepet boş'); return; }
    const body = {
      shippingName: $('shipName').value || 'Müşteri',
      shippingPhone: $('shipPhone').value || '',
      shippingAddress: $('shipAddr').value || 'Adres',
      items
    };
    try {
      const resp = await jsonFetch('/api/orders/checkout', {
        method: 'POST',
        body: JSON.stringify(body)
      });

      $('orderInfo').textContent = `Sipariş #${resp.orderId}, toplam ${resp.total}.\nIdempotency-Key: ${resp.paymentIdempotencyKey}`;
      $('payBtn').disabled = false;
      $('payBtn').dataset.orderId = resp.orderId;
      $('payBtn').dataset.amount = resp.total;
      $('payBtn').dataset.idem = resp.paymentIdempotencyKey;
      renderOutput(resp);
    } catch (err) {
      renderOutput(err);
    }
  });

  $('payBtn').addEventListener('click', async (e) => {
    if (!getJwt()) { renderOutput('Önce giriş yapınız'); return; }
    const orderId = Number(e.target.dataset.orderId);
    const amount = Number(e.target.dataset.amount);
    const idempotency = e.target.dataset.idem || uuid();
    try {
      const paymentToken = e.target.dataset.token || '';
      const resp = await jsonFetch('/api/payments', {
        method: 'POST',
        headers: { 'Idempotency-Key': idempotency },
        body: JSON.stringify({
          orderId,
          amount,
          currency: 'TRY',
          method: 'CARD',
          paymentToken
        })
      });
      renderOutput(resp);
    } catch (err) {
      renderOutput(err);
    }
  });

  // Tokenization (admin) — /internal/tokenize çağırıp token'ı ödeme butonuna set eder
  const tokenizeBtn = document.getElementById('tokenizeBtn');
  if (tokenizeBtn) {
    tokenizeBtn.addEventListener('click', async () => {
      const pan = $('pan').value.trim();
      const expMonth = Number($('expMonth').value.trim());
      const expYear = Number($('expYear').value.trim());
      if (!pan || !expMonth || !expYear) { renderOutput('PAN/exp gerekli'); return; }
      try {
        const resp = await jsonFetch('/api/tokenize', {
          method: 'POST',
          body: JSON.stringify({ pan, expMonth, expYear })
        });
        $('tokenInfo').textContent = `Token: ${resp.token} (brand: ${resp.brand}, last4: ${resp.last4})`;
        $('payBtn').dataset.token = resp.token;
        renderOutput(resp);
      } catch (err) {
        renderOutput(err);
      }
    });
  }

  // Arka planda token üretimi (PAN/SKT değiştiğinde)
  const maybeTokenize = debounce(async () => {
    // Customer kart alanları
    let pan = (document.getElementById('cardPan')?.value || '').replace(/\s+/g, '');
    let m = Number(document.getElementById('cardExpMonth')?.value || 0);
    let y = Number(document.getElementById('cardExpYear')?.value || 0);
    let infoEl = document.getElementById('cardTokenInfo');

    // Admin panelindeki alanlar (varsa) öncelik kazanır
    const adminPanEl = document.getElementById('pan');
    if (adminPanEl) {
      const ap = (adminPanEl.value || '').replace(/\s+/g, '');
      const am = Number(document.getElementById('expMonth')?.value || 0);
      const ay = Number(document.getElementById('expYear')?.value || 0);
      if (ap && am && ay) { pan = ap; m = am; y = ay; infoEl = document.getElementById('tokenInfo'); }
    }

    const fp = `${pan}|${m}|${y}`;
    if (!pan || !m || !y) return;
    if (!luhn(pan)) { if(infoEl) infoEl.textContent = 'Kart numarası geçersiz (Luhn)'; return; }
    if (m < 1 || m > 12) { if(infoEl) infoEl.textContent = 'Ay 1-12 olmalı'; return; }
    if (fp === lastCardFingerprint) return; // değişmemiş
    try {
      lastCardFingerprint = fp;
      const resp = await jsonFetch('/api/tokenize', { method: 'POST', body: JSON.stringify({ pan, expMonth: m, expYear: y }) });
      if (infoEl) infoEl.textContent = `Hazır: ${resp.brand} •••• ${resp.last4}`;
      $('payBtn').dataset.token = resp.token;
    } catch (err) {
      if (infoEl) infoEl.textContent = 'Tokenizasyon başarısız';
    }
  }, 500);

  ['cardPan', 'cardExpMonth', 'cardExpYear', 'pan', 'expMonth', 'expYear'].forEach(id => {
    const el = document.getElementById(id);
    if (el) el.addEventListener('input', maybeTokenize);
  });

  //Bildirimleri yükleme
  async function loadNotifications(unreadOnly = true) {
    try {
      const list = await jsonFetch(`/api/notifications?unreadOnly=${unreadOnly}&limit=50`);
      renderNotifications(list);
      const unread = list.filter(n => !n.read).length;
      const badge = $('notifCount');
      if (badge) badge.textContent = `(${unread})`;
    } catch (err) {
      renderOutput(err);
    }
  }
  
  function renderNotifications(list) {
    const tbody = $('notifList');
    if (!tbody) return;
    tbody.innerHTML = '';
    list.forEach(n => {
      const tr = document.createElement('tr');
      tr.innerHTML = `
        <td>${n.title ?? '-'}</td>
        <td>${n.message ?? ''}</td>
        <td>${formatDate(n.createdAt)}</td>
        <td>
          ${n.read ? '' : `<button class="markRead" data-id="${n.id}">Oku</button>`}
          <button class="del" data-id="${n.id}">Sil</button>
        </td>
      `;
      tbody.appendChild(tr);
    });
  }
  
  // Paneli aç/kapa ve açılınca verileri getir
  const notifBtn = $('notifBtn');
  if (notifBtn) {
    notifBtn.addEventListener('click', async () => {
      const panel = $('notifPanel');
      if (!panel) return;
      const showing = panel.style.display !== 'none';
      panel.style.display = showing ? 'none' : '';
      if (!showing) await loadNotifications(true);
    });
  }
  
  // Satır içi 'Oku' ve 'Sil' aksiyonları
  const notifList = $('notifList');
  if (notifList) {
    notifList.addEventListener('click', async (e) => {
      const btnRead = e.target.closest('button.markRead');
      const btnDel = e.target.closest('button.del');
  
      if (btnRead) {
        const id = btnRead.getAttribute('data-id');
        try {
          await jsonFetch(`/api/notifications/${id}/read`, { method: 'PATCH' });
          await loadNotifications(true);
        } catch {}
      } else if (btnDel) {
        const id = btnDel.getAttribute('data-id');
        try {
          await jsonFetch(`/api/notifications/${id}`, { method: 'DELETE' });
          await loadNotifications(true);
        } catch {}
      }
    });
  }
  
  // İlk yükleme: sayaç için unread çek
  loadNotifications(true);
  
  // Periyodik: 30 sn'de bir sadece unread'leri güncelle
  setInterval(() => {
    loadNotifications(true);
  }, 30000);


});


