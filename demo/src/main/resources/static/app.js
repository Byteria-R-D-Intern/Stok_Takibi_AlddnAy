// -----------------------------------------------------------------------------
// Minimal fetch wrapper with JWT support
// -----------------------------------------------------------------------------
// NEDEN: Tarayıcı depolamasındaki JWT'yi okumak için basit yardımcı
function getJwt() { return localStorage.getItem('jwt'); }
// NEDEN: JWT'yi tarayıcı depolamasına yazmak için basit yardımcı
function setJwt(token) { localStorage.setItem('jwt', token); }
// NEDEN: Çıkış yaparken JWT'yi temizlemek için basit yardımcı
function clearJwt() { localStorage.removeItem('jwt'); }

// NEDEN: JSON istek/yanıtları için genel amaçlı fetch sarmalayıcı
async function jsonFetch(url, { method = 'GET', headers = {}, body } = {}) {
  // NEDEN: Varsayılan içerik türünü JSON olarak belirliyoruz; çağıran ekstra header ekleyebilir
  const finalHeaders = { 'Content-Type': 'application/json', ...headers };
  // NEDEN: Varsa JWT'yi Authorization başlığına ekleyelim (Bearer ...)
  const jwt = getJwt();
  if (jwt) finalHeaders['Authorization'] = jwt;

  // NEDEN: İsteği ağ üzerinden gönderiyoruz
  const response = await fetch(url, { method, headers: finalHeaders, body });
  // NEDEN: Yanıtı önce ham metin olarak alıyoruz (JSON olmayan durumlar için de güvenli)
  const text = await response.text();
  // NEDEN: Metni JSON'a çevirmeyi deniyoruz; başarısızsa ham metni döndüreceğiz
  let parsed; try { parsed = JSON.parse(text); } catch { parsed = text; }
  // NEDEN: HTTP hata kodlarında istisna fırlatıp durumu/cevabı iletelim
  if (!response.ok) {
    throw { status: response.status, body: parsed };
  }
  // NEDEN: Başarılıysa parse edilmiş (veya ham) içeriği döndür
  return parsed;
}

// -----------------------------------------------------------------------------
// UI helpers
// -----------------------------------------------------------------------------
// NEDEN: Kısa yoldan DOM elemanı seçmek için yardımcı
function $(id) { return document.getElementById(id); }
// NEDEN: Üstte küçük durum metnini kolay güncellemek için yardımcı
function renderStatus(message) { $('status').textContent = message; }
// NEDEN: Altta sonuç/çıkış bölümüne metin veya JSON yazdırmak için yardımcı
function renderOutput(data) {
  // NEDEN: Nesneleri okunur JSON'a çevir, metinse olduğu gibi yaz
  const pretty = typeof data === 'string' ? data : JSON.stringify(data, null, 2);
  $('output').textContent = pretty;
}

// -----------------------------------------------------------------------------
// App bootstrap
// -----------------------------------------------------------------------------
document.addEventListener('DOMContentLoaded', () => {
  // NEDEN: Form ve buton referanslarını bir kez alıp kullanacağız
  const form = $('loginForm');
  const btnProducts = $('testProducts');
  const btnOrders = $('testMyOrders');
  const btnLogout = $('logoutBtn');

  // NEDEN: Eğer kullanıcı zaten GEÇERLİ bir JWT ile geldiyse, uygun panele yönlendirelim
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
        // NEDEN: Token süresi dolmuş/bozuksa temizle ve login'de kal
        clearJwt();
      }
    }
  } catch { clearJwt(); }

  // NEDEN: Sayfa açılır açılmaz durum metnini netleştir
  renderStatus('Durum: Oturum kapalı');

  // NEDEN: Giriş formu gönderilince API'ye istek at, başarıda token'ı kaydet
  form.addEventListener('submit', async (e) => {
    // NEDEN: Formun sayfayı yenilemesini engelle
    e.preventDefault();
    // NEDEN: Kullanıcı girdi değerlerini oku
    const email = $('email').value.trim();
    const password = $('password').value;
    try {
      // NEDEN: Kimlik doğrulama isteğini yap
      const resp = await jsonFetch('/api/auth/login', {
        method: 'POST',
        body: JSON.stringify({ email, password })
      });
      // NEDEN: Başarılıysa JWT'yi depola (Bearer önekiyle)
      setJwt('Bearer ' + resp.token);
      // NEDEN: Rol'e göre ilgili panele hemen yönlendir (geri tuşunda login'e dönmesin)
      try {
        const payload = JSON.parse(atob(resp.token.split('.')[1] || ''));
        const role = (payload.role || payload.authorities || '').toString();
        const isAdmin = /ADMIN/i.test(role);
        window.location.replace(isAdmin ? '/admin.html' : '/store.html');
      } catch {
        window.location.replace('/store.html');
      }
    } catch (err) {
      // NEDEN: Hata olursa ayrıntıyı kullanıcıya göster
      renderOutput('Giriş başarısız: ' + JSON.stringify(err, null, 2));
    }
  });

  // NEDEN: Çıkış butonuna basılınca token'ı sil ve durumu güncelle
  btnLogout.addEventListener('click', () => {
    clearJwt();
    // NEDEN: Çıkış sonrası login sayfasına dön (history temiz kalsın)
    window.location.replace('/login.html');
  });

  // NEDEN: Public ürün listesini GET ile çek ve ekranda göster
  btnProducts.addEventListener('click', async () => {
    try {
      const resp = await jsonFetch('/api/products');
      renderOutput(resp);
    } catch (err) {
      renderOutput('Hata: ' + JSON.stringify(err, null, 2));
    }
  });

  // NEDEN: JWT gerektiren sipariş listesini GET ile çek ve ekranda göster
  btnOrders.addEventListener('click', async () => {
    try {
      const resp = await jsonFetch('/api/orders');
      renderOutput(resp);
    } catch (err) {
      renderOutput('Hata: ' + JSON.stringify(err, null, 2));
    }
  });
});



