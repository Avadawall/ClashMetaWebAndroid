package config

import (
	"net/http"
	U "net/url"
	"os"
	P "path"
	"sync"

	"cfa/native/route"

	"github.com/Dreamacro/clash/config"
	"github.com/Dreamacro/clash/log"
	"github.com/go-chi/render"
)

const (
	File = iota
	Url
)

type WriteHandler struct {
	processPath string
	profile     *Profile
	lock        sync.Mutex
}

type Profile struct {
	Uuid     string `json:"uuid"`
	Name     string `json:"name"`
	PType    int    `json:"ptype"`
	Source   string `json:"source"`
	Interval uint64 `json:"interval"`
	HasErr   bool   `json:"hasErr"`
	Err      string `json:"err"`
}

func (p *Profile) setErr(hasErr bool, err string) {
	p.HasErr = hasErr
	p.Err = err
}

type CfgRequest struct {
	Uuid     string `json:"uuid"`
	Name     string `json:"name"`
	PType    int    `json:"pType"`
	Source   string `json:"source"`
	Interval uint64 `json:"interval"`
	Payload  string `json:"payload"`
}

func NewHandler(path, uuid, name string, pType int, source string, interval uint64) *WriteHandler {
	log.Warnln("=======> %s--->%s---->%s------>%d----->%s------>%d", path, uuid, name, pType, source, interval)
	return &WriteHandler{
		processPath: path,
		profile: &Profile{
			Uuid:     uuid,
			Name:     name,
			PType:    pType,
			Source:   source,
			Interval: interval,
			HasErr:   false,
			Err:      "",
		},
		//sem: semaphore.NewWeighted(1),
	}
}

func (wh *WriteHandler) GetProfile() *Profile {
	return wh.profile
}

func (wh *WriteHandler) HasError() bool {
	return wh.profile.HasErr
}

func (wh *WriteHandler) ErrProfile() string {
	return wh.profile.Err
}

func (wh *WriteHandler) Validate(w http.ResponseWriter, r *http.Request) {

	wh.lock.Lock()
	defer wh.lock.Unlock()
	req := CfgRequest{}
	if err := render.DecodeJSON(r.Body, &req); err != nil {
		RendErr(w, r, http.StatusBadRequest, route.ErrBadRequest)
		wh.profile.setErr(true, err.Error())
		return
	}
	log.Warnln("+++++++++>%s++++%s", wh.profile.Uuid, req.Uuid)
	if wh.profile.Uuid != req.Uuid {
		RendErr(w, r, http.StatusNoContent, route.ErrNotFound)
		wh.profile.setErr(true, "uuid not match")
		return
	}
	log.Warnln("request: %s, %d, %s, %d, %s, %s", req.Uuid, req.PType, req.Name, req.Interval, req.Source, req.Payload)
	switch req.PType {
	case File:
		wh.fileProcessor(&req)
	case Url:
		wh.urlProcessor(&req)
	}
}

func (wh *WriteHandler) urlProcessor(r *CfgRequest) {
	if r.Source == "" {
		wh.profile.setErr(true, "url should not empty")
		return
	}

	url, err := U.Parse(r.Source)
	if err != nil {
		wh.profile.setErr(true, err.Error())
		return
	}
	//configPath :=
	//log.Warnln("urlProcessor processPath=%s, configPath=%s\n", wh.processPath, configPath)

	if err := fetch(url, P.Join(wh.processPath, "config.yaml")); err != nil {
		wh.profile.setErr(true, err.Error())
		return
	}

	rawCfg, err := UnmarshalAndPatch(wh.processPath)
	if err != nil {
		wh.profile.setErr(true, err.Error())
		return
	}

	err = providers(rawCfg)
	if err != nil {
		wh.profile.setErr(true, err.Error())
		return
	}
	p := &Profile{
		Uuid:     r.Uuid,
		Name:     r.Name,
		PType:    r.PType,
		Source:   r.Source,
		Interval: r.Interval,
		HasErr:   false,
		Err:      "",
	}
	wh.profile = p
}

func (wh *WriteHandler) fileProcessor(r *CfgRequest) {
	if r.Payload == "" {
		wh.profile.setErr(true, "payload is empty")
		return
	}

	rawCfg, err := config.UnmarshalRawConfig([]byte(r.Payload))
	if err != nil {
		wh.profile.setErr(true, err.Error())
		return
	}

	err = providers(rawCfg)
	if err != nil {
		wh.profile.setErr(true, err.Error())
		return
	}

	err = os.WriteFile(P.Join(wh.processPath, "config.yaml"), []byte(r.Payload), 766)
	if err != nil {
		wh.profile.setErr(true, err.Error())
		return
	}

	p := &Profile{
		Uuid:     r.Uuid,
		Name:     r.Name,
		PType:    r.PType,
		Source:   "",
		Interval: r.Interval,
		HasErr:   false,
		Err:      "",
	}
	wh.profile = p
}

func providers(rawCfg *config.RawConfig) error {
	forEachProviders(rawCfg, func(index int, total int, key string, provider map[string]any) {

		u, uok := provider["url"]
		p, pok := provider["path"]

		if !uok || !pok {
			return
		}

		us, uok := u.(string)
		ps, pok := p.(string)

		if !uok || !pok {
			return
		}

		if _, err := os.Stat(ps); err != nil {
			return
		}

		url, err := U.Parse(us)
		if err != nil {
			return
		}
		_ = fetch(url, ps)
	})

	cfg, err := Parse(rawCfg)
	if err != nil {
		return err
	}

	destroyProviders(cfg)
	return nil
}

func (wh *WriteHandler) GetConfig(w http.ResponseWriter, r *http.Request) {
	configPath := P.Join(wh.processPath, "config.yaml")
	configData, err := os.ReadFile(configPath)
	if err != nil {
		RendErr(w, r, http.StatusBadRequest, &route.HTTPError{Message: err.Error()})
		return
	}
	render.JSON(w, r, render.M{
		"uuid":     wh.profile.Uuid,
		"name":     wh.profile.Name,
		"pType":    wh.profile.PType,
		"source":   wh.profile.Source,
		"interval": wh.profile.Interval,
		"payload":  configData,
	})
}

func RendErr(w http.ResponseWriter, r *http.Request, httpStatus int, err *route.HTTPError) {
	render.Status(r, httpStatus)
	render.JSON(w, r, err)
}
