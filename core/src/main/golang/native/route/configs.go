package route

import "C"
import (
	"net/http"
	"net/netip"

	"github.com/Dreamacro/clash/component/dialer"
	"github.com/Dreamacro/clash/component/resolver"
	CON "github.com/Dreamacro/clash/constant"
	"github.com/Dreamacro/clash/hub/executor"
	P "github.com/Dreamacro/clash/listener"
	LC "github.com/Dreamacro/clash/listener/config"
	"github.com/Dreamacro/clash/log"
	"github.com/Dreamacro/clash/tunnel"

	"github.com/go-chi/chi/v5"
	"github.com/go-chi/render"
)

func configRouter() http.Handler {
	r := chi.NewRouter()
	r.Get("/", getConfigs)
	//r.Put("/", updateConfigs)
	//r.Patch("/", patchConfigs)
	return r
}

type configSchema struct {
	Port              *int               `json:"port"`
	SocksPort         *int               `json:"socks-port"`
	RedirPort         *int               `json:"redir-port"`
	TProxyPort        *int               `json:"tproxy-port"`
	MixedPort         *int               `json:"mixed-port"`
	Tun               *tunSchema         `json:"tun"`
	TuicServer        *tuicServerSchema  `json:"tuic-server"`
	ShadowSocksConfig *string            `json:"ss-config"`
	VmessConfig       *string            `json:"vmess-config"`
	TcptunConfig      *string            `json:"tcptun-config"`
	UdptunConfig      *string            `json:"udptun-config"`
	AllowLan          *bool              `json:"allow-lan"`
	BindAddress       *string            `json:"bind-address"`
	Mode              *tunnel.TunnelMode `json:"mode"`
	LogLevel          *log.LogLevel      `json:"log-level"`
	IPv6              *bool              `json:"ipv6"`
	Sniffing          *bool              `json:"sniffing"`
	TcpConcurrent     *bool              `json:"tcp-concurrent"`
	InterfaceName     *string            `json:"interface-name"`
}

type tunSchema struct {
	Enable              bool          `yaml:"enable" json:"enable"`
	Device              *string       `yaml:"device" json:"device"`
	Stack               *CON.TUNStack `yaml:"stack" json:"stack"`
	DNSHijack           *[]string     `yaml:"dns-hijack" json:"dns-hijack"`
	AutoRoute           *bool         `yaml:"auto-route" json:"auto-route"`
	AutoDetectInterface *bool         `yaml:"auto-detect-interface" json:"auto-detect-interface"`
	//RedirectToTun       []string   		  `yaml:"-" json:"-"`

	MTU *uint32 `yaml:"mtu" json:"mtu,omitempty"`
	//Inet4Address           *[]config.ListenPrefix `yaml:"inet4-address" json:"inet4-address,omitempty"`
	Inet6Address           *[]netip.Prefix `yaml:"inet6-address" json:"inet6-address,omitempty"`
	StrictRoute            *bool           `yaml:"strict-route" json:"strict-route,omitempty"`
	Inet4RouteAddress      *[]netip.Prefix `yaml:"inet4-route-address" json:"inet4-route-address,omitempty"`
	Inet6RouteAddress      *[]netip.Prefix `yaml:"inet6-route-address" json:"inet6-route-address,omitempty"`
	IncludeUID             *[]uint32       `yaml:"include-uid" json:"include-uid,omitempty"`
	IncludeUIDRange        *[]string       `yaml:"include-uid-range" json:"include-uid-range,omitempty"`
	ExcludeUID             *[]uint32       `yaml:"exclude-uid" json:"exclude-uid,omitempty"`
	ExcludeUIDRange        *[]string       `yaml:"exclude-uid-range" json:"exclude-uid-range,omitempty"`
	IncludeAndroidUser     *[]int          `yaml:"include-android-user" json:"include-android-user,omitempty"`
	IncludePackage         *[]string       `yaml:"include-package" json:"include-package,omitempty"`
	ExcludePackage         *[]string       `yaml:"exclude-package" json:"exclude-package,omitempty"`
	EndpointIndependentNat *bool           `yaml:"endpoint-independent-nat" json:"endpoint-independent-nat,omitempty"`
	UDPTimeout             *int64          `yaml:"udp-timeout" json:"udp-timeout,omitempty"`
	FileDescriptor         *int            `yaml:"file-descriptor" json:"file-descriptor"`
}

