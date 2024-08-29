package com.next.view.loading;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;
import androidx.core.view.animation.PathInterpolatorCompat;

/**
 * ClassName:加载控件类
 *
 * @author Afton
 * @time 2023/10/30
 * @auditor
 */
public class LoadingView extends View {

    //默认圆环的宽度
    public static final float STROKE_WIDTH_DEFAULT = 6f;
    //默认加载条的颜色
    public static final int LOADING_COLOR_DEFAULT = Color.BLACK;
    //默认背景透明度
    public static final int BACKGROUND_ALPHA_DEFAULT = 40;
    //最大摆动角度
    public static final int MAX_SWING_ANGLE_DEFAULT = 180;
    //动画持续时间
    public static final int DURATION_DEFAULT = 1800;
    //淡入动画持续时间
    public static final int FADE_IN_DURATION_DEFAULT = 300;
    //淡出动画持续时间
    public static final int FADE_OUT_DURATION_DEFAULT = 150;
    //放大动画持续时间
    public static final int SCALE_DURATION_DEFAULT = 300;
    //缩小动画持续时间
    public static final int SHRINK_DURATION_DEFAULT = 150;

    //圆环的宽度
    private float strokeWidth;

    //加载条的颜色
    private int loadingColor;

    //前景画笔
    private Paint foregroundPaint;

    //背景画笔
    private Paint backgroundPaint;

    //起始角度动画
    private ValueAnimator startAngleAnimator;

    //摆动角度动画
    private ValueAnimator swingAngleAnimator;

    //淡入动画
    private ValueAnimator fadeInAnimator;

    //淡出动画
    private ValueAnimator fadeOutAnimator;

    //放大动画
    private ValueAnimator scaleAnimator;

    //缩小动画
    private ValueAnimator shrinkAnimator;

    //起始角度
    private int startAngle = 0;

    //摆动角度
    private int swingAngle = 0;

    //是否正在加载
    private boolean isLoading = false;

    public LoadingView(Context context) {
        super(context);

        //初始化
        init(context, null);
    }

