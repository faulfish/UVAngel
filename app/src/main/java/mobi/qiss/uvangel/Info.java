package mobi.qiss.uvangel;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import mobi.qiss.carousel.CarouselView;


public class Info extends ActionBarActivity {

    private Drawable[] mDrawables;
    private Drawable[] mDrawableSelects;
    private CarouselView mCarouselView;
    private int mUVIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }

        Intent intent = getIntent();
        mUVIndex = intent.getIntExtra("UVIndex",-1);
        //Toast.makeText(getApplicationContext(), String.valueOf(mUVIndex), Toast.LENGTH_LONG).show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_info, menu);

        mDrawables = new Drawable[12];
        mDrawables[0] = getResources().getDrawable(R.drawable.button_index_0_intro_nor);
        mDrawables[1] = getResources().getDrawable(R.drawable.button_index_1_nor);
        mDrawables[2] = getResources().getDrawable(R.drawable.button_index_2_nor);
        mDrawables[3] = getResources().getDrawable(R.drawable.button_index_3_nor);
        mDrawables[4] = getResources().getDrawable(R.drawable.button_index_4_nor);
        mDrawables[5] = getResources().getDrawable(R.drawable.button_index_5_nor);
        mDrawables[6] = getResources().getDrawable(R.drawable.button_index_6_nor);
        mDrawables[7] = getResources().getDrawable(R.drawable.button_index_7_nor);
        mDrawables[8] = getResources().getDrawable(R.drawable.button_index_8_nor);
        mDrawables[9] = getResources().getDrawable(R.drawable.button_index_9_nor);
        mDrawables[10] = getResources().getDrawable(R.drawable.button_index_10_nor);
        mDrawables[11] = getResources().getDrawable(R.drawable.button_index_11_nor);

        mDrawableSelects = new Drawable[12];
        mDrawableSelects[0] = getResources().getDrawable(R.drawable.button_index_0_intro_prs);
        mDrawableSelects[1] = getResources().getDrawable(R.drawable.button_index_1_prs);
        mDrawableSelects[2] = getResources().getDrawable(R.drawable.button_index_2_prs);
        mDrawableSelects[3] = getResources().getDrawable(R.drawable.button_index_3_prs);
        mDrawableSelects[4] = getResources().getDrawable(R.drawable.button_index_4_prs);
        mDrawableSelects[5] = getResources().getDrawable(R.drawable.button_index_5_prs);
        mDrawableSelects[6] = getResources().getDrawable(R.drawable.button_index_6_prs);
        mDrawableSelects[7] = getResources().getDrawable(R.drawable.button_index_7_prs);
        mDrawableSelects[8] = getResources().getDrawable(R.drawable.button_index_8_prs);
        mDrawableSelects[9] = getResources().getDrawable(R.drawable.button_index_9_prs);
        mDrawableSelects[10] = getResources().getDrawable(R.drawable.button_index_10_prs);
        mDrawableSelects[11] = getResources().getDrawable(R.drawable.button_index_11_prs);

        mCarouselView = (CarouselView) findViewById(R.id.indicatorscrollview);
        if (mCarouselView != null) {
            mCarouselView.setListener(new CarouselView.Listener() {
                @Override
                public void onItemSelectionChanged(CarouselView view, int position) {
                    //mChartView.add(position);
                }

                @Override
                public void onItemClicked(CarouselView view, int position) {
                    //Toast.makeText(getApplicationContext(), String.valueOf(position), Toast.LENGTH_SHORT).show();
                    mUVIndex = position;
                    updateUI();
                }
            });
            mCarouselView.init2(mDrawables, mDrawableSelects);
            mCarouselView.setItemSelection(mUVIndex);
        }

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

        updateUI();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.home) {
            return true;
        }

        onBackPressed();//Fix me:currently, not any else, default back button
        return true;
    }

    @Override
    public void onBackPressed() {
        Intent returnIntent = new Intent();
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
            View rootView = inflater.inflate(R.layout.fragment_info, container, false);
            return rootView;
        }
    }

    private void updateUI() {
        RelativeLayout introlayout = (RelativeLayout) findViewById(R.id.introlayout);
        RelativeLayout indexlayout = (RelativeLayout) findViewById(R.id.indexlayout);
        View barView = findViewById(R.id.indexbar);
        View numberView = findViewById(R.id.indexrecommend);
        View indexnumberView = findViewById(R.id.indexnumber);
        TextView indexpromptView = (TextView)findViewById(R.id.indexprompt);
        TextView indexpromptdescriptionView = (TextView)findViewById(R.id.indexpromptdescription);
        TextView indexpromptdescriptiondetailView = (TextView)findViewById(R.id.indexpromptdescriptiondetail);
        if(mUVIndex == 0) {
            introlayout.setVisibility(View.VISIBLE);
            indexlayout.setVisibility(View.INVISIBLE);
            barView.setBackground(getResources().getDrawable(R.drawable.icon_index_bar_0));
        }
        else if(mUVIndex == 1 || mUVIndex == 2) {
            introlayout.setVisibility(View.INVISIBLE);
            indexlayout.setVisibility(View.VISIBLE);
            barView.setBackground(getResources().getDrawable(R.drawable.icon_index_bar_1_2));
            numberView.setBackground(getResources().getDrawable(R.drawable.icon_index_1_2));
            indexnumberView.setBackground(getResources().getDrawable(R.drawable.icon_index_number_1_2));
            indexpromptView.setText(R.string.uv_prompt_1_2);
            indexpromptView.setTextColor(getResources().getColor(R.color.color_uv_1_2));
            indexpromptdescriptionView.setText(R.string.uv_prompt_description_1_2);
            indexpromptdescriptionView.setTextColor(getResources().getColor(R.color.color_uv_1_2));
            indexpromptdescriptiondetailView.setText(R.string.uv_prompt_description_detail_1_2);
        }
        else if(mUVIndex == 3 || mUVIndex == 4 || mUVIndex == 5) {
            introlayout.setVisibility(View.INVISIBLE);
            indexlayout.setVisibility(View.VISIBLE);
            barView.setBackground(getResources().getDrawable(R.drawable.icon_index_bar_3_4_5));
            numberView.setBackground(getResources().getDrawable(R.drawable.icon_index_3_4_5));
            indexnumberView.setBackground(getResources().getDrawable(R.drawable.icon_index_number_3_4_5));
            indexpromptView.setText(R.string.uv_prompt_3_4_5);
            indexpromptView.setTextColor(getResources().getColor(R.color.color_uv_3_4_5));
            indexpromptdescriptionView.setText(R.string.uv_prompt_description_3_4_5);
            indexpromptdescriptionView.setTextColor(getResources().getColor(R.color.color_uv_3_4_5));
            indexpromptdescriptiondetailView.setText(R.string.uv_prompt_description_detail_3_4_5);
        }
        else if(mUVIndex == 6 || mUVIndex == 7) {
            introlayout.setVisibility(View.INVISIBLE);
            indexlayout.setVisibility(View.VISIBLE);
            barView.setBackground(getResources().getDrawable(R.drawable.icon_index_bar_6_7));
            numberView.setBackground(getResources().getDrawable(R.drawable.icon_index_6_7));
            indexnumberView.setBackground(getResources().getDrawable(R.drawable.icon_index_number_6_7));
            indexpromptView.setText(R.string.uv_prompt_6_7);
            indexpromptView.setTextColor(getResources().getColor(R.color.color_uv_6_7));
            indexpromptdescriptionView.setText(R.string.uv_prompt_description_6_7);
            indexpromptdescriptionView.setTextColor(getResources().getColor(R.color.color_uv_6_7));
            indexpromptdescriptiondetailView.setText(R.string.uv_prompt_description_detail_6_7);
        }
        else if(mUVIndex == 8 || mUVIndex == 9 || mUVIndex == 10) {
            introlayout.setVisibility(View.INVISIBLE);
            indexlayout.setVisibility(View.VISIBLE);
            barView.setBackground(getResources().getDrawable(R.drawable.icon_index_bar_8_9_10));
            numberView.setBackground(getResources().getDrawable(R.drawable.icon_index_8_9_10));
            indexnumberView.setBackground(getResources().getDrawable(R.drawable.icon_index_number_8_9_10));
            indexpromptView.setText(R.string.uv_prompt_8_9_10);
            indexpromptView.setTextColor(getResources().getColor(R.color.color_uv_8_9_10));
            indexpromptdescriptionView.setText(R.string.uv_prompt_description_8_9_10);
            indexpromptdescriptionView.setTextColor(getResources().getColor(R.color.color_uv_8_9_10));
            indexpromptdescriptiondetailView.setText(R.string.uv_prompt_description_detail_8_9_10);
        }
        else if(mUVIndex == 11) {
            introlayout.setVisibility(View.INVISIBLE);
            indexlayout.setVisibility(View.VISIBLE);
            barView.setBackground(getResources().getDrawable(R.drawable.icon_index_bar_11));
            numberView.setBackground(getResources().getDrawable(R.drawable.icon_index_11));
            indexnumberView.setBackground(getResources().getDrawable(R.drawable.icon_index_number_11));
            indexpromptView.setText(R.string.uv_prompt_11);
            indexpromptView.setTextColor(getResources().getColor(R.color.color_uv_11));
            indexpromptdescriptionView.setText(R.string.uv_prompt_description_11);
            indexpromptdescriptionView.setTextColor(getResources().getColor(R.color.color_uv_11));
            indexpromptdescriptiondetailView.setText(R.string.uv_prompt_description_detail_11);
        }
    }
}
