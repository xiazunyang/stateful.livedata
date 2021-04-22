package cn.numeron.stateful.livedata

import cn.numeron.common.State

data class Stateful<T>(
        val state: State,
        val value: T? = null,
        val failure: Throwable? = null,
        val progress: Float = -1f,
        val message: String? = null,
        internal val version: Int = Int.MIN_VALUE
)