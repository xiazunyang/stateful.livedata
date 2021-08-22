package cn.numeron.stateful.livedata

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import cn.numeron.android.AppRuntime
import java.util.*

class StatefulLiveData<T> @JvmOverloads constructor(value: T? = null) : MediatorLiveData<Stateful<T>>() {

    val value: T?
        @JvmName("value")
        get() = getValue().value

    val requireValue: T
        @JvmName("requireValue")
        get() = value!!

    private val emptyMessage: String
    private val loadingMessage: String
    private val failureMessage: String
    private val observers = LinkedList<StatefulObserver<*>>()

    init {
        val context = AppRuntime.context
        emptyMessage = context.getString(R.string.stateful_empty_message)
        loadingMessage = context.getString(R.string.stateful_loading_message)
        failureMessage = context.getString(R.string.stateful_failure_message)
        setValue(Stateful(value, emptyMessage))
    }

    override fun getValue(): Stateful<T> {
        return super.getValue() ?: Stateful(value = null, emptyMessage = emptyMessage)
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
        postLoading(this.loadingMessage, progress)
    }

    @JvmOverloads
    fun postLoading(message: String = this.loadingMessage, progress: Float = -1f) {
        if (AppRuntime.mainExecutor.isMainThread) {
            observers.map(StatefulObserver<*>::callback)
                .forEach {
                    it.onLoading(message, progress)
                }
        } else {
            AppRuntime.mainExecutor.execute {
                postLoading(message, progress)
            }
        }
    }

    fun postSuccess(value: T) {
        postValue(getValue().copy(value = value))
    }

    fun postFailure(cause: Throwable, message: String = this.failureMessage) {
        if (AppRuntime.mainExecutor.isMainThread) {
            observers
                .map(StatefulObserver<*>::callback)
                .forEach {
                    it.onFailure(message, cause)
                }
        } else {
            AppRuntime.mainExecutor.execute {
                postFailure(cause, message)
            }
        }
    }

    fun postFailure(cause: Throwable) {
        postFailure(cause, failureMessage)
    }

    fun postMessage(message: String) {
        if (AppRuntime.mainExecutor.isMainThread) {
            observers.map(StatefulObserver<*>::callback)
                .forEach {
                    it.onMessage(message)
                }
        } else {
            AppRuntime.mainExecutor.execute {
                postMessage(message)
            }
        }
    }

    fun postEmpty(message: String = emptyMessage) {
        if (AppRuntime.mainExecutor.isMainThread) {
            getValue().emptyMessage = message
            observers.map(StatefulObserver<*>::callback)
                .forEach {
                    it.onEmpty(message)
                }
        } else {
            AppRuntime.mainExecutor.execute {
                postEmpty(message)
            }
        }
    }

    override fun postValue(value: Stateful<T>) {
        if (AppRuntime.mainExecutor.isMainThread) {
            setValue(value)
        } else {
            AppRuntime.mainExecutor.execute(PostRunnable(value))
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

}