package mobi.qiss.uvangel;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.format.Time;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link mobi.qiss.uvangel.UVAngel_4_1_WidgetConfigureActivity UVAngel_4_1_WidgetConfigureActivity}
 */
public class UVAngel_4_2_Widget extends AppWidgetProvider {

    private static final String SYNC_CLICKED = "automaticWidgetSyncButtonClick";
    private static final String UVANGEL_BROADCAST = "mobi.qiss.uvangel.update";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {

            // Construct the RemoteViews object
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.uvangel_4_2_widget);

            Intent intent = new Intent();
            intent.setClass(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            views.setOnClickPendingIntent(R.id.uvwidget_4x2_all, pendingIntent);

            //views.setOnClickPendingIntent(R.id.uvwidget_bgstop, getPendingSelfIntent(context, SYNC_CLICKED));
            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetIds[i], views);

            //updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        final int N = appWidgetIds.length;
        //Toast.makeText(context, "onDeleted", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
        //Toast.makeText(context, "onEnabled", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
        //Toast.makeText(context, "onDisabled", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        //
        //Toast.makeText(context, "onAppWidgetOptionsChanged", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        super.onReceive(context, intent);
        if (UVANGEL_BROADCAST.equals(intent.getAction())) {

            int UVIndex = intent.getIntExtra("UVIndex", 0);
            int SPFIndex = intent.getIntExtra("SPFIndex", 0);
            int PAIndex = intent.getIntExtra("SPFPLUSIndex", 0);
            boolean beginForProtect = intent.getBooleanExtra("SPFProtect", false);
            long startTimemillis = intent.getLongExtra("SPFProtectStartTime", 0);
            long leftTimemillis = intent.getLongExtra("SPFProtectLeftTime", 0);
            long lastUpdateTimemillis = intent.getLongExtra("SPFLastUpdateTime", 0);
            String location = intent.getStringExtra("Location");

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            ComponentName thisWidget;
            thisWidget = new ComponentName(context, UVAngel_4_2_Widget.class);
            int appWidgetIds[] = appWidgetManager.getAppWidgetIds(thisWidget);
            for (int appWidgetID : appWidgetIds) {

                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.uvangel_4_2_widget);
                Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.uv_index_widget_1);
                CharSequence strPrompt = context.getResources().getString(R.string.uv_prompt_1_2);
                views.setTextViewText(R.id.uvwidget_4x2_spfindex, String.valueOf((SPFIndex + 1) * 5));
                String strPA = "+";
                for (int i = 0; i < PAIndex; i++)
                    strPA += "+";
                views.setTextViewText(R.id.uvwidget_4x2_paindex, strPA);
                views.setTextViewText(R.id.uvwidget_4x2_stoptext, context.getResources().getString(beginForProtect ? R.string.uv_stop_button : R.string.uv_start_button));
                if (beginForProtect) {
                    long secondT = (leftTimemillis / 1000) % (60);
                    long minuteT = (leftTimemillis / (1000 * 60) % 60);
                    long hourT = (leftTimemillis / 1000) / (60 * 60);
                    String str = String.format("%01d:%02d:%02d", hourT, minuteT, secondT);
                    views.setTextViewText(R.id.uvwidget_4x2_duration, str);
                } else {
                    views.setTextViewText(R.id.uvwidget_4x2_duration, context.getResources().getString(R.string.uv_default_zero_time));
                }
                if(lastUpdateTimemillis == 0) {
                    views.setViewVisibility(R.id.uvwidget_4x2_updatetime,View.INVISIBLE);
                    views.setViewVisibility(R.id.uvwidget_4x2_update,View.INVISIBLE);
                    views.setViewVisibility(R.id.uvwidget_4x2_location,View.INVISIBLE);
                }
                else {
                    views.setViewVisibility(R.id.uvwidget_4x2_updatetime,View.VISIBLE);
                    views.setViewVisibility(R.id.uvwidget_4x2_update,View.VISIBLE);
                    views.setViewVisibility(R.id.uvwidget_4x2_location,View.VISIBLE);
                    Time time = new Time();
                    time.setToNow();
                    String myFormat = "hh:mm a"; // your own format
                    SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
                    Date date = new Date(time.toMillis(true));
                    String  formated_time = sdf.format(date); //format your time
                    views.setTextViewText(R.id.uvwidget_4x2_updatetime, formated_time);
                    views.setTextViewText(R.id.uvwidget_4x2_location, location);
                }

