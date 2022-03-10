package cn.numeron.stateful.livedata

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/** 把[LiveData]转换成[StatefulLiveData] */
fun <T> LiveData<T>.toStateful(): StatefulLiveData<T> {
    val statefulLiveData = StatefulLiveData<T>()
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

/** 把[Flow]转换成[StatefulLiveData]，并捕获[Flow]中的异常 */
fun <T> Flow<T>.toStatefulLiveData(scope: CoroutineScope): StatefulLiveData<T> {
    return object : StatefulLiveData<T>() {

        private var collectJob: Job? = null

        override fun onActive() {
            super.onActive()
            collectJob = scope.launch {
                catch { cause ->
                    postFailure(cause)
                }.collectLatest {
                    if (it == null) {
                        postEmpty()
                    } else {
                        postSuccess(it)
                    }
                }
            }
        }

        override fun onInactive() {
            collectJob?.cancel()
            super.onInactive()
        }
    }
}