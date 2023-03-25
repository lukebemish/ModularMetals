package dev.lukebemish.modularmetals.data.filter.string

interface StringFilterFinder<T> {
    boolean isLocation(T thing, String location)
}
