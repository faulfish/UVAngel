package mobi.qiss.uvangel;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.format.Time;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import mobi.qiss.carousel.CarouselView;


public class Spf extends ActionBarActivity {

    private Drawable[] mDrawables;
    private CarouselView mCarouselView;
    private Button mPlusView;
    private int mSPFIndex = 0;
    private int mSPFPLUSIndex = 0;
    private int mPreviousSPFPLUSIndex = mSPFPLUSIndex;
    private int mPreviousSPFIndex = mSPFIndex;

    public void onPlus(View view) {
        String strPlus = "";

        mSPFPLUSIndex++;
        mSPFPLUSIndex = mSPFPLUSIndex % 3;
        for (int i = 0; i <= mSPFPLUSIndex; i++) {
            strPlus += "+";
        }
        if (mPlusView != null)
            mPlusView.setText(strPlus);
    }

    public void onSPFOK(View view) {
        if (mCarouselView != null) {
            mSPFIndex = mCarouselView.getItemSelection();
        }
        onBackPressed();
    }

    public void onSPFCancel(View view) {
        mSPFPLUSIndex = mPreviousSPFPLUSIndex;
        mSPFIndex = mPreviousSPFIndex;
        onBackPressed();
    }

    public BitmapDrawable writeOnDrawable(int drawableId, String text) {

        float w = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 117, getResources().getDisplayMetrics());
        float h = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 117, getResources().getDisplayMetrics());
        float sp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 69, getResources().getDisplayMetrics());

        //Fix me: work around for convert DP to PX.....
        w = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, w, getResources().getDisplayMetrics());
        h = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, h, getResources().getDisplayMetrics());
        sp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, getResources().getDisplayMetrics());

        Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
        Bitmap bm = Bitmap.createBitmap(getResources().getDisplayMetrics(), (int) w, (int) h, conf); // this creates a MUTABLE bitmap
        Canvas canvas = new Canvas(bm);
        //canvas.drawColor(Color.BLUE); //DEBUG ONLY

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTextSize(sp);

        Bitmap bmp = BitmapFactory.decodeResource(getResources(), drawableId);
        Rect orgRect = new Rect(0, 0, bmp.getWidth() - 1, bmp.getHeight() - 1);
        RectF targetRect = new RectF(0, 0, w - 1, h - 1);
        canvas.drawBitmap(bmp, orgRect, targetRect, paint);
        float textWidth = paint.measureText(text, 0, text.length());
        int xPos = (int) ((canvas.getWidth() / 2) - textWidth / 2);
        int yPos = (int) ((canvas.getHeight() / 2) - ((paint.descent() + paint.ascent()) / 2));
        //((textPaint.descent() + textPaint.ascent()) / 2) is the distance from the baseline to the center.

        canvas.drawText(text, xPos, yPos, paint);

        return new BitmapDrawable(bm);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spf);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }

        mDrawables = new Drawable[14];
        mDrawables[0] = writeOnDrawable(R.drawable.button_sunscreen_spf_number, "5");//5
        mDrawables[1] = writeOnDrawable(R.drawable.button_sunscreen_spf_number, "10");//10
        mDrawables[2] = writeOnDrawable(R.drawable.button_sunscreen_spf_number, "15");//15
        mDrawables[3] = writeOnDrawable(R.drawable.button_sunscreen_spf_number, "20");//20
        mDrawables[4] = writeOnDrawable(R.drawable.button_sunscreen_spf_number, "25");//25
        mDrawables[5] = writeOnDrawable(R.drawable.button_sunscreen_spf_number, "30");//30
        mDrawables[6] = writeOnDrawable(R.drawable.button_sunscreen_spf_number, "35");//35
        mDrawables[7] = writeOnDrawable(R.drawable.button_sunscreen_spf_number, "40");//40
        mDrawables[8] = writeOnDrawable(R.drawable.button_sunscreen_spf_number, "45");//45
        mDrawables[9] = writeOnDrawable(R.drawable.button_sunscreen_spf_number, "50");//50
        mDrawables[10] = writeOnDrawable(R.drawable.button_sunscreen_spf_number, "55");//55
        mDrawables[11] = writeOnDrawable(R.drawable.button_sunscreen_spf_number, "60");//50
        mDrawables[12] = writeOnDrawable(R.drawable.button_sunscreen_spf_number, "65");//65
        mDrawables[13] = writeOnDrawable(R.drawable.button_sunscreen_spf_number, "70");//70

        Intent intent = getIntent();
        mSPFIndex = intent.getIntExtra("SPFIndex", -1);
        mSPFPLUSIndex = intent.getIntExtra("SPFPLUSIndex", -1);
        //Toast.makeText(getApplicationContext(), String.valueOf(mSPFIndex), Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_spf, menu);
        mPreviousSPFPLUSIndex = mSPFPLUSIndex;
        mPreviousSPFIndex = mSPFIndex;

        mPlusView = (Button) findViewById(R.id.spfplusbutton);
        String strPlus = "";
        mSPFPLUSIndex = mSPFPLUSIndex % 3;
        for (int i = 0; i <= mSPFPLUSIndex; i++) {
            strPlus += "+";
        }
        if (mPlusView != null)
            mPlusView.setText(strPlus);
        mCarouselView = (CarouselView) findViewById(R.id.indicatorscrollview);
        if (mCarouselView != null) {
            mCarouselView.init(mDrawables);
            mCarouselView.setListener(new CarouselView.Listener() {
                @Override
                public void onItemSelectionChanged(CarouselView view, int position) {
                    //mChartView.add(position);
                }

                @Override
                public void onItemClicked(CarouselView view, int position) {
                    Time time = new Time();
                    time.setToNow();
                    mSPFIndex = position;
                }
            });
        }
        updateUI();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setLogo(R.drawable.button_app_icon);
            actionBar.setDisplayUseLogoEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            GradientDrawable gd = new GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM,
                    new int[]{0xff3fa4b0, 0xff3fa4b0});
            gd.setCornerRadius(0f);
            actionBar.setBackgroundDrawable(gd);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        //default back
        onSPFCancel(null);
        return true;
    }

    @Override
    public void onBackPressed() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("SPFIndex", mSPFIndex);
        returnIntent.putExtra("SPFPLUSIndex", mSPFPLUSIndex);
        setResult(RESULT_OK, returnIntent);
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBackPressed();
            return true;
        }
        return false;
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_spf, container, false);
        }
    }

    private void updateUI() {
        if (mCarouselView != null) {
            mCarouselView.setItemSelection(mSPFIndex);
        }
    }
}
