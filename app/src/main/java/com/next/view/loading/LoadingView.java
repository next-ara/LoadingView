package com.next.view.loading;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

/**
 * ClassName:加载控件类
 *
 * @author Afton
 * @time 2023/10/30
 * @auditor
 */
public class LoadingView extends androidx.appcompat.widget.AppCompatImageView {

    private LoadingDrawable mLoadingDrawable;

    public LoadingView(Context context) {
        super(context);
    }

    public LoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.initAttrs(context, attrs);
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        try {
            LoadingRenderer loadingRenderer = new LevelLoadingRenderer(context, attrs);
            this.setLoadingRenderer(loadingRenderer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setLoadingRenderer(LoadingRenderer loadingRenderer) {
        this.mLoadingDrawable = new LoadingDrawable(loadingRenderer);
        this.setImageDrawable(this.mLoadingDrawable);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.startAnimation();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.stopAnimation();
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);

        final boolean visible = visibility == VISIBLE && this.getVisibility() == VISIBLE;
        if (visible) {
            this.startAnimation();
        } else {
            this.stopAnimation();
        }
    }

    private void startAnimation() {
        if (this.mLoadingDrawable != null) {
            this.mLoadingDrawable.start();
        }
    }

    private void stopAnimation() {
        if (this.mLoadingDrawable != null) {
            this.mLoadingDrawable.stop();
        }
    }
}