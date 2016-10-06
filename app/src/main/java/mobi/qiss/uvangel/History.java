package mobi.qiss.uvangel;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.format.Time;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import mobi.qiss.carousel.CarouselView;
import mobi.qiss.chart.ChartView;

class ListAdapter extends ArrayAdapter<ChartView.LOG.UVData> {

    Context mContext;
    private int mSelectedItem = -1;

    public ListAdapter(Context context, int resource, List<ChartView.LOG.UVData> items) {
        super(context, resource, items);
        mContext = context;
    }

    public void setSelection(int selectedItem) {
        mSelectedItem = selectedItem;
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {

            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.history_list_item, null);

        }

        ChartView.LOG.UVData p = getItem(position);

        if (p != null && p.mUVIndex <= 11 && p.mUVIndex > 0) {

            TextView timeView = (TextView) v.findViewById(R.id.time);
            if (timeView != null) {
                Time time = new Time();
                time.set(p.mTime.getTime());
                String myFormat = "hh:mm a"; // your own format
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
                Date date = new Date(time.toMillis(true));
                String formatedTime = sdf.format(date); //format your time
                timeView.setText(formatedTime);
            }
            TextView uvView = (TextView) v.findViewById(R.id.uv);
            if (uvView != null) {
                uvView.setText(String.format("UV %d", p.mUVIndex));
                if (p.mUVIndex >= 11)
                    uvView.setTextColor(mContext.getResources().getColor(R.color.color_uv_11));
                else if (p.mUVIndex >= 8)
                    uvView.setTextColor(mContext.getResources().getColor(R.color.color_uv_8_9_10));
                else if (p.mUVIndex >= 6)
                    uvView.setTextColor(mContext.getResources().getColor(R.color.color_uv_6_7));
                else if (p.mUVIndex >= 3)
                    uvView.setTextColor(mContext.getResources().getColor(R.color.color_uv_3_4_5));
                else //if(p.mUVIndex >= 1)
                    uvView.setTextColor(mContext.getResources().getColor(R.color.color_uv_1_2));
            }
            TextView spfView = (TextView) v.findViewById(R.id.spf);
            if (spfView != null) {
                spfView.setText(String.format("SPF %d", (p.mSPFIndex + 1) * 5));
            }
            TextView spfplusView = (TextView) v.findViewById(R.id.pa);
            if (spfplusView != null) {
                String strPlus = "PA ";
                for (int i = 0; i <= p.mPlusIndex; i++)
                    strPlus += "+";
                spfplusView.setText(strPlus);
            }
        }

        if (position == mSelectedItem) {
            // set your color
            v.setBackgroundColor(0xff6ccdd9);
        } else {
            v.setBackgroundColor(0xffcfd0d1);
        }

        return v;

    }
}

public class History extends ActionBarActivity {

    private ArrayList<ChartView.LOG.UVData> itemArrey;
    private ListAdapter mItemAdapter;
    private ListView mListView;
    TextView mDateView;// = (TextView) findViewById(R.id.date);
    Date mFirstDate = null;
    ChartView mChartView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_history, menu);

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

        setUpView();
        Map<Date, ChartView.LOG.UVData> logs = ChartView.LOG.get(getApplicationContext());
        for (Map.Entry<Date, ChartView.LOG.UVData> entry : logs.entrySet()) {
            addItemList(entry);
            if (mFirstDate == null)
                mFirstDate = entry.getKey();
        }

        mDateView = (TextView) findViewById(R.id.date);
        if (logs.size() == 0) {
            if (mDateView != null)
                mDateView.setText("");
        } else {
            if (mDateView != null && mFirstDate != null) {
                Time time = new Time();
                time.set(mFirstDate.getTime());
                mDateView.setText(time.format("%Y / %m / %d"));
            }
        }

        mChartView = (ChartView) findViewById(R.id.logview);
        mChartView.setListener(new ChartView.Listener() {
            @Override
            public void onItemSelectionChanged(ChartView view, int position) {
                TextView dateView = (TextView) findViewById(R.id.date);
                dateView.setText(String.valueOf(position));

                Map<Date, ChartView.LOG.UVData> logs = ChartView.LOG.get(getApplicationContext());
                if (logs.size() > 0) {
                    if (mDateView != null && mFirstDate != null) {
                        Time time = new Time();
                        time.set(mFirstDate.getTime() + position * 1000 * 60 * 60 * 24);
                        dateView.setText(time.format("%Y / %m / %d"));
                    }
                }
            }

            @Override
            public void onItemClicked(ChartView view, int position) {
                mListView.setSelection(itemArrey.size() - position - 1);
                mItemAdapter.setSelection(itemArrey.size() - position - 1);
                mListView.smoothScrollToPosition(itemArrey.size() - position - 1);
            }
        });

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
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        Intent returnIntent = new Intent();
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
            return inflater.inflate(R.layout.fragment_history, container, false);
        }
    }

    private void setUpView() {
        mListView = (ListView) this.findViewById(R.id.listView_items);
        itemArrey = new ArrayList<ChartView.LOG.UVData>();
        itemArrey.clear();
        mItemAdapter = new ListAdapter(this, R.layout.history_list_item, itemArrey);
        mListView.setAdapter(mItemAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View item, int position, long id) {
                ChartView.LOG.UVData uvData = mItemAdapter.getItem(position);
                mChartView.setItemSelection(itemArrey.size() - position - 1);
                mItemAdapter.setSelection(position);
            }
        });

    }

    protected void addItemList(Map.Entry<Date, ChartView.LOG.UVData> entry) {
        Date datetime = entry.getKey();
        Time time = new Time();
        time.set(datetime.getTime());
        ChartView.LOG.UVData data = new ChartView.LOG.UVData();
        data = entry.getValue();
        if (data.mUVIndex <= 11 && data.mUVIndex > 0)
            itemArrey.add(0, data);
        mItemAdapter.notifyDataSetChanged();

    }
}
