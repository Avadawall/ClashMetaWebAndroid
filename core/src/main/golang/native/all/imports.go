package all

import (
	_ "cfa/native/app"
	_ "cfa/native/common"
	_ "cfa/native/config"
	_ "cfa/native/delegate"
	_ "cfa/native/platform"
	_ "cfa/native/proxy"
	_ "cfa/native/route"
	_ "cfa/native/tun"
	_ "cfa/native/tunnel"

	_ "golang.org/x/sync/semaphore"

	_ "github.com/Dreamacro/clash/log"
)
