package com.richfit.common_lib.rxutils;

import android.support.annotation.NonNull;

import io.reactivex.Flowable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;

/**
 * 用RxJava2.0实现一个简单的EventBus.
 * 注意AsyncSubject,BehaviorSubject,PublishSubject,ReplaySubject,
 * 在2.x中属于Observable家族，不支持被压。而AsyncProcessor，BehaviorProcessor,
 * PublishProcessor,ReplayProcess支持被压。
 * 至于上面4中Subject的意义和使用注意事项我推进两篇文章:
 * 1. http://blog.csdn.net/jdsjlzx/article/details/51502781
 *
 * 2. http://blog.csdn.net/PrototypeZ/article/details/51113828
 * Created by monday on 2016/10/24.
 */

public class SimpleRxBus {

    private final FlowableProcessor<Object> mBus;

    private SimpleRxBus() {
        mBus = PublishProcessor.create().toSerialized();
    }

    private static class Holder {
        private static SimpleRxBus instance = new SimpleRxBus();
    }

    public static SimpleRxBus getInstance() {
        return Holder.instance;
    }

    public void post(@NonNull Object obj) {
        mBus.onNext(obj);
    }

    public <T> Flowable<T> register(Class<T> clz) {
        return mBus.ofType(clz);
    }

    public void unregisterAll() {
        //会将所有由mBus 生成的 Flowable 都置  completed 状态  后续的 所有消息  都收不到了
        mBus.onComplete();
    }

    public boolean hasSubscribers() {
        return mBus.hasSubscribers();
    }
}
