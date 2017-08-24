package com.administrator.customviewtest.successview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.animation.DecelerateInterpolator;


import com.administrator.customviewtest.util.DateUtil;
import com.administrator.customviewtest.util.DensityUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/7/7.
 */

public class CurveChartView extends View {
    private static final String TAG = "CurveChartView";
    private Paint HLinePaint;
    private Paint VLinePaint;
    private Paint textPaint;
    private Paint linePaint;
    private Paint circleRedPaint;
    private Paint circleGreenPaint;
    private Paint circleBluePaint;
    private Paint rectTransPaint;
    private Paint colorFulPaint;

    private Rect textRect;
    private int vPoints = 11;
    private int hPoints;
    private int lineStroke = 2;

    private int segmentLone = DensityUtils.dp2px(getContext(), 40);
    private int width;
    private int height;
    private float currentDay = 1;
    private Path vLineDashPath = new Path();
    private String[] textV = {"33.0", "15.0", "11.0", "9.0", "8.0", "7.0", "6.0", "5.0", "4.0", "2.5", ""};
    /**
     * 模式显示几天
     */
    private List<String> list;
    private String[] textH;

    private float downX, moveX, offsetX, lastMove;
    private boolean isFirstShow;
    private Rect transRect = new Rect();

    private VelocityTracker velocityTracker = VelocityTracker.obtain();
    private int xVelocity = 0;
    /**
     * 模式5天 1天，和显示小时
     */
    private MODULE module = MODULE.ONEDAY;

    public enum MODULE {
        ONEDAY,
        FIVEDAY,
        HOUR,
    }

    public void setModule(MODULE module) {
        this.module = module;
        init();
        invalidate();
    }

    private ValueAnimator valueAnimator;

    private Path path = new Path();

    private float transRectTop = 7.5f, transRectBottom = 2.4f;
    private String transRectTopStr = "10.0", transRectBottomStr = "4.4";
    private List<PointF> pointList = new ArrayList<>();
    private boolean haveTransCriclePoint = false;

    public CurveChartView(Context context) {
        this(context, null);
    }

