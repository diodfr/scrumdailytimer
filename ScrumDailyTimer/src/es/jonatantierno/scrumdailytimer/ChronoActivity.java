
package es.jonatantierno.scrumdailytimer;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.inject.Inject;

/**
 * Timer to use in a Scrum Daily Meeting. Main Screen.
 */
public class ChronoActivity extends RoboActivity {

    @InjectView(R.id.wholeLayout)
    private View mWholeLayout;
    @InjectView(R.id.countDownTextView)
    private TextView mCountDownTextView;
    @InjectView(R.id.participantTextView)
    private TextView mParticipantTextView;
    @InjectView(R.id.totalTimeTextView)
    private TextView mTotalTimeTextView;
    @InjectView(R.id.tapForNextTextView)
    private TextView mTapForNextTextView;

    @Inject
    private ScrumTimer mScrumTimer;

    @Inject
    private Provider mProvider;

    private MediaPlayer mAlarmPlayer;

    private int mNumberOfParticipants = 5;
    private int mCurrentParticipant = 1;
    private ChronoStatus mStatus = ChronoStatus.NOT_STARTED;
    private Vibrator mVibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Ensure screen does not turn off
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        mAlarmPlayer = mProvider.getAlarmPlayer(this);

        mCurrentParticipant = 1;

        setContentView(R.layout.activity_fullscreen);

        mScrumTimer.setActivity(this);

        mWholeLayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                vibrate();

                switch (mStatus) {
                    case NOT_STARTED:
                        mStatus = ChronoStatus.STARTED;
                        mTapForNextTextView.setText(R.string.tap_for_first_participant);
                        mScrumTimer.startTimer();
                        break;
                    case STARTED:
                        mStatus = ChronoStatus.COUNTDOWN;

                        mParticipantTextView.setVisibility(View.VISIBLE);

                        repaintParticipants();

                        mTapForNextTextView.setText(R.string.tap_for_next);

                        mScrumTimer.resetCountDown();
                        break;
                    case COUNTDOWN:
                        mCurrentParticipant++;
                        repaintParticipants();

                        mScrumTimer.resetCountDown();
                        resetBackground();

                        if (mCurrentParticipant == mNumberOfParticipants) {
                            mTapForNextTextView.setText(R.string.tap_when_done);
                            mStatus = ChronoStatus.LAST_COUNTDOWN;
                        }
                        break;
                    case LAST_COUNTDOWN:
                        mTapForNextTextView.setText(R.string.tap_to_finish_daily);
                        mParticipantTextView.setVisibility(View.GONE);
                        mStatus = ChronoStatus.END;

                        mScrumTimer.stopCountDown();
                        break;
                    case END:

                        mScrumTimer.stopTimer();
                        break;
                    default:
                }
            }

        });
    }

    @Override
    protected void onStop() {
        super.onStop();

        mScrumTimer.stopTimer();
        mAlarmPlayer.release();
        mAlarmPlayer = null;
    }

    /**
     * Haptic feedback. For > Honeycomb that would be:
     * mWholeLayout.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY); But seems to subtle. Will use
     * {@link Vibrator}, and be Gingerbread compatible besides.
     */
    private void vibrate() {
        if (mVibrator != null) {
            mVibrator.vibrate(170);
        }
    }

    private void repaintParticipants() {

        StringBuffer sb = new StringBuffer(getString(R.string.participant));
        sb.append(mCurrentParticipant);
        sb.append('/');
        sb.append(mNumberOfParticipants);
        mParticipantTextView.setText(sb.toString());
    }

    /**
     * Sets time for the total daily meeting timer.
     * 
     * @param string time to show.
     */
    public void setDailyTimer(final String string) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                mTotalTimeTextView.setText(string);
            }
        });

    }

    /**
     * Sets time for the countdown of the current participant.
     * 
     * @param string time to show.
     */
    public void setCountDown(final String string) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                mCountDownTextView.setText(string);
            }
        });

    }

    /**
     * Call when countdown expires
     */
    public void timeOut() {
        if (mAlarmPlayer != null) {
            mAlarmPlayer.start();
        }

        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                mWholeLayout.setBackgroundColor(getResources().getColor(R.color.timeout_background));
            }
        });

    }

    /**
     * Set normal background
     */
    public void resetBackground() {
        mWholeLayout.setBackgroundColor(getResources().getColor(R.color.background));
    }
}

enum ChronoStatus {
    NOT_STARTED, STARTED, COUNTDOWN, LAST_COUNTDOWN, END;
}
