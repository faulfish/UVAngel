package mobi.qiss.uvangel;

import android.graphics.drawable.Drawable;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import mobi.qiss.carousel.CarouselView;


public class MainActivity extends ActionBarActivity {

    private Drawable[] mDrawables;
    private CarouselView mCarouselView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDrawables = new Drawable[8];
        mDrawables[0] = getResources().getDrawable(mobi.qiss.countdowntimer.R.drawable.countdown_indicator);
        mDrawables[1] = getResources().getDrawable(mobi.qiss.countdowntimer.R.drawable.countdown_indicator);
        mDrawables[2] = getResources().getDrawable(mobi.qiss.countdowntimer.R.drawable.countdown_indicator);
        mDrawables[3] = getResources().getDrawable(mobi.qiss.countdowntimer.R.drawable.countdown_indicator);
        mDrawables[4] = getResources().getDrawable(mobi.qiss.countdowntimer.R.drawable.countdown_indicator);
        mDrawables[5] = getResources().getDrawable(mobi.qiss.countdowntimer.R.drawable.countdown_indicator);
        mDrawables[6] = getResources().getDrawable(mobi.qiss.countdowntimer.R.drawable.countdown_indicator);
        mDrawables[7] = getResources().getDrawable(mobi.qiss.countdowntimer.R.drawable.countdown_indicator);
        mCarouselView = (CarouselView) findViewById(R.id.scrollview);
        mCarouselView.init(mDrawables);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

        return super.onOptionsItemSelected(item);
    }
}
