package cn.numeron.stateful.livedata;

import cn.numeron.common.State;

import org.jetbrains.annotations.NotNull;

public interface StatefulCallback<T> {

    void onSuccess(@NotNull T value);

    void onLoading(@NotNull String message, float progress);

    void onFailure(@NotNull String message, @NotNull Throwable cause);

    default void onEmpty(@NotNull String message) {
    }

    default void onMessage(@NotNull String message) {
    }

    default void onStateChanged(@NotNull State state) {
    }

}
