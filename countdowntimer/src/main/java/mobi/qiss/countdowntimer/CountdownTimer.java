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
import android.os.Parcelable;
import android.text.format.Time;
import android.view.View;
import android.util.AttributeSet;


import static mobi.qiss.countdowntimer.R.styleable.CountdownTimer_progress;

/**
 * Created by Alan.Tu on 2/4/15.
 */
public class CountdownTimer extends View {

    //public.............................................

    public CountdownTimer(Context context) {
        this(context, null);
    }

    public CountdownTimer(Context context, AttributeSet attrs) {
        super(context);

        final TypedArray attributes = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CountdownTimer, 0, 0);

        initProgressValue = attributes.getInt(CountdownTimer_progress, (int) DEFAULT_PROGRESS);
        maxValue = attributes.getInt(R.styleable.CountdownTimer_duration_millis, (int) DEFAULT_PROGRESS);
        delayTimeMillis = attributes.getInt(R.styleable.CountdownTimer_frequence_millis, (int) DEFAULT_FREQUENCE);
        lineWidthDefault = attributes.getInt(R.styleable.CountdownTimer_line_width_px, (int) DEFAULT_LINE_WIDTH);

        boolean autostart = attributes.getBoolean(R.styleable.CountdownTimer_autostart, false);

        attributes.recycle();

        worker.start();
        threadHandler = new Handler(worker.getLooper());
        threadHandler.post(updateViewTask);

        setDuration(maxValue, (long) initProgressValue);
        if (autostart)
            start(delayTimeMillis);
    }

    public float getProgress() {
        return progressValue;
    }

    public void setProgress(float progress) {
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

    public void start(long delayMillis) {
        isStart = true;
        delayTimeMillis = delayMillis;
        startTimeMillis = System.currentTimeMillis();
        threadHandler.postDelayed(updateViewTask, Math.abs(delayTimeMillis));
    }

    public void stop() {
        isStart = false;
    }

    public void setDuration(long durationMillis, long progress) {
        minValue = 0;
        maxValue = durationMillis;
        setProgress(progress);
    }

    public void setDuration(Time durationMillis) {
        minValue = 0;
        maxValue = durationMillis.toMillis(true);
        setProgress(maxValue);
    }

    //Local parameter.......................................

    private final long DEFAULT_PROGRESS = 1000;
    private final long DEFAULT_FREQUENCE = 10;
    private final long DEFAULT_LINE_WIDTH = 40;

    Bitmap fullBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.countdown_bg);
    Rect fullImageRect = new Rect(0, 0, fullBitmap.getWidth(), fullBitmap.getHeight());
    Bitmap emptyBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.countdown_empty_bg);
    Rect emptyImageRect = new Rect((int) 0, (int) 0, (int) emptyBitmap.getWidth(), (int) (emptyBitmap.getHeight()));
    Bitmap indicatorBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.countdown_indicator);
    Rect indicatorImageRect = new Rect(0, 0, indicatorBitmap.getWidth(), indicatorBitmap.getHeight());

    long startTimeMillis;
    private boolean isStart = false;
    private long delayTimeMillis = DEFAULT_FREQUENCE;
    private long minValue = 0;
    private long maxValue = DEFAULT_PROGRESS;
    private RectF rectView = new RectF();
    private Paint paint = new Paint();
    float lineWidthDefault = DEFAULT_LINE_WIDTH;
    final float degreeTolerate = 0;
    float progressValue = DEFAULT_PROGRESS;
    float initProgressValue = minValue;
    Handler threadHandler;
    HandlerThread worker = new HandlerThread("updateView");
    private Runnable updateViewTask = new Runnable() {
        public void run() {
            if (isStart) {
                long duration = System.currentTimeMillis() - startTimeMillis;
                if(delayTimeMillis >= 0)
                    duration += initProgressValue;
                else
                    duration =  (long)initProgressValue - duration;
                setProgress(duration);
                if (delayTimeMillis >= 0 && getProgress() >= maxValue) {
                    stop();
                }
                else if (delayTimeMillis < 0 && getProgress() <= minValue){
                    stop();
                }
                else {
                    threadHandler.postDelayed(updateViewTask, Math.abs(delayTimeMillis));
                }
            }
        }
    };

    //overwrite.............................................

    @Override
    public void invalidate() {
        super.invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        rectView.set(0, 0, MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        RectF rectF = new RectF(rectView);

        paint.setAntiAlias(true);

        //canvas.drawColor(Color.RED);//FOR DEBUG

        float xRation = rectF.width() / fullImageRect.width();
        float yRation = rectF.height() / fullImageRect.height();
        float lineWidth = lineWidthDefault * xRation;
        float r1 = (rectF.width() / 2) - lineWidth;
        float r2 = r1 - (lineWidth);

        rectF.offset(lineWidth, lineWidth);
        rectF.bottom -= (lineWidth * 2);
        rectF.right -= (lineWidth * 2);

        canvas.drawBitmap(emptyBitmap, emptyImageRect, rectF, paint);

        canvas.save();

        double initValue = (rectF.height() - r1) / r1;
        double initRadio = Math.atan(initValue);
        double initDegree = Math.toDegrees(initRadio);
        float angleStart = (float) (180.0 - initDegree - degreeTolerate/*Tolerate*/);
        float angleEnd = (float) (initDegree + degreeTolerate/*Tolerate*/);
        float sweep = (float) ((360.0 - (angleStart - angleEnd)) * (progressValue / (float) maxValue));

        Path path = new Path();
        path.setLastPoint(r1 + lineWidth, r1 + lineWidth);//circle center
        path.lineTo(
                r1 + (float) Math.cos(Math.toRadians(angleStart)) * r1 + lineWidth,
                r1 + (float) Math.sin(Math.toRadians(angleStart)) * r1 + lineWidth);
        path.addArc(new RectF(lineWidth, lineWidth, rectF.width() + lineWidth, rectF.width()), angleStart, sweep);
        path.lineTo(rectF.width() / 2 + lineWidth, (rectF.width() / 2) + lineWidth);
        paint.setStrokeWidth(1);
        paint.setStyle(Paint.Style.STROKE);
        //canvas.drawPath(path,paint);//FOR DEBUG
        canvas.clipPath(path);

        //canvas.drawColor(Color.BLUE);//FOR DEBUG

        canvas.drawBitmap(fullBitmap, fullImageRect, rectF, paint);

        canvas.restore();

        float x1 = r1 + (float) Math.cos(Math.toRadians(angleStart + sweep)) * r1 + (lineWidth) * 1;
        float y1 = r1 + (float) Math.sin(Math.toRadians(angleStart + sweep)) * r1 + (lineWidth) * 1;
        float x2 = r2 + (float) Math.cos(Math.toRadians(angleStart + sweep)) * r2 + (lineWidth) * 2;
        float y2 = r2 + (float) Math.sin(Math.toRadians(angleStart + sweep)) * r2 + (lineWidth) * 2;
        float xMid = (x1 + x2) / 2;
        float yMid = (y1 + y2) / 2;
        RectF indicatorRect = new RectF(
                xMid - (xRation * indicatorImageRect.width() / 2),
                yMid - (yRation * indicatorImageRect.height() / 2),
                xMid + (xRation * indicatorImageRect.width() / 2),
                yMid + (yRation * indicatorImageRect.height() / 2));
        canvas.drawBitmap(indicatorBitmap, indicatorImageRect, indicatorRect, paint);

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
        return bundle;
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
