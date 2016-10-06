package mobi.qiss.countdowntimer;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;//FOR DEBUG
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.text.format.Time;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.util.AttributeSet;


import static mobi.qiss.countdowntimer.R.styleable.CountdownTimer_progress;

/**
 * Created by Alan.Tu on 2/4/15.
 */
public class CountdownTimer extends View {

    //private............................................
    private static final boolean DEBUG_DEFAULT = false;
    private boolean DEBUG = DEBUG_DEFAULT;
    private final long DEFAULT_LINE_WIDTH = 40;
    private float lineWidthDefault = DEFAULT_LINE_WIDTH;
    private final long DEFAULT_PROGRESS = 1000;
    private float progressValue = DEFAULT_PROGRESS;
    private final long DEFAULT_FREQUENCY = 10;// frequency
    private long delayTimeMillis = DEFAULT_FREQUENCY;

    Bitmap mFullBitmap;
    Rect mFullImageRect;
    Bitmap mEmptyBitmap;
    Rect mEmptyImageRect;
    Bitmap mIndicatorBitmap;
    Bitmap mIndicatorPressBitmap;
    Rect mIndicatorImageRect;
    CountdownTimer mThis = null;
    Path mPath = new Path();
    RectF mIndicatorRect = new RectF();
    RectF mDrawRect = new RectF();
    RectF mDrawRectView = new RectF();
    private GestureDetector mGestureDetector;
    Context mContext;
    String mDebugString = "";
    boolean mDragMode = false;
    boolean mEnableDrag = false;

    private Listener mListener = null;

    //public.............................................
    public interface Listener {
        void onUpdate(CountdownTimer view, long timeMillis);

        void onFinish(CountdownTimer view);
    }

    public void registerListener(Listener listener) {
        mListener = listener;
    }

    public void unRegisterListener(Listener listener) {
        if (listener == mListener)
            mListener = null;
    }

    public CountdownTimer(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        final TypedArray attributes = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CountdownTimer, 0, 0);

        initProgressValue = attributes.getInt(CountdownTimer_progress, (int) DEFAULT_PROGRESS);
        maxValue = attributes.getInt(R.styleable.CountdownTimer_duration_millis, (int) DEFAULT_PROGRESS);
        delayTimeMillis = attributes.getInt(R.styleable.CountdownTimer_frequence_millis, (int) DEFAULT_FREQUENCY);
        lineWidthDefault = attributes.getInt(R.styleable.CountdownTimer_line_width_px, (int) DEFAULT_LINE_WIDTH);
        DEBUG = attributes.getBoolean(R.styleable.CountdownTimer_debug_CountdownTimer, DEBUG_DEFAULT);
        boolean autostart = attributes.getBoolean(R.styleable.CountdownTimer_autostart, false);
        attributes.recycle();

