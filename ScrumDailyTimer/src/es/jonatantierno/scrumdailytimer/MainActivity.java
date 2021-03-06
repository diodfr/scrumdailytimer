
package es.jonatantierno.scrumdailytimer;

import roboguice.activity.RoboFragmentActivity;
import roboguice.inject.InjectView;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.inject.Inject;

/**
 * Main Activity containing a view pager.
 */
public class MainActivity extends RoboFragmentActivity {
    private MediaPlayer mAlarmPlayer;

    @InjectView(R.id.pager)
    ViewPager mPager;

    @Inject
    ScrumTimer mScrumTimer;

    final ViewPager.OnPageChangeListener mPagerListener;

    ChronoFragment mChronoFragment;
    ResultsFragment mResultsFragment;

    // Exit when back pressed twice. This resets when the user taps.
    private boolean mBackPressedOnce = false;

    void resetBackPresses() {
        mBackPressedOnce = false;
    }

    public MainActivity() {
        mPagerListener = new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int i) {
                // Results
                if (i == 1) {
                    mChronoFragment.storeSlotTime();
                    mChronoFragment.pauseTickSound();
                    mResultsFragment.displayData(mChronoFragment);
                    mScrumTimer.configure(mResultsFragment);
                } else {
                    // i = 0 -> Chrono fragment
                    if (mScrumTimer.isStopped()) {
                        // Restart meeting.
                        restartMeeting();
                    } else {
                        // Continue meeting.
                        mScrumTimer.configure(mChronoFragment);
                    }
                }
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
                // Nothing to do
            }

            @Override
            public void onPageScrollStateChanged(int scrollState) {
                // Hide totalTimeTextView so that swipe view is seen properly.
                View totalTimeTextView = MainActivity.this.findViewById(R.id.totalTimeTextView);
                switch (scrollState) {
                    case ViewPager.SCROLL_STATE_DRAGGING:
                        totalTimeTextView.setVisibility(View.GONE);
                        break;
                    case ViewPager.SCROLL_STATE_IDLE:
                        totalTimeTextView.setVisibility(View.VISIBLE);
                        break;
                    default:
                        break;
                }
            }
        };
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Ensure screen does not turn off
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

        ChronoPagerAdapter adapter = new ChronoPagerAdapter(getSupportFragmentManager(), this);
        mChronoFragment = adapter.mChronoFragment;
        mResultsFragment = adapter.mResultsFragment;

        mPager.setAdapter(adapter);
        mPager.setOnPageChangeListener(mPagerListener);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mPager.setPageTransformer(true, new EndMeetingPageTransformer());
        }

    }

    protected void onResume() {
        super.onResume();
        // Lights out mode
        mPager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        mBackPressedOnce = false;
    };

    /**
     * For testing.
     * 
     * @return listener that receives the page change.
     */
    OnPageChangeListener getPagerListener() {
        return mPagerListener;
    }

    @Override
    public void onBackPressed() {
        if (mChronoFragment.isBackPressReset) {
            mChronoFragment.isBackPressReset = false;
            mBackPressedOnce = false;
        }

        if (mBackPressedOnce) {
            super.onBackPressed();
        } else {
            mBackPressedOnce = true;

            Toast toast = Toast.makeText(this, R.string.press_back_again, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    // Finish meeting, do not restart.
    public void endMeeting() {
        mChronoFragment.onStop();
    }

    /**
     * Call to end the meeting and restart.
     */
    public void restartMeeting() {
        mChronoFragment.onStart();
        mChronoFragment.onResume();
    }

}