    public CurveChartView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CurveChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        width = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        switch (heightMode) {
            case MeasureSpec.EXACTLY:
                height = MeasureSpec.getSize(heightMeasureSpec);
                textPaint.getTextBounds(textV[0], 0, textV[0].length(), textRect);
                segmentLone = (height - textRect.height() / 2 * 3) / (vPoints - 1);
                setMeasuredDimension(width, height);
                break;
            case MeasureSpec.AT_MOST:
                height = segmentLone * (vPoints + 1);
                textPaint.getTextBounds(textV[0], 0, textV[0].length(), textRect);
                setMeasuredDimension(width, segmentLone * (vPoints - 1) + textRect.height() / 2 * 3);
                break;
        }
    }

    private void init() {
        offsetX = 0;
        moveX = 0;
        lastMove = 0;
        isFirstShow = true;
        haveTransCriclePoint = false;
        switch (module) {
            case FIVEDAY:
                list = DateUtil.getLastYearFiveDay(30);
                break;
            case ONEDAY:
                list = DateUtil.getLastYearDay(30);
                break;
            case HOUR:
                list = DateUtil.getLastYearHourDay(30);
                break;
        }
        textH = list.toArray(new String[list.size()]);

        circleRedPaint = new Paint();
        circleRedPaint.setStyle(Paint.Style.STROKE);
        circleRedPaint.setColor(Color.RED);
        circleRedPaint.setAntiAlias(true);
        circleRedPaint.setDither(true);

        circleGreenPaint = new Paint();
        circleGreenPaint.setStyle(Paint.Style.STROKE);
        circleGreenPaint.setColor(Color.GREEN);
        circleGreenPaint.setAntiAlias(true);
        circleGreenPaint.setDither(true);

        circleBluePaint = new Paint();
        circleBluePaint.setStyle(Paint.Style.STROKE);
        circleBluePaint.setColor(Color.BLUE);
        circleBluePaint.setAntiAlias(true);
        circleBluePaint.setDither(true);

        linePaint = new Paint();
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(lineStroke);
        linePaint.setColor(Color.RED);
        linePaint.setAntiAlias(true);
        linePaint.setDither(true);

        colorFulPaint = new Paint();
        colorFulPaint.setStyle(Paint.Style.STROKE);
        colorFulPaint.setStrokeWidth(lineStroke);
        colorFulPaint.setColor(Color.GREEN);
        colorFulPaint.setAntiAlias(true);
        colorFulPaint.setDither(true);

        HLinePaint = new Paint();
        HLinePaint.setStrokeWidth(lineStroke);
        HLinePaint.setColor(Color.GRAY);

        rectTransPaint = new Paint();
        rectTransPaint.setStyle(Paint.Style.FILL);
        rectTransPaint.setColor(0x66C8C4C4);

        VLinePaint = new Paint();
        VLinePaint.setStyle(Paint.Style.STROKE);
        VLinePaint.setStrokeWidth(lineStroke);
        VLinePaint.setColor(Color.GRAY);

        textPaint = new Paint();
        textPaint.setColor(Color.GRAY);
        textRect = new Rect();
        textPaint.setTextSize(DensityUtils.sp2px(getContext(), 12));

    }

    public void setPointList(List<PointF> pointList) {
        this.pointList = pointList;
        init();
        invalidate();
    }

    public void setTransTopAndBottom(int top, int bottom,String topStr,String bottomStr) {
        this.transRectTop = top;
        this.transRectBottom = bottom;
        this.transRectTopStr = topStr;
        this.transRectBottomStr = bottomStr;
        init();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawHLinesAndText(canvas);
        drawVLinesAndText(canvas);
        drawTransRectAntText(canvas);
        drawLinesAndPoint(canvas);
    }

    private void drawLinesAndPoint(Canvas canvas) {
        canvas.clipRect(new RectF(textRect.width() + 10, 0, width, height));
        drawCircles(pointList, canvas);
        drawCurveLine(canvas);
    }

    private void drawCurveLine(Canvas canvas) {
        if (pointList.size() <= 1) {
            return;
        }
        if (transRectTop == 0 && transRectBottom == 0) {
            //普通线
            for (int i = 0; i < pointList.size() - 1; i++) {
                canvas.drawLine(getDayX(pointList.get(i).x), getDayY(pointList.get(i).y),
                        getDayX(pointList.get(i + 1).x), getDayY(pointList.get(i + 1).y), linePaint);
            }
        } else {
            //彩色线
            drawColorfulLines(canvas);
        }
    }

    private void drawCircles(List<PointF> pointList, Canvas canvas) {
        if (pointList.size() == 0) {
            return;
        }
        for (PointF pointF : pointList) {
            if (transRect.contains(((int) getDayX(pointF.x)), ((int) getDayY(pointF.y)))) {
                canvas.drawCircle(getDayX(pointF.x), getDayY(pointF.y), 10, circleGreenPaint);
            } else if (getDayY(pointF.y) > getDayY(transRectTop)) {
                canvas.drawCircle(getDayX(pointF.x), getDayY(pointF.y), 10, circleBluePaint);
            } else if (getDayY(pointF.y) < getDayY(transRectBottom)) {
                canvas.drawCircle(getDayX(pointF.x), getDayY(pointF.y), 10, circleRedPaint);

            }
        }
    }

    private boolean on1Area(float y) {
        return y > transRectTop;
    }

    private boolean on2Area(float y) {
        return y <= transRectTop && y >= transRectBottom;
    }

    private boolean on3Area(float y) {
        return y <= transRectBottom;
    }

    private void drawColorfulLines(Canvas canvas) {
        //每两个点之间分布可以分为6种情况   11 12 13 22 23 33

        for (int i = 0; i < pointList.size() - 1; i++) {
            if (on1Area(pointList.get(i).y) && on1Area(pointList.get(i+1).y)) {
                //11  红色线段
                path.moveTo(getDayX(pointList.get(i).y), getDayX(pointList.get(i).y));
                path.lineTo(getDayX(pointList.get(i + 1).y), getDayX(pointList.get(i + 1).y));
                linePaint.setColor(Color.RED);
                canvas.drawPath(path, linePaint);
                path.reset();
            } else if ((on1Area(pointList.get(i).y))&&on2Area(pointList.get(i+1).y)||(on1Area(pointList.get(i+1).y))&&on2Area(pointList.get(i).y)) {
                //12  21
                path.moveTo(getDayX(pointList.get(i).x), getDayY(pointList.get(i).y));
                path.lineTo(getDayX(pointList.get(i + 1).x), getDayY(pointList.get(i + 1).y));
                PathMeasure pathMeasure = new PathMeasure(path, false);
                //获取交差点
                float[] floats = new float[2];
                pathMeasure.getPosTan(pathMeasure.getLength() * (Math.abs(getDayY(pointList.get(i).y) - getDayY(transRectTop)) / (Math.abs(getDayY(pointList.get(i).y) - getDayY(pointList.get(i + 1).y)))), floats, null);
                path.reset();

                path.moveTo(floats[0],floats[1]);

                path.lineTo(getDayX(pointList.get(i).x), getDayY(pointList.get(i).y));
                if (getDayY(pointList.get(i).y)>floats[1]){
                    canvas.drawPath(path,circleGreenPaint);
                }else {
                    canvas.drawPath(path,circleRedPaint);
                }

                path.reset();
                path.moveTo(floats[0],floats[1]);

                path.lineTo(getDayX(pointList.get(i + 1).x), getDayY(pointList.get(i + 1).y));
                if (getDayY(pointList.get(i + 1).y)>floats[1]){
                    canvas.drawPath(path,circleGreenPaint);
                }else {
                    canvas.drawPath(path,circleRedPaint);
                }
                path.reset();
            } else if ((on1Area(pointList.get(i).y)&&on3Area(pointList.get(i+1).y))||(on1Area(pointList.get(i+1).y)&&on3Area(pointList.get(i).y))) {
                //13  31
                path.moveTo(getDayX(pointList.get(i).x), getDayY(pointList.get(i).y));
                path.lineTo(getDayX(pointList.get(i + 1).x), getDayY(pointList.get(i + 1).y));
                PathMeasure pathMeasure = new PathMeasure(path, false);
                //获取2个交叉点
                float[] floats1 = new float[2];
                float[] floats2 = new float[2];
                pathMeasure.getPosTan(pathMeasure.getLength() * (Math.abs(getDayY(pointList.get(i).y) - getDayY(transRectTop)) / (Math.abs(getDayY(pointList.get(i).y) - getDayY(pointList.get(i + 1).y)))), floats1, null);
                pathMeasure.getPosTan(pathMeasure.getLength() * (Math.abs(getDayY(pointList.get(i).y) - getDayY(transRectBottom)) / (Math.abs(getDayY(pointList.get(i).y) - getDayY(pointList.get(i + 1).y)))), floats2, null);
                path.reset();

                if(pointList.get(i).y>transRectTop){
                    path.moveTo(getDayX(pointList.get(i).x),getDayY(pointList.get(i).y));
                    path.lineTo(floats1[0],floats1[1]);
                    canvas.drawPath(path,circleRedPaint);
                    path.reset();
                    path.moveTo(floats1[0],floats1[1]);
                    path.lineTo(floats2[0],floats2[1]);
                    canvas.drawPath(path,circleGreenPaint);
                    path.reset();
                    path.moveTo(floats2[0],floats2[1]);
                    path.lineTo(getDayX(pointList.get(i+1).x),getDayY(pointList.get(i+1).y));
                    canvas.drawPath(path,circleBluePaint);
                }else {
                    path.moveTo(getDayX(pointList.get(i).x),getDayY(pointList.get(i).y));
                    path.lineTo(floats2[0],floats2[1]);
                    canvas.drawPath(path,circleBluePaint);
                    path.reset();
                    path.moveTo(floats2[0],floats2[1]);
                    path.lineTo(floats1[0],floats1[1]);
                    canvas.drawPath(path,circleGreenPaint);
                    path.reset();
                    path.moveTo(floats1[0],floats1[1]);
                    path.lineTo(getDayX(pointList.get(i+1).x),getDayY(pointList.get(i+1).y));
                    canvas.drawPath(path,circleRedPaint);
                }
                path.reset();
            } else if (on2Area(pointList.get(i).y)&&on2Area(pointList.get(i+1).y)) {
                //22
                path.moveTo(getDayX(pointList.get(i).y), getDayX(pointList.get(i).y));
                path.lineTo(getDayX(pointList.get(i + 1).y), getDayX(pointList.get(i + 1).y));
                canvas.drawPath(path, circleGreenPaint);
                path.reset();
            } else if ((on2Area(pointList.get(i).y)&&on3Area(pointList.get(i+1).y))||(on2Area(pointList.get(i+1).y)&&on3Area(pointList.get(i).y))) {
                //23  有一个中间点
                path.moveTo(getDayX(pointList.get(i).x), getDayY(pointList.get(i).y));
                path.lineTo(getDayX(pointList.get(i + 1).x), getDayY(pointList.get(i + 1).y));
                PathMeasure pathMeasure = new PathMeasure(path, false);
                //获取交差点
                float[] floats = new float[2];
                pathMeasure.getPosTan(pathMeasure.getLength() * (Math.abs(getDayY(pointList.get(i).y) - getDayY(transRectBottom)) / (Math.abs(getDayY(pointList.get(i).y) - getDayY(pointList.get(i + 1).y)))), floats, null);
                path.reset();

                path.moveTo(floats[0],floats[1]);

                path.lineTo(getDayX(pointList.get(i).x), getDayY(pointList.get(i).y));
                if (getDayY(pointList.get(i).y)>floats[1]){
                    canvas.drawPath(path,circleBluePaint);
                }else {
                    canvas.drawPath(path,circleGreenPaint);
                }

                path.reset();
                path.moveTo(floats[0],floats[1]);

                path.lineTo(getDayX(pointList.get(i + 1).x), getDayY(pointList.get(i + 1).y));
                if (getDayY(pointList.get(i + 1).y)>floats[1]){
                    canvas.drawPath(path,circleBluePaint);
                }else {
                    canvas.drawPath(path,circleGreenPaint);
                }
                path.reset();
            } else if (on3Area(pointList.get(i).y)&&on3Area(pointList.get(i+1).y)) {
                //33
                path.moveTo(getDayX(pointList.get(i).y), getDayX(pointList.get(i).y));
                path.lineTo(getDayX(pointList.get(i + 1).y), getDayX(pointList.get(i + 1).y));
                canvas.drawPath(path, circleRedPaint);
                path.reset();
            }
        }
    }


    public void setCurrentDay(float currentDay) {
        switch (module) {
            case FIVEDAY:
                this.currentDay = currentDay / 5.0f + 1;
                break;
            case ONEDAY:
                this.currentDay = currentDay;
                break;
            case HOUR:
                this.currentDay = currentDay * 6;
                break;
        }
        init();
        invalidate();
    }

    private void drawTransRectAntText(Canvas canvas) {
        if ((transRectTop == 0 && transRectBottom == 0) || transRectBottom >= transRectTop) {
            return;
        }

        textPaint.getTextBounds(textV[0], 0, textV[0].length(), textRect);
        transRect.set(textRect.width() + 10, (int) getDayY(transRectTop), width, (int) getDayY(transRectBottom));
        canvas.drawRect(transRect, rectTransPaint);

        canvas.drawText(transRectTopStr, width - textRect.width() - 5, getDayY(transRectTop), textPaint);
        canvas.drawText(transRectBottomStr, width - textRect.width() - 5, getDayY(transRectBottom) + textRect.height(), textPaint);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        velocityTracker.computeCurrentVelocity(1000);
        velocityTracker.addMovement(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (valueAnimator != null && valueAnimator.isRunning()) {
                    valueAnimator.cancel();
                }
                downX = (int) event.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                moveX = (int) event.getX();
                offsetX = moveX - downX + lastMove;
                if (offsetX < getDayOffset(1)) {
                    offsetX = getDayOffset(1);
                } else if (offsetX > 0) {
                    offsetX = 0;
                }

                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                lastMove = offsetX;
                xVelocity = (int) velocityTracker.getXVelocity();
                autoVelocityScroll(xVelocity);
                break;
        }
        return true;
    }

    private void autoVelocityScroll(int xVelocity) {
        valueAnimator = ValueAnimator.ofFloat(xVelocity / 100, 0).setDuration(1000);
        valueAnimator.setInterpolator(new DecelerateInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                offsetX = (float) animation.getAnimatedValue() + lastMove;

                if (offsetX < getDayOffset(1)) {
                    offsetX = getDayOffset(1);
                } else if (offsetX > 0) {
                    offsetX = 0;
                }

                lastMove = offsetX;
                invalidate();
            }
        });
        valueAnimator.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        velocityTracker.clear();
        velocityTracker.recycle();
    }

    private void drawHLinesAndText(Canvas canvas) {
        canvas.save();
        int count = 0;

        canvas.clipRect(new RectF(0, 0, width, height));

        for (int i = 0; i < vPoints; i++) {
            if (i == 0) {
                textPaint.getTextBounds(textV[0], 0, textV[0].length(), textRect);
                canvas.translate(textRect.width() + 10, 0);
                count = textRect.height() / 2;
            } else if (i == 1) {
                count = segmentLone;
            }
            textPaint.getTextBounds(textV[i], 0, textV[i].length(), textRect);
            canvas.translate(0, count);
            canvas.drawText(textV[i], 0 - textRect.width() - 8, textRect.height() - textRect.height() / 2, textPaint);
            canvas.drawLine(0, 0, width, 0, HLinePaint);
        }
        canvas.restore();
    }

    private void drawVLinesAndText(Canvas canvas) {
        canvas.save();
        hPoints = textH.length;
        int count = 0;
        for (int i = 0; i < hPoints; i++) {
            if (i == 0) {
                textPaint.getTextBounds(textV[0], 0, textV[0].length(), textRect);
                canvas.translate(0, textRect.height() / 2);
                count = textRect.width() + 10;
                canvas.clipRect(new RectF(textRect.width() + 10, 0, width, height));
            } else if (i == 1) {

                if (isFirstShow) {
                    //期初的偏移量=线段数（底部文字数-1）* segmentLone;
                    offsetX = getDayOffset(currentDay);
                    lastMove = offsetX;
                    isFirstShow = false;
                }
                canvas.translate(offsetX, 0);
                count = segmentLone;
            }

            textPaint.getTextBounds(textH[i], 0, textH[i].length(), textRect);
            canvas.translate(count, 0);
            canvas.drawText(textH[i], -textRect.width() / 2, segmentLone * (vPoints - 1) + textRect.height() + 8, textPaint);
            canvas.drawLine(0, 0, 0, segmentLone * (vPoints - 1), HLinePaint);
//            drawVDshLine(canvas);
        }

        canvas.restore();
    }

    /**
     * 画竖直方向虚线
     *
     * @param canvas
     */
    private void drawVDshLine(Canvas canvas) {
        VLinePaint.setPathEffect(new DashPathEffect(new float[]{4, 4}, 0));
        vLineDashPath.moveTo(0, 0);
        vLineDashPath.lineTo(0, segmentLone * (vPoints - 1));
        canvas.drawPath(vLineDashPath, VLinePaint);
    }

    private float getDayX(float day) {
        //5是偏差值
        switch (module) {
            case FIVEDAY:
                return (offsetX - getDayOffset(1) - textRect.width() / 2 - 5 + width - segmentLone / 5.0f * (day - 1.0f));
            case ONEDAY:
                return offsetX - getDayOffset(1) - textRect.width() / 2 - 5 + width - segmentLone * (day - 1.0f);
            case HOUR:
                return offsetX - getDayOffset(1) - textRect.width() / 2 - 5 + width - segmentLone * 6 * (day - 1.0f) - segmentLone * 5;
        }
        return 0;
    }

    private float getDayY(float y) {
        return textRect.height() / 2 + segmentLone * (vPoints - 1 - y);
    }

    private float getDayOffset(float currentDay) {
        return -segmentLone * (textH.length - currentDay + 1) + width;
    }

    private float getCurrentDay() {
        return currentDay;
    }

}
