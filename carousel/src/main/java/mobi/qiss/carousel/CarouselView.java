package mobi.qiss.carousel;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;


/**
 * Created by faulfish on 2/10/15.
 */
public class CarouselView extends HorizontalScrollView {
    private static final int SWIPE_MIN_DISTANCE = 5;
    private int swipMinDistance = SWIPE_MIN_DISTANCE;
    private static final int SWIPE_THRESHOLD_VELOCITY = 300;
    private static final double SIZE_DEFAULT_INTERVAL = 0.2;
    private double sizeInterval = SIZE_DEFAULT_INTERVAL;
    private static final double ALPHA_DEFAULT_INTERVAL = 0.2;
    private double alphaInterval = ALPHA_DEFAULT_INTERVAL;
    private static final double HEIGHT_DEFAULT_INTERVAL = 0;
    private double heightInterval = HEIGHT_DEFAULT_INTERVAL;
    private static final double RADIUS_DEFAULT_INTERVAL = 10000;
    private double radius = RADIUS_DEFAULT_INTERVAL;
    private static final int TOP_DEFAULT_PADDING = 0;
    private int topPadding = TOP_DEFAULT_PADDING;
    private static final int ITEM_WIDTH_DEFAULT_EXTRA = 0;
    private int itemWidthExtra = ITEM_WIDTH_DEFAULT_EXTRA;
    private static final boolean AUTO_SCROLL_TO_CENTER_DEFAULT = true;
    private boolean auto_ScrollToCenter_CarouselView = AUTO_SCROLL_TO_CENTER_DEFAULT;
    private static final boolean DEBUG_DEFAULT = false;
    private boolean DEBUG = DEBUG_DEFAULT;
    private int mOffset = 0;

    public interface Listener {
        void onItemSelectionChanged(CarouselView view, int pos);

        void onItemClicked(CarouselView view, int pos);
    }

    private Listener mListener;
    private LinearLayout mContainer;
    private GestureDetector mGestureDetector;
    private int mItemWidthHint;
    private int mItemHeightHint;
    private int mItemWidth;
    private int mItemHeight;
    private int mItemSelection;
    private Drawable[] mItems;

    public CarouselView(Context context, AttributeSet attrs) {
        super(context, attrs);

        final TypedArray attributes = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CarouselView, 0, 0);

        alphaInterval = attributes.getFloat(R.styleable.CarouselView_alpha_interval, (float) ALPHA_DEFAULT_INTERVAL);
        sizeInterval = attributes.getFloat(R.styleable.CarouselView_size_interval, (float) SIZE_DEFAULT_INTERVAL);
        heightInterval = attributes.getFloat(R.styleable.CarouselView_height_interval, (float) HEIGHT_DEFAULT_INTERVAL);
        radius = attributes.getFloat(R.styleable.CarouselView_radius, (float) RADIUS_DEFAULT_INTERVAL);
        itemWidthExtra = attributes.getInt(R.styleable.CarouselView_item_extra_width, ITEM_WIDTH_DEFAULT_EXTRA);
        swipMinDistance = attributes.getInt(R.styleable.CarouselView_swipe_min_distance, SWIPE_MIN_DISTANCE);
        topPadding = attributes.getInt(R.styleable.CarouselView_top_padding, TOP_DEFAULT_PADDING);
        auto_ScrollToCenter_CarouselView = attributes.getBoolean(R.styleable.CarouselView_auto_ScrollToCenter_CarouselView, AUTO_SCROLL_TO_CENTER_DEFAULT);
        DEBUG = attributes.getBoolean(R.styleable.CarouselView_debug_CarouselView, DEBUG_DEFAULT);

