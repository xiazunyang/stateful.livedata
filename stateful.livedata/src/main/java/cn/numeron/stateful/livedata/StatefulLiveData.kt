package cn.numeron.stateful.livedata

import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import cn.numeron.common.State

class StatefulLiveData<T> @JvmOverloads constructor(
        private val loading: String = "正在加载",
        private val failure: String = "加载失败",
        private val empty: String = "没有数据"
) : MediatorLiveData<Stateful<T>>() {

    val value: T?
        @JvmName("value")
        get() = getValue().value

    val requireValue: T
        @JvmName("requireValue")
        get() = value!!

    constructor(value: T) : this() {
        setValue(Stateful(State.Success, value))
    }

    override fun getValue(): Stateful<T> {
        return super.getValue() ?: Stateful(State.Empty)
    }

    fun postLoading(progress: Float) {
        postLoading(this.loading, progress)
    }

    @JvmOverloads
    fun postLoading(message: String = this.loading, progress: Float = -1f) {
        postValue(getValue().copy(state = State.Loading, progress = progress, message = message))
    }

    fun postSuccess(value: T) {
        postValue(getValue().copy(state = State.Success, value = value))
    }

    fun postFailure(cause: Throwable, message: String = this.failure) {
        postValue(getValue().copy(state = State.Failure, failure = cause, message = message))
    }

    fun postFailure(cause: Throwable) {
        postFailure(cause, failure)
    }

    fun postMessage(message: String) {
        postValue(getValue().copy(message = message, version = getValue().version + 1))
    }

    fun postEmpty(message: String = empty) {
        postValue(getValue().copy(state = State.Empty, message = message))
    }

    @Synchronized
    //使用同步锁，保证快速调用的顺序也是一致的
    override fun postValue(value: Stateful<T>) {
        if (isMainThread) {
            super.setValue(value)
        } else {
            super.postValue(value)
        }
    }

    companion object {

        fun <T> LiveData<T>.toStateful(
                loading: String = "正在加载",
                failure: String = "加载失败",
                empty: String = "没有数据"): StatefulLiveData<T> {
            val statefulLiveData = StatefulLiveData<T>(loading, failure, empty)
            val observer = Observer<T?> {
                if (it == null) {
                    statefulLiveData.postEmpty()
                } else {
                    statefulLiveData.postSuccess(it)
                }
            }
            statefulLiveData.addSource(this, observer)
            return statefulLiveData
        }

        private val isMainThread: Boolean
            get() = Looper.myLooper() == Looper.getMainLooper()

    }

}