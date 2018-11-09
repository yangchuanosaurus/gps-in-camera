package master.infant.gpscamera.preview;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

public class CameraView extends FrameLayout implements LifecycleObserver {

    private static final String TAG = CameraView.class.getSimpleName();
    private static final CameraLogger LOGGER = CameraLogger.create(TAG);

    public final static int PERMISSION_CAMERA_REQUEST_CODE = 0xAA;

    private Handler mUiHandler;

    private CameraPreview mCameraPreview;
    private LogicCamera mLogicCamera;

    private Lifecycle mLifecycle;

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
        mLogicCamera = new LogicCamera();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mCameraPreview == null) {
            instantiatePreview();
        }
    }

    void instantiatePreview() {
        mCameraPreview = new SurfaceCameraPreview(getContext(), this, null);
        mLogicCamera.setPreview(mCameraPreview);
    }

    public void setLifecycleOwner(LifecycleOwner owner) {
        if (mLifecycle != null) mLifecycle.removeObserver(this);
        mLifecycle = owner.getLifecycle();
        mLifecycle.addObserver(this);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void startCameraPreview() {
        if (!isEnabled()) return;

        if (checkPermissions()) {
            mLogicCamera.start();
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void stopCameraPreview() {
        mLogicCamera.stop();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void destroy() {
        mLogicCamera.destroy();
    }

    @SuppressLint("NewApi")
    protected boolean checkPermissions() {
        // Manifest is OK at this point. Let's check runtime permissions.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true;

        Context c = getContext();
        boolean needsCamera = true;

        needsCamera = needsCamera && c.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED;

        if (needsCamera) {
            requestPermissions(needsCamera);
            return false;
        }
        return true;
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void requestPermissions(boolean requestCamera) {
        Activity activity = null;
        Context context = getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                activity = (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }

        List<String> permissions = new ArrayList<>();
        if (requestCamera) permissions.add(Manifest.permission.CAMERA);

        if (activity != null) {
            activity.requestPermissions(permissions.toArray(new String[permissions.size()]),
                    PERMISSION_CAMERA_REQUEST_CODE);
        }
    }
}
