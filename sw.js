const CACHE = "take5-v3";
const SHELL = [
    "./",
    "./index.html",
    "./jspdf.umd.min.js",
    "./manifest.webmanifest",
    "./icon-192.png",
    "./icon-512.png",
    "./apple-touch-icon.png",
    "./app/src/main/res/drawable-nodpi/ivt_logo.png"
];

self.addEventListener("install", function (event) {
    event.waitUntil(caches.open(CACHE).then(function (cache) {
        return cache.addAll(SHELL);
    }));
    self.skipWaiting();
});

self.addEventListener("activate", function (event) {
    event.waitUntil(caches.keys().then(function (keys) {
        return Promise.all(keys.filter(function (key) { return key !== CACHE; }).map(function (key) {
            return caches.delete(key);
        }));
    }));
    self.clients.claim();
});

self.addEventListener("fetch", function (event) {
    if (event.request.method !== "GET") return;
    const url = new URL(event.request.url);
    if (url.origin !== self.location.origin) return;
    event.respondWith(fetch(event.request).then(function (response) {
        if (response.ok) {
            const copy = response.clone();
            caches.open(CACHE).then(function (cache) { cache.put(event.request, copy); });
        }
        return response;
    }).catch(function () {
        return caches.match(event.request).then(function (cached) {
            if (cached) return cached;
            if (event.request.mode === "navigate") return caches.match("./index.html");
            return undefined;
        });
    }));
});
