package master.infant.gpscamera.preview;

import android.arch.lifecycle.LifecycleObserver;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class CameraView extends FrameLayout implements LifecycleObserver {

    private Handler mUiHandler;

    private CameraPreview mCameraPreview;

    public CameraView(@NonNull Context context) {
        this(context, null);
    }

    public CameraView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(@NonNull Context context, @Nullable AttributeSet attrs) {
        setWillNotDraw(false);

        mUiHandler = new Handler(Looper.getMainLooper());

    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

    }

}
