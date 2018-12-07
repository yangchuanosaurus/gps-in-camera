package master.infant.gpscamera.preview;

import android.content.Context;
import android.opengl.Matrix;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import master.infant.gpscamera.R;

public class SurfaceCameraPreview extends CameraPreview<View, SurfaceHolder> {

    private final static CameraLogger LOGGER = CameraLogger.create(SurfaceCameraPreview.class.getSimpleName());

    private SurfaceView mSurfaceView;

    private int mCamWidth, mCamHeight;
    private final static float Z_NEAR = 1.0f;
    private final static float Z_FAR = 2000;


    SurfaceCameraPreview(Context context, ViewGroup parent, SurfaceCallback callback) {
        super(context, parent, callback);
    }

    @NonNull
    @Override
    protected View onCreateView(Context context, ViewGroup parent) {
        View rootView = LayoutInflater.from(context).inflate(R.layout.cameraview_surface_view, parent, false);
        parent.addView(rootView, 0);

        mSurfaceView = (SurfaceView) rootView;

        final SurfaceHolder holder = mSurfaceView.getHolder();
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        holder.addCallback(new SurfaceHolder.Callback() {
            private boolean mFirstTime = true;

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                LOGGER.i("callback:", "surfaceCreated");
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                LOGGER.i("callback:", "surfaceChanged", "w:", width, "h:", height, "firstTime:", mFirstTime);
                mCamWidth = width;
                mCamHeight = height;

                if (mFirstTime) {
                    onSurfaceAvailable(width, height);
                    mFirstTime = false;
                } else {
                    onSurfaceSizeChanged(width, height);
                }

                generateProjectionMatrix();
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                LOGGER.i("callback:", "surfaceDestroyed");
                onSurfaceDestroyed();
                mFirstTime = true;
            }
        });

        return rootView;
    }

    @Override
    Surface getSurface() {
        return getOutput().getSurface();
    }

    @Override
    Class<SurfaceHolder> getOutputClass() {
        return SurfaceHolder.class;
    }

    @Override
    SurfaceHolder getOutput() {
        return mSurfaceView.getHolder();
    }

    private void generateProjectionMatrix() {
        float[] projectionMatrix = new float[16];
        float ratio = (float) mCamWidth / mCamHeight;
        final int OFFSET = 0;
        final float LEFT =  -ratio;
        final float RIGHT = ratio;
        final float BOTTOM = -1;
        final float TOP = 1;
        Matrix.frustumM(projectionMatrix, OFFSET, LEFT, RIGHT, BOTTOM, TOP, Z_NEAR, Z_FAR);

        onProjectionMatrix(projectionMatrix);
    }
}
