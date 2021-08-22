package cn.numeron.stateful.livedata

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

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