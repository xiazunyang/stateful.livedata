package cn.numeron.stateful.livedata

import androidx.lifecycle.Observer

class StatefulObserver<T>(internal val callback: StatefulCallback<T>) : Observer<Stateful<T>> {

    override fun onChanged(stateful: Stateful<T>) {
        if (stateful.value == null) {
            callback.onEmpty(stateful.emptyMessage)
        } else {
            callback.onSuccess(stateful.value)
        }
    }

}