        mThis = this;
        mFullBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.button_main_screen_timeline_color);
        mFullImageRect = new Rect(0, 0, mFullBitmap.getWidth(), mFullBitmap.getHeight());
        mEmptyBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.button_main_screen_timeline_gray);
        mEmptyImageRect = new Rect((int) 0, (int) 0, (int) mEmptyBitmap.getWidth(), (int) (mEmptyBitmap.getHeight()));
        mIndicatorBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.button_main_screen_timeline_control_nor);
        mIndicatorPressBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.button_main_screen_timeline_control_prs);
        mIndicatorImageRect = new Rect(0, 0, mIndicatorBitmap.getWidth(), mIndicatorBitmap.getHeight());

        worker.start();
        threadHandler = new Handler(worker.getLooper());
        threadHandler.post(updateViewTask);

        setDuration(maxValue, (long) initProgressValue);
        if (autostart)
            start(delayTimeMillis);
    }

    public boolean enableDrag(boolean enableDrag) {
        mEnableDrag = enableDrag;
        if (!mEnableDrag) mDragMode = false;
        return mEnableDrag;
    }

    public float getProgress() {
        return progressValue;
    }

    public void setProgress(float progress) {
        if (this.progressValue != progress) {
            this.progressValue = progress;
            if (delayTimeMillis >= 0) {
                progressValue = progressValue > maxValue ? maxValue : progressValue;
            } else {
                progressValue = progressValue < minValue ? minValue : progressValue;
            }
        /*
        if (this.progressValue > maxValue) {
            this.progressValue %= maxValue;
        }
        if (this.progressValue < 0) {
            this.progressValue += maxValue;
        }
        */
            postInvalidate();
        }
    }

    public void start(long delayMillis) {
        isStart = true;
        delayTimeMillis = delayMillis;
        startTimeMillis = System.currentTimeMillis();
        mCustomTimeMillis = 0;
        threadHandler.postDelayed(updateViewTask, Math.abs(delayTimeMillis));
    }

    public void stop() {
        isStart = false;
    }

    public void setDuration(long durationMillis, long progress) {
        minValue = 0;
        maxValue = durationMillis;
        initProgressValue = progress;
        setProgress(progress);
    }

    public void setDuration(Time durationMillis) {
        minValue = 0;
        maxValue = durationMillis.toMillis(true);
        setProgress(maxValue);
    }

    //Local parameter.......................................

    public static final int MEG_UPDATE = 9527;
    public static final int MEG_FINISH = 9528;
    private long startTimeMillis;
    private long mCustomTimeMillis = 0;
    private float mPreviousProgressValue;
    private long mLeftTimeMillis;
    private boolean isStart = false;
    private long minValue = 0;
    private long maxValue = DEFAULT_PROGRESS;
    private RectF mRectView = new RectF();
    private Paint paint = new Paint();
    final float degreeTolerate = 5;
    float initProgressValue = minValue;
    Handler threadHandler;
    HandlerThread worker = new HandlerThread("updateView");
    private Runnable updateViewTask = new Runnable() {
        public void run() {
            if (isStart) {
                long duration = System.currentTimeMillis() - startTimeMillis + mCustomTimeMillis;
                if (delayTimeMillis >= 0)
                    duration += initProgressValue;
                else
                    duration = (long) initProgressValue - duration;
                setProgress(duration);
                if (delayTimeMillis >= 0 && getProgress() >= maxValue) {
                    stop();

                    Message m = new Message();
                    m.what = MEG_FINISH;
                    mHandler.sendMessage(m);
                } else if (delayTimeMillis < 0 && getProgress() <= minValue) {
                    stop();

                    Message m = new Message();
                    m.what = MEG_FINISH;
                    mHandler.sendMessage(m);
                } else {
                    if (!mDragMode) {
                        mLeftTimeMillis = duration;
                        threadHandler.postDelayed(updateViewTask, Math.abs(delayTimeMillis));

                        Message m = new Message();
                        m.what = MEG_UPDATE;
                        mHandler.sendMessage(m);
                    }
                }
            }
        }
    };

    long mLastSecondT = -1;

    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MEG_UPDATE: {
                    long secondT = (mLeftTimeMillis / 1000) % (60);
                    if (mListener != null && mLastSecondT != secondT) {
                        mLastSecondT = secondT;
                        mListener.onUpdate(mThis, mLeftTimeMillis);
                    }
                    break;
                }
                case MEG_FINISH:
                    if (mListener != null) mListener.onFinish(mThis);
                    break;

            }
            super.handleMessage(msg);
        }

    };

    //overwrite.............................................

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        boolean res = false;
//        if (mGestureDetector != null && mGestureDetector.onTouchEvent(ev)) {
//            //mDebugString = String.valueOf(ev.getX());
//            return true;
//        } else
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            if (mEnableDrag) {
                mDebugString = String.valueOf(ev.getX());

                float xRation = (mDrawRectView.width() - lineWidthDefault * 2) / mFullImageRect.width();
                float yRation = (mDrawRectView.height() - lineWidthDefault * 2) / mFullImageRect.height();
                float lineWidth = lineWidthDefault * xRation;
                float marginHigh = lineWidthDefault * yRation * (mDrawRectView.height() / mDrawRectView.width());
                float r1 = (mDrawRectView.width() / 2) - lineWidth;
                float r2 = r1 - (lineWidth);

                double initValue = (mDrawRectView.height() - marginHigh * 2 - r1) / r1;
                double initRadio = Math.atan(initValue);
                double initDegree = Math.toDegrees(initRadio);
                float angleStart = (float) (180.0 - initDegree);
                float angleEnd = (float) (initDegree);
                float sweep = (float) ((360.0 - (angleStart - angleEnd)) * (progressValue / (float) (maxValue == 0 ? 100 : maxValue)));

                float x1 = r1 + (float) Math.cos(Math.toRadians(angleStart + sweep)) * r1 + (lineWidth) * 1;
                float y1 = r1 + (float) Math.sin(Math.toRadians(angleStart + sweep)) * r1 + (lineWidth) * 1;
                float x2 = r2 + (float) Math.cos(Math.toRadians(angleStart + sweep)) * r2 + (lineWidth) * 2;
                float y2 = r2 + (float) Math.sin(Math.toRadians(angleStart + sweep)) * r2 + (lineWidth) * 2;
                float xMid = (x1 + x2) / 2;
                float yMid = (y1 + y2) / 2;
                mIndicatorRect.left = xMid - (xRation * mIndicatorImageRect.width() / 2);
                mIndicatorRect.top = yMid - (yRation * mIndicatorImageRect.height() / 2);
                mIndicatorRect.right = xMid + (xRation * mIndicatorImageRect.width() / 2);
                mIndicatorRect.bottom = yMid + (yRation * mIndicatorImageRect.height() / 2);

                if (Math.abs(mIndicatorRect.left + mIndicatorImageRect.width() / 2 - ev.getX()) < 150 && Math.abs(mIndicatorRect.top + mIndicatorRect.height() / 2 - ev.getY()) < 150) {
                    mDebugString = "Got it";
                    mDragMode = true;
                    mPreviousProgressValue = getProgress() + mCustomTimeMillis;
                    res = true;
                } else {
                    mDragMode = false;
                    res = true;
                }
                invalidate();
            }
            return res;
            //return super.dispatchTouchEvent(ev);
        } else if (ev.getAction() == MotionEvent.ACTION_MOVE) {
            if (mEnableDrag) {
                if (mDragMode) {
                    //
                    mDebugString = String.valueOf(ev.getX()) + ", " + String.valueOf(ev.getY());
                    float xRation = (mDrawRectView.width() - lineWidthDefault * 2) / mFullImageRect.width();
                    float yRation = (mDrawRectView.height() - lineWidthDefault * 2) / mFullImageRect.height();
                    float lineWidth = lineWidthDefault * xRation;
                    float marginHigh = lineWidthDefault * yRation * (mDrawRectView.height() / mDrawRectView.width());
                    float r1 = (mDrawRectView.width() / 2) - lineWidth;
                    float r2 = r1 - (lineWidth);

                    double initValue = (mDrawRectView.height() - marginHigh * 2 - r1) / r1;
                    double initRadio = Math.atan(initValue);
                    double initDegree = Math.toDegrees(initRadio);
                    float angleStart = (float) (180.0 - initDegree);
                    float angleEnd = (float) (initDegree);
                    float sweep = (float) ((360.0 - (angleStart - angleEnd)) * (progressValue / (float) (maxValue == 0 ? 100 : maxValue)));

                    float x1 = r1 + (float) Math.cos(Math.toRadians(angleStart + sweep)) * r1 + (lineWidth) * 1;
                    float y1 = r1 + (float) Math.sin(Math.toRadians(angleStart + sweep)) * r1 + (lineWidth) * 1;
                    float x2 = r2 + (float) Math.cos(Math.toRadians(angleStart + sweep)) * r2 + (lineWidth) * 2;
                    float y2 = r2 + (float) Math.sin(Math.toRadians(angleStart + sweep)) * r2 + (lineWidth) * 2;
                    float xMid = (x1 + x2) / 2;
                    float yMid = (y1 + y2) / 2;
                    double cX = mDrawRectView.width() / 2;
                    double cY = mDrawRectView.width() / 2;
                    double currentAngel = Math.atan((ev.getY() - cY) / (ev.getX() - cX));
                    double currentDegree = Math.toDegrees(currentAngel);
                    if ((ev.getX() - cX) < 0)
                        currentDegree += 180.0;
                    else if ((ev.getY() - cY) < 0)
                        currentDegree += 360.0;
                    if (currentDegree > 90)
                        currentDegree = currentDegree > angleStart ? currentDegree : angleStart;
                    if (currentDegree < 90)
                        currentDegree = currentDegree < angleEnd ? currentDegree : angleEnd;
                    mDebugString = String.valueOf(currentDegree);
                    if (currentDegree < 90)
                        progressValue = ((float) (currentDegree + 360 - angleStart) / (float) (360.0 - (angleStart - angleEnd))) * (float) maxValue;
                    else
                        progressValue = ((float) (currentDegree - angleStart) / (float) (360.0 - (angleStart - angleEnd))) * (float) maxValue;
                    mCustomTimeMillis = (long) (mPreviousProgressValue - progressValue);
                    invalidate();
                } else {
                    //
                }
            }

            return super.dispatchTouchEvent(ev);
        } else if (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_CANCEL) {
            if (mEnableDrag) {
                if (mDragMode) {
                    mDragMode = false;
                    mDebugString = "Release it";
                    invalidate();
                    threadHandler.postDelayed(updateViewTask, Math.abs(delayTimeMillis));
                } else {
                    mDebugString = "ACTION_UP or ACTION_CANCEL";
                    mDragMode = false;
                    invalidate();
                }
                return super.dispatchTouchEvent(ev);
            }
            return super.dispatchTouchEvent(ev);
        }

        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void invalidate() {
        super.invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
//        mGestureDetector = new GestureDetector(mContext, new GestureDetector.SimpleOnGestureListener() {
//
//            @Override
//            public boolean onDown(MotionEvent e) {
//                return true;
//            }
//
//            @Override
//            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
//                return false;
//            }
//        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mRectView.set(0, 0, MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mDrawRectView.left = mRectView.left;
        mDrawRectView.top = mRectView.top;
        mDrawRectView.right = mRectView.right;
        mDrawRectView.bottom = mRectView.bottom;

        paint.setAntiAlias(true);

        if (DEBUG) canvas.drawColor(Color.RED);//FOR DEBUG

        float xRation = (mDrawRectView.width() - lineWidthDefault * 2) / mFullImageRect.width();
        float yRation = (mDrawRectView.height() - lineWidthDefault * 2) / mFullImageRect.height();
        float lineWidth = lineWidthDefault * xRation;
        float marginHigh = lineWidthDefault * yRation * (mDrawRectView.height() / mDrawRectView.width());
        float r1 = (mDrawRectView.width() / 2) - lineWidth;
        float r2 = r1 - (lineWidth);

        mDrawRectView.offset(lineWidth, marginHigh);
        mDrawRectView.bottom -= (marginHigh * 2);
        mDrawRectView.right -= (lineWidth * 2);

        canvas.drawBitmap(mEmptyBitmap, mEmptyImageRect, mDrawRectView, paint);

        canvas.save();

        double initValue = (mDrawRectView.height() - marginHigh * 2 - r1) / r1;
        double initRadio = Math.atan(initValue);
        double initDegree = Math.toDegrees(initRadio);
        float angleStart = (float) (180.0 - initDegree);
        float angleEnd = (float) (initDegree);
        float sweep = (float) ((360.0 - (angleStart - angleEnd)) * (progressValue / (float) (maxValue == 0 ? 100 : maxValue)));

        mPath = new Path();
        mPath.setLastPoint(r1 + lineWidth, r1 + lineWidth);//circle center
        mPath.lineTo(
                r1 + (float) Math.cos(Math.toRadians(angleStart)) * r1 + lineWidth,
                r1 + (float) Math.sin(Math.toRadians(angleStart)) * r1 + lineWidth);
        mDrawRect.left = lineWidth;
        mDrawRect.top = lineWidth + 0;//+ 0 for avoid warning
        mDrawRect.right = mDrawRectView.width() + lineWidth;
        mDrawRect.bottom = mDrawRectView.width() + lineWidth;
        mPath.addArc(mDrawRect, angleStart - degreeTolerate, sweep + degreeTolerate);
        mPath.lineTo(r1 + lineWidth, r1 + lineWidth);//circle center
        paint.setStrokeWidth(4);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);

        if (DEBUG) canvas.drawPath(mPath, paint);//FOR DEBUG
        canvas.clipPath(mPath);
        mPath.close();

        if (DEBUG) canvas.drawColor(Color.BLUE);//FOR DEBUG

        canvas.drawBitmap(mFullBitmap, mFullImageRect, mDrawRectView, paint);

        canvas.restore();

        float x1 = r1 + (float) Math.cos(Math.toRadians(angleStart + sweep)) * r1 + (lineWidth) * 1;
        float y1 = r1 + (float) Math.sin(Math.toRadians(angleStart + sweep)) * r1 + (lineWidth) * 1;
        float x2 = r2 + (float) Math.cos(Math.toRadians(angleStart + sweep)) * r2 + (lineWidth) * 2;
        float y2 = r2 + (float) Math.sin(Math.toRadians(angleStart + sweep)) * r2 + (lineWidth) * 2;
        float xMid = (x1 + x2) / 2;
        float yMid = (y1 + y2) / 2;
        mIndicatorRect.left = xMid - (xRation * mIndicatorImageRect.width() / 2);
        mIndicatorRect.top = yMid - (yRation * mIndicatorImageRect.height() / 2);
        mIndicatorRect.right = xMid + (xRation * mIndicatorImageRect.width() / 2);
        mIndicatorRect.bottom = yMid + (yRation * mIndicatorImageRect.height() / 2);
        if (mDragMode)
            canvas.drawBitmap(mIndicatorPressBitmap, mIndicatorImageRect, mIndicatorRect, paint);
        else
            canvas.drawBitmap(mIndicatorBitmap, mIndicatorImageRect, mIndicatorRect, paint);

        if (!DEBUG) return;
        //For debug
        paint.setAlpha(255);
        paint.setColor(Color.BLACK);
        paint.setTextSize(30);
        canvas.drawText(mDebugString, 100, 60, paint);//FOR DEBUG

    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Bundle bundle = new Bundle();
