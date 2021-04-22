# StatefulLiveData
辅助android开发者在中间层向view层传递数据和状态的LiveData，可扩展普通的LiveData为StatefulLiveData。

# 当前最新版本号：[![](https://jitpack.io/v/cn.numeron/statefulLiveData.svg)](https://jitpack.io/#cn.numeron/statefulLiveData)

### 特性
* 辅助android开发者在中间层向view层传递数据和状态的LiveData。
* kotlin协程项目推荐使用
* 不用考虑在中间层切线程，直接通过`postValue`方法把数据提交到`StatefulLiveData`中，观察`StatefulLiveData`的View层代码会自动切换到主线程处理数据。
* 在子线程中发生的异常会提交到`StatefulLiveData`中，由`view`层进行详尽的分析与处理。
* 子线程可以随时将自己处理数据的进度与状态提交到`StatefulLiveData`中，`view`层可及时做出响应。
* 在任意一个`LiveData`上通过`toStateful()`扩展方法为升级为一个新的`StatefulLiveData`实例，原`LiveData`的新数据会通过`StatefulLiveData`的`onSuccess`方法响应。

* 详细用法参考： [掘金-关于LiveData的最佳使用方法](https://juejin.im/post/6844904150031941639)


### 引入
1.  在你的android工程的根目录下的build.gradle文件中的适当的位置添加以下代码：
```groovy
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```
2. 在你的android工程中对应的android模块的build.gradle文件中的适当位置添加以下代码：
```groovy
implementation 'cn.numeron:stateful.livedata:latest_version'
```
