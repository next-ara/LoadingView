package com.next.view.loading;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.animation.LinearInterpolator;

/**
 * ClassName:加载渲染器基类
 *
 * @author Afton
 * @time 2023/10/30
 * @auditor
 */
abstract public class LoadingRenderer {

    private static final long ANIMATION_DURATION = 1333;
    private static final float DEFAULT_SIZE = 56.0f;

    private final ValueAnimator.AnimatorUpdateListener mAnimatorUpdateListener
            = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            computeRender((float) animation.getAnimatedValue());
            invalidateSelf();
        }
    };

    protected final Rect mBounds = new Rect();

    private Drawable.Callback mCallback;
    private ValueAnimator mRenderAnimator;

    protected long mDuration;

    protected float mWidth;
    protected float mHeight;

    public LoadingRenderer(Context context, AttributeSet attrs) {
        initParams(context);
        setupAnimators();
    }

    @Deprecated
    protected void draw(Canvas canvas, Rect bounds) {
    }

    protected void draw(Canvas canvas) {
        draw(canvas, this.mBounds);
    }

    protected abstract void computeRender(float renderProgress);

    protected abstract void setAlpha(int alpha);

    protected abstract void setColorFilter(ColorFilter cf);

    protected abstract void reset();

    protected void addRenderListener(Animator.AnimatorListener animatorListener) {
        this.mRenderAnimator.addListener(animatorListener);
    }

    void start() {
        this.reset();
        this.mRenderAnimator.addUpdateListener(this.mAnimatorUpdateListener);

        this.mRenderAnimator.setRepeatCount(ValueAnimator.INFINITE);
        this.mRenderAnimator.setDuration(this.mDuration);
        this.mRenderAnimator.start();
    }

    void stop() {
        this.mRenderAnimator.removeUpdateListener(this.mAnimatorUpdateListener);

        this.mRenderAnimator.setRepeatCount(0);
        this.mRenderAnimator.setDuration(0);
        this.mRenderAnimator.end();
    }

    boolean isRunning() {
        return this.mRenderAnimator.isRunning();
    }

    void setCallback(Drawable.Callback callback) {
        this.mCallback = callback;
    }

    void setBounds(Rect bounds) {
        this.mBounds.set(bounds);
    }

    private void initParams(Context context) {
        this.mWidth = DEFAULT_SIZE;
        this.mHeight = DEFAULT_SIZE;

        this.mDuration = ANIMATION_DURATION;
    }

    private void setupAnimators() {
        this.mRenderAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
        this.mRenderAnimator.setRepeatCount(ValueAnimator.INFINITE);
        this.mRenderAnimator.setRepeatMode(ValueAnimator.REVERSE);
        this.mRenderAnimator.setDuration(this.mDuration);

        this.mRenderAnimator.setInterpolator(new LinearInterpolator());
        this.mRenderAnimator.addUpdateListener(this.mAnimatorUpdateListener);
    }

    private void invalidateSelf() {
        this.mCallback.invalidateDrawable(null);
    }
}