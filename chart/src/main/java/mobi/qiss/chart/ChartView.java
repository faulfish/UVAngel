package mobi.qiss.chart;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import mobi.qiss.carousel.CarouselView;

/**
 * Created by faulfish on 2/13/15.
 */
public class ChartView extends CarouselView {

    public static int MAXProtectTime = 8 * 60 * 60 * 1000;

    public static class LOG {
        public static class UVData implements Serializable {
            public Integer mUVIndex;
            public Date mTime;
            public Integer mSPFIndex;
            public Integer mPlusIndex;
            public String mLocation;
        }

        private static Map<Date, UVData> mLogs = null;
        private static final String LogFileName_DEFAULT = "LogFile";
        private static String mLogFileName = LogFileName_DEFAULT;

        private static void init(Context context) {
            if (mLogs == null && context != null) {
                mLogs = new HashMap<Date, UVData>();
                try {
                    try {
                        File file = context.getExternalFilesDir(null);
                        if (file != null) {
                            String strPath = file.getPath();
                            File readfile = new File(strPath, mLogFileName);
                            FileInputStream inf = new FileInputStream(readfile);
                            ObjectInputStream ins = new ObjectInputStream(inf);
                            mLogs = (Map<Date, UVData>) ins.readObject();
                            ins.close();
                            mLogs = new TreeMap<Date, UVData>(mLogs);//For sort

                        }
                    } catch (ClassNotFoundException e) {
                        //Noting
                    }
                } catch (IOException e) {
                    String str = e.getLocalizedMessage();
                    Log.w("ChartView", str);
                }
            }
        }

        public static Map<Date, UVData> get(Context context) {
            init(context);
            return mLogs;
        }

        public static void add(int newUVIndex, Time newTime, int newSPFIndex, int newSPFPLUSIndex, String newLocation, Context context) {

            init(context);

            Date newDatetime = new Date();
            newDatetime.setTime(newTime.toMillis(true));
            UVData data = new UVData();
            data.mLocation = newLocation;
            data.mSPFIndex = newSPFIndex;
            data.mUVIndex = newUVIndex;
            data.mPlusIndex = newSPFPLUSIndex;
            data.mTime = newDatetime;
            mLogs.put(newDatetime, data);

            try {
                Date datetime = new Date();
                datetime.setTime(newTime.toMillis(true));
                File dir = context.getExternalFilesDir(null);
                if (dir != null) {
                    String strPath = dir.getPath();
                    File file = new File(strPath, mLogFileName);
                    boolean bRes = false;
                    if (!file.exists())
                        bRes = file.createNewFile();
                    if (file.exists()) {
                        FileOutputStream f = new FileOutputStream(file);
                        ObjectOutputStream s = new ObjectOutputStream(f);
                        s.writeObject(mLogs);
                        s.flush();
                        f.flush();
                        s.close();
                        f.close();
                    }
                }
            } catch (IOException e) {
                //Fix me
            } catch (NullPointerException e) {
                //Fix me
            }
        }

    }

    private static final boolean DEBUG_DEFAULT = false;
    private boolean DEBUG = DEBUG_DEFAULT;
    private String mDebugString = "";
    private int mDayWidth;
    private int mTotalWidth;
    private double mMinValue = MinValue_DEFAULT;
    private static final double MinValue_DEFAULT = 1.0;
    private double mMaxValue = MaxValue_DEFAULT;
    private static final double MaxValue_DEFAULT = 11.0;
    private double mYAxisWidth = YAxisWidth_DEFAULT;
    private static final double YAxisWidth_DEFAULT = 30.0;
    private double mXAxisHeight = XAxisHeight_DEFAULT;
    private static final double XAxisHeight_DEFAULT = 20.0;
    private int mXAxisColor = XAxisColor_DEFAULT;
    private static final int XAxisColor_DEFAULT = R.color.color_gray_chart;
    private LinearLayout mContainer;
    private Context mContext;
    private Rect mRect = new Rect();
    private Paint mPaint = new Paint();
    Time startT;
    Time endT;
    Date startD = new Date();
    Date endD = new Date();

    public interface Listener {
        void onItemSelectionChanged(ChartView view, int pos);

        void onItemClicked(ChartView view, int pos);
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    private Listener mListener;

    private int mItemSelection = 0;//select Day
    private int mItemLogSelection = -1;

