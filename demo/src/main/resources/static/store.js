function $(id) {
  return document.getElementById(id);
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

document.addEventListener('DOMContentLoaded', () => {
  // Guard: JWT yoksa login sayfasına yönlendir
  if (!getJwt()) {
    window.location.href = '/login.html';
    return;
  }

  renderStatus();
  loadProducts();

  $('logoutBtn').addEventListener('click', () => {
    clearJwt();
    renderStatus();
    renderOutput('Çıkış yapıldı');
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

      const resp = await jsonFetch('/api/payments', {
        method: 'POST',
        headers: { 'Idempotency-Key': idempotency },
        body: JSON.stringify({
          orderId,
          amount,
          currency: 'TRY',
          method: 'CARD',
          paymentToken: ''
        })
      });
      renderOutput(resp);
    } catch (err) {
      renderOutput(err);
    }
  });
});


