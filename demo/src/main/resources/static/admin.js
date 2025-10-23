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

  $('tokenizeBtn').addEventListener('click', async ()=>{
    const pan = $('pan').value.replace(/\s+/g,'');
    const expMonth = Number($('expMonth').value||0);
    const expYear = Number($('expYear').value||0);
    const adminJwt = $('adminJwt').value.trim();
    if(!pan || !expMonth || !expYear || !adminJwt){ $('output').textContent = 'PAN/exp/admin JWT gerekli'; return; }
    try{
      const resp = await jsonFetch('/internal/tokenize', { method:'POST', headers:{ Authorization: adminJwt }, body: JSON.stringify({ pan, expMonth, expYear }) });
      $('tokenInfo').textContent = `Token: ${resp.token} (brand: ${resp.brand}, last4: ${resp.last4})`;
      $('output').textContent = JSON.stringify(resp, null, 2);
    }catch(err){ $('output').textContent = JSON.stringify(err, null, 2); }
  });
});