    //For onDraw
    Time mTime = new Time();
    Bitmap mBitmap_1_2 = BitmapFactory.decodeResource(getResources(), R.drawable.button_history_uv_point_1_2_nor);
    Bitmap mBitmap_3_4_5 = BitmapFactory.decodeResource(getResources(), R.drawable.button_history_uv_point_3_4_5_nor);
    Bitmap mBitmap_6_7 = BitmapFactory.decodeResource(getResources(), R.drawable.button_history_uv_point_6_7_nor);
    Bitmap mBitmap_8_9_10 = BitmapFactory.decodeResource(getResources(), R.drawable.button_history_uv_point_8_9_10_nor);
    Bitmap mBitmap_11 = BitmapFactory.decodeResource(getResources(), R.drawable.button_history_uv_point_11_nor);
    Bitmap mBitmapSelected_1_2 = BitmapFactory.decodeResource(getResources(), R.drawable.button_history_uv_point_1_2_prs);
    Bitmap mBitmapSelected_3_4_5 = BitmapFactory.decodeResource(getResources(), R.drawable.button_history_uv_point_3_4_5_prs);
    Bitmap mBitmapSelected_6_7 = BitmapFactory.decodeResource(getResources(), R.drawable.button_history_uv_point_6_7_prs);
    Bitmap mBitmapSelected_8_9_10 = BitmapFactory.decodeResource(getResources(), R.drawable.button_history_uv_point_8_9_10_prs);
    Bitmap mBitmapSelected_11 = BitmapFactory.decodeResource(getResources(), R.drawable.button_history_uv_point_11_prs);

    public ChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        final TypedArray attributes = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ChartView, 0, 0);

