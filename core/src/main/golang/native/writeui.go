package main

//#include "bridge.h"
import "C"
import (
	"encoding/json"
	"net/http"
	"runtime"
	"strconv"
	"sync"
	"unsafe"

	"cfa/native/app"
	"cfa/native/config"
	"cfa/native/route"

	"github.com/Dreamacro/clash/log"
	"github.com/go-chi/chi/v5"
	"github.com/gorilla/websocket"
	"golang.org/x/sync/semaphore"
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

var serviceCb *serviceCallback = nil

type serviceCallback struct {
	callback unsafe.Pointer
	sem      *semaphore.Weighted
	handler  *config.WriteHandler
}

func (sc *serviceCallback) reportStatus(json string) {
	C.ui_report(sc.callback, marshalString(json))
}

//export startWuiListener
func startWuiListener(callback unsafe.Pointer, processPath C.c_string, port C.int, secret C.c_string,
	uuid C.c_string, name C.c_string, pType C.int, source C.c_string, interval C.ulong) {
	stopLocked()
	serviceCb = &serviceCallback{
		handler:  config.NewHandler(C.GoString(processPath), C.GoString(uuid), C.GoString(name), int(pType), C.GoString(source), uint64(interval)),
		callback: callback,
		sem:      semaphore.NewWeighted(1),
	}
	app.ApplyMode(int(pType))

	go func(port int, secret string) {
		r := chi.NewRouter()
		r = route.AppendCors(r)
		r = appendWrite(r, secret)
		r = route.AppendUI(r)
		addr := "0.0.0.0" + ":" + strconv.Itoa(port)
		server = &http.Server{
			Addr:    addr,
			Handler: r,
		}

		log.Infoln("RESTful API listening at: %s", addr)
		if err := server.ListenAndServe(); err != http.ErrServerClosed {
			log.Errorln("Could not listen on %s: %v", addr, err)
		}
	}(int(port), C.GoString(secret))

}

//export stopWriteListener
func stopWriteListener() {
	lock.Lock()
	defer lock.Unlock()
	stopLocked()
}

func appendWrite(next *chi.Mux, secret string) *chi.Mux {
	next.Group(func(r chi.Router) {
		r.Use(route.Authentication(secret))
		r.Get("/", route.Hello)
		r.Get("/version", route.Version)
		r.Mount("/primitive", primitive())

	})
	return next
}

func primitive() http.Handler {
	r := chi.NewRouter()
	r.Get("/", getPrimitive)
	r.Put("/", commitAndVerify)
	return r
}

func getPrimitive(w http.ResponseWriter, r *http.Request) {
	serviceCb.handler.GetConfig(w, r)
}

func commitAndVerify(w http.ResponseWriter, r *http.Request) {

	if !serviceCb.sem.TryAcquire(1) {
		config.RendErr(w, r, http.StatusTooManyRequests, &route.HTTPError{Message: "only single thread to apply"})
	}
	defer serviceCb.sem.Release(1)
	serviceCb.handler.Validate(w, r)

	if !serviceCb.handler.HasError() {
		bytes, _ := json.Marshal(serviceCb.handler.GetProfile())
		serviceCb.reportStatus(string(bytes))
	} else {
		config.RendErr(w, r, http.StatusBadRequest, &route.HTTPError{
			Message: serviceCb.handler.ErrProfile(),
		})
	}
}

func stopLocked() {
	if server != nil {
		server.Close()
	}
	runtime.GC()
	server = nil
}
