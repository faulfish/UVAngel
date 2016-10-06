package mobi.qiss.uvangel;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.GradientDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Vibrator;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.ShareActionProvider;
import android.text.format.Time;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import mobi.qiss.chart.ChartView;
import mobi.qiss.countdowntimer.CountdownTimer;


public class MainActivity extends ActionBarActivity {

    private static final int RESULT_SETTING = 2;
    private static final int RESULT_REFRESH = 3;
    private static final int RESULT_SHARE = 4;
    private static final int RESULT_HISTORY = 5;
    private static final int RESULT_SPF = 6;
    private static final int RESULT_INFO = 7;

//    * @param minTime minimum time interval between location updates, in milliseconds
//    * @param minDistance minimum distance between location updates, in meters
    private static final int LOCATION_REFRESH_TIME = 5000;
    private static final int LOCATION_REFRESH_DISTANCE = 10;

    private ShareActionProvider mShareActionProvider;
    private LocationManager mLocationManager;
    private String mLocation = new String();
    StringBuffer mBuffer;

    //Keep log
    private String mLogFileName = LogFileName_DEFAULT;
    private static final String LogFileName_DEFAULT = "LogFile";
    private static final String ShareFileName_DEFAULT = "ShareUV.png";
    private static String mShareFileName = ShareFileName_DEFAULT;
    private Map<Date, Integer> mLogs = null;

    private int mSunBurnFactor = 0;
    private int mUVIndex = 11;
    private int mSPFIndex = 0;
    private int mSPFPLUSIndex = 0;
    private boolean mBeginForProtect = false;
    private boolean mBeginForProtectNotification = false;
    Time mProtectStartTime;
    long mProtectLeftTime;
    long mLastUpdateTime = 0;
    private CountdownTimer mCountdownTimer;
    TextView mLeftTextView;
    TextView mBeginTextView;
    TextView mEndTextView;
    TextView mStopView;

