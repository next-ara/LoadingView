package com.next.view.loading;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Size;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

/**
 * ClassName:Level加载渲染器类
 *
 * @author Afton
 * @time 2023/10/30
 * @auditor
 */
public class LevelLoadingRenderer extends LoadingRenderer {

    private static final Interpolator LINEAR_INTERPOLATOR = new LinearInterpolator();
    private static final Interpolator MATERIAL_INTERPOLATOR = new FastOutSlowInInterpolator();
    private static final Interpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();
    private static final Interpolator DECELERATE_INTERPOLATOR = new DecelerateInterpolator();

    private static final int NUM_POINTS = 5;
    private static final int DEGREE_360 = 360;

    private static final float MAX_SWIPE_DEGREES = 0.8f * DEGREE_360;
    private static final float FULL_GROUP_ROTATION = 3.0f * DEGREE_360;

    private static final float[] LEVEL_SWEEP_ANGLE_OFFSETS = new float[]{1.0f, 7.0f / 8.0f, 5.0f / 8.0f};

    private static final float START_TRIM_DURATION_OFFSET = 0.5f;
    private static final float END_TRIM_DURATION_OFFSET = 1.0f;

    private static final float DEFAULT_STROKE_WIDTH = 2.5f;

    private final Paint mPaint = new Paint();
    private final RectF mTempBounds = new RectF();

    private final Animator.AnimatorListener mAnimatorListener = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationRepeat(Animator animator) {
            super.onAnimationRepeat(animator);
            storeOriginals();

            LevelLoadingRenderer.this.mStartDegrees = LevelLoadingRenderer.this.mEndDegrees;
            LevelLoadingRenderer.this.mRotationCount = (LevelLoadingRenderer.this.mRotationCount + 1) % (NUM_POINTS);
        }

