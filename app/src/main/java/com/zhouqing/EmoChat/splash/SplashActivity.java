package com.zhouqing.EmoChat.splash;

import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.LinearLayout;

import com.zhouqing.EmoChat.R;
import com.zhouqing.EmoChat.common.ui.BaseActivity;


public class SplashActivity extends BaseActivity implements SplashContract.View {

    private LinearLayout ll_splash;
    private SplashContract.Presenter mPresenter;

    protected void initUi() {
        setContentView(R.layout.activity_splash);
        ll_splash = (LinearLayout) findViewById(R.id.ll_splash);
        mPresenter = new SplashPresenter(this,this);
        initAnimation();

    }



    private void initAnimation() {
        AlphaAnimation animation = new AlphaAnimation(0, 1);
        animation.setDuration(1000);
        ll_splash.setAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                mPresenter.endSplash();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        animation.start();
    }



    @Override
    public void setPresenter(SplashContract.Presenter presenter) {
        mPresenter = presenter;
    }
}