    public LoadingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        //初始化
        init(context, attrs);
    }

    public LoadingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        //初始化
        init(context, attrs);
    }

    public LoadingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        //初始化
        init(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //绘制背景
        canvas.drawArc(this.strokeWidth, this.strokeWidth, this.getWidth() - this.strokeWidth, this.getHeight() - this.strokeWidth, 0, 360, false, this.backgroundPaint);
        //绘制加载条
        canvas.drawArc(this.strokeWidth, this.strokeWidth, this.getWidth() - this.strokeWidth, this.getHeight() - this.strokeWidth, this.startAngle, this.swingAngle, false, this.foregroundPaint);
    }

    /**
     * 开始加载
     */
    public void startLoading() {
        if (this.isLoading) {
            return;
        }

        this.isLoading = true;
        //启动动画
        this.startAnimation();
    }

    /**
     * 停止加载
     */
    public void stopLoading() {
        if (!this.isLoading) {
            return;
        }

        this.isLoading = false;
        //停止动画
        this.stopAnimation();
    }

    /**
     * 初始化
     *
     * @param context 上下文
     * @param attrs   属性
     */
    private void init(Context context, AttributeSet attrs) {
        //初始化属性
        this.initAttrs(context, attrs);
        //初始化画笔
        this.initPaint();
        //初始化动画
        this.initAnimator();
    }

    /**
     * 初始化属性
     *
     * @param context 上下文
     * @param attrs   属性
     */
    private void initAttrs(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.LoadingView);
            this.loadingColor = typedArray.getColor(R.styleable.LoadingView_loadingColor, LOADING_COLOR_DEFAULT);
            this.strokeWidth = typedArray.getDimension(R.styleable.LoadingView_loadingStrokeWidth, STROKE_WIDTH_DEFAULT);
            typedArray.recycle();
        } else {
            this.strokeWidth = STROKE_WIDTH_DEFAULT;
            this.loadingColor = LOADING_COLOR_DEFAULT;
        }

        //初始透明度为0
        this.setAlpha(0f);
    }

    /**
     * 初始化画笔
     */
    private void initPaint() {
        this.foregroundPaint = new Paint();
        this.foregroundPaint.setAntiAlias(true);
        this.foregroundPaint.setStrokeWidth(this.strokeWidth);
        this.foregroundPaint.setColor(this.loadingColor);
        this.foregroundPaint.setStyle(Paint.Style.STROKE);
        this.foregroundPaint.setStrokeCap(Paint.Cap.ROUND);

        this.backgroundPaint = new Paint();
        this.backgroundPaint.setAntiAlias(true);
        this.backgroundPaint.setStrokeWidth(this.strokeWidth);
        this.backgroundPaint.setColor(this.loadingColor);
        this.backgroundPaint.setAlpha(BACKGROUND_ALPHA_DEFAULT);
        this.backgroundPaint.setStyle(Paint.Style.STROKE);
        this.backgroundPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    /**
     * 初始化动画
     */
    private void initAnimator() {
        this.startAngleAnimator = ValueAnimator.ofInt(-90, 630);
        this.startAngleAnimator.setInterpolator(PathInterpolatorCompat.create(0.43f, 0.37f, 0.57f, 0.63f));
        this.startAngleAnimator.addUpdateListener(animation -> {
            this.startAngle = (int) animation.getAnimatedValue();
            invalidate();
        });

        this.swingAngleAnimator = ValueAnimator.ofInt(1, MAX_SWING_ANGLE_DEFAULT - 1);
        this.swingAngleAnimator.setInterpolator(new LinearInterpolator());
        this.swingAngleAnimator.addUpdateListener(animation -> {
            this.swingAngle = (int) animation.getAnimatedValue();
            if (this.swingAngle > MAX_SWING_ANGLE_DEFAULT / 2) {
                this.swingAngle = MAX_SWING_ANGLE_DEFAULT - this.swingAngle;
            }
        });

        this.fadeInAnimator = ValueAnimator.ofFloat(0f, 1f);
        this.fadeInAnimator.setInterpolator(PathInterpolatorCompat.create(0f, 0f, 0f, 1f));
        this.fadeInAnimator.addUpdateListener(animation -> {
            this.setAlpha((Float) animation.getAnimatedValue());
        });

        this.fadeOutAnimator = ValueAnimator.ofFloat(1f, 0f);
        this.fadeOutAnimator.setInterpolator(PathInterpolatorCompat.create(0f, 0f, 1f, 0f));
        this.fadeOutAnimator.addUpdateListener(animation -> {
            this.setAlpha((Float) animation.getAnimatedValue());
        });

        this.scaleAnimator = ValueAnimator.ofFloat(0.8f, 1f);
        this.scaleAnimator.setInterpolator(PathInterpolatorCompat.create(0f, 0f, 0f, 1f));
        this.scaleAnimator.addUpdateListener(animation -> {
            this.setScaleX((Float) animation.getAnimatedValue());
            this.setScaleY((Float) animation.getAnimatedValue());
        });

        this.shrinkAnimator = ValueAnimator.ofFloat(1f, 0.8f);
        this.shrinkAnimator.setInterpolator(PathInterpolatorCompat.create(0f, 0f, 1f, 0f));
        this.shrinkAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                LoadingView.this.startAngleAnimator.setRepeatCount(0);
                LoadingView.this.startAngleAnimator.setDuration(0);
                LoadingView.this.startAngleAnimator.end();

                LoadingView.this.swingAngleAnimator.setRepeatCount(0);
                LoadingView.this.swingAngleAnimator.setDuration(0);
                LoadingView.this.swingAngleAnimator.end();
            }
        });
        this.shrinkAnimator.addUpdateListener(animation -> {
            this.setScaleX((Float) animation.getAnimatedValue());
            this.setScaleY((Float) animation.getAnimatedValue());
        });
    }

    /**
     * 开始动画
     */
    private void startAnimation() {
        this.startAngleAnimator.setRepeatCount(ValueAnimator.INFINITE);
        this.startAngleAnimator.setDuration(DURATION_DEFAULT);
        this.startAngleAnimator.start();

        this.swingAngleAnimator.setRepeatCount(ValueAnimator.INFINITE);
        this.swingAngleAnimator.setDuration(DURATION_DEFAULT);
        this.swingAngleAnimator.start();

        this.fadeInAnimator.setDuration(FADE_IN_DURATION_DEFAULT);
        this.fadeInAnimator.start();

        this.scaleAnimator.setDuration(SCALE_DURATION_DEFAULT);
        this.scaleAnimator.start();
    }

    /**
     * 停止动画
     */
    private void stopAnimation() {
        this.fadeOutAnimator.setDuration(FADE_OUT_DURATION_DEFAULT);
        this.fadeOutAnimator.start();

        this.shrinkAnimator.setDuration(SHRINK_DURATION_DEFAULT);
        this.shrinkAnimator.start();
    }
}