        @Override
        public void onAnimationStart(Animator animation) {
            super.onAnimationStart(animation);
            LevelLoadingRenderer.this.mRotationCount = 0;
        }
    };

    @Size(3)
    private int[] mLevelColors;
    @Size(3)
    private float[] mLevelSwipeDegrees;

    private float mRotationCount;
    private float mGroupRotation;

    private float mEndDegrees;
    private float mStartDegrees;
    private float mOriginEndDegrees;
    private float mOriginStartDegrees;

    private float mStrokeWidth;

    public LevelLoadingRenderer(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init(context, attrs);
        this.setupPaint();
        this.addRenderListener(this.mAnimatorListener);
    }

    private void init(Context context, AttributeSet attrs) {
        this.mLevelSwipeDegrees = new float[3];

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.LoadingView);
        int levelColor = typedArray.getColor(R.styleable.LoadingView_loadingColor, Color.BLACK);
        this.mStrokeWidth = typedArray.getDimension(R.styleable.LoadingView_loadingStrokeWidth, DEFAULT_STROKE_WIDTH);
        typedArray.recycle();

        this.mLevelColors = new int[]{levelColor, levelColor, levelColor};
    }

    private void setupPaint() {
        this.mPaint.setAntiAlias(true);
        this.mPaint.setStrokeWidth(this.mStrokeWidth);
        this.mPaint.setStyle(Paint.Style.STROKE);
        this.mPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    @Override
    protected void draw(Canvas canvas) {
        int saveCount = canvas.save();

        this.mTempBounds.set(this.mBounds);
        this.mTempBounds.inset(this.mStrokeWidth * 2, this.mStrokeWidth * 2);
        canvas.rotate(this.mGroupRotation, this.mTempBounds.centerX(), this.mTempBounds.centerY());

        for (int i = 0; i < 3; i++) {
            if (this.mLevelSwipeDegrees[i] != 0) {
                this.mPaint.setColor(this.mLevelColors[i]);
                canvas.drawArc(this.mTempBounds, this.mEndDegrees, this.mLevelSwipeDegrees[i], false, this.mPaint);
            }
        }

        canvas.restoreToCount(saveCount);
    }

    @Override
    protected void computeRender(float renderProgress) {
        if (renderProgress <= START_TRIM_DURATION_OFFSET) {
            float startTrimProgress = (renderProgress) / START_TRIM_DURATION_OFFSET;
            this.mStartDegrees = this.mOriginStartDegrees + MAX_SWIPE_DEGREES * MATERIAL_INTERPOLATOR.getInterpolation(startTrimProgress);

            float mSwipeDegrees = this.mEndDegrees - this.mStartDegrees;
            float levelSwipeDegreesProgress = Math.abs(mSwipeDegrees) / MAX_SWIPE_DEGREES;

            float level1Increment = DECELERATE_INTERPOLATOR.getInterpolation(levelSwipeDegreesProgress) - LINEAR_INTERPOLATOR.getInterpolation(levelSwipeDegreesProgress);
            float level3Increment = ACCELERATE_INTERPOLATOR.getInterpolation(levelSwipeDegreesProgress) - LINEAR_INTERPOLATOR.getInterpolation(levelSwipeDegreesProgress);

            this.mLevelSwipeDegrees[0] = -mSwipeDegrees * LEVEL_SWEEP_ANGLE_OFFSETS[0] * (1.0f + level1Increment);
            this.mLevelSwipeDegrees[1] = -mSwipeDegrees * LEVEL_SWEEP_ANGLE_OFFSETS[1] * 1.0f;
            this.mLevelSwipeDegrees[2] = -mSwipeDegrees * LEVEL_SWEEP_ANGLE_OFFSETS[2] * (1.0f + level3Increment);
        }

        if (renderProgress > START_TRIM_DURATION_OFFSET) {
            float endTrimProgress = (renderProgress - START_TRIM_DURATION_OFFSET) / (END_TRIM_DURATION_OFFSET - START_TRIM_DURATION_OFFSET);
            this.mEndDegrees = this.mOriginEndDegrees + MAX_SWIPE_DEGREES * MATERIAL_INTERPOLATOR.getInterpolation(endTrimProgress);

            float mSwipeDegrees = this.mEndDegrees - this.mStartDegrees;
            float levelSwipeDegreesProgress = Math.abs(mSwipeDegrees) / MAX_SWIPE_DEGREES;

            if (levelSwipeDegreesProgress > LEVEL_SWEEP_ANGLE_OFFSETS[1]) {
                this.mLevelSwipeDegrees[0] = -mSwipeDegrees;
                this.mLevelSwipeDegrees[1] = MAX_SWIPE_DEGREES * LEVEL_SWEEP_ANGLE_OFFSETS[1];
                this.mLevelSwipeDegrees[2] = MAX_SWIPE_DEGREES * LEVEL_SWEEP_ANGLE_OFFSETS[2];
            } else if (levelSwipeDegreesProgress > LEVEL_SWEEP_ANGLE_OFFSETS[2]) {
                this.mLevelSwipeDegrees[0] = 0;
                this.mLevelSwipeDegrees[1] = -mSwipeDegrees;
                this.mLevelSwipeDegrees[2] = MAX_SWIPE_DEGREES * LEVEL_SWEEP_ANGLE_OFFSETS[2];
            } else {
                this.mLevelSwipeDegrees[0] = 0;
                this.mLevelSwipeDegrees[1] = 0;
                this.mLevelSwipeDegrees[2] = -mSwipeDegrees;
            }
        }

        this.mGroupRotation = ((FULL_GROUP_ROTATION / NUM_POINTS) * renderProgress) + (FULL_GROUP_ROTATION * (this.mRotationCount / NUM_POINTS));
    }

    @Override
    protected void setAlpha(int alpha) {
        this.mPaint.setAlpha(alpha);
    }

    @Override
    protected void setColorFilter(ColorFilter cf) {
        this.mPaint.setColorFilter(cf);
    }

    @Override
    protected void reset() {
        this.resetOriginals();
    }

    private void storeOriginals() {
        this.mOriginEndDegrees = this.mEndDegrees;
        this.mOriginStartDegrees = this.mEndDegrees;
    }

    private void resetOriginals() {
        this.mOriginEndDegrees = 0;
        this.mOriginStartDegrees = 0;

        this.mEndDegrees = 0;
        this.mStartDegrees = 0;

        this.mLevelSwipeDegrees[0] = 0;
        this.mLevelSwipeDegrees[1] = 0;
        this.mLevelSwipeDegrees[2] = 0;
    }

    private void apply(Builder builder) {
        this.mWidth = builder.mWidth > 0 ? builder.mWidth : this.mWidth;
        this.mHeight = builder.mHeight > 0 ? builder.mHeight : this.mHeight;
        this.mStrokeWidth = builder.mStrokeWidth > 0 ? builder.mStrokeWidth : this.mStrokeWidth;

        this.mDuration = builder.mDuration > 0 ? builder.mDuration : this.mDuration;

        this.mLevelColors = builder.mLevelColors != null ? builder.mLevelColors : this.mLevelColors;

        this.setupPaint();
    }

    public static class Builder {
        private Context mContext;
        private AttributeSet attrs;
        private int mWidth;
        private int mHeight;
        private int mStrokeWidth;
        private int mCenterRadius;

        private int mDuration;

        @Size(3)
        private int[] mLevelColors;

        public Builder(Context mContext, AttributeSet attrs) {
            this.mContext = mContext;
            this.attrs = attrs;
        }

        public Builder setWidth(int width) {
            this.mWidth = width;
            return this;
        }

        public Builder setHeight(int height) {
            this.mHeight = height;
            return this;
        }

        public Builder setStrokeWidth(int strokeWidth) {
            this.mStrokeWidth = strokeWidth;
            return this;
        }

        public Builder setCenterRadius(int centerRadius) {
            this.mCenterRadius = centerRadius;
            return this;
        }

        public Builder setDuration(int duration) {
            this.mDuration = duration;
            return this;
        }

        public Builder setLevelColors(@Size(3) int[] colors) {
            this.mLevelColors = colors;
            return this;
        }

        public Builder setLevelColor(int color) {
            return this.setLevelColors(new int[]{this.oneThirdAlphaColor(color), this.twoThirdAlphaColor(color), color});
        }

        public LevelLoadingRenderer build() {
            LevelLoadingRenderer loadingRenderer = new LevelLoadingRenderer(this.mContext, this.attrs);
            loadingRenderer.apply(this);
            return loadingRenderer;
        }

        private int oneThirdAlphaColor(int colorValue) {
            int startA = (colorValue >> 24) & 0xff;
            int startR = (colorValue >> 16) & 0xff;
            int startG = (colorValue >> 8) & 0xff;
            int startB = colorValue & 0xff;

            return (startA / 3 << 24) | (startR << 16) | (startG << 8) | startB;
        }

        private int twoThirdAlphaColor(int colorValue) {
            int startA = (colorValue >> 24) & 0xff;
            int startR = (colorValue >> 16) & 0xff;
            int startG = (colorValue >> 8) & 0xff;
            int startB = colorValue & 0xff;

            return (startA * 2 / 3 << 24) | (startR << 16) | (startG << 8) | startB;
        }
    }
}