if(!self.define){let e,s={};const i=(i,n)=>(i=new URL(i+".js",n).href,s[i]||new Promise((s=>{if("document"in self){const e=document.createElement("script");e.src=i,e.onload=s,document.head.appendChild(e)}else e=i,importScripts(i),s()})).then((()=>{let e=s[i];if(!e)throw new Error(`Module ${i} didn’t register its module`);return e})));self.define=(n,r)=>{const l=e||("document"in self?document.currentScript.src:"")||location.href;if(s[l])return;let o={};const t=e=>i(e,l),u={module:{uri:l},exports:o,require:t};s[l]=Promise.all(n.map((e=>u[e]||t(e)))).then((e=>(r(...e),o)))}}define(["./workbox-56a10583"],(function(e){"use strict";self.skipWaiting(),e.clientsClaim(),e.precacheAndRoute([{url:"assets/Config-3d210598.js",revision:null},{url:"assets/Connections-959e81a5.js",revision:null},{url:"assets/global-aebafdec.js",revision:null},{url:"assets/index-052de771.css",revision:null},{url:"assets/index-d4aad9d6.js",revision:null},{url:"assets/index-e2183211.js",revision:null},{url:"assets/Logs-2f7364bd.js",revision:null},{url:"assets/Proxies-5ba234a5.js",revision:null},{url:"assets/Rules-45f47923.js",revision:null},{url:"assets/Setup-84a19848.js",revision:null},{url:"assets/vendor-5f1229f9.js",revision:null},{url:"index.html",revision:"3edb22ba22e3cdc993d1ce2a12c82dc5"},{url:"registerSW.js",revision:"402b66900e731ca748771b6fc5e7a068"},{url:"favicon.svg",revision:"f5b3372f312fbbe60a6ed8c03741ff80"},{url:"pwa-192x192.png",revision:"c45f48fc59b5bf47e6cbf1626aff51fc"},{url:"pwa-512x512.png",revision:"a311504ae6a46bd29b5678a410aaafc6"},{url:"manifest.webmanifest",revision:"4d78c8bc6207146065400ff644fe5a13"}],{}),e.cleanupOutdatedCaches(),e.registerRoute(new e.NavigationRoute(e.createHandlerBoundToURL("index.html")))}));