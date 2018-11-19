package master.infant.gpscamera.compass;

import android.animation.ObjectAnimator;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.RelativeLayout;

import master.infant.gpscamera.R;

public class CompassView extends RelativeLayout implements DialPlateView.OnDrawCallback,
        LogicCompass.CompassListener, LifecycleObserver {

    private static final String TAG = CompassView.class.getSimpleName();

    private Lifecycle mLifecycle;

    private DialPlateView mDialPlateView;

    private Paint mRangePaint;
    private RectF mRangeOval;

    private LogicCompass mCompass;

    private static final float MIN_AZIMUTH_DIFFERENCE_BETWEEN_COMPASS_UPDATES = 1;
    private static final float MIN_PITCH_DIFFERENCE_BETWEEN_COMPASS_UPDATES = 1;
    private static final float MIN_ROLL_DIFFERENCE_BETWEEN_COMPASS_UPDATES = 1;

    public CompassView(@NonNull Context context) {
        this(context, null);
    }

    public CompassView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CompassView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(@NonNull Context context) {
        setWillNotDraw(false);

        mCompass = LogicCompass.newInstance(context, this);

        mRangePaint = new Paint();
        mRangePaint.setAntiAlias(true);
        mRangePaint.setColor(ContextCompat.getColor(context, R.color.compass_border));
        mRangePaint.setStyle(Paint.Style.FILL);

        //DialPlateView dialPlateView = new DialPlateView(context);
        View dialPlateView = LayoutInflater.from(context)
                .inflate(R.layout.view_dial_plate, this, false);
        this.addView(dialPlateView);

        mDialPlateView = (DialPlateView) dialPlateView;
        mDialPlateView.mOnDrawCallback = this;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.d(TAG, "onDraw " + mRangeOval);
        if (mRangeOval == null) return;

        canvas.drawArc(mRangeOval, -120F, 60F, true, mRangePaint);
    }

    @Override
    public void onDrawReady() {
        Log.d(TAG, "onDrawReady");
        float left = getMeasuredWidth() / 2 - mDialPlateView.mRadius;
        float top = getMeasuredHeight() / 2 - mDialPlateView.mRadius;
        mRangeOval = new RectF(left, top, getMeasuredWidth() - left, getMeasuredHeight() - top);

        this.invalidate();
    }

    float mCurrentDegrees;
    @Override
    public void onOrientationChanged(float azimuth, float pitch, float roll) {
        // Log.d(TAG, "onOrientationChanged azimuth=" + azimuth + ", pitch=" + pitch + ", roll=" + roll);

        float degrees = 360 - azimuth;
        Log.d(TAG, "onOrientationChanged ====> " + degrees + ", azimuth=" + azimuth);

        mDialPlateView.animate().rotation(degrees).start();
    }

    public void setLifecycleOwner(LifecycleOwner owner) {
        if (mLifecycle != null) mLifecycle.removeObserver(this);
        mLifecycle = owner.getLifecycle();
        mLifecycle.addObserver(this);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void onResume() {
        if (mCompass != null) {
            mCompass.start(MIN_AZIMUTH_DIFFERENCE_BETWEEN_COMPASS_UPDATES,
                    MIN_PITCH_DIFFERENCE_BETWEEN_COMPASS_UPDATES,
                    MIN_ROLL_DIFFERENCE_BETWEEN_COMPASS_UPDATES);
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void onPause() {
        if (mCompass != null) {
            mCompass.stop();
        }
    }
}
