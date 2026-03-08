//go:build !linux

package collector

import "fmt"

type runtimeStatfs struct {
	Blocks uint64
	Bfree  uint64
}

func runtimeStatFS(_ string, _ *runtimeStatfs) error {
	return fmt.Errorf("statfs unsupported")
}
