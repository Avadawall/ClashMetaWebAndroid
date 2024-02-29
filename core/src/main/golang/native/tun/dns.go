package tun

import (
	"net"

	"github.com/Dreamacro/clash/dns"

	D "github.com/miekg/dns"
)

func shouldHijackDns(dns net.IP, target net.IP, targetPort int) bool {
	if targetPort != 53 {
		return false
	}

	return net.IPv4zero.Equal(dns) || target.Equal(dns)
}

func relayDns(payload []byte) ([]byte, error) {
	msg := &D.Msg{}
	if err := msg.Unpack(payload); err != nil {
		return nil, err
	}

	r, err := dns.ServeDNSWithDefaultServer(msg)
	if err != nil {
		return nil, err
	}

	r.SetRcode(msg, r.Rcode)

	return r.Pack()
}
