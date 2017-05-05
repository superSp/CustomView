package com.administrator.customviewtest.successview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Administrator on 2017/5/3.
 */

public class ZFBCashBookView extends View {

    private Paint arcPaint;
    private Paint bitmapPaint;
    private int arcWidth = 100;
    private int width;

    private int[] arcs = {0, 130, 100, 70, 30, 30};
    private int[] colors = {0xffff9a38, 0xffffc444, 0xfffb8800, 0xffffd427, 0xffffa518};

    private float[] point = new float[2];
    private float[] tan = new float[2];

    private ArcRange startArcRange;
    private boolean shouldRoate = false;
    private float roate;

    private boolean translate = false;
    private boolean degress = false;
    private float trans = 0;
    private float degre = 0;
    private ValueAnimator valueAnimator;

    private RectF rectF = new RectF();
    private RectF bitMapRectF = new RectF();

    private List<Bitmap> bitmapList = new ArrayList<>();

    private List<ArcRange> arcRangeList = new ArrayList<>();

    private List<Float> bitmapDegress = new ArrayList<>();

    public ZFBCashBookView(Context context) {
        this(context, null);
    }

    public ZFBCashBookView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ZFBCashBookView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

        arcPaint = new Paint();
        arcPaint.setDither(true);
        arcPaint.setAntiAlias(true);
        arcPaint.setStyle(Paint.Style.STROKE);
        arcPaint.setStrokeWidth(arcWidth);


        bitmapPaint = new Paint();
        bitmapPaint.setDither(true);
        bitmapPaint.setAntiAlias(true);

        startArcRange = new ArcRange(0f, 0f);

        initBitmap();

