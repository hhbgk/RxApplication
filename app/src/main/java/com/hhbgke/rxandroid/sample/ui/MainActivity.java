package com.hhbgke.rxandroid.sample.ui;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hhbgke.rxandroid.sample.R;
import com.hhbgke.rxandroid.sample.bean.Singer;
import com.hhbgke.rxandroid.sample.bean.Song;
import com.hhbgke.rxandroid.sample.tool.DataManager;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    private static String tag = MainActivity.class.getSimpleName();
    private TextView mTextView;
    private String mString = "";
    private ProgressBar mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextView = (TextView) findViewById(R.id.text_view);
        mProgress = (ProgressBar) findViewById(R.id.progress);
        test_basic();
        //test_just();
        //test_from();
        //test_scheduler();
        //test_map();
        //test_filter_map_take();
        //test_flatMap();
    }

    /**
     * 作者：扔物线
     * reference: http://gank.io/post/560e15be2dca930e00da1083
     *
     * 与传统观察者模式不同， RxJava 的事件回调方法除了普通事件 onNext() （相当于 onClick() / onEvent()）之外，还定义了两个特殊的事件：onCompleted() 和 onError()。
     *---- onCompleted(): 事件队列完结。RxJava 不仅把每个事件单独处理，还会把它们看做一个队列。
     *     RxJava 规定，当不会再有新的 onNext() 发出时，需要触发 onCompleted() 方法作为标志。
     *---- onError(): 事件队列异常。在事件处理过程中出异常时，onError() 会被触发，同时队列自动终止，不允许再有事件发出。
     *在一个正确运行的事件序列中, onCompleted() 和 onError() 有且只有一个，并且是事件序列中的最后一个。需要注意的是，onCompleted() 和 onError() 二者也是互斥的，
     * 即在队列中调用了其中一个，就不应该再调用另一个。
     */
    private void test_basic() {
        Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                subscriber.onNext("Hello ");
                subscriber.onNext("RxJava");
                subscriber.onCompleted();
            }
        }).subscribe(new Observer<String>() {
            @Override
            public void onCompleted() {
                Toast.makeText(MainActivity.this, "Completed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(String s) {
                mString += s;
                mTextView.setText(mString);
            }
        });
    }

    /**
     * 作者：扔物线
     * reference: http://gank.io/post/560e15be2dca930e00da1083
     *
     * just(T...): 将传入的参数依次发送出来。
     */
    private void test_just() {
        Observable.just("Hello ", "RxJava").subscribe(new Observer<String>() {
            @Override
            public void onCompleted() {
                Toast.makeText(MainActivity.this, "test1 Completed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(String s) {
                mString += s;
                mTextView.setText(mString);
            }
        });
    }

    /**
     * 作者：扔物线
     * reference: http://gank.io/post/560e15be2dca930e00da1083
     *
     * 将传入的数组或 Iterable 拆分成具体对象后，依次发送出来。
     */
    private void test_from() {
        List<String> list = new ArrayList<>();
        list.add("Hello ");
        list.add("RxJava");

        Observable.from(list).subscribe(new Observer<String>() {
            @Override
            public void onCompleted() {
                Toast.makeText(MainActivity.this, "test2 Completed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(String s) {
                mString += s;
                mTextView.setText(mString);
            }
        });
    }

    /**
     * 作者：扔物线
     * reference: http://gank.io/post/560e15be2dca930e00da1083
     *
     * 在RxJava 中，Scheduler ——调度器，相当于线程控制器，RxJava 通过它来指定每一段代码应该运行在什么样的线程。
     * RxJava 已经内置了几个 Scheduler ，它们已经适合大多数的使用场景：
     *---- Schedulers.immediate(): 直接在当前线程运行，相当于不指定线程。这是默认的 Scheduler。
     *---- Schedulers.newThread(): 总是启用新线程，并在新线程执行操作。
     *---- Schedulers.io(): I/O 操作（读写文件、读写数据库、网络信息交互等）所使用的 Scheduler。
     *     行为模式和 newThread() 差不多，区别在于 io() 的内部实现是是用一个无数量上限的线程池，可以重用空闲的线程，因此多数情况下 io() 比 newThread() 更有效率。
     *     不要把计算工作放在 io() 中，可以避免创建不必要的线程。
     *---- Schedulers.computation(): 计算所使用的 Scheduler。这个计算指的是 CPU 密集型计算，即不会被 I/O 等操作限制性能的操作，例如图形的计算。
     *     这个 Scheduler 使用的固定的线程池，大小为 CPU 核数。不要把 I/O 操作放在 computation() 中，否则 I/O 操作的等待时间会浪费 CPU。
     *另外， Android 还有一个专用的 AndroidSchedulers.mainThread()，它指定的操作将在 Android 主线程运行。
     *有了这几个 Scheduler ，就可以使用 subscribeOn() 和 observeOn() 两个方法来对线程进行控制了。
     *--- subscribeOn(): 指定 subscribe() 所发生的线程，即 Observable.OnSubscribe 被激活时所处的线程。或者叫做事件产生的线程。
     *--- observeOn(): 指定 Subscriber 所运行在的线程。或者叫做事件消费的线程。
     *
     * 显示图片
     * 后台线程取数据，主线程显示
     * 加载图片将会发生在 IO 线程，而设置图片则被设定在了主线程。这就意味着，即使加载图片耗费了几十甚至几百毫秒的时间，也不会造成丝毫界面的卡顿。
     */
    private void test_scheduler() {

        Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                SystemClock.sleep(3000);
                final int background = R.mipmap.ic_launcher;
                subscriber.onNext(background);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io())
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        mProgress.setVisibility(View.VISIBLE);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread()).
                subscribe(new Observer<Integer>() {
                    @Override
                    public void onCompleted() {
                        mProgress.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Integer integer) {
                        mTextView.setBackgroundResource(integer);
                    }
                });
    }

    /**
     * 作者：扔物线
     * reference: http://gank.io/post/560e15be2dca930e00da1083
     *
     * map(): (对)事件对象的直接变换
     */
    private void test_map() {
        final int background = R.mipmap.ic_launcher;
        Observable.just(background).map(new Func1<Integer, Drawable>() {
            @Override
            public Drawable call(Integer integer) {
                SystemClock.sleep(3000);
                return getResources().getDrawable(integer);
            }
        }).subscribeOn(Schedulers.io()).doOnSubscribe(new Action0() {
            @Override
            public void call() {
                mProgress.setVisibility(View.VISIBLE);
            }
        }).
                observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Drawable>() {
                    @Override
                    public void onCompleted() {
                        mProgress.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Drawable drawable) {
                        mTextView.setBackground(drawable);
                    }
                });
    }

    /**
     * filter:输出它接收到的且满足设定条件的元素。
     * take:输出最多指定数量的元素, 如果指定的数量超出实际的数量，结果只会输出实际的大小
     */
    private void test_filter_map_take() {
        Integer[] data = {1,3,5,7,9,12,15,17,18,19,20,21};
        Observable.from(data).filter(new Func1<Integer, Boolean>() {
            @Override
            public Boolean call(Integer integer) {
                return integer % 3 == 0;
            }
        }).map(new Func1<Integer, String>() {
            @Override
            public String call(Integer integer) {
                return String.valueOf(integer) + " ";
            }
        }).take(15).subscribe(new Observer<String>() {
            @Override
            public void onCompleted() {
                mTextView.setText(mString);
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(String s) {
                mString += s;
            }
        });
    }

    /**
     * 作者：扔物线
     * reference: http://gank.io/post/560e15be2dca930e00da1083
     *
     * flatMap() 和 map() 有一个相同点：它也是把传入的参数转化之后返回另一个对象。但需要注意，和 map() 不同的是，
     * flatMap() 中返回的是个 Observable 对象，并且这个 Observable 对象并不是被直接发送到了 Subscriber 的回调方法中。
     *
     * flatMap() 的原理是这样的：
     * 1. 使用传入的事件对象创建一个 Observable 对象；
     * 2. 并不发送这个 Observable, 而是将它激活，于是它开始发送事件；
     * 3. 每一个创建出来的 Observable 发送的事件，都被汇入同一个 Observable ，而这个 Observable 负责将这些事件统一交给 Subscriber 的回调方法。
     * 这三个步骤，把事件拆成了两级，通过一组新创建的 Observable 将初始的对象『铺平』之后通过统一路径分发了下去。
     * 而这个『铺平』就是 flatMap() 所谓的 flat。
     */
    private void test_flatMap() {
        Observable.from(DataManager.getInstance().getData(5, 5))

                .flatMap(new Func1<Singer, Observable<Song>>() {
                    @Override
                    public Observable<Song> call(Singer singer) {
                        SystemClock.sleep(1000);
                        return Observable.from(singer.getSongs());
                    }
                }).subscribeOn(Schedulers.io()).doOnSubscribe(new Action0() {
            @Override
            public void call() {
                mProgress.setVisibility(View.VISIBLE);
            }
        })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Song>() {
                    @Override
                    public void onCompleted() {
                        mTextView.setMovementMethod(new ScrollingMovementMethod());
                        mProgress.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Song song) {
                        //Log.i(tag, "Song id=" + song.getId() + ", song title=" + song.getTitle());
                        mString += song.getId() + ", " + song.getTitle() + "\n";
                        mTextView.setText(mString);
                    }
                });
    }
}
