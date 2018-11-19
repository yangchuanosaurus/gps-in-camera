package master.infant.gpscamera.preview;

import android.hardware.Camera;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LogicCamera implements Camera.PreviewCallback,
        Camera.ErrorCallback, CameraPreview.SurfaceCallback, Thread.UncaughtExceptionHandler {

    private static final String TAG = LogicCamera.class.getSimpleName();
    private static final CameraLogger LOGGER = CameraLogger.create(TAG);

    private WorkerHandler mHandler;
    private CameraPreview mPreview;
    private Camera mCamera;
    private int mCameraId;
    private int mSensorOffset;
    private int mDisplayOffset;
    private int mFacing;
    private boolean mIsBound = false;

    protected Size mPictureSize;
    protected Size mPreviewSize;
    protected SizeSelector mPictureSizeSelector;

    static final int STATE_STOPPING = -1; // Camera is about to be stopped.
    static final int STATE_STOPPED = 0; // Camera is stopped.
    static final int STATE_STARTING = 1; // Camera is about to start.
    static final int STATE_STARTED = 2; // Camera is available and we can set parameters.

    protected int mState = STATE_STOPPED;

    LogicCamera() {
        mHandler = WorkerHandler.get("LogicCamera");
        mHandler.getThread().setUncaughtExceptionHandler(this);

        mPictureSizeSelector = SizeSelectors.biggest();
    }

    public void setPreview(CameraPreview preview) {
        mPreview = preview;
        mPreview.setSurfaceCallback(this);
    }

    final void start() {
        LOGGER.i("Start:", "posting runnable. State:");
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                LOGGER.i("Start:", "executing. State:");
                if (mState >= STATE_STARTING) return;
                mState = STATE_STARTING;
                LOGGER.i("Start:", "About to call onStart().");
                onStart();
                LOGGER.i("Start:", "Returned from onStart().", "Dispatching.");
                mState = STATE_STARTED;
                //dispatchOnCameraOpened();
            }
        });
    }

    final void stop() {
        LOGGER.i("Stop:", "posting runnable.");
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                LOGGER.i("Stop:", "executing.");
                if (mState <= STATE_STOPPED) return;
                mState = STATE_STOPPING;
                LOGGER.i("Stop:", "About to call onStop().");
                onStop();
                LOGGER.i("Stop", "returned from onStop().", "Dispatching.");
                mState = STATE_STOPPED;
            }
        });
    }

    final void stopImmediately() {
        try {
            LOGGER.i("stopImmediately:");
            if (mState == STATE_STOPPED) return;
            mState = STATE_STOPPING;
            onStop();
            mState = STATE_STOPPED;
            LOGGER.i("stopImmediately:", "Stopped.");
        } catch (Exception e) {
            LOGGER.i("stopImmediately:", "Swallowing exception while stopping.", e);
            mState = STATE_STOPPED;
        }
    }

    final void destroy() {
        LOGGER.i("destroy:");
        mHandler.getThread().setUncaughtExceptionHandler(new NoOpExceptionHandler());
        stopImmediately();
    }

    private static class NoOpExceptionHandler implements Thread.UncaughtExceptionHandler {
        @Override
        public void uncaughtException(Thread t, Throwable e) {
            // No-op
        }
    }

    @WorkerThread
    void onStart() {
        if (isCameraAvailable()) {
            LOGGER.w("onStart:", "Camera not available. Should not happen.");
            onStop();
        }
        if (collectCameraId()) {
            try {
                mCamera = Camera.open(mCameraId);
            } catch (Exception e) {
                LOGGER.e("onStart:", "Failed to connect. Maybe in use by other app?");
                throw new RuntimeException(e);
            }
            mCamera.setErrorCallback(this);

            LOGGER.i("onStart:", "Applying default parameters.");
            Camera.Parameters params = mCamera.getParameters();
            applyDefaultFocus(params);
            mCamera.setParameters(params);

            mCamera.setDisplayOrientation(computeSensorToViewOffset());
            if (shouldBindToSurface()) bindToSurface();
            LOGGER.i("onStart:", "Ended");
        }
    }

    private void applyDefaultFocus(Camera.Parameters params) {
        List<String> modes = params.getSupportedFocusModes();

        if (modes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            return;
        }

        if (modes.contains(Camera.Parameters.FOCUS_MODE_INFINITY)) {
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
            return;
        }

        if (modes.contains(Camera.Parameters.FOCUS_MODE_FIXED)) {
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_FIXED);
            return;
        }
    }

    @WorkerThread
    void onStop() {
        if (mCamera != null) {
            try {
                mCamera.stopPreview();
            } catch(Exception e) {
                LOGGER.w("onStop:", "Clean up.", "Exception while stopping preview.", e);
            }

            try {
                mCamera.release();
            } catch (Exception e) {
                LOGGER.w("onStop:", "Clean up.", "Exception while releasing camera.", e);
            }
        }
        mCamera = null;
        mPreviewSize = null;
        mPictureSize = null;
        mIsBound = false;
        LOGGER.w("onStop:", "Clean up.", "Returning.");
    }

    protected final int computeSensorToViewOffset() {
        if (mFacing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            // Here we had ((mSensorOffset - mDisplayOffset) + 360 + 180) % 360
            // And it seemed to give the same results for various combinations, but not for all (e.g. 0 - 270).
            return (360 - ((mSensorOffset + mDisplayOffset) % 360)) % 360;
        } else {
            return (mSensorOffset - mDisplayOffset + 360) % 360;
        }
    }


    private boolean collectCameraId() {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0, count = Camera.getNumberOfCameras(); i < count; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                mSensorOffset = cameraInfo.orientation;
                mCameraId = i;
                return true;
            }
        }
        return false;
    }

    @Override
    public void onError(int error, Camera camera) {

    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {

    }

    @Override
    public void onSurfaceAvailable() {
        LOGGER.i("onSurfaceAvailable", "Size is", mPreview.getSurfaceSize());
        schedule(null, false, new Runnable() {
            @Override
            public void run() {
                if (shouldBindToSurface()) bindToSurface();
            }
        });
    }

    @Override
    public void onSurfaceChanged() {
        schedule(null, true, new Runnable() {
            @Override
            public void run() {
                if (!mIsBound) return;

                List<Size> supportedSizes = sizesFromList(mCamera.getParameters().getSupportedPictureSizes());
                Size newSize = computePreviewSize(supportedSizes);

                if (newSize.equals(mPreviewSize)) return;

                LOGGER.i("onSurfaceChanged:", "Computed a new preview size. Going on.");
                mPreviewSize = newSize;
                mCamera.stopPreview();
                applySizesAndStartPreview("onSurfaceChanged:");
            }
        });
    }

    private boolean shouldBindToSurface() {
        return isCameraAvailable() && mPreview != null && mPreview.isReady() && !mIsBound;
    }

    @WorkerThread
    private void bindToSurface() {
        LOGGER.i("bindToSurface:", "Started");
        Object output = mPreview.getOutput();
        if (mPreview.getOutputClass() == SurfaceHolder.class) {
            try {
                mCamera.setPreviewDisplay((SurfaceHolder) output);
            } catch (IOException e) {
                LOGGER.e("bindToSurface:", "Failed to bind.", e);
                throw new RuntimeException(e);
            }
        }

        List<Size> sizes = sizesFromList(mCamera.getParameters().getSupportedPreviewSizes());
        mPictureSize = computePictureSize(sizes);
        mPreviewSize = computePreviewSize(sizes);
        applySizesAndStartPreview("bindToSurface:");
        mIsBound = true;
    }

    private void applySizesAndStartPreview(String log) {
        LOGGER.i(log, "Dispatching onCameraPreviewSizeChanged.");

        boolean invertPreviewSizes = shouldFlipSizes();
        mPreview.setDesiredSize(
                invertPreviewSizes ? mPreviewSize.getHeight() : mPreviewSize.getWidth(),
                invertPreviewSizes ? mPreviewSize.getWidth() : mPreviewSize.getHeight()
        );

        Camera.Parameters params = mCamera.getParameters();
        params.setPreviewSize(mPreviewSize.getWidth(), mPreviewSize.getHeight()); // <- not allowed during preview
        params.setPictureSize(mPictureSize.getWidth(), mPictureSize.getHeight()); // <- allowed
        mCamera.setParameters(params);

        LOGGER.i(log, "Starting preview with startPreview().");
        try {
            mCamera.startPreview();
        } catch (Exception e) {
            LOGGER.e(log, "Failed to start preview.", e);
            throw new RuntimeException(e);
        }
        LOGGER.i(log, "Started preview.");
    }

    final boolean shouldFlipSizes() {
        int offset = computeSensorToViewOffset();
        LOGGER.i("shouldFlipSizes:", "displayOffset=", mDisplayOffset, "sensorOffset=", mSensorOffset);
        LOGGER.i("shouldFlipSizes:", "sensorToDisplay=", offset);
        return offset % 180 != 0;
    }

    final Size computePictureSize(List<Size> sizes) {

        boolean flip = shouldFlipSizes();
        SizeSelector selector = SizeSelectors.or(mPictureSizeSelector, SizeSelectors.biggest());

        List<Size> list = new ArrayList<>(sizes);
        Size result = selector.select(list).get(0);
        LOGGER.i("computePictureSize:", "result:", result);
        //if (flip) result = result.flip();
        return result;
    }

    final Size computePreviewSize(List<Size> previewSizes) {
        boolean flip = shouldFlipSizes();

        AspectRatio targetRatio = AspectRatio.of(mPictureSize.getWidth(), mPictureSize.getHeight());
        Size targetMinSize = mPreview.getSurfaceSize();
        //if (flip) targetMinSize = targetMinSize.flip();

        SizeSelector matchRatio = SizeSelectors.aspectRatio(targetRatio, 0);
        SizeSelector matchSize = SizeSelectors.and(
                SizeSelectors.minHeight(targetMinSize.getHeight()),
                SizeSelectors.minWidth(targetMinSize.getWidth()));
        SizeSelector matchAll = SizeSelectors.or(
                SizeSelectors.and(matchRatio, matchSize),
                SizeSelectors.and(matchRatio, SizeSelectors.biggest()), // If couldn't match both, match ratio and biggest.
                SizeSelectors.biggest() // If couldn't match any, take the biggest.
        );
        Size result = matchAll.select(previewSizes).get(0);
        LOGGER.i("computePreviewSize:", "result:", result);
        return result;
    }

    @Nullable
    private List<Size> sizesFromList(List<Camera.Size> sizes) {
        if (sizes == null) return null;
        List<Size> result = new ArrayList<>(sizes.size());
        for (Camera.Size size : sizes) {
            Size add = new Size(size.width, size.height);
            if (!result.contains(add)) result.add(add);
        }
        LOGGER.i("size:", "sizesFromList:", result);
        return result;
    }

    private boolean isCameraAvailable() {
        switch (mState) {
            case STATE_STOPPED: return false;
            case STATE_STOPPING: return false;
            case STATE_STARTED: return true;
            case STATE_STARTING: return mCamera != null;
        }
        return false;
    }

    private void schedule(final Task<Void> task, final boolean ensureAvailable, final Runnable action) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (ensureAvailable && !isCameraAvailable()) {
                    if (task != null) task.end(null);
                } else {
                    action.run();
                    if (task != null) task.end(null);
                }
            }
        });
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        // todo
    }
}
