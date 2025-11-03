function $(id){ return document.getElementById(id); }
function getJwt(){ return localStorage.getItem('jwt'); }
function clearJwt(){ localStorage.removeItem('jwt'); }

function isValidBearer(raw){ return /^Bearer\s+[^.]+\.[^.]+\.[^.]+$/.test(raw||''); }
function decodeJwt(raw){ try{ const t=raw.replace(/^Bearer\s+/i,''); return JSON.parse(atob(t.split('.')[1]||'')); }catch{ return null; } }

async function jsonFetch(url, { method='GET', headers={}, body }={}){
  const finalHeaders = { 'Content-Type': 'application/json', ...headers };
  const jwt = getJwt(); if (jwt) finalHeaders['Authorization'] = jwt;
  const res = await fetch(url, { method, headers: finalHeaders, body });
  const text = await res.text(); let parsed; try{ parsed = JSON.parse(text);}catch{ parsed=text }
  if(!res.ok){
    if(res.status===401 || res.status===403){ clearJwt(); try{ window.location.href='/login.html'; }catch{} }
    throw { status: res.status, body: parsed };
  }
  return parsed;
}

document.addEventListener('DOMContentLoaded', () => {
  // Guard: token yok/bozuk/expired ise login'e; admin değilse store'a
  const raw = getJwt();
  if(!isValidBearer(raw)){ clearJwt(); window.location.href='/login.html'; return; }
  const payload = decodeJwt(raw);
  const now = Math.floor(Date.now()/1000);
  if(!payload || (payload.exp && payload.exp < now)){ clearJwt(); window.location.href='/login.html'; return; }
  const role = (payload.role || payload.authorities || '').toString();
  const isAdmin = /ADMIN/i.test(role);
  $('roleBadge').textContent = isAdmin ? 'Rol: ADMIN' : 'Rol: CUSTOMER';
  if(!isAdmin){ window.location.href = '/store.html'; return; }

  $('status').textContent = 'Durum: Oturum açık';
  $('logoutBtn').addEventListener('click', ()=>{ clearJwt(); window.location.replace('/login.html'); });

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

      const body = {
        name: pName.value.trim(),
        price: Number(pPrice.value),
        stock: Number(pStock.value),
        sku: (document.getElementById('pSku')?.value || '').trim() || null,
        description: document.getElementById('pDesc')?.value || null,
        metadata: document.getElementById('pMeta')?.value || null
      };

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
        stock: pStock.value ? Number(pStock.value) : undefined,
        sku: (document.getElementById('pSku')?.value || undefined),
        description: (document.getElementById('pDesc')?.value || undefined),
        metadata: (document.getElementById('pMeta')?.value || undefined)
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


  // tarihi parse etmek için yardımcı bir fonk
  function formatDate(iso){
    try {
      const d = new Date(iso);
      if (isNaN(d.getTime())) return String(iso ?? '');
      return d.toLocaleString('tr-TR');
    } catch {
      return String(iso ?? '');
    }
  }
  //logları tabloya basar
  function renderLogs(logs){

     const tbody = document.getElementById('logsList');
     logs.innerHTML = '';

     logs.forEach(l => {

      const tr = document.createElement('tr');
      //daha okunuabilir olsun diye
      const target =`${l.targetType}#${l.targetId}`;

      tr.innerHTML = `
      <td>${l.id}</td>
      <td>${l.actorUserId}</td>
      <td>${target}</td>
      <td>${l.action}</td>
      <td>${l.message}</td>
      <td>
        ${l.changesJson ? `<button class="show">Göster</button>
          <pre class="detail" style="display:none; max-width:420px; white-space:pre-wrap; word-break:break-word;">${
            typeof l.changesJson==='string' ? l.changesJson : JSON.stringify(l.changesJson, null, 2)
          }</pre>` : '-'}
      </td>
      <td>${formatDate(l.createdAt)}</td>
      `;

      const btn = tr.querySelector('button.show');
      if (btn) {
        const pre = tr.querySelector('pre.detail');
        btn.addEventListener('click', () => {
          pre.style.display = pre.style.display === 'none' ? 'block' : 'none';
        });
      }
  
      // satırın tabloya eklenmesi
      tbody.appendChild(tr);
    })

}

  //logları yükleme butonu 
  document.getElementById('loadLogsBtn').addEventListener('click', async ()=> {

    try {
      const logs = await jsonFetch('/api/admin/audit?limit=50');
      renderLogs(logs);
    } catch (err) {
      alert('Log yüklenemedi');
    }
  })

  // İlk açılışta ürün listesi
  loadProducts();
});


