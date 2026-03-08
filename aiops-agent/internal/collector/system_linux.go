//go:build linux

package collector

import "syscall"

type runtimeStatfs = syscall.Statfs_t

func runtimeStatFS(path string, stat *runtimeStatfs) error {
	return syscall.Statfs(path, stat)
}