type tuicServerSchema struct {
	Enable                bool               `yaml:"enable" json:"enable"`
	Listen                *string            `yaml:"listen" json:"listen"`
	Token                 *[]string          `yaml:"token" json:"token"`
	Users                 *map[string]string `yaml:"users" json:"users,omitempty"`
	Certificate           *string            `yaml:"certificate" json:"certificate"`
	PrivateKey            *string            `yaml:"private-key" json:"private-key"`
	CongestionController  *string            `yaml:"congestion-controller" json:"congestion-controller,omitempty"`
	MaxIdleTime           *int               `yaml:"max-idle-time" json:"max-idle-time,omitempty"`
	AuthenticationTimeout *int               `yaml:"authentication-timeout" json:"authentication-timeout,omitempty"`
	ALPN                  *[]string          `yaml:"alpn" json:"alpn,omitempty"`
	MaxUdpRelayPacketSize *int               `yaml:"max-udp-relay-packet-size" json:"max-udp-relay-packet-size,omitempty"`
	CWND                  *int               `yaml:"cwnd" json:"cwnd,omitempty"`
}

func getConfigs(w http.ResponseWriter, r *http.Request) {
	general := executor.GetGeneral()
	render.JSON(w, r, general)
}

func pointerOrDefault(p *int, def int) int {
	if p != nil {
		return *p
	}

	return def
}

func pointerOrDefaultString(p *string, def string) string {
	if p != nil {
		return *p
	}

	return def
}

func pointerOrDefaultTun(p *tunSchema, def LC.Tun) LC.Tun {
	if p != nil {
		def.Enable = p.Enable
		if p.Device != nil {
			def.Device = *p.Device
		}
		if p.Stack != nil {
			def.Stack = *p.Stack
		}
		if p.DNSHijack != nil {
			def.DNSHijack = *p.DNSHijack
		}
		if p.AutoRoute != nil {
			def.AutoRoute = *p.AutoRoute
		}
		if p.AutoDetectInterface != nil {
			def.AutoDetectInterface = *p.AutoDetectInterface
		}
		if p.MTU != nil {
			def.MTU = *p.MTU
		}
		//if p.Inet4Address != nil {
		//	def.Inet4Address = *p.Inet4Address
		//}
		if p.Inet6Address != nil {
			def.Inet6Address = *p.Inet6Address
		}
		if p.IncludeUID != nil {
			def.IncludeUID = *p.IncludeUID
		}
		if p.IncludeUIDRange != nil {
			def.IncludeUIDRange = *p.IncludeUIDRange
		}
		if p.ExcludeUID != nil {
			def.ExcludeUID = *p.ExcludeUID
		}
		if p.ExcludeUIDRange != nil {
			def.ExcludeUIDRange = *p.ExcludeUIDRange
		}
		if p.IncludeAndroidUser != nil {
			def.IncludeAndroidUser = *p.IncludeAndroidUser
		}
		if p.IncludePackage != nil {
			def.IncludePackage = *p.IncludePackage
		}
		if p.ExcludePackage != nil {
			def.ExcludePackage = *p.ExcludePackage
		}
		if p.EndpointIndependentNat != nil {
			def.EndpointIndependentNat = *p.EndpointIndependentNat
		}
		if p.UDPTimeout != nil {
			def.UDPTimeout = *p.UDPTimeout
		}
		if p.FileDescriptor != nil {
			def.FileDescriptor = *p.FileDescriptor
		}
	}
	return def
}

func pointerOrDefaultTuicServer(p *tuicServerSchema, def LC.TuicServer) LC.TuicServer {
	if p != nil {
		def.Enable = p.Enable
		if p.Listen != nil {
			def.Listen = *p.Listen
		}
		if p.Token != nil {
			def.Token = *p.Token
		}
		if p.Users != nil {
			def.Users = *p.Users
		}
		if p.Certificate != nil {
			def.Certificate = *p.Certificate
		}
		if p.PrivateKey != nil {
			def.PrivateKey = *p.PrivateKey
		}
		if p.CongestionController != nil {
			def.CongestionController = *p.CongestionController
		}
		if p.MaxIdleTime != nil {
			def.MaxIdleTime = *p.MaxIdleTime
		}
		if p.AuthenticationTimeout != nil {
			def.AuthenticationTimeout = *p.AuthenticationTimeout
		}
		if p.ALPN != nil {
			def.ALPN = *p.ALPN
		}
		if p.MaxUdpRelayPacketSize != nil {
			def.MaxUdpRelayPacketSize = *p.MaxUdpRelayPacketSize
		}
		if p.CWND != nil {
			def.CWND = *p.CWND
		}
	}
	return def
}