//        bundle.putParcelable(INSTANCE_STATE, super.onSaveInstanceState());
//        bundle.putInt(INSTANCE_TEXT_COLOR, getTextColor());
//        bundle.putFloat(INSTANCE_TEXT_SIZE, getTextSize());
//        bundle.putInt(INSTANCE_FINISHED_STROKE_COLOR, getFinishedColor());
//        bundle.putInt(INSTANCE_UNFINISHED_STROKE_COLOR, getUnfinishedColor());
//        bundle.putInt(INSTANCE_MAX, getMax());
//        bundle.putInt(INSTANCE_PROGRESS, getProgress());
//        bundle.putString(INSTANCE_SUFFIX, getSuffixText());
//        bundle.putString(INSTANCE_PREFIX, getPrefixText());
        return super.onSaveInstanceState();
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
//        if(state instanceof Bundle) {
////            final Bundle bundle = (Bundle) state;
////            textColor = bundle.getInt(INSTANCE_TEXT_COLOR);
////            textSize = bundle.getFloat(INSTANCE_TEXT_SIZE);
////            finishedColor = bundle.getInt(INSTANCE_FINISHED_STROKE_COLOR);
////            unfinishedColor = bundle.getInt(INSTANCE_UNFINISHED_STROKE_COLOR);
////            initPainters();
////            setMax(bundle.getInt(INSTANCE_MAX));
////            setProgress(bundle.getInt(INSTANCE_PROGRESS));
////            prefixText = bundle.getString(INSTANCE_PREFIX);
////            suffixText = bundle.getString(INSTANCE_SUFFIX);
////            super.onRestoreInstanceState(bundle.getParcelable(INSTANCE_STATE));
//            return;
//        }
        super.onRestoreInstanceState(state);
    }
}
