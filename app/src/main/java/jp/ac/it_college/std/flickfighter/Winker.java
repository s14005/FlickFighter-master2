package jp.ac.it_college.std.flickfighter;


import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Winker {

    private Handler mHandler;
    private View view;
    private ScheduledExecutorService mScheduledExecutor;
    final long awaitTime = 5 * 1000;

    public Winker(View view) {
        this.view = view;
        mHandler = new Handler(Looper.getMainLooper());
    }

    public void stopWink() {
        try {
            mScheduledExecutor.shutdown();

            if(!mScheduledExecutor.awaitTermination(awaitTime, TimeUnit.MILLISECONDS)){
                // タイムアウトした場合、全てのスレッドを中断(interrupted)してスレッドプールを破棄する。
                mScheduledExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            // awaitTerminationスレッドがinterruptedした場合も、全てのスレッドを中断する
            System.out.println("awaitTermination interrupted: " + e);
            mScheduledExecutor.shutdownNow();
        }

    }

    public void startWink() {
        mScheduledExecutor = Executors.newScheduledThreadPool(2);

        mScheduledExecutor.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        view.setVisibility(View.VISIBLE);

                        // HONEYCOMBより前のAndroid SDKがProperty Animation非対応のため
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                            animateAlpha();
                        }
                    }
                });
            }


            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            private void animateAlpha() {

                // 実行するAnimatorのリスト
                List<Animator> animatorList = new ArrayList<Animator>();

                // alpha値を0から1へ1000ミリ秒かけて変化させる。
                ObjectAnimator animeFadeIn = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
                animeFadeIn.setDuration(1000);

                // alpha値を1から0へ600ミリ秒かけて変化させる。
                ObjectAnimator animeFadeOut = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
                animeFadeOut.setDuration(600);

                // 実行対象Animatorリストに追加。
                animatorList.add(animeFadeIn);
                animatorList.add(animeFadeOut);

                final AnimatorSet animatorSet = new AnimatorSet();

                // リストの順番に実行
                animatorSet.playSequentially(animatorList);

                animatorSet.start();
            }
        }, 0, 1700, TimeUnit.MILLISECONDS);
    }
}
