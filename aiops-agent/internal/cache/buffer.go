package cache

import "sync"

type Buffer[T any] struct {
	mu    sync.Mutex
	items []T
}

func New[T any]() *Buffer[T] {
	return &Buffer[T]{}
}

func (b *Buffer[T]) Push(item T) {
	b.mu.Lock()
	defer b.mu.Unlock()
	b.items = append(b.items, item)
}

func (b *Buffer[T]) Drain() []T {
	b.mu.Lock()
	defer b.mu.Unlock()
	items := b.items
	b.items = nil
	return items
}

func (b *Buffer[T]) Snapshot() []T {
	b.mu.Lock()
	defer b.mu.Unlock()
	dup := make([]T, len(b.items))
	copy(dup, b.items)
	return dup
}
