package route

import (
	"bytes"
	"encoding/json"
	"io/fs"
	"net/http"
	"strings"
	"sync"
	"time"

	"cfa/blob"
	"cfa/native/app"

	"github.com/Dreamacro/clash/log"
	"github.com/Dreamacro/clash/tunnel/statistic"
	"github.com/go-chi/chi/v5"
	"github.com/go-chi/cors"
	"github.com/go-chi/render"
	"github.com/gorilla/websocket"
)

var (
	server   *http.Server
	err      error
	lock     sync.Mutex
	upgrader = websocket.Upgrader{
		CheckOrigin: func(r *http.Request) bool {
			return true
		},
	}
)

func GetFileOfUI() http.FileSystem {
	fsSys, _ := fs.Sub(blob.EmbedUI, "webui")

	return http.FS(fsSys)
}

func AppendCors(next *chi.Mux) *chi.Mux {
	cors := cors.New(cors.Options{
		AllowedOrigins: []string{"*"},
		AllowedMethods: []string{"GET", "POST", "PUT", "PATCH", "DELETE"},
		AllowedHeaders: []string{"Content-Type", "Authorization"},
		MaxAge:         300,
	})

	next.Use(cors.Handler)
	return next
}

func AppendUI(next *chi.Mux) *chi.Mux {
	fs := http.StripPrefix("/ui", http.FileServer(GetFileOfUI()))
	next.Get("/ui", http.RedirectHandler("/ui/", http.StatusTemporaryRedirect).ServeHTTP)
	next.Get("/ui/*", func(w http.ResponseWriter, r *http.Request) {
		fs.ServeHTTP(w, r)
	})
	return next
}

func appendBoard(next *chi.Mux, secret string) *chi.Mux {
	next.Group(func(r chi.Router) {
		r.Use(Authentication(secret))
		r.Get("/", Hello)
		r.Get("/logs", getLogs)
		r.Get("/traffic", traffic)
		r.Get("/memory", memory)
		r.Get("/version", Version)
		r.Mount("/configs", configRouter())
		r.Mount("/proxies", proxyRouter())
		r.Mount("/group", GroupRouter())
		r.Mount("/rules", ruleRouter())
		r.Mount("/connections", connectionRouter())
		r.Mount("/providers/proxies", proxyProviderRouter())
		r.Mount("/providers/rules", ruleProviderRouter())
	})
	return next
}

//

func StartReadBoard(addr, secret string) error {
	stopLocked()
	r := chi.NewRouter()
	r = AppendCors(r)
	r = appendBoard(r, secret)
	r = AppendUI(r)

	server = &http.Server{
		Addr:    addr,
		Handler: r,
	}

	log.Infoln("RESTful API listening at: %s", addr)

	if err := server.ListenAndServe(); err != http.ErrServerClosed {
		log.Errorln("Could not listen on %s: %v", addr, err)
		return err
	}
	return nil

}

//func StartReadBoard(addr, secret string) {
//	stopLocked()
//
//	serverSecret = secret
//	r := chi.NewRouter()
//
//	cors := cors.New(cors.Options{
//		AllowedOrigins: []string{"*"},
//		AllowedMethods: []string{"GET", "POST", "PUT", "PATH", "DELETE"},
//		AllowedHeaders: []string{"Content-Type", "Authorization"},
//		MaxAge:         300,
//	})
//
//	r.Use(cors.Handler)
//	r.Group(func(r chi.Router) {
//		r.Use(authentication)
//		r.Get("/", hello)
//		r.Get("/logs", getLogs)
//		r.Get("/version", version)
//		r.Mount("/configs", configRouter())
//		r.Mount("/proxies", proxyRouter())
//		r.Mount("/rules", ruleRouter())
//		r.Mount("/connections", connectionRouter())
//		r.Mount("/providers/proxies", proxyProviderRouter())
//
//	})
//
//	r.Group(func(r chi.Router) {
//		fs := http.StripPrefix("/ui", http.FileServer(GetFileOfUI()))
//		r.Get("/ui", http.RedirectHandler("/ui/", http.StatusTemporaryRedirect).ServeHTTP)
//		r.Get("/ui/*", func(w http.ResponseWriter, r *http.Request) {
//			fs.ServeHTTP(w, r)
//		})
//	})
//	listener, err = net.Listen("tcp", addr)
//
//	if err != nil {
//		log.Errorln("External controller listen error: %s", err)
//		return
//	}
//	serverAddr := listener.Addr().String()
//	log.Infoln("RESTful API listening at: %s", serverAddr)
//
//	if err = http.Serve(listener, r); err != nil {
//		log.Errorln("External Controller serve error: %s", err)
//	}
//}

// func startWriteUi(add, secret string) {
// 	//stopLocked()
// }