        initValueAnimator();


    }

    private void initBitmap() {

        for (int i = 1; i < arcs.length + 1; i++) {
            int id = getResources().getIdentifier("emoj" + i, "drawable", getContext().getPackageName());
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 4;
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), id, options);
            bitmapList.add(bitmap);
        }

    }

    private void initValueAnimator() {
        for (int i = 0; i < arcs.length - 1; i++) {

            initSweep(i);

        }
    }

    private void initSweep(int i) {
        int beginAngel = 0, sweepAngel;

        for (int j = 0; j <= i; j++) {
            beginAngel += arcs[j];
        }

        sweepAngel = arcs[i + 1];

        ArcRange endArcRange = new ArcRange(beginAngel, sweepAngel);

        arcRangeList.add(new ArcRange());

        valueAnimator = ValueAnimator.ofObject(new ArcRangeEvaluator(), startArcRange, endArcRange);

        final int finalI = i;

        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                arcRangeList.set(finalI, (ArcRange) valueAnimator.getAnimatedValue());
                invalidate();
            }
        });

        //最后一个valueAnimator结束后,旋转第一个到正下方
        if (i == arcs.length - 2) {
            final int finalI1 = i;

            valueAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    initFirstArc(finalI1);
                }
            });
        }


        valueAnimator.setDuration(2000);
        valueAnimator.start();

    }

    private void initFirstArc(final int i) {

        shouldRoate = true;

        valueAnimator = ValueAnimator.ofFloat(0f, (180f - arcs[1]) / 2f);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                roate = (float) valueAnimator.getAnimatedValue();
                invalidate();
            }
        });
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                initTransAnim(i);
            }
        });
        valueAnimator.setDuration(1000);
        valueAnimator.setInterpolator(new DecelerateInterpolator());
        valueAnimator.start();
    }

    private void initTransAnim(final int i) {

        translate = true;

        valueAnimator = ValueAnimator.ofFloat(0f, 10f);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                trans = (float) valueAnimator.getAnimatedValue();
                invalidate();
            }
        });
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                initDegerssAnim(i);
            }
        });

        valueAnimator.setDuration(1000);
        valueAnimator.setInterpolator(new DecelerateInterpolator());
        valueAnimator.start();
    }

    private void initDegerssAnim(final int i) {
        degress = true;

        valueAnimator = ValueAnimator.ofFloat(0f, bitmapDegress.get(i));
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                degre = (float) valueAnimator.getAnimatedValue();
                bitmapDegress.set(i, degre);
                invalidate();
            }
        });
        valueAnimator.setDuration(1000);
        valueAnimator.setInterpolator(new DecelerateInterpolator());
        valueAnimator.start();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        width = getMeasuredWidth();

        setMeasuredDimension(Math.min(width, getMeasuredHeight()), Math.min(width, getMeasuredHeight()) + 50);

    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);

        canvas.translate(width / 2, width / 2);

        if (shouldRoate) {
            canvas.rotate(roate);
        }


        for (int i = 0; i < arcs.length - 1; i++) {

            arcPaint.setColor(colors[i]);

            if (translate && i == 0) {

                rectF.set(-(width + trans) / 2 + (arcWidth) / 2, -(width + trans) / 2 + (arcWidth) / 2,
                        (width + trans) / 2 - (arcWidth) / 2, (width + trans) / 2 - (arcWidth) / 2);

                bitMapRectF.set(-(width + trans) / 2 + arcWidth / 2 - bitmapList.get(i).getWidth() / 2, -(width + trans) / 2 + arcWidth / 2 - bitmapList.get(i).getHeight() / 2,
                        (width + trans) / 2 - arcWidth / 2 - bitmapList.get(i).getWidth() / 2, (width + trans) / 2 - arcWidth / 2 - bitmapList.get(i).getHeight() / 2);

                arcPaint.setStrokeWidth(arcWidth + trans);

                canvas.drawArc(rectF, arcRangeList.get(i).getStart() + 1, arcRangeList.get(i).getSweep() - 2, false, arcPaint);



            } else {

                arcPaint.setStrokeWidth(arcWidth);

                rectF.set(-width / 2 + arcWidth / 2, -width / 2 + arcWidth / 2,
                        width / 2 - arcWidth / 2, width / 2 - arcWidth / 2);

                bitMapRectF.set(-width / 2 + arcWidth / 2 - bitmapList.get(i).getWidth() / 2, -width / 2 + arcWidth / 2 - bitmapList.get(i).getHeight() / 2,
                        width / 2 - arcWidth / 2 - bitmapList.get(i).getWidth() / 2, width / 2 - arcWidth / 2 - bitmapList.get(i).getHeight() / 2);

                canvas.drawArc(rectF, arcRangeList.get(i).getStart(), arcRangeList.get(i).getSweep(), false, arcPaint);

            }

            drawBitmap(i, canvas);

        }

    }

    private void drawBitmap(int i, Canvas canvas) {

        Matrix matrix = new Matrix();

        Path path = new Path();

        path.addArc(bitMapRectF, arcRangeList.get(i).getStart(), arcRangeList.get(i).getSweep());

        PathMeasure measure = new PathMeasure(path, false);

        measure.getPosTan(measure.getLength() / 2, point, tan);

        if (shouldRoate) {

            float degrees = (float) (Math.atan2(tan[1], tan[0]) * 180.0 / Math.PI); // 计算图片旋转角度

            bitmapDegress.add(degrees);

//            matrix.postRotate(degrees - 180, bitmapList.get(i).getWidth() / 2, bitmapList.get(i).getHeight() / 2);   // 旋转图片

        }

        if (degress) {
            matrix.postRotate(bitmapDegress.get(i) - 180, bitmapList.get(i).getWidth() / 2, bitmapList.get(i).getHeight() / 2);   // 旋转图片
        }

        matrix.postTranslate(point[0], point[1]);

        canvas.drawBitmap(bitmapList.get(i), matrix, bitmapPaint);

    }

    public class ArcRangeEvaluator implements TypeEvaluator<ArcRange> {

        @Override
        public ArcRange evaluate(float fraction, ArcRange arcRange1, ArcRange arcRange2) {

            return new ArcRange(arcRange1.getStart() + fraction * (arcRange2.getStart() - arcRange1.getStart()),
                    arcRange1.getSweep() + fraction * (arcRange2.getSweep() - arcRange1.getSweep()));

        }

    }

    public class ArcRange {
        private float start;
        private float sweep;


        ArcRange(float start, float sweep) {
            this.start = start;
            this.sweep = sweep;
        }

        ArcRange() {
        }

        @Override
        public String toString() {
            return "start:" + start + " end:" + sweep;
        }

        public float getStart() {
            return start;
        }

        public float getSweep() {
            return sweep;
        }

    }
}
