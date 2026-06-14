const CACHE_NAME = 'vanessa-doces-v24';

// Assets obrigatórios — se qualquer um falhar, a instalação aborta
const ASSETS_REQUIRED = [
  '/VanessaDoces-app/',
  '/VanessaDoces-app/index.html',
  '/VanessaDoces-app/manifest.json'
];

// Assets opcionais — falha individual não impede a instalação
const ASSETS_OPTIONAL = [
  '/VanessaDoces-app/icon-192.png',
  '/VanessaDoces-app/icon-512.png'
];

// Instala e armazena os arquivos estáticos em cache
self.addEventListener('install', event => {
  event.waitUntil(
    caches.open(CACHE_NAME).then(cache => {
      return cache.addAll(ASSETS_REQUIRED).then(() => {
        // Ícones são cacheados individualmente — erro individual não derruba o install
        return Promise.allSettled(
          ASSETS_OPTIONAL.map(url =>
            cache.add(url).catch(err =>
              console.warn(`[SW] Asset opcional não encontrado: ${url}`, err)
            )
          )
        );
      });
    })
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

  // Apenas requisições GET são cacheáveis
  if (event.request.method !== 'GET') {
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
