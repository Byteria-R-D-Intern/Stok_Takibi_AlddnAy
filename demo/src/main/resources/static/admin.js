function $(id){ return document.getElementById(id); }
function getJwt(){ return localStorage.getItem('jwt'); }
function clearJwt(){ localStorage.removeItem('jwt'); }

async function jsonFetch(url, { method='GET', headers={}, body }={}){
  const finalHeaders = { 'Content-Type': 'application/json', ...headers };
  const jwt = getJwt(); if (jwt) finalHeaders['Authorization'] = jwt;
  const res = await fetch(url, { method, headers: finalHeaders, body });
  const text = await res.text(); let parsed; try{ parsed = JSON.parse(text);}catch{ parsed=text }
  if(!res.ok) throw { status: res.status, body: parsed };
  return parsed;
}

document.addEventListener('DOMContentLoaded', () => {
  // Guard: admin rolü yoksa store'a yönlendir
  try{
    const token = (getJwt()||'').replace(/^Bearer\s+/i, '');
    const payload = JSON.parse(atob(token.split('.')[1]||''));
    const role = (payload.role || payload.authorities || '').toString();
    const isAdmin = /ADMIN/i.test(role);
    $('roleBadge').textContent = isAdmin ? 'Rol: ADMIN' : 'Rol: CUSTOMER';
    if(!isAdmin){ window.location.href = '/store.html'; return; }
  }catch{ window.location.href = '/login.html'; return; }

  $('status').textContent = 'Durum: Oturum açık';
  $('logoutBtn').addEventListener('click', ()=>{ clearJwt(); window.location.href = '/login.html'; });

  // Admin Ürün Yönetimi
  const pName = $('pName'), pPrice = $('pPrice'), pStock = $('pStock');
  const createBtn = $('createBtn'), updateBtn = $('updateBtn'), clearBtn = $('clearBtn');
  const formInfo = $('formInfo'), prodList = $('prodList');
  const loadLogsBtn = $('loadLogsBtn'), logsOut = $('logsOut');

  let editingId = null;

  async function loadProducts(){
    try{
      const list = await jsonFetch('/api/admin/products');
      prodList.innerHTML = '';
      list.forEach(p => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
          <td>${p.id}</td>
          <td>${p.name}</td>
          <td>${Number(p.price).toFixed(2)}</td>
          <td>${p.stock ?? '-'}</td>
          <td>
            <button class="edit" data-id="${p.id}" data-name="${p.name}" data-price="${p.price}" data-stock="${p.stock ?? 0}">Düzenle</button>
            <button class="del" data-id="${p.id}">Sil</button>
          </td>`;
        prodList.appendChild(tr);
      });
    }catch(err){ formInfo.textContent = 'Listeleme hatası'; }
  }

  createBtn.addEventListener('click', async ()=>{
    try{
      const body = { name: pName.value.trim(), price: Number(pPrice.value), stock: Number(pStock.value) };
      const resp = await jsonFetch('/api/admin/products', { method:'POST', body: JSON.stringify(body) });
      formInfo.textContent = `Eklendi (id=${resp.id})`;
      editingId = null; updateBtn.disabled = true;
      pName.value=''; pPrice.value=''; pStock.value='';
      await loadProducts();
    }catch(err){ formInfo.textContent = 'Ekleme hatası'; }
  });

  updateBtn.addEventListener('click', async ()=>{
    if(!editingId) return;
    try{
      const body = {
        name: pName.value.trim() || undefined,
        price: pPrice.value ? Number(pPrice.value) : undefined,
        stock: pStock.value ? Number(pStock.value) : undefined
      };
      await jsonFetch(`/api/admin/products/${editingId}`, { method:'PUT', body: JSON.stringify(body) });
      formInfo.textContent = `Güncellendi (id=${editingId})`;
      editingId = null; updateBtn.disabled = true;
      pName.value=''; pPrice.value=''; pStock.value='';
      await loadProducts();
    }catch(err){ formInfo.textContent = 'Güncelleme hatası'; }
  });

  clearBtn.addEventListener('click', ()=>{
    editingId = null; updateBtn.disabled = true; formInfo.textContent = '';
    pName.value=''; pPrice.value=''; pStock.value='';
  });

  prodList.addEventListener('click', async (e)=>{
    const edit = e.target.closest('button.edit');
    const del = e.target.closest('button.del');
    if(edit){
      editingId = Number(edit.dataset.id);
      pName.value = edit.dataset.name;
      pPrice.value = edit.dataset.price;
      pStock.value = edit.dataset.stock;
      updateBtn.disabled = false;
      formInfo.textContent = `Düzenleme modunda (id=${editingId})`;
    } else if (del){
      const id = Number(del.dataset.id);
      if(!confirm(`Silinsin mi? (id=${id})`)) return;
      try{
        await jsonFetch(`/api/admin/products/${id}`, { method:'DELETE' });
        formInfo.textContent = `Silindi (id=${id})`;
        if(editingId===id){ editingId=null; updateBtn.disabled=true; }
        await loadProducts();
      }catch(err){ formInfo.textContent = 'Silme hatası'; }
    }
  });

  loadLogsBtn.addEventListener('click', async ()=>{
    try{
      const logs = await jsonFetch('/api/admin/audit?limit=50');
      logsOut.textContent = JSON.stringify(logs, null, 2);
    }catch(err){
      logsOut.textContent = 'Log yükleme hatası veya endpoint yok.\n' + JSON.stringify(err, null, 2);
    }
  });

  // İlk açılışta ürün listesi
  loadProducts();
});