                if (UVIndex == 1) {
                    bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.uv_index_widget_1);
                    views.setImageViewBitmap(R.id.uvwidget_4x2_indexnumber, bmp);
                    strPrompt = context.getResources().getString(R.string.uv_prompt_1_2);
                    views.setTextViewText(R.id.uvwidget_4x2_prompt, strPrompt);
                    strPrompt = context.getResources().getString(R.string.uv_prompt_description_1_2);
                    views.setTextViewText(R.id.uvwidget_4x2_prompt_description, strPrompt);
//                    bmp = BitmapFactory.decodeResource(context.getResources(),R.drawable.bg_widget_4x1_1_2);
//                    views.setImageViewBitmap(R.id.uvwidgetindexbg, bmp);
                    strPrompt = context.getResources().getString(R.string.uv_prompt_recommend1_1_2);
                    views.setTextViewText(R.id.uvwidget_4x2_prompt_line1, strPrompt);

                    bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_main_screen_sunglasses_nor);
                    views.setImageViewBitmap(R.id.uvwidget_4x2_glass,bmp);
                    bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_main_screen_hat_dis);
                    views.setImageViewBitmap(R.id.uvwidget_4x2_hat,bmp);
                    bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_main_screen_cover_dis);
                    views.setImageViewBitmap(R.id.uvwidget_4x2_cover,bmp);
                    bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_main_screen_house_dis);
                    views.setImageViewBitmap(R.id.uvwidget_4x2_house,bmp);
                } else if (UVIndex == 2) {
                    bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.uv_index_widget_2);
                    views.setImageViewBitmap(R.id.uvwidget_4x2_indexnumber, bmp);
                    strPrompt = context.getResources().getString(R.string.uv_prompt_1_2);
                    views.setTextViewText(R.id.uvwidget_4x2_prompt, strPrompt);
                    strPrompt = context.getResources().getString(R.string.uv_prompt_description_1_2);
                    views.setTextViewText(R.id.uvwidget_4x2_prompt_description, strPrompt);
//                    bmp = BitmapFactory.decodeResource(context.getResources(),R.drawable.bg_widget_4x1_1_2);
//                    views.setImageViewBitmap(R.id.uvwidgetindexbg, bmp);
                    strPrompt = context.getResources().getString(R.string.uv_prompt_recommend1_1_2);
                    views.setTextViewText(R.id.uvwidget_4x2_prompt_line1, strPrompt);

                    bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_main_screen_sunglasses_nor);
                    views.setImageViewBitmap(R.id.uvwidget_4x2_glass,bmp);
                    bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_main_screen_hat_dis);
                    views.setImageViewBitmap(R.id.uvwidget_4x2_hat,bmp);
                    bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_main_screen_cover_dis);
                    views.setImageViewBitmap(R.id.uvwidget_4x2_cover,bmp);
                    bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_main_screen_house_dis);
                    views.setImageViewBitmap(R.id.uvwidget_4x2_house,bmp);
                } else if (UVIndex == 3) {
                    bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.uv_index_widget_3);
                    views.setImageViewBitmap(R.id.uvwidget_4x2_indexnumber, bmp);
                    strPrompt = context.getResources().getString(R.string.uv_prompt_3_4_5);
                    views.setTextViewText(R.id.uvwidget_4x2_prompt, strPrompt);
                    strPrompt = context.getResources().getString(R.string.uv_prompt_description_3_4_5);
                    views.setTextViewText(R.id.uvwidget_4x2_prompt_description, strPrompt);