    //For draw
    String mStringUVPrompt_1_2;// = getResources().getString(R.string.uv_prompt_1_2);
    String mStringUVPrompt_3_4_5;// = getResources().getString(R.string.uv_prompt_3_4_5);
    String mStringUVPrompt_6_7;// = getResources().getString(R.string.uv_prompt_6_7);
    String mStringUVPrompt_8_9_10;// = getResources().getString(R.string.uv_prompt_8_9_10);
    String mStringUVPrompt_11;// = getResources().getString(R.string.uv_prompt_11);
    View uvinfoBGView;// = findViewById(R.id.bg11);
    ImageView uvinfoIndexView;// = (ImageView) findViewById(R.id.uvinfoIndex);
    ImageView uvprompticonGlassView;// = (ImageView) findViewById(R.id.uvprompticon1);
    ImageView uvprompticonHatView;// = (ImageView) findViewById(R.id.uvprompticon2);
    ImageView uvprompticonCoverView;// = (ImageView) findViewById(R.id.uvprompticon3);
    ImageView uvprompticonHouseView;// = (ImageView) findViewById(R.id.uvprompticon4);
    TextView uvpromptView;// = (TextView) findViewById(R.id.uvprompt);
    TextView uvpromptdescriptionView;// = (TextView) findViewById(R.id.uvpromptdescription);
    TextView uvpromptdescription1View;// = (TextView) findViewById(R.id.uvpromptdescription1);
    TextView spfView;// = (TextView) findViewById(R.id.spfnumber);
    TextView spfplusView;// = (TextView) findViewById(R.id.spfplus);
    TextView mLasttimeView;
    View mLasttimeClockView;
    RelativeLayout mLayoutShare;// = (RelativeLayout) findViewById(R.id.layout2);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_REFRESH_TIME,
                LOCATION_REFRESH_DISTANCE, mLocationListener);//GPS_PROVIDER//NETWORK_PROVIDER

        mLayoutShare = (RelativeLayout) findViewById(R.id.layout1);
        mLayoutShare.setDrawingCacheEnabled(true);
        mStringUVPrompt_1_2 = getResources().getString(R.string.uv_prompt_1_2);
        mStringUVPrompt_3_4_5 = getResources().getString(R.string.uv_prompt_3_4_5);
        mStringUVPrompt_6_7 = getResources().getString(R.string.uv_prompt_6_7);
        mStringUVPrompt_8_9_10 = getResources().getString(R.string.uv_prompt_8_9_10);
        mStringUVPrompt_11 = getResources().getString(R.string.uv_prompt_11);
        uvinfoBGView = findViewById(R.id.bg11);
        uvinfoIndexView = (ImageView) findViewById(R.id.uvinfoIndex);
        uvprompticonGlassView = (ImageView) findViewById(R.id.uvprompticon1);
        uvprompticonHatView = (ImageView) findViewById(R.id.uvprompticon2);
        uvprompticonCoverView = (ImageView) findViewById(R.id.uvprompticon3);
        uvprompticonHouseView = (ImageView) findViewById(R.id.uvprompticon4);
        uvpromptView = (TextView) findViewById(R.id.uvprompt);
        uvpromptdescriptionView = (TextView) findViewById(R.id.uvpromptdescription);
        uvpromptdescription1View = (TextView) findViewById(R.id.uvpromptdescription1);
        spfView = (TextView) findViewById(R.id.spfnumber);
        spfplusView = (TextView) findViewById(R.id.spfplus);
        mLasttimeView = (TextView) findViewById(R.id.lasttime);
        mLasttimeClockView = findViewById(R.id.lasttimeclock);
        updateUI();
    }

    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            String finalAddress = "";

            Geocoder geoCoder = new Geocoder(getApplicationContext(), Locale.ENGLISH);//Fix me:chinese will get empty
            StringBuilder builder = new StringBuilder();
            try {
                double longitude = location.getLongitude();
                double latitude = location.getLatitude();
                List<Address> address = geoCoder.getFromLocation(latitude, longitude, 1);
                int maxLines = address.get(0).getMaxAddressLineIndex();
                for (int i = 0; i < maxLines; i++) {
                    String addressStr = address.get(0).getAddressLine(i);
                    builder.append(addressStr);
                    builder.append(" ");
                }
                finalAddress = builder.toString(); //This is the complete address.
                int pos = finalAddress.lastIndexOf(", ");
                if (pos > -1) {
                    mLocation = finalAddress.substring(pos + 2);
                    mBuffer = new StringBuffer(mLocation.length() + 1);
                    for (int i = 0; i < mLocation.length(); i++) {
                        char c = mLocation.charAt(i);
                        mBuffer.append(c);
                    }
                    mBuffer.append('\0');
                }
            } catch (IOException e) {
                //Nothing
            } catch (NullPointerException e) {
                //Nothing
            }
            //Toast.makeText(getApplicationContext(), mLocation, Toast.LENGTH_LONG).show();
            TextView locView = (TextView) findViewById(R.id.location2);
            if (locView != null) {
                //locView.setTextColor(Color.BLUE);
                locView.setText(mBuffer.toString());
                locView.invalidate();
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
            //Toast.makeText(getApplicationContext(), provider,Toast.LENGTH_LONG).show();
        }

        @Override
        public void onProviderEnabled(String provider) {
            //Toast.makeText(getApplicationContext(), provider,Toast.LENGTH_LONG).show();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            //
        }
    };

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
//            actionBar.setDisplayOptions(actionBar.getDisplayOptions()
//                    | ActionBar.DISPLAY_SHOW_CUSTOM);
//            ImageView imageView = new ImageView(actionBar.getThemedContext());
//            imageView.setScaleType(ImageView.ScaleType.CENTER);
//            imageView.setImageResource(R.drawable.mainicon);
//            ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(
//                    ActionBar.LayoutParams.WRAP_CONTENT,
//                    ActionBar.LayoutParams.WRAP_CONTENT, Gravity.RIGHT
//                    | Gravity.CENTER_VERTICAL);
//            layoutParams.rightMargin = 40;
//            imageView.setLayoutParams(layoutParams);
//            actionBar.setCustomView(imageView);

            GradientDrawable gd = new GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM,
                    new int[]{0xff3fa4b0, 0xff3fa4b0});
            gd.setCornerRadius(0f);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setLogo(R.drawable.button_app_icon);
            actionBar.setDisplayUseLogoEnabled(true);
            actionBar.setBackgroundDrawable(gd);
            actionBar.setDisplayShowHomeEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem shareItem = menu.findItem(R.id.action_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
        mShareActionProvider.setShareIntent(getDefaultIntent());
        mShareActionProvider.setOnShareTargetSelectedListener(new ShareActionProvider.OnShareTargetSelectedListener() {
            @Override
            public boolean onShareTargetSelected(ShareActionProvider actionProvider, Intent intent) {
                ConvertToBitmap(mLayoutShare);
                return true;
            }
        });

        mLeftTextView = (TextView) findViewById(R.id.leftTime);
        mBeginTextView = (TextView) findViewById(R.id.beginTime);
        mEndTextView = (TextView) findViewById(R.id.endTime);
        mCountdownTimer = (CountdownTimer) findViewById(R.id.spfcountdowntimer);
        mCountdownTimer.registerListener(new CountdownTimer.Listener() {
            @Override
            public void onUpdate(CountdownTimer view, long timeMillis) {
                mProtectLeftTime = timeMillis;
                long secondT = (timeMillis / 1000) % (60);
                long minuteT = (timeMillis / (1000 * 60) % 60);
                long hourT = (timeMillis / 1000) / (60 * 60);
                String str = String.format("%01d:%02d:%02d", hourT, minuteT, secondT);

                if (mLeftTextView != null) mLeftTextView.setText(str);
                sendBroadcast();

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                boolean isNotification = prefs.getBoolean("checkbox_preference_notification", false);
                if(!mBeginForProtectNotification && mBeginForProtect && hourT == 0 && minuteT == 15) {
                    mBeginForProtectNotification = true;
                    sendNotification();
                }
            }

            @Override
            public void onFinish(CountdownTimer view) {
                //Toast.makeText(getApplicationContext(), "Finish", Toast.LENGTH_LONG).show();
                mBeginForProtect = false;
                mBeginForProtectNotification = false;
                mCountdownTimer.enableDrag(false);

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                boolean isSoundPlay = prefs.getBoolean("checkbox_preference_sound", false);
                boolean isVibrate = prefs.getBoolean("checkbox_preference_vibrate", false);

                try {
                    if (isSoundPlay) {
                        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                        r.play();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (isVibrate) {
                    Vibrator v = (Vibrator) getApplicationContext().getSystemService(getApplicationContext().VIBRATOR_SERVICE);
                    // Vibrate for 500 milliseconds
                    v.vibrate(500);
                }

                updateUI();
                sendBroadcast();
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Defines a default (dummy) share intent to initialize the action provider.
     * However, as soon as the actual content to be used in the intent
     * is known or changes, you must update the share intent by again calling
     * mShareActionProvider.setShareIntent()
     */
    private Intent getDefaultIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/*");
        File file = getShareFile();
        Uri uri = Uri.fromFile(file);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        return shareIntent;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_setting:
                Intent i = new Intent(this, Settings.class);
                startActivityForResult(i, RESULT_SETTING);
                return true;
            case R.id.action_history:
                Intent historyIntent = new Intent(this, History.class);
                startActivityForResult(historyIntent, RESULT_HISTORY);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void infoButtonClick(View view) {
        Intent infoIntent = new Intent(this, Info.class);
        infoIntent.putExtra("UVIndex", mUVIndex);
        startActivityForResult(infoIntent, RESULT_INFO);
    }

    public void spfButtonClick(View view) {
        Intent spfIntent = new Intent(this, Spf.class);
        spfIntent.putExtra("SPFIndex", mSPFIndex);
        spfIntent.putExtra("SPFPLUSIndex", mSPFPLUSIndex);
        startActivityForResult(spfIntent, RESULT_SPF);
    }

    public void refreshButtonClick(View view) {
        Intent infoIntent = new Intent(this, Refesh.class);
        infoIntent.putExtra("UVIndex", mUVIndex);
        startActivityForResult(infoIntent, RESULT_REFRESH);
    }

    public void stopButtonClick(View view) {
        if (mBeginForProtect) {
            mBeginForProtect = false;
            mBeginForProtectNotification = false;
            if (mCountdownTimer != null) {
                mCountdownTimer.stop();
            }
            if (mLeftTextView != null) mLeftTextView.setText(R.string.uv_default_zero_time);
            mProtectLeftTime = 0;
            mCountdownTimer.enableDrag(false);
        } else {
            mBeginForProtect = true;
            mBeginForProtectNotification = false;
            mCountdownTimer.enableDrag(true);

            int protectTime = 0;//milliseconds
            protectTime = (mSPFIndex + 1) * 5 * mSunBurnFactor * 60 * 1000;
            protectTime = protectTime > ChartView.MAXProtectTime ? ChartView.MAXProtectTime : protectTime;

            long secondT = (protectTime / 1000) % (60);
            long minuteT = (protectTime / (1000 * 60) % 60);
            long hourT = (protectTime / 1000) / (60 * 60);
            String str = String.format("%02d:%02d:%02d", hourT, minuteT, secondT);
            mBeginTextView.setText(str);
            if (mCountdownTimer != null) {
                mCountdownTimer.setDuration(protectTime, protectTime);
                mCountdownTimer.start(-1);
            }

            if (mProtectStartTime == null)
                mProtectStartTime = new Time();
            mProtectStartTime.setToNow();
            ChartView.LOG.add(mUVIndex, mProtectStartTime, mSPFIndex, mSPFPLUSIndex, mLocation, getApplicationContext());
        }
        updateUI();
        sendBroadcast();
    }

    void sendBroadcast() {
        //Toast.makeText(getApplicationContext(), String.valueOf(mUVIndex), Toast.LENGTH_LONG).show();
        Intent intent = new Intent();
        intent.setAction("mobi.qiss.uvangel.update");
        intent.putExtra("UVIndex", mUVIndex);
        intent.putExtra("SPFIndex", mSPFIndex);
        intent.putExtra("SPFPLUSIndex", mSPFPLUSIndex);
        intent.putExtra("SPFProtect", mBeginForProtect);//boolean
        intent.putExtra("SPFProtectStartTime", mProtectStartTime == null ? 0 : mProtectStartTime.toMillis(true));//long
        intent.putExtra("SPFProtectLeftTime", mProtectLeftTime);//long
        intent.putExtra("SPFLastUpdateTime", mLastUpdateTime);//long
        intent.putExtra("Location", mLocation);
        sendBroadcast(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == RESULT_INFO) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
            }
        } else if (requestCode == RESULT_SPF) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                mSPFIndex = data.getIntExtra("SPFIndex", -1);
                mSPFPLUSIndex = data.getIntExtra("SPFPLUSIndex", -1);
                //Toast.makeText(getApplicationContext(), String.valueOf(mSPFIndex), Toast.LENGTH_LONG).show();
                updateUI();
                sendBroadcast();
            }
        } else if (requestCode == RESULT_HISTORY) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                //Toast.makeText(getApplicationContext(), String.valueOf(mUVIndex), Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == RESULT_SETTING) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                //Toast.makeText(getApplicationContext(), String.valueOf(mUVIndex), Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == RESULT_REFRESH) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                mUVIndex = data.getIntExtra("UVIndex", -1);//Fix me, until UV sensor ready
                Time time = new Time();
                time.setToNow();
                mLastUpdateTime = time.toMillis(true);
                String myFormat = "hh:mm a"; // your own format
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
                Date date = new Date(time.toMillis(true));
                String  formated_time = sdf.format(date); //format your time
                mLasttimeView.setText(formated_time);
                mLasttimeClockView.setVisibility(View.VISIBLE);
                updateUI();
                sendBroadcast();
            }
        }
    }

    private void updateUI() {
        if (mStopView == null) mStopView = (TextView) findViewById(R.id.stopbuttontext);
        spfView.setText(String.valueOf((mSPFIndex + 1) * 5));
        String strPlus = "";
        mSPFPLUSIndex = mSPFPLUSIndex % 3;
        for (int i = 0; i <= mSPFPLUSIndex; i++)
            strPlus += "+";
        spfplusView.setText(strPlus);
        mStopView.setText(mBeginForProtect ? R.string.uv_stop_button : R.string.uv_start_button);
        if (mUVIndex >= 11) {
            mSunBurnFactor = 3;
            uvinfoIndexView.setImageDrawable(getResources().getDrawable(R.drawable.uv_index_mind_11));
            uvinfoBGView.setBackground(getResources().getDrawable(R.drawable.bg_uv_11));
            uvprompticonGlassView.setBackground(getResources().getDrawable(R.drawable.icon_main_screen_sunglasses_nor));
            uvprompticonHatView.setBackground(getResources().getDrawable(R.drawable.icon_main_screen_hat_nor));
            uvprompticonCoverView.setBackground(getResources().getDrawable(R.drawable.icon_main_screen_cover_nor));
            uvprompticonHouseView.setBackground(getResources().getDrawable(R.drawable.icon_main_screen_house_nor));
            String strBuffer = mStringUVPrompt_11;
            strBuffer = !strBuffer.contains("UV") ? strBuffer : strBuffer.substring(strBuffer.indexOf("UV") + 3);
            uvpromptView.setText(strBuffer.toUpperCase());
            uvpromptdescriptionView.setText(R.string.uv_prompt_description_11);
            uvpromptdescription1View.setText(R.string.uv_prompt_recommend1_11);
        } else if (mUVIndex >= 10) {
            mSunBurnFactor = 5;
            uvinfoIndexView.setImageDrawable(getResources().getDrawable(R.drawable.uv_index_mind_10));
            uvinfoBGView.setBackground(getResources().getDrawable(R.drawable.bg_uv_8_9_10));
            uvprompticonGlassView.setBackground(getResources().getDrawable(R.drawable.icon_main_screen_sunglasses_nor));
            uvprompticonHatView.setBackground(getResources().getDrawable(R.drawable.icon_main_screen_hat_nor));
            uvprompticonCoverView.setBackground(getResources().getDrawable(R.drawable.icon_main_screen_cover_nor));
            uvprompticonHouseView.setBackground(getResources().getDrawable(R.drawable.icon_main_screen_house_nor));
            String strBuffer = mStringUVPrompt_8_9_10;
            strBuffer = !strBuffer.contains("UV") ? strBuffer : strBuffer.substring(strBuffer.indexOf("UV") + 3);
            uvpromptView.setText(strBuffer.toUpperCase());
            uvpromptdescriptionView.setText(R.string.uv_prompt_description_8_9_10);
            uvpromptdescription1View.setText(R.string.uv_prompt_recommend1_8_9_10);
        } else if (mUVIndex >= 9) {
            mSunBurnFactor = 5;
            uvinfoIndexView.setImageDrawable(getResources().getDrawable(R.drawable.uv_index_mind_9));
            uvinfoBGView.setBackground(getResources().getDrawable(R.drawable.bg_uv_8_9_10));
            uvprompticonGlassView.setBackground(getResources().getDrawable(R.drawable.icon_main_screen_sunglasses_nor));
            uvprompticonHatView.setBackground(getResources().getDrawable(R.drawable.icon_main_screen_hat_nor));
            uvprompticonCoverView.setBackground(getResources().getDrawable(R.drawable.icon_main_screen_cover_nor));
            uvprompticonHouseView.setBackground(getResources().getDrawable(R.drawable.icon_main_screen_house_nor));
            String strBuffer = mStringUVPrompt_8_9_10;
            strBuffer = !strBuffer.contains("UV") ? strBuffer : strBuffer.substring(strBuffer.indexOf("UV") + 3);
            uvpromptView.setText(strBuffer.toUpperCase());
            uvpromptdescriptionView.setText(R.string.uv_prompt_description_8_9_10);
            uvpromptdescription1View.setText(R.string.uv_prompt_recommend1_8_9_10);
        } else if (mUVIndex >= 8) {
            mSunBurnFactor = 5;
            uvinfoIndexView.setImageDrawable(getResources().getDrawable(R.drawable.uv_index_mind_8));
            uvinfoBGView.setBackground(getResources().getDrawable(R.drawable.bg_uv_8_9_10));
            uvprompticonGlassView.setBackground(getResources().getDrawable(R.drawable.icon_main_screen_sunglasses_nor));
            uvprompticonHatView.setBackground(getResources().getDrawable(R.drawable.icon_main_screen_hat_nor));
            uvprompticonCoverView.setBackground(getResources().getDrawable(R.drawable.icon_main_screen_cover_nor));
            uvprompticonHouseView.setBackground(getResources().getDrawable(R.drawable.icon_main_screen_house_nor));
            String strBuffer = mStringUVPrompt_8_9_10;
            strBuffer = !strBuffer.contains("UV") ? strBuffer : strBuffer.substring(strBuffer.indexOf("UV") + 3);
            uvpromptView.setText(strBuffer.toUpperCase());
            uvpromptdescriptionView.setText(R.string.uv_prompt_description_8_9_10);
            uvpromptdescription1View.setText(R.string.uv_prompt_recommend1_8_9_10);
        } else if (mUVIndex >= 7) {
            mSunBurnFactor = 10;
            uvinfoIndexView.setImageDrawable(getResources().getDrawable(R.drawable.uv_index_mind_7));
            uvinfoBGView.setBackground(getResources().getDrawable(R.drawable.bg_uv_6_7));
            uvprompticonGlassView.setBackground(getResources().getDrawable(R.drawable.icon_main_screen_sunglasses_nor));
            uvprompticonHatView.setBackground(getResources().getDrawable(R.drawable.icon_main_screen_hat_nor));
            uvprompticonCoverView.setBackground(getResources().getDrawable(R.drawable.icon_main_screen_cover_nor));
            uvprompticonHouseView.setBackground(getResources().getDrawable(R.drawable.icon_main_screen_house_dis));
            String strBuffer = mStringUVPrompt_6_7;
            strBuffer = !strBuffer.contains("UV") ? strBuffer : strBuffer.substring(strBuffer.indexOf("UV") + 3);
            uvpromptView.setText(strBuffer.toUpperCase());
            uvpromptdescriptionView.setText(R.string.uv_prompt_description_6_7);
            uvpromptdescription1View.setText(R.string.uv_prompt_recommend1_6_7);
        } else if (mUVIndex >= 6) {
            mSunBurnFactor = 10;
            uvinfoIndexView.setImageDrawable(getResources().getDrawable(R.drawable.uv_index_mind_6));
            uvinfoBGView.setBackground(getResources().getDrawable(R.drawable.bg_uv_6_7));
            uvprompticonGlassView.setBackground(getResources().getDrawable(R.drawable.icon_main_screen_sunglasses_nor));
            uvprompticonHatView.setBackground(getResources().getDrawable(R.drawable.icon_main_screen_hat_nor));
            uvprompticonCoverView.setBackground(getResources().getDrawable(R.drawable.icon_main_screen_cover_nor));
            uvprompticonHouseView.setBackground(getResources().getDrawable(R.drawable.icon_main_screen_house_dis));
            String strBuffer = mStringUVPrompt_6_7;
            strBuffer = !strBuffer.contains("UV") ? strBuffer : strBuffer.substring(strBuffer.indexOf("UV") + 3);
            uvpromptView.setText(strBuffer.toUpperCase());
            uvpromptdescriptionView.setText(R.string.uv_prompt_description_6_7);
            uvpromptdescription1View.setText(R.string.uv_prompt_recommend1_6_7);
        } else if (mUVIndex >= 5) {
            mSunBurnFactor = 15;
            uvinfoIndexView.setImageDrawable(getResources().getDrawable(R.drawable.uv_index_mind_5));
            uvinfoBGView.setBackground(getResources().getDrawable(R.drawable.bg_uv_3_4_5));
            uvprompticonGlassView.setBackground(getResources().getDrawable(R.drawable.icon_main_screen_sunglasses_nor));
            uvprompticonHatView.setBackground(getResources().getDrawable(R.drawable.icon_main_screen_hat_nor));
            uvprompticonCoverView.setBackground(getResources().getDrawable(R.drawable.icon_main_screen_cover_nor));
            uvprompticonHouseView.setBackground(getResources().getDrawable(R.drawable.icon_main_screen_house_dis));
            String strBuffer = mStringUVPrompt_3_4_5;
            strBuffer = !strBuffer.contains("UV") ? strBuffer : strBuffer.substring(strBuffer.indexOf("UV") + 3);
            uvpromptView.setText(strBuffer.toUpperCase());
            uvpromptdescriptionView.setText(R.string.uv_prompt_description_3_4_5);
            uvpromptdescription1View.setText(R.string.uv_prompt_recommend1_3_4_5);
        } else if (mUVIndex >= 4) {
            mSunBurnFactor = 15;
            uvinfoIndexView.setImageDrawable(getResources().getDrawable(R.drawable.uv_index_mind_4));
            uvinfoBGView.setBackground(getResources().getDrawable(R.drawable.bg_uv_3_4_5));
            uvprompticonGlassView.setBackground(getResources().getDrawable(R.drawable.icon_main_screen_sunglasses_nor));
            uvprompticonHatView.setBackground(getResources().getDrawable(R.drawable.icon_main_screen_hat_nor));
            uvprompticonCoverView.setBackground(getResources().getDrawable(R.drawable.icon_main_screen_cover_nor));
            uvprompticonHouseView.setBackground(getResources().getDrawable(R.drawable.icon_main_screen_house_dis));
            String strBuffer = mStringUVPrompt_3_4_5;
            strBuffer = !strBuffer.contains("UV") ? strBuffer : strBuffer.substring(strBuffer.indexOf("UV") + 3);
            uvpromptView.setText(strBuffer.toUpperCase());
            uvpromptdescriptionView.setText(R.string.uv_prompt_description_3_4_5);
            uvpromptdescription1View.setText(R.string.uv_prompt_recommend1_3_4_5);
        } else if (mUVIndex >= 3) {
            mSunBurnFactor = 15;
            uvinfoIndexView.setImageDrawable(getResources().getDrawable(R.drawable.uv_index_mind_3));
            uvinfoBGView.setBackground(getResources().getDrawable(R.drawable.bg_uv_3_4_5));
            uvprompticonGlassView.setBackground(getResources().getDrawable(R.drawable.icon_main_screen_sunglasses_nor));
            uvprompticonHatView.setBackground(getResources().getDrawable(R.drawable.icon_main_screen_hat_nor));
            uvprompticonCoverView.setBackground(getResources().getDrawable(R.drawable.icon_main_screen_cover_nor));
            uvprompticonHouseView.setBackground(getResources().getDrawable(R.drawable.icon_main_screen_house_dis));
            String strBuffer = mStringUVPrompt_3_4_5;
            strBuffer = !strBuffer.contains("UV") ? strBuffer : strBuffer.substring(strBuffer.indexOf("UV") + 3);
            uvpromptView.setText(strBuffer.toUpperCase());
            uvpromptdescriptionView.setText(R.string.uv_prompt_description_3_4_5);
            uvpromptdescription1View.setText(R.string.uv_prompt_recommend1_3_4_5);
        } else if (mUVIndex >= 2) {
            mSunBurnFactor = 30;
            uvinfoIndexView.setImageDrawable(getResources().getDrawable(R.drawable.uv_index_mind_2));
            uvinfoBGView.setBackground(getResources().getDrawable(R.drawable.bg_uv_1_2));
            uvprompticonGlassView.setBackground(getResources().getDrawable(R.drawable.icon_main_screen_sunglasses_nor));
            uvprompticonHatView.setBackground(getResources().getDrawable(R.drawable.icon_main_screen_hat_dis));
            uvprompticonCoverView.setBackground(getResources().getDrawable(R.drawable.icon_main_screen_cover_dis));
            uvprompticonHouseView.setBackground(getResources().getDrawable(R.drawable.icon_main_screen_house_dis));
            String strBuffer = mStringUVPrompt_1_2;
            strBuffer = !strBuffer.contains("UV") ? strBuffer : strBuffer.substring(strBuffer.indexOf("UV") + 3);
            uvpromptView.setText(strBuffer.toUpperCase());
            uvpromptdescriptionView.setText(R.string.uv_prompt_description_1_2);
            uvpromptdescription1View.setText(R.string.uv_prompt_recommend1_1_2);
        } else if (mUVIndex >= 1) {
            mSunBurnFactor = 30;
            uvinfoIndexView.setImageDrawable(getResources().getDrawable(R.drawable.uv_index_mind_1));
            uvinfoBGView.setBackground(getResources().getDrawable(R.drawable.bg_uv_1_2));
            uvprompticonGlassView.setBackground(getResources().getDrawable(R.drawable.icon_main_screen_sunglasses_nor));
            uvprompticonHatView.setBackground(getResources().getDrawable(R.drawable.icon_main_screen_hat_dis));
            uvprompticonCoverView.setBackground(getResources().getDrawable(R.drawable.icon_main_screen_cover_dis));
            uvprompticonHouseView.setBackground(getResources().getDrawable(R.drawable.icon_main_screen_house_dis));
            String strBuffer = mStringUVPrompt_1_2;
            strBuffer = !strBuffer.contains("UV") ? strBuffer : strBuffer.substring(strBuffer.indexOf("UV") + 3);
            uvpromptView.setText(strBuffer.toUpperCase());
            uvpromptdescriptionView.setText(R.string.uv_prompt_description_1_2);
            uvpromptdescription1View.setText(R.string.uv_prompt_recommend1_1_2);
        } else if (mUVIndex >= 0) {
            mSunBurnFactor = 30;
            uvinfoIndexView.setImageDrawable(null);
            uvinfoBGView.setBackground(getResources().getDrawable(R.drawable.bg_uv_1_2));
            uvprompticonGlassView.setBackground(getResources().getDrawable(R.drawable.icon_main_screen_sunglasses_nor));
            uvprompticonHatView.setBackground(getResources().getDrawable(R.drawable.icon_main_screen_hat_dis));
            uvprompticonCoverView.setBackground(getResources().getDrawable(R.drawable.icon_main_screen_cover_dis));
            uvprompticonHouseView.setBackground(getResources().getDrawable(R.drawable.icon_main_screen_house_dis));
            String strBuffer = mStringUVPrompt_1_2;
            strBuffer = !strBuffer.contains("UV") ? strBuffer : strBuffer.substring(strBuffer.indexOf("UV") + 3);
            uvpromptView.setText(strBuffer.toUpperCase());
            uvpromptdescriptionView.setText(R.string.uv_prompt_description_1_2);
            uvpromptdescription1View.setText(R.string.uv_prompt_recommend1_1_2);
        }
    }

    protected Bitmap ConvertToBitmap(RelativeLayout layout) {
        //http://magiclen.org/android-drawingcache/
        Bitmap bitmap;
        layout.setDrawingCacheEnabled(true);
        bitmap = layout.getDrawingCache();

        if (bitmap != null) {
            try {
                File file = getShareFile();
                if (file.exists()) {
                    FileOutputStream f = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, f);
                    f.flush();
                    f.close();
                }
            } catch (IOException e) {
                //Nothing
            }
        }
        layout.setDrawingCacheEnabled(false);

        return bitmap;
    }

    private File getShareFile() {
        File file = null;

        File dir = getApplicationContext().getExternalFilesDir(null);
        //File dir = Environment.getExternalStorageDirectory();
        try {
            if (dir != null) {
                String strPath = dir.getPath();
                file = new File(strPath, mShareFileName);
                boolean bRes = false;
                if (file.exists())
                    bRes = file.delete();
                bRes = file.createNewFile();
            }
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
        }
        return file;
    }

    private static final int MY_NOTIFICATION_ID=1;
    private NotificationManager notificationManager;
    private Notification myNotification;

    private void sendNotification() {
        Context context = getApplicationContext();

        notificationManager =
        (NotificationManager)getSystemService(context.NOTIFICATION_SERVICE);
        myNotification = new Notification(R.drawable.button_app_icon,
                context.getResources().getString(R.string.uv_notification_title),
                System.currentTimeMillis());
        String notificationTitle = context.getResources().getString(R.string.uv_notification_title);
        String notificationText = context.getResources().getString(R.string.uv_notification_text);
        Intent intent = new Intent();
        intent.setClass(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        myNotification.defaults |= Notification.DEFAULT_SOUND;
        myNotification.flags |= Notification.FLAG_AUTO_CANCEL;
        myNotification.setLatestEventInfo(context,
                notificationTitle,
                notificationText,
                pendingIntent);
        notificationManager.notify(MY_NOTIFICATION_ID, myNotification);
    }
}
