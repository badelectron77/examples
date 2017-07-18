package wang.wangxinarhat.rxandroidsamples.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import wang.wangxinarhat.rxandroidsamples.R;
import wang.wangxinarhat.rxandroidsamples.adapter.LogAdapter;

/**
 * Created by wang on 2016/4/9.
 */
public class ConcurrencySchedulersFragment extends BaseFragment {

    @Bind(R.id.progress)
    ProgressBar progress;
    @Bind(R.id.recycler)
    RecyclerView recycler;
    private List<String> mLogs;
    private LogAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_concurrency_schedulers, container, false);
        ButterKnife.bind(this, view);


        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        recycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        mLogs = new ArrayList<>();
        mAdapter = new LogAdapter(mLogs);
        recycler.setAdapter(mAdapter);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @OnClick(R.id.btn)
    public void onClick() {

        progress.setVisibility(View.VISIBLE);
        mLogs.clear();
        mAdapter.notifyDataSetChanged();

        log("点击事件");

        subscription = getObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(getObserver());


    }

    private Observer<Integer> getObserver() {
        return new Observer<Integer>() {
            @Override
            public void onCompleted() {
                log("观察者 onCompleted");
                progress.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onError(Throwable e) {
                progress.setVisibility(View.INVISIBLE);
                log("观察者 onError  message  :  " + e.getMessage());


            }

            @Override
            public void onNext(Integer integer) {
                log("观察者 onNext  return  :  " + integer);
            }
        };
    }

    private Observable<Integer> getObservable() {
        return Observable.just(22).map(new Func1<Integer, Integer>() {
            @Override
            public Integer call(Integer integer) {
                log("可观察对象 发射");
                doSomeLongOperation();
                return integer;
            }
        });
    }

    private void doSomeLongOperation() {

        log("耗时操作");

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Timber.d("Operation was interrupted");
        }

    }


    private void log(String logMsg) {

        if (isCurrentlyOnMainThread()) {
            mLogs.add(logMsg + " (main thread) ");
            mAdapter.notifyDataSetChanged();

        } else {

            mLogs.add(logMsg + " (NOT main thread) ");

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    mAdapter.notifyDataSetChanged();
                }
            });

        }
    }

    private boolean isCurrentlyOnMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }
}