func Authentication(secret string) func(handler http.Handler) http.Handler {
	return func(next http.Handler) http.Handler {
		fn := func(w http.ResponseWriter, r *http.Request) {
			if secret == "" {
				next.ServeHTTP(w, r)
				return
			}

			if websocket.IsWebSocketUpgrade(r) && r.URL.Query().Get("token") != "" {
				token := r.URL.Query().Get("token")
				if token != secret {
					render.Status(r, http.StatusUnauthorized)
					render.JSON(w, r, ErrUnauthorized)
					return
				}
				next.ServeHTTP(w, r)
				return
			}

			header := r.Header.Get("Authorization")
			text := strings.SplitN(header, " ", 2)
			hasInvalidHeader := text[0] != "Bearer"
			hasInvalidSecret := len(text) != 2 || text[1] != secret

			if hasInvalidHeader || hasInvalidSecret {
				render.Status(r, http.StatusUnauthorized)
				render.JSON(w, r, ErrUnauthorized)
				return
			}
			next.ServeHTTP(w, r)
		}
		return http.HandlerFunc(fn)
	}
}

func StopReadListener() {
	lock.Lock()
	defer lock.Unlock()
	stopLocked()
}

func stopLocked() {
	if server != nil {
		server.Close()
	}
	server = nil
}

func Hello(w http.ResponseWriter, r *http.Request) {
	render.JSON(w, r, render.M{"hello": "clash"})
}

type Log struct {
	Type    string `json:"type"`
	Payload string `json:"payload"`
}

func getLogs(w http.ResponseWriter, r *http.Request) {
	levelText := r.URL.Query().Get("level")
	if levelText == "" {
		levelText = "info"
	}

	level, ok := log.LogLevelMapping[levelText]
	if !ok {
		render.Status(r, http.StatusBadRequest)
		render.JSON(w, r, ErrBadRequest)
		return
	}

	var wsConn *websocket.Conn
	if websocket.IsWebSocketUpgrade(r) {
		var err error
		wsConn, err = upgrader.Upgrade(w, r, nil)
		if err != nil {
			return
		}
	}

	if wsConn == nil {
		w.Header().Set("Content-Type", "application/json")
		render.Status(r, http.StatusOK)
	}

	ch := make(chan log.Event, 1024)
	sub := log.Subscribe()
	defer log.UnSubscribe(sub)
	buf := &bytes.Buffer{}

	go func() {
		for logM := range sub {
			select {
			case ch <- logM:
			default:
			}
		}
		close(ch)
	}()

	for logM := range ch {
		if logM.LogLevel < level {
			continue
		}
		buf.Reset()

		if err := json.NewEncoder(buf).Encode(Log{
			Type:    logM.Type(),
			Payload: logM.Payload,
		}); err != nil {
			break
		}

		var err error
		if wsConn == nil {
			_, err = w.Write(buf.Bytes())
			w.(http.Flusher).Flush()
		} else {
			err = wsConn.WriteMessage(websocket.TextMessage, buf.Bytes())
		}

		if err != nil {
			break
		}
	}
}

type Traffic struct {
	Up   int64 `json:"up"`
	Down int64 `json:"down"`
}

func traffic(w http.ResponseWriter, r *http.Request) {
	var wsConn *websocket.Conn
	if websocket.IsWebSocketUpgrade(r) {
		var err error
		wsConn, err = upgrader.Upgrade(w, r, nil)
		if err != nil {
			return
		}
	}

	if wsConn == nil {
		w.Header().Set("Content-Type", "application/json")
		render.Status(r, http.StatusOK)
	}

	tick := time.NewTicker(time.Second)
	defer tick.Stop()
	t := statistic.DefaultManager
	buf := &bytes.Buffer{}
	var err error
	for range tick.C {
		buf.Reset()
		up, down := t.Now()
		if err := json.NewEncoder(buf).Encode(Traffic{
			Up:   up,
			Down: down,
		}); err != nil {
			break
		}

		if wsConn == nil {
			_, err = w.Write(buf.Bytes())
			w.(http.Flusher).Flush()
		} else {
			err = wsConn.WriteMessage(websocket.TextMessage, buf.Bytes())
		}

		if err != nil {
			break
		}
	}
}

type Memory struct {
	Inuse   uint64 `json:"inuse"`
	OSLimit uint64 `json:"oslimit"` // maybe we need it in the future
}

func memory(w http.ResponseWriter, r *http.Request) {
	var wsConn *websocket.Conn
	if websocket.IsWebSocketUpgrade(r) {
		var err error
		wsConn, err = upgrader.Upgrade(w, r, nil)
		if err != nil {
			return
		}
	}

	if wsConn == nil {
		w.Header().Set("Content-Type", "application/json")
		render.Status(r, http.StatusOK)
	}

	tick := time.NewTicker(time.Second)
	defer tick.Stop()
	t := statistic.DefaultManager
	buf := &bytes.Buffer{}
	var err error
	first := true
	for range tick.C {
		buf.Reset()

		inuse := t.Memory()
		// make chat.js begin with zero
		// this is shit var,but we need output 0 for first time
		if first {
			inuse = 0
			first = false
		}
		if err := json.NewEncoder(buf).Encode(Memory{
			Inuse:   inuse,
			OSLimit: 0,
		}); err != nil {
			break
		}
		if wsConn == nil {
			_, err = w.Write(buf.Bytes())
			w.(http.Flusher).Flush()
		} else {
			err = wsConn.WriteMessage(websocket.TextMessage, buf.Bytes())
		}

		if err != nil {
			break
		}
	}
}

func Version(w http.ResponseWriter, r *http.Request) {
	render.JSON(w, r, render.M{
		"version": app.VersionName(),
		"meta":    true,
		"mode":    app.AppMode(),
	})
}