        attributes.recycle();
    }

    public void init(Drawable[] drawables) {
        mItems = drawables;
        mContainer = (LinearLayout) getChildAt(0);
        mItemSelection = -1;
        mItemWidthHint = 0;
        mItemHeightHint = 0;

        for (Drawable item : mItems) {
            ImageView view = new ImageView(getContext());
            //view.setScaleType(ImageView.ScaleType.FIT_CENTER);
            view.setScaleType(ImageView.ScaleType.MATRIX);
            view.setImageDrawable(item);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        int pos = mContainer.indexOfChild(v);
                        if (pos >= 0) {
                            mListener.onItemClicked(CarouselView.this, pos);
                        }
                    }
                }
            });
            mContainer.addView(view);
            mItemWidthHint = Math.max(mItemWidthHint, item.getIntrinsicWidth());
            mItemHeightHint = Math.max(mItemHeightHint, item.getIntrinsicHeight());
        }

        final float minDistance = swipMinDistance * getResources().getDisplayMetrics().density;
        mGestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            private int mOriginItemSelection;

            @Override
            public boolean onDown(MotionEvent e) {
                mOriginItemSelection = mItemSelection;
                return false;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                return false;
//                if (e1 != null && e2 != null && mItemWidth > 0) {
//                    float distance = e2.getX() - e1.getX();
//                    if (Math.abs(distance) >= minDistance && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
//                        int pos = mOriginItemSelection + ((distance > 0) ? -1 : 1);
//                        return snapItemSelection(pos);
//                    }
//                }
//                return false;
            }
        });
    }

    private void updateItems() {

        double currentCenterPos = ((float) mOffset / (float) mItemWidth);
        Matrix matrix = new Matrix();
        for (int i = 0; i < mContainer.getChildCount(); i++) {
            ImageView view = (ImageView) mContainer.getChildAt(i);
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) view.getLayoutParams();
            double sizeScale = 1 - Math.abs((float) i - currentCenterPos) * sizeInterval;
            sizeScale = sizeScale < 0 ? sizeInterval : sizeScale;
            Matrix oldMatrix = view.getMatrix();
            matrix.set(oldMatrix);
            RectF drawableRect = new RectF(0, 0, mItemWidth, mItemHeight);

            double angle = Math.atan((lp.width*(Math.abs((float) i - currentCenterPos)/radius)));
            float deltaHeight = (float)(topPadding - radius * (1-Math.cos(angle)));
            deltaHeight = (float) (deltaHeight + ((1.0 - sizeScale) / 2.0) * lp.height);//vertical center
            deltaHeight -= (float) (Math.abs((float) i - currentCenterPos) * heightInterval);

            float deltaWidth = (float) ((mItemWidth - (lp.width * sizeScale)) / 2.0);//horizontal center
            RectF viewRect = new RectF(deltaWidth, deltaHeight, (float) (lp.width * sizeScale + deltaWidth), (float) (lp.height * sizeScale + deltaHeight));
            matrix.setRectToRect(drawableRect, viewRect, Matrix.ScaleToFit.CENTER);
            view.setImageMatrix(matrix);

            double alphaScale = 1 - Math.abs((float) i - currentCenterPos) * alphaInterval;
            alphaScale = alphaScale < 0 ? 0 : alphaScale;
            view.setImageAlpha((int) (255 * alphaScale));
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        final int topPadding = 0;//getResources().getDimensionPixelOffset(R.dimen.widget_gallery_top_padding);
        int intervalW = itemWidthExtra;

        mItemHeight = h;
        mItemWidth = mItemWidthHint + intervalW;

        if (mContainer != null) {
            for (int i = 0; i < mContainer.getChildCount(); i++) {
                ImageView view = (ImageView) mContainer.getChildAt(i);
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) view.getLayoutParams();
                lp.width = mItemWidth;
                lp.height = mItemHeight;
                view.setLayoutParams(lp);
            }

            int padding = (w - mItemWidth) / 2;
            mContainer.setPadding(padding, topPadding, padding, 0);

            if (mItemSelection >= 0) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        scrollTo(mItemWidth * mItemSelection, 0);
                    }
                });
            }

            updateItems();
        }
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);

        mOffset = l;

        if (mItemWidth <= 0) return;
        int pos = (l + mItemWidth / 2) / mItemWidth;
        if (pos < mItems.length && pos != mItemSelection && mListener != null) {
            mItemSelection = pos;
            mListener.onItemSelectionChanged(this, pos);
        }

        updateItems();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mGestureDetector.onTouchEvent(ev)) {
            return true;
        } else if (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_CANCEL) {
            super.dispatchTouchEvent(ev);
            if (mItemWidth > 0) {
                int pos = (getScrollX() + mItemWidth / 2) / mItemWidth;
                snapItemSelection(pos);
            }
            return true;
        } else {
            return super.dispatchTouchEvent(ev);
        }
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public int getItemSelection() {
        return mItemSelection;
    }

    private boolean snapItemSelection(final int pos) {
        if (mItemWidth > 0 && pos >= 0 && pos < mItems.length && auto_ScrollToCenter_CarouselView) {
            smoothScrollTo(pos * mItemWidth, 0);
            return true;
        }
        return false;
    }

    public void setItemSelection(int pos) {
        if (pos >= 0 && pos < mItems.length && pos != mItemSelection) {
            mItemSelection = pos;
            if (mItemWidth > 0) {
                scrollTo(pos * mItemWidth, 0);
            }
            if (mListener != null) {
                mListener.onItemSelectionChanged(this, pos);
            }
        }
    }

    private Paint mPaint = new Paint();

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (DEBUG == false) return;

        //For DEBUG
        mPaint.setAntiAlias(true);
        canvas.drawColor(Color.RED - mOffset);//FOR DEBUG
        String strDebug = String.valueOf(mOffset);
        mPaint.setColor(Color.BLACK);
        canvas.drawText(strDebug, 20 + mOffset, 20, mPaint);

        //For DEBUG
        for (int i = 0; i < mContainer.getChildCount(); i++) {
            ImageView view = (ImageView) mContainer.getChildAt(i);

            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) view.getLayoutParams();
            strDebug = String.valueOf(lp.width);
            canvas.drawText(strDebug, 20 + mOffset, 60 + i * 20, mPaint);//FOR DEBUG
            strDebug = String.valueOf(lp.height);
            canvas.drawText(strDebug, 60 + mOffset, 60 + i * 20, mPaint);//FOR DEBUG
            view.setLayoutParams(lp);
            Rect rect = new Rect();
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
            view.getLocalVisibleRect(rect);
            rect.left = view.getLeft();
            rect.top = view.getTop();
            rect.right = view.getRight();
            rect.bottom = view.getBottom();
            mPaint.setColor(Color.TRANSPARENT - i * 20);
            canvas.drawRect(rect, mPaint);

        }
    }
}
