package cn.numeron.stateful.livedata

import androidx.lifecycle.Observer
import cn.numeron.common.State

class StatefulObserver<T>(private val callback: StatefulCallback<T>) : Observer<Stateful<T>> {

    private var latestState: State? = null
    private var latestVersion = Int.MIN_VALUE

    override fun onChanged(stateful: Stateful<T>) {
        if (stateful.state != latestState) {
            //当前状态与上次记录的状态不一致时，执行状态回调
            callback.onStateChanged(stateful.state)
        }
        if (stateful.version > latestVersion) {
            //当前版本号大于上次记录的版本号时，则执行消息回调
            callback.onMessage(stateful.message!!)
        } else when (stateful.state) {
            //如果不是消息更新了，则根据状态执行回调
            State.Empty -> callback.onEmpty(stateful.message!!)
            State.Loading -> callback.onLoading(stateful.message!!, stateful.progress)
            State.Failure -> callback.onFailure(stateful.message!!, stateful.failure!!)
            State.Success -> callback.onSuccess(stateful.value!!)
        }
        latestState = stateful.state
        latestVersion = stateful.version
    }

}