func patchConfigs(w http.ResponseWriter, r *http.Request) {
	general := &configSchema{}
	if err := render.DecodeJSON(r.Body, &general); err != nil {
		render.Status(r, http.StatusBadRequest)
		render.JSON(w, r, ErrBadRequest)
		return
	}

	if general.AllowLan != nil {
		P.SetAllowLan(*general.AllowLan)
	}

	if general.BindAddress != nil {
		P.SetBindAddress(*general.BindAddress)
	}

	if general.Sniffing != nil {
		tunnel.SetSniffing(*general.Sniffing)
	}

	if general.TcpConcurrent != nil {
		dialer.SetTcpConcurrent(*general.TcpConcurrent)
	}

	if general.InterfaceName != nil {
		dialer.DefaultInterface.Store(*general.InterfaceName)
	}

	ports := P.GetPorts()

	P.ReCreateHTTP(pointerOrDefault(general.Port, ports.Port), tunnel.Tunnel)
	P.ReCreateSocks(pointerOrDefault(general.SocksPort, ports.SocksPort), tunnel.Tunnel)
	P.ReCreateRedir(pointerOrDefault(general.RedirPort, ports.RedirPort), tunnel.Tunnel)
	P.ReCreateTProxy(pointerOrDefault(general.TProxyPort, ports.TProxyPort), tunnel.Tunnel)
	P.ReCreateMixed(pointerOrDefault(general.MixedPort, ports.MixedPort), tunnel.Tunnel)
	P.ReCreateTun(pointerOrDefaultTun(general.Tun, P.LastTunConf), tunnel.Tunnel)
	P.ReCreateShadowSocks(pointerOrDefaultString(general.ShadowSocksConfig, ports.ShadowSocksConfig), tunnel.Tunnel)
	P.ReCreateVmess(pointerOrDefaultString(general.VmessConfig, ports.VmessConfig), tunnel.Tunnel)
	P.ReCreateTuic(pointerOrDefaultTuicServer(general.TuicServer, P.LastTuicConf), tunnel.Tunnel)

	if general.Mode != nil {
		tunnel.SetMode(*general.Mode)
	}

	if general.LogLevel != nil {
		log.SetLevel(*general.LogLevel)
	}

	if general.IPv6 != nil {
		resolver.DisableIPv6 = !*general.IPv6
	}

	render.NoContent(w, r)
}

//type updateConfigRequest struct {
//	Path    string `json:"path"`
//	Payload string `json:"payload"`
//}

//func updateConfigs(w http.ResponseWriter, r *http.Request) {
//	req := updateConfigRequest{}
//	if err := render.DecodeJSON(r.Body, &req); err != nil {
//		render.Status(r, http.StatusBadRequest)
//		render.JSON(w, r, ErrBadRequest)
//		return
//	}
//
//	force := r.URL.Query().Get("force") == "true"
//	var cfg *config.Config
//	var err error
//
//	if req.Payload != "" {
//		cfg, err = executor.ParseWithBytes([]byte(req.Payload))
//		if err != nil {
//			render.Status(r, http.StatusBadRequest)
//			render.JSON(w, r, newError(err.Error()))
//			return
//		}
//	} else {
//		if req.Path == "" {
//			req.Path = constant.Path.Config()
//		}
//		if !filepath.IsAbs(req.Path) {
//			render.Status(r, http.StatusBadRequest)
//			render.JSON(w, r, newError("path is not a absolute path"))
//			return
//		}
//
//		cfg, err = executor.ParseWithPath(req.Path)
//		if err != nil {
//			render.Status(r, http.StatusBadRequest)
//			render.JSON(w, r, newError(err.Error()))
//			return
//		}
//	}
//
//	executor.ApplyConfig(cfg, force)
//	render.NoContent(w, r)
//}
