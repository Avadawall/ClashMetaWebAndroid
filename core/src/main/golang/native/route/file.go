package route

import (
	"os"
	"path"
	"sync"

	"github.com/Dreamacro/clash/config"
)

var (
	locker     sync.Mutex
	GlobalPath string
)

func readConfig(processingPath string) (*config.RawConfig, error) {
	locker.Lock()
	defer locker.Unlock()

	configPath := path.Join(processingPath, "config.yaml")
	configData, err := os.ReadFile(configPath)
	if err != nil {
		return nil, err
	}
	rawConfig, err := config.UnmarshalRawConfig(configData)
	if err != nil {
		return nil, err
	}
	return rawConfig, nil
}
