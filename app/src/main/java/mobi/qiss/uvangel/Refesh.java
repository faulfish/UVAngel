package mobi.qiss.uvangel;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.ImageView;


public class Refesh extends ActionBarActivity {

    private int mUVIndex = 0;
    public static final int MEG_UPDATE = 19528;
    private int mIndex = 0;
    private View mRefreshView;
    Matrix mMatrix = new Matrix();
    private View mLightView;
    private ImageView mImageAnimView;
    private Drawable mWaveBG[] = new Drawable[5];
    private Drawable mLightBG[] = new Drawable[4];
    Handler threadHandler;
    HandlerThread worker = new HandlerThread("updateView");
    private Runnable updateViewTask = new Runnable() {
        public void run() {
            mIndex++;
            mIndex = mIndex % 360;
            Message m = new Message();
            m.what = MEG_UPDATE;
            if (mHandler != null)
                mHandler.sendMessage(m);
            threadHandler.postDelayed(updateViewTask, Math.abs(100));
        }
    };

    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MEG_UPDATE:
                    if (mRefreshView != null && mIndex % 5 == 0)
                        mRefreshView.setBackground(mWaveBG[(mIndex/5) % 5]);
                    if (mLightView != null && mIndex % 5 == 0)
                        mLightView.setBackground(mLightBG[(mIndex/5) % 4]);
                    if (mImageAnimView != null) {
                        mMatrix.postRotate((float) 10, mImageAnimView.getWidth() / 2, mImageAnimView.getHeight() / 2);
                        mImageAnimView.setImageMatrix(mMatrix);
                    }
                    break;

            }
            super.handleMessage(msg);
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_refesh);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setLogo(R.drawable.button_app_icon);
            actionBar.setDisplayUseLogoEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        Intent intent = getIntent();
        mUVIndex = intent.getIntExtra("UVIndex",-1);

        worker.start();
        threadHandler = new Handler(worker.getLooper());
        threadHandler.postDelayed(updateViewTask, Math.abs(1));
    }

    public void onREFRESHCancel(View view) {
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_refesh, menu);

        mImageAnimView = (ImageView) findViewById(R.id.refreshanim);
        mImageAnimView.setScaleType(ImageView.ScaleType.MATRIX);   //required
        mImageAnimView.setImageDrawable(getResources().getDrawable(R.drawable.icon_updating_uv_updating));
        mRefreshView = findViewById(R.id.wavebg);
        mLightView = findViewById(R.id.lightbg);
        mLightBG[0] = getResources().getDrawable(R.drawable.light0);
        mLightBG[1] = getResources().getDrawable(R.drawable.light1);
        mLightBG[2] = getResources().getDrawable(R.drawable.light2);
        mLightBG[3] = getResources().getDrawable(R.drawable.light3);
        mWaveBG[0] = getResources().getDrawable(R.drawable.uv_wave1);
        mWaveBG[1] = getResources().getDrawable(R.drawable.uv_wave2);
        mWaveBG[2] = getResources().getDrawable(R.drawable.uv_wave3);
        mWaveBG[3] = getResources().getDrawable(R.drawable.uv_wave4);
        mWaveBG[4] = getResources().getDrawable(R.drawable.uv_wave5);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
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
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                onBackPressed();//Fix me:currently, not any else, default back button
                return true;
        }

        onBackPressed();//Fix me:currently, not any else, default back button
        return true;
    }

    @Override
    public void onBackPressed() {
        Intent returnIntent = new Intent();
        mUVIndex++;//Fix me
        mUVIndex = mUVIndex % 12;//Fix me
        mUVIndex = mUVIndex == 0 ? 1 : mUVIndex;//Fix me
        returnIntent.putExtra("UVIndex",mUVIndex);
        setResult(RESULT_OK,returnIntent);
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if(keyCode == KeyEvent.KEYCODE_BACK) {
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
            View rootView = inflater.inflate(R.layout.fragment_refesh, container, false);
            return rootView;
        }
    }
}