//                    bmp = BitmapFactory.decodeResource(context.getResources(),R.drawable.bg_widget_4x1_3_4_5);
//                    views.setImageViewBitmap(R.id.uvwidgetindexbg, bmp);
                    strPrompt = context.getResources().getString(R.string.uv_prompt_recommend1_3_4_5);
                    views.setTextViewText(R.id.uvwidget_4x2_prompt_line1, strPrompt);

                    bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_main_screen_sunglasses_nor);
                    views.setImageViewBitmap(R.id.uvwidget_4x2_glass,bmp);
                    bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_main_screen_hat_nor);
                    views.setImageViewBitmap(R.id.uvwidget_4x2_hat,bmp);
                    bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_main_screen_cover_nor);
                    views.setImageViewBitmap(R.id.uvwidget_4x2_cover,bmp);
                    bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_main_screen_house_dis);
                    views.setImageViewBitmap(R.id.uvwidget_4x2_house,bmp);
                } else if (UVIndex == 4) {
                    bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.uv_index_widget_4);
                    views.setImageViewBitmap(R.id.uvwidget_4x2_indexnumber, bmp);
                    strPrompt = context.getResources().getString(R.string.uv_prompt_3_4_5);
                    views.setTextViewText(R.id.uvwidget_4x2_prompt, strPrompt);
                    strPrompt = context.getResources().getString(R.string.uv_prompt_description_3_4_5);
                    views.setTextViewText(R.id.uvwidget_4x2_prompt_description, strPrompt);
                    strPrompt = context.getResources().getString(R.string.uv_prompt_recommend1_3_4_5);
                    views.setTextViewText(R.id.uvwidget_4x2_prompt_line1, strPrompt);

                    bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_main_screen_sunglasses_nor);
                    views.setImageViewBitmap(R.id.uvwidget_4x2_glass,bmp);
                    bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_main_screen_hat_nor);
                    views.setImageViewBitmap(R.id.uvwidget_4x2_hat,bmp);
                    bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_main_screen_cover_nor);
                    views.setImageViewBitmap(R.id.uvwidget_4x2_cover,bmp);
                    bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_main_screen_house_dis);
                    views.setImageViewBitmap(R.id.uvwidget_4x2_house,bmp);
                } else if (UVIndex == 5) {
                    bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.uv_index_widget_5);
                    views.setImageViewBitmap(R.id.uvwidget_4x2_indexnumber, bmp);
                    strPrompt = context.getResources().getString(R.string.uv_prompt_3_4_5);
                    views.setTextViewText(R.id.uvwidget_4x2_prompt, strPrompt);
                    strPrompt = context.getResources().getString(R.string.uv_prompt_description_3_4_5);
                    views.setTextViewText(R.id.uvwidget_4x2_prompt_description, strPrompt);
                    strPrompt = context.getResources().getString(R.string.uv_prompt_recommend1_3_4_5);
                    views.setTextViewText(R.id.uvwidget_4x2_prompt_line1, strPrompt);

                    bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_main_screen_sunglasses_nor);
                    views.setImageViewBitmap(R.id.uvwidget_4x2_glass,bmp);
                    bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_main_screen_hat_nor);
                    views.setImageViewBitmap(R.id.uvwidget_4x2_hat,bmp);
                    bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_main_screen_cover_nor);
                    views.setImageViewBitmap(R.id.uvwidget_4x2_cover,bmp);
                    bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_main_screen_house_dis);
                    views.setImageViewBitmap(R.id.uvwidget_4x2_house,bmp);
                } else if (UVIndex == 6) {
                    bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.uv_index_widget_6);
                    views.setImageViewBitmap(R.id.uvwidget_4x2_indexnumber, bmp);
                    strPrompt = context.getResources().getString(R.string.uv_prompt_6_7);
                    views.setTextViewText(R.id.uvwidget_4x2_prompt, strPrompt);
                    strPrompt = context.getResources().getString(R.string.uv_prompt_description_6_7);
                    views.setTextViewText(R.id.uvwidget_4x2_prompt_description, strPrompt);
                    strPrompt = context.getResources().getString(R.string.uv_prompt_recommend1_6_7);
                    views.setTextViewText(R.id.uvwidget_4x2_prompt_line1, strPrompt);

                    bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_main_screen_sunglasses_nor);
                    views.setImageViewBitmap(R.id.uvwidget_4x2_glass,bmp);
                    bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_main_screen_hat_nor);
                    views.setImageViewBitmap(R.id.uvwidget_4x2_hat,bmp);
                    bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_main_screen_cover_nor);
                    views.setImageViewBitmap(R.id.uvwidget_4x2_cover,bmp);
                    bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_main_screen_house_dis);
                    views.setImageViewBitmap(R.id.uvwidget_4x2_house,bmp);
                } else if (UVIndex == 7) {
                    bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.uv_index_widget_7);
                    views.setImageViewBitmap(R.id.uvwidget_4x2_indexnumber, bmp);
                    strPrompt = context.getResources().getString(R.string.uv_prompt_6_7);
                    views.setTextViewText(R.id.uvwidget_4x2_prompt, strPrompt);
                    strPrompt = context.getResources().getString(R.string.uv_prompt_description_6_7);
                    views.setTextViewText(R.id.uvwidget_prompt_description, strPrompt);
                    strPrompt = context.getResources().getString(R.string.uv_prompt_recommend1_6_7);
                    views.setTextViewText(R.id.uvwidget_4x2_prompt_line1, strPrompt);

                    bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_main_screen_sunglasses_nor);
                    views.setImageViewBitmap(R.id.uvwidget_4x2_glass,bmp);
                    bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_main_screen_hat_nor);
                    views.setImageViewBitmap(R.id.uvwidget_4x2_hat,bmp);
                    bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_main_screen_cover_nor);
                    views.setImageViewBitmap(R.id.uvwidget_4x2_cover,bmp);
                    bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_main_screen_house_dis);
                    views.setImageViewBitmap(R.id.uvwidget_4x2_house,bmp);
                } else if (UVIndex == 8) {
                    bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.uv_index_widget_8);
                    views.setImageViewBitmap(R.id.uvwidget_4x2_indexnumber, bmp);
                    strPrompt = context.getResources().getString(R.string.uv_prompt_8_9_10);
                    views.setTextViewText(R.id.uvwidget_4x2_prompt, strPrompt);
                    strPrompt = context.getResources().getString(R.string.uv_prompt_description_8_9_10);
                    views.setTextViewText(R.id.uvwidget_4x2_prompt_description, strPrompt);
                    strPrompt = context.getResources().getString(R.string.uv_prompt_recommend1_8_9_10);
                    views.setTextViewText(R.id.uvwidget_4x2_prompt_line1, strPrompt);

                    bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_main_screen_sunglasses_nor);
                    views.setImageViewBitmap(R.id.uvwidget_4x2_glass,bmp);
                    bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_main_screen_hat_nor);
                    views.setImageViewBitmap(R.id.uvwidget_4x2_hat,bmp);
                    bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_main_screen_cover_nor);
                    views.setImageViewBitmap(R.id.uvwidget_4x2_cover,bmp);
                    bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_main_screen_house_nor);
                    views.setImageViewBitmap(R.id.uvwidget_4x2_house,bmp);
                } else if (UVIndex == 9) {
                    bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.uv_index_widget_9);
                    views.setImageViewBitmap(R.id.uvwidget_4x2_indexnumber, bmp);
                    strPrompt = context.getResources().getString(R.string.uv_prompt_8_9_10);
                    views.setTextViewText(R.id.uvwidget_4x2_prompt, strPrompt);
                    strPrompt = context.getResources().getString(R.string.uv_prompt_description_8_9_10);
                    views.setTextViewText(R.id.uvwidget_4x2_prompt_description, strPrompt);
                    strPrompt = context.getResources().getString(R.string.uv_prompt_recommend1_8_9_10);
                    views.setTextViewText(R.id.uvwidget_4x2_prompt_line1, strPrompt);

                    bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_main_screen_sunglasses_nor);
                    views.setImageViewBitmap(R.id.uvwidget_4x2_glass,bmp);
                    bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_main_screen_hat_nor);
                    views.setImageViewBitmap(R.id.uvwidget_4x2_hat,bmp);
                    bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_main_screen_cover_nor);
                    views.setImageViewBitmap(R.id.uvwidget_4x2_cover,bmp);
                    bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_main_screen_house_nor);
                    views.setImageViewBitmap(R.id.uvwidget_4x2_house,bmp);
                } else if (UVIndex == 10) {
                    bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.uv_index_widget_10);
                    views.setImageViewBitmap(R.id.uvwidget_4x2_indexnumber, bmp);
                    strPrompt = context.getResources().getString(R.string.uv_prompt_8_9_10);
                    views.setTextViewText(R.id.uvwidget_4x2_prompt, strPrompt);
                    strPrompt = context.getResources().getString(R.string.uv_prompt_description_8_9_10);
                    views.setTextViewText(R.id.uvwidget_4x2_prompt_description, strPrompt);
                    strPrompt = context.getResources().getString(R.string.uv_prompt_recommend1_8_9_10);
                    views.setTextViewText(R.id.uvwidget_4x2_prompt_line1, strPrompt);

                    bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_main_screen_sunglasses_nor);
                    views.setImageViewBitmap(R.id.uvwidget_4x2_glass,bmp);
                    bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_main_screen_hat_nor);
                    views.setImageViewBitmap(R.id.uvwidget_4x2_hat,bmp);
                    bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_main_screen_cover_nor);
                    views.setImageViewBitmap(R.id.uvwidget_4x2_cover,bmp);
                    bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_main_screen_house_nor);
                    views.setImageViewBitmap(R.id.uvwidget_4x2_house,bmp);
                } else if (UVIndex == 11) {
                    bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.uv_index_widget_11);
                    views.setImageViewBitmap(R.id.uvwidget_4x2_indexnumber, bmp);
                    strPrompt = context.getResources().getString(R.string.uv_prompt_11);
                    views.setTextViewText(R.id.uvwidget_4x2_prompt, strPrompt);
                    strPrompt = context.getResources().getString(R.string.uv_prompt_description_11);
                    views.setTextViewText(R.id.uvwidget_4x2_prompt_description, strPrompt);
                    strPrompt = context.getResources().getString(R.string.uv_prompt_recommend1_11);
                    views.setTextViewText(R.id.uvwidget_4x2_prompt_line1, strPrompt);

                    bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_main_screen_sunglasses_nor);
                    views.setImageViewBitmap(R.id.uvwidget_4x2_glass,bmp);
                    bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_main_screen_hat_nor);
                    views.setImageViewBitmap(R.id.uvwidget_4x2_hat,bmp);
                    bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_main_screen_cover_nor);
                    views.setImageViewBitmap(R.id.uvwidget_4x2_cover,bmp);
                    bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_main_screen_house_nor);
                    views.setImageViewBitmap(R.id.uvwidget_4x2_house,bmp);
                }
                appWidgetManager.updateAppWidget(appWidgetID, views);
            }
        } else if (SYNC_CLICKED.equals(intent.getAction())) {
            Intent myIntent = new Intent(context, MainActivity.class);
            context.startActivity(myIntent);
        }
    }

    protected PendingIntent getPendingSelfIntent(Context context, String action) {
        Intent intent = new Intent(context, getClass());
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.uvangel_4_2_widget);
        //views.setTextViewText(R.id.appwidget_text, widgetText);
        //views.setOnClickPendingIntent(R.id.uvwidget_bgstop, getPendingSelfIntent(context, SYNC_CLICKED));

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    public void onWidgetClick(View view) {
        //Intent infoIntent = new Intent(this, Refesh.class);
        //infoIntent.putExtra("UVIndex", mUVIndex);
        //startActivityForResult(infoIntent, RESULT_REFRESH);
        Toast.makeText(view.getContext(), "onWidgetClick", Toast.LENGTH_LONG).show();
    }
}


