package cn.numeron.stateful.livedata

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import com.numeron.android.MainThreadExecutor
import java.util.*

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

    private val observers = LinkedList<StatefulObserver<*>>()

    constructor(value: T) : this() {
        setValue(Stateful(value = value, emptyMessage = empty))
    }

    override fun getValue(): Stateful<T> {
        return super.getValue() ?: Stateful(value = null, emptyMessage = empty)
    }

    override fun observe(owner: LifecycleOwner, observer: Observer<in Stateful<T>>) {
        super.observe(owner, observer)
        if (observer is StatefulObserver<*>) {
            observers.add(observer)
        }
    }

    override fun observeForever(observer: Observer<in Stateful<T>>) {
        super.observeForever(observer)
        if (observer is StatefulObserver<*>) {
            observers.add(observer)
        }
    }

    override fun removeObserver(observer: Observer<in Stateful<T>>) {
        super.removeObserver(observer)
        if (observer is StatefulObserver<*>) {
            observers.remove(observer)
        }
    }

    fun postLoading(progress: Float) {
        postLoading(this.loading, progress)
    }

    @JvmOverloads
    fun postLoading(message: String = this.loading, progress: Float = -1f) {
        if (MainThreadExecutor.isMainThread) {
            observers.map(StatefulObserver<*>::callback)
                    .forEach {
                        it.onLoading(message, progress)
                    }
        } else {
            MainThreadExecutor.execute {
                postLoading(message, progress)
            }
        }
    }

    fun postSuccess(value: T) {
        postValue(getValue().copy(value = value))
    }

    fun postFailure(cause: Throwable, message: String = this.failure) {
        if (MainThreadExecutor.isMainThread) {
            observers
                    .map(StatefulObserver<*>::callback)
                    .forEach {
                        it.onFailure(message, cause)
                    }
        } else {
            MainThreadExecutor.execute {
                postFailure(cause, message)
            }
        }
    }

    fun postFailure(cause: Throwable) {
        postFailure(cause, failure)
    }

    fun postMessage(message: String) {
        if (MainThreadExecutor.isMainThread) {
            observers.map(StatefulObserver<*>::callback)
                    .forEach {
                        it.onMessage(message)
                    }
        } else {
            MainThreadExecutor.execute {
                postMessage(message)
            }
        }
    }

    fun postEmpty(message: String = empty) {
        if (MainThreadExecutor.isMainThread) {
            getValue().emptyMessage = message
            observers.map(StatefulObserver<*>::callback)
                    .forEach {
                        it.onEmpty(message)
                    }
        } else {
            MainThreadExecutor.execute {
                postEmpty(message)
            }
        }
    }

    override fun postValue(value: Stateful<T>) {
        if (MainThreadExecutor.isMainThread) {
            setValue(value)
        } else {
            MainThreadExecutor.execute(PostRunnable(value))
        }
    }

    fun <R> map(function: (T) -> R): LiveData<R> {
        return object : LiveData<R>(), Observer<Stateful<T>> {

            override fun onActive() {
                this@StatefulLiveData.observeForever(this)
            }

            override fun onInactive() {
                this@StatefulLiveData.removeObserver(this)
            }

            override fun onChanged(statefulValue: Stateful<T>) {
                val tValue = statefulValue.value ?: return
                value = function(tValue)
            }
        }
    }

    fun <R> switchMap(function: (T) -> LiveData<R>): LiveData<R> {
        return object : LiveData<R>(), Observer<Stateful<T>> {

            private var rLiveData: LiveData<R>? = null

            private val rObserver = Observer(::setValue)

            override fun onActive() {
                this@StatefulLiveData.observeForever(this)
                rLiveData?.observeForever(rObserver)
            }

            override fun onInactive() {
                rLiveData?.removeObserver(rObserver)
                this@StatefulLiveData.removeObserver(this)
            }

            override fun onChanged(statefulValue: Stateful<T>) {
                val tValue = statefulValue.value ?: return
                rLiveData?.removeObserver(rObserver)
                rLiveData = function(tValue)
                rLiveData?.observeForever(rObserver)
            }
        }
    }

    private inner class PostRunnable(private val value: Stateful<T>) : Runnable {
        override fun run() = setValue(value)
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

    }

}