        mMinValue = attributes.getFloat(R.styleable.ChartView_mMinValue, (float) MinValue_DEFAULT);
        mMaxValue = attributes.getFloat(R.styleable.ChartView_mMaxValue, (float) MaxValue_DEFAULT);
        mYAxisWidth = attributes.getFloat(R.styleable.ChartView_mYAxisWidth, (float) YAxisWidth_DEFAULT);
        mYAxisWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (int) mYAxisWidth, getResources().getDisplayMetrics());
        mXAxisHeight = attributes.getFloat(R.styleable.ChartView_mXAxisHeight, (float) XAxisHeight_DEFAULT);
        mXAxisHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (int) mXAxisHeight, getResources().getDisplayMetrics());
        mXAxisColor = attributes.getInt(R.styleable.ChartView_mXAxisColor, XAxisColor_DEFAULT);
        //String LogFileName = attributes.getString(R.styleable.ChartView_mLogFile);
        //mLogFileName = LogFileName.length() > 1 ? LogFileName : LogFileName_DEFAULT;
        DEBUG = attributes.getBoolean(R.styleable.ChartView_debug_ChartView, DEBUG_DEFAULT);

        attributes.recycle();

    }

    private void addWithoutWrite(int data, Time time) {

        if (startT == null) {
            startT = time;
        }
        if (endT == null) {
            endT = time;
        }
        if (time.before(startT)) startT = time;
        if (time.after(endT)) endT = time;

        startD.setTime(startT.toMillis(true));
        startD.setHours(0);
        startD.setMinutes(0);
        startD.setSeconds(0);
        endD.setTime(endT.toMillis(true));
        endD.setHours(0);
        endD.setMinutes(0);
        endD.setSeconds(0);

        Date datetime = new Date();
        datetime.setTime(time.toMillis(true));

        mContainer = (LinearLayout) getChildAt(0);
        if (mContainer.getChildCount() > 0) {
            long diff = endD.getTime() - startD.getTime();
            long diffDays = diff / (24 * 60 * 60 * 1000);
            ImageView view = (ImageView) mContainer.getChildAt(0);
            mTotalWidth = (int) (mDayWidth * (diffDays + 1) + mYAxisWidth);
            view.setMinimumWidth((int) mTotalWidth);
        } else {
            ImageView view = new ImageView(getContext());
            mTotalWidth = (int) (mDayWidth + mYAxisWidth);
            view.setMinimumWidth((int) mTotalWidth);
            view.setScaleType(ImageView.ScaleType.MATRIX);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                if (mListener != null) {
                    int pos = mContainer.indexOfChild(v);
                    if (pos >= 0) {
//                        mListener.onItemClicked(CarouselView.this, pos);
                        invalidate();
                    }
//                }
                }
            });
            mContainer.addView(view);
            //mContainer.setPadding( 0, 0, 0, 0);//put the center
        }

        long diff = time.toMillis(true) - startT.toMillis(true);
        long diffDays = (diff / (24 * 60 * 60 * 1000));
        double xPos = mDayWidth * (diffDays);
        smoothScrollTo((int) xPos, 0);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mDayWidth = getWidth() - (int) mYAxisWidth;

        LOG.init(getContext());

        for (Map.Entry<Date, LOG.UVData> entry : LOG.mLogs.entrySet()) {
            LOG.UVData data = entry.getValue();
            Date readDatetime = entry.getKey();
            Time readTime = new Time();
            readTime.set(readDatetime.getTime());
            addWithoutWrite(data.mUVIndex, readTime);
        }

        mGestureDetector = new GestureDetector(mContext, new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onDown(MotionEvent e) {
                return false;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                return false;
            }
        });
    }

    private GestureDetector mGestureDetector;

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);

        mOffset = l;
        mDragMode = true;

        if (mDayWidth <= 0) return;
        int pos = (l + mDayWidth / 2) / mDayWidth;
        if (pos < mTotalWidth && pos != mItemSelection && mListener != null) {
            mItemSelection = pos;
            mListener.onItemSelectionChanged(this, pos);
        }
    }

    private boolean mDragMode = false;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mGestureDetector != null && mGestureDetector.onTouchEvent(ev)) {
            return true;
        } else if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            mDragMode = false;
            return super.dispatchTouchEvent(ev);
        } else if (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_CANCEL) {
            super.dispatchTouchEvent(ev);
            mDebugString = String.valueOf(ev.getX() + mOffset);
            if (mDragMode) {
                mDebugString = "";
            } else {
                //find the closest log
                float x = ev.getX() + mOffset;
                float y = ev.getY();
                double height = getHeight() - mXAxisHeight;
                int nSelectedIndex = -1;
                boolean foundMatchLog = false;
                for (Map.Entry<Date, LOG.UVData> entry : LOG.mLogs.entrySet()) {
                    nSelectedIndex++;
                    LOG.UVData data = entry.getValue();
                    int value = data.mUVIndex;
                    if (value > mMaxValue || value < mMinValue)
                        continue;
                    Date datetime = entry.getKey();
                    mTime.set(datetime.getTime());

                    if (mContainer != null) {
                        long diff = mTime.toMillis(true) - startD.getTime();
                        long diffDays = (diff / (24 * 60 * 60 * 1000));
                        double xPos = mDayWidth * ((mTime.hour * 60.0 * 60.0 + mTime.minute * 60.0 + mTime.second) / (24 * 60.0 * 60) + diffDays);
                        double yPos = (height * (mMaxValue - value) / (mMaxValue - mMinValue + 1.0));
                        if (Math.abs(x - xPos) < 100 && Math.abs(y - yPos) < 100) {
                            mDebugString = "Got it";
                            mItemLogSelection = nSelectedIndex;
                            if (mListener != null)
                                mListener.onItemClicked(this, mItemLogSelection);
                            foundMatchLog = true;
                            break;
                        }
                    }
                }
                if (!foundMatchLog) {
                    mItemLogSelection = -1;
                    if (mListener != null)
                        mListener.onItemClicked(this, mItemLogSelection);
                }
            }
            invalidate();
            if (mDayWidth > 0) {
                int pos = (int) ((getScrollX() + mDayWidth / 2) / mDayWidth);
                snapItemSelection(pos);
            }
            return true;
        } else {
            return super.dispatchTouchEvent(ev);
        }
    }

    private boolean snapItemSelection(final int pos) {
        if (mDayWidth > 0 && pos >= 0) {
            smoothScrollTo((int) (pos * mDayWidth), 0);
            return true;
        }
        return false;
    }

    private void drawCenterText(String text, Canvas canvas, Paint paint, int x, int y) {
        float textWidth = paint.measureText(text, 0, text.length());
        int xPos = (int) (x - textWidth / 2);
        int yPos = (int) (y - ((paint.descent() + paint.ascent()) / 2));
        canvas.drawText(text, xPos, yPos, paint);
    }

    public void setItemSelection(int pos) {
        mItemLogSelection = pos;

        int nIndexSelection = -1;
        for (Map.Entry<Date, LOG.UVData> entry : LOG.mLogs.entrySet()) {
            nIndexSelection++;
            if (mItemLogSelection == nIndexSelection) {
                Date date = entry.getKey();
                long diff = date.getTime() - startD.getTime();
                long diffDays = (diff / (24 * 60 * 60 * 1000));
                snapItemSelection((int) diffDays);
                break;
            }
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (DEBUG) {
            mPaint.setAntiAlias(true);
            //canvas.drawColor(Color.RED - mOffset);//FOR DEBUG
            String strDebug = String.valueOf(mOffset);
            //mPaint.setColor(Color.BLACK);
            canvas.drawText(strDebug, 20 + mOffset, 20, mPaint);
        }

        double height = getHeight() - mXAxisHeight;

        //draw day background
        int totalDays = (int) ((mTotalWidth - mYAxisWidth) / mDayWidth);
        for (int indexDay = 1; indexDay <= totalDays; indexDay++) {
            mPaint.setColor(indexDay % 2 == 0 ? 0xe5e5e5 : 0xffffff);
            mPaint.setAlpha((int) (255.0 * 1));
            canvas.drawRect((float) (mYAxisWidth + (mDayWidth * (indexDay - 1))), (float) 0, (float) (mYAxisWidth + (mDayWidth * (indexDay))), (float) height, mPaint);
        }

        //draw log background
        double xPosEndLast = 0;
        for (Map.Entry<Date, LOG.UVData> entry : LOG.mLogs.entrySet()) {
            LOG.UVData data = entry.getValue();
            int value = data.mUVIndex;
            if (value > mMaxValue || value < mMinValue)
                continue;
            Date datetime = entry.getKey();
            mTime.set(datetime.getTime());

            int sunBurnFactor = 0;
            if (data.mUVIndex >= 11)
                sunBurnFactor = 3;
            else if (data.mUVIndex >= 8)
                sunBurnFactor = 5;
            else if (data.mUVIndex >= 6)
                sunBurnFactor = 10;
            else if (data.mUVIndex >= 3)
                sunBurnFactor = 15;
            else if (data.mUVIndex >= 1)
                sunBurnFactor = 30;
            int protectTime = 0;//milliseconds
            protectTime = (data.mSPFIndex + 1) * 5 * sunBurnFactor * 60 * 1000;
            protectTime = protectTime > MAXProtectTime ? MAXProtectTime : protectTime;

            if (mContainer != null) {
                long diff = mTime.toMillis(true) - startD.getTime();
                long diffDays = (diff / (24 * 60 * 60 * 1000));
                double xPos = mDayWidth * ((mTime.hour * 60.0 * 60.0 + mTime.minute * 60.0 + mTime.second) / (24 * 60.0 * 60) + diffDays);
                double xPosEnd = xPos + mDayWidth * (protectTime / (24 * 60.0 * 60 * 1000));
                mPaint.setColor(0xff6ccdd9);
                mPaint.setAlpha((int) (255.0 * 0.4));
                xPos = xPos > xPosEndLast ? xPos : xPosEndLast;
                if (xPosEnd > xPos && xPosEnd > xPosEndLast)
                    canvas.drawRect((float) (xPos), (float) 0, (float) xPosEnd, (float) height, mPaint);
                xPosEndLast = xPosEnd > xPosEndLast ? xPosEnd : xPosEndLast;
            }
        }

        //draw log
        int nIndexSelection = -1;
        for (Map.Entry<Date, LOG.UVData> entry : LOG.mLogs.entrySet()) {
            nIndexSelection++;
            LOG.UVData data = entry.getValue();
            int value = data.mUVIndex;
            if (value > mMaxValue || value < mMinValue)
                continue;
            Date datetime = entry.getKey();
            mTime.set(datetime.getTime());

            if (mContainer != null) {
                long diff = mTime.toMillis(true) - startD.getTime();
                long diffDays = (diff / (24 * 60 * 60 * 1000));
                double xPos = mDayWidth * ((mTime.hour * 60.0 * 60.0 + mTime.minute * 60.0 + mTime.second) / (24 * 60.0 * 60) + diffDays);
                double yPos = (height * (mMaxValue - value) / (mMaxValue - mMinValue + 1.0));
                //canvas.drawCircle((float) xPos, (float) yPos, (float) mPointRadius, mPaint);
                if (mItemLogSelection == nIndexSelection) {
                    if (value >= 11) {
                        canvas.drawBitmap(mBitmapSelected_11, (int) xPos, (int) yPos, mPaint);
                    } else if (value >= 8) {
                        canvas.drawBitmap(mBitmapSelected_8_9_10, (int) xPos, (int) yPos, mPaint);
                    } else if (value >= 6) {
                        canvas.drawBitmap(mBitmapSelected_6_7, (int) xPos, (int) yPos, mPaint);
                    } else if (value >= 3) {
                        canvas.drawBitmap(mBitmapSelected_3_4_5, (int) xPos, (int) yPos, mPaint);
                    } else if (value >= 1) {
                        canvas.drawBitmap(mBitmapSelected_1_2, (int) xPos, (int) yPos, mPaint);
                    }
                } else {
                    if (value >= 11) {
                        canvas.drawBitmap(mBitmap_11, (int) xPos, (int) yPos, mPaint);
                    } else if (value >= 8) {
                        canvas.drawBitmap(mBitmap_8_9_10, (int) xPos, (int) yPos, mPaint);
                    } else if (value >= 6) {
                        canvas.drawBitmap(mBitmap_6_7, (int) xPos, (int) yPos, mPaint);
                    } else if (value >= 3) {
                        canvas.drawBitmap(mBitmap_3_4_5, (int) xPos, (int) yPos, mPaint);
                    } else if (value >= 1) {
                        canvas.drawBitmap(mBitmap_1_2, (int) xPos, (int) yPos, mPaint);
                    }
                }
            }
        }

        //draw X
        mPaint.setColor(mXAxisColor);
        if (mContainer != null) {
            canvas.drawRect((float) mYAxisWidth, (float) (getHeight() - mXAxisHeight), mContainer.getWidth(), (float) getHeight(), mPaint);
        } else {
            canvas.drawRect((float) mYAxisWidth, (float) (getHeight() - mXAxisHeight), getWidth(), (float) getHeight(), mPaint);
        }
        if (mContainer != null) {
            int totalWidth = mContainer.getWidth();
            float textWidth = mPaint.measureText("00", 0, 2);
            float marginWidth = textWidth + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics());
            float hourDegree = (mDayWidth - marginWidth * 2) / 24;
            float dp;
            float sp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, getResources().getDisplayMetrics());
            mPaint.setTextSize(sp);
            mPaint.setColor(getResources().getColor(R.color.color_hour_chart));

            do {
                dp = marginWidth + hourDegree * 0;
                drawCenterText("00", canvas, mPaint, (int) (dp + totalWidth - mDayWidth), (int) (getHeight() - mXAxisHeight / 2));
                dp = marginWidth + hourDegree * 8;
                drawCenterText("08", canvas, mPaint, (int) (dp + totalWidth - mDayWidth), (int) (getHeight() - mXAxisHeight / 2));
                dp = marginWidth + hourDegree * 16;
                drawCenterText("16", canvas, mPaint, (int) (dp + totalWidth - mDayWidth), (int) (getHeight() - mXAxisHeight / 2));
                dp = marginWidth + hourDegree * 24;
                drawCenterText("00", canvas, mPaint, (int) (dp + totalWidth - mDayWidth), (int) (getHeight() - mXAxisHeight / 2));
                totalWidth -= mDayWidth;
            }
            while (totalWidth > 0);
        } else {
            int totalWidth = canvas.getWidth();
            float textWidth = mPaint.measureText("00", 0, 2);
            float marginWidth = textWidth + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics());
            float hourDegree = (mDayWidth - marginWidth * 2) / 24;
            float dp;
            float sp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, getResources().getDisplayMetrics());
            mPaint.setTextSize(sp);
            mPaint.setColor(getResources().getColor(R.color.color_hour_chart));

            do {
                dp = marginWidth + hourDegree * 0;
                drawCenterText("00", canvas, mPaint, (int) (dp + totalWidth - mDayWidth), (int) (getHeight() - mXAxisHeight / 2));
                dp = marginWidth + hourDegree * 8;
                drawCenterText("08", canvas, mPaint, (int) (dp + totalWidth - mDayWidth), (int) (getHeight() - mXAxisHeight / 2));
                dp = marginWidth + hourDegree * 16;
                drawCenterText("16", canvas, mPaint, (int) (dp + totalWidth - mDayWidth), (int) (getHeight() - mXAxisHeight / 2));
                dp = marginWidth + hourDegree * 24;
                drawCenterText("00", canvas, mPaint, (int) (dp + totalWidth - mDayWidth), (int) (getHeight() - mXAxisHeight / 2));
                totalWidth -= mDayWidth;
            }
            while (totalWidth > 0);
        }

        //draw Y
        float yHeight = (float) getHeight() - (float) mXAxisHeight;
        float degree = 11;//SPF 1-11
        float yDegreeHeight = yHeight / degree;

        mPaint.setColor(getResources().getColor(R.color.color_uv_11_chart));
        canvas.drawRect((float) mOffset, yDegreeHeight * 0, (float) (mOffset + mYAxisWidth), yDegreeHeight * 1, mPaint);
        mPaint.setColor(getResources().getColor(R.color.color_uv_8_9_10_chart));
        canvas.drawRect((float) mOffset, yDegreeHeight * 1, (float) (mOffset + mYAxisWidth), yDegreeHeight * 4, mPaint);
        mPaint.setColor(getResources().getColor(R.color.color_uv_6_7_chart));
        canvas.drawRect((float) mOffset, yDegreeHeight * 4, (float) (mOffset + mYAxisWidth), yDegreeHeight * 6, mPaint);
        mPaint.setColor(getResources().getColor(R.color.color_uv_3_4_5_chart));
        canvas.drawRect((float) mOffset, yDegreeHeight * 6, (float) (mOffset + mYAxisWidth), yDegreeHeight * 9, mPaint);
        mPaint.setColor(getResources().getColor(R.color.color_uv_1_2_chart));
        canvas.drawRect((float) mOffset, yDegreeHeight * 9, (float) (mOffset + mYAxisWidth), yDegreeHeight * 11, mPaint);
        mPaint.setColor(getResources().getColor(R.color.color_title_chart));
        canvas.drawRect((float) mOffset, yDegreeHeight * 11, (float) (mOffset + mYAxisWidth), getHeight(), mPaint);
        mPaint.setColor(Color.WHITE);
        float sp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, getResources().getDisplayMetrics());
        mPaint.setTextSize(sp);
        for (int i = 0; i < 5; i++) {
            String text = String.valueOf((i + 1) * 2);
            float textWidth = mPaint.measureText(text, 0, text.length());
            int xPos = (int) (mOffset + (mYAxisWidth / 2) - textWidth / 2);
            int yPos = (int) (getHeight() - mXAxisHeight - (yDegreeHeight * ((i + 1.0) * 2.0 - 1) - ((mPaint.descent() + mPaint.ascent()) / 2)));
            //((textPaint.descent() + textPaint.ascent()) / 2) is the distance from the baseline to the center.
            canvas.drawText(text, xPos, yPos, mPaint);
        }
        String text = "UV";
        float textWidth = mPaint.measureText(text, 0, text.length());
        int xPos = (int) (mOffset + (mYAxisWidth / 2) - textWidth / 2);
        int yPos = (int) (getHeight() - mXAxisHeight / 2 - ((mPaint.descent() + mPaint.ascent()) / 2));
        canvas.drawText(text, xPos, yPos, mPaint);

        if (!DEBUG) return;
        //For debug
        mPaint.setAlpha(255);
        mPaint.setColor(Color.BLACK);
        canvas.drawText(mDebugString, 500 + mOffset, 60, mPaint);//FOR DEBUG

        String strDebug;
        for (int i = 0; mContainer != null && i < mContainer.getChildCount(); i++) {
            ImageView view = (ImageView) mContainer.getChildAt(i);

            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) view.getLayoutParams();
            strDebug = String.valueOf(lp.width);
            canvas.drawText(strDebug, 20 + mOffset, 60 + i * 20, mPaint);//FOR DEBUG
            strDebug = String.valueOf(lp.height);
            canvas.drawText(strDebug, 60 + mOffset, 60 + i * 20, mPaint);//FOR DEBUG
            view.setLayoutParams(lp);
            int loc[] = new int[4];

            mPaint.setColor(Color.BLACK);
            strDebug = String.valueOf(view.getLeft());
            canvas.drawText(strDebug, 100 + mOffset, 60 + i * 20, mPaint);//FOR DEBUG
            strDebug = String.valueOf(view.getTop());
            canvas.drawText(strDebug, 140 + mOffset, 60 + i * 20, mPaint);//FOR DEBUG
            strDebug = String.valueOf(view.getRight());
            canvas.drawText(strDebug, 180 + mOffset, 60 + i * 20, mPaint);//FOR DEBUG
            strDebug = String.valueOf(view.getBottom());
            canvas.drawText(strDebug, 220 + mOffset, 60 + i * 20, mPaint);//FOR DEBUG
            view.getLocationInWindow(loc);
            view.getLocalVisibleRect(mRect);
            mRect.left = view.getLeft();
            mRect.top = view.getTop();
            mRect.right = view.getRight();
            mRect.bottom = view.getBottom();
            mPaint.setColor(Color.TRANSPARENT - i * 20);
            canvas.drawRect(mRect, mPaint);

        }
    }
}
