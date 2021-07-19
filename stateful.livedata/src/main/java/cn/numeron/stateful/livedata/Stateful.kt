package cn.numeron.stateful.livedata

data class Stateful<T>(
        val value: T? = null,
        var emptyMessage: String
)