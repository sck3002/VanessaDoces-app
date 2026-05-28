const CACHE_NAME = 'vanessa-doces-v8';
const ASSETS = [
  '/VanessaDoces-app/',
  '/VanessaDoces-app/index.html',
  '/VanessaDoces-app/manifest.json',
  '/VanessaDoces-app/icon-192.png',
  '/VanessaDoces-app/icon-512.png'
];

// Instala e armazena os arquivos estáticos em cache
self.addEventListener('install', event => {
  event.waitUntil(
    caches.open(CACHE_NAME).then(cache => cache.addAll(ASSETS))
  );
  self.skipWaiting();
});

// Remove caches antigos ao ativar nova versão
self.addEventListener('activate', event => {
  event.waitUntil(
    caches.keys().then(keys =>
      Promise.all(keys.filter(k => k !== CACHE_NAME).map(k => caches.delete(k)))
    )
  );
  self.clients.claim();
});

// Estratégia: Network First para chamadas Supabase, Cache First para assets locais
self.addEventListener('fetch', event => {
  const url = new URL(event.request.url);

  // Requisições ao Supabase sempre vão para a rede (dados em tempo real)
  if (url.hostname.includes('supabase.co')) {
    event.respondWith(fetch(event.request));
    return;
  }

  // Assets locais: tenta cache primeiro, senão busca na rede
  event.respondWith(
    caches.match(event.request).then(cached => {
      return cached || fetch(event.request).then(response => {
        // Armazena em cache a nova resposta
        if (response.ok) {
          const clone = response.clone();
          caches.open(CACHE_NAME).then(cache => cache.put(event.request, clone));
        }
        return response;
      });
    }).catch(() => {
      // Fallback offline: retorna a index se for navegação
      if (event.request.mode === 'navigate') {
        return caches.match('/VanessaDoces-app/index.html');
      }
    })
  );
});
