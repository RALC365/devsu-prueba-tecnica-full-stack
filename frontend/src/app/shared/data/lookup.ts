import { Resource, Signal, computed } from '@angular/core';

export interface Lookup<T> {
  readonly items: readonly T[];
  readonly loading: boolean;
  readonly error: string | null;
}

export function toLookup<T>(resource: Resource<readonly T[]>): Signal<Lookup<T>> {
  return computed(() => ({
    items: resource.hasValue() ? resource.value() : [],
    loading: resource.isLoading(),
    error: resource.error()?.message ?? null,
  }));
}
