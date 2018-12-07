package master.infant.gpscamera;

import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.location.Location;
import android.opengl.Matrix;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import master.infant.gpscamera.compass.CompassView;
import master.infant.gpscamera.preview.CameraLogger;
import master.infant.gpscamera.preview.CameraView;
import master.infant.gpscamera.projection.ProjectionView;

/**
 * Below is a single app base on related AR GPS location features.
 *
 * (2 days)
 * 1. Camera Widget
 * - setCameraPreviewEnabled(boolean); show or hide the camera preview layer
 * - setCompassEnabled(boolean); show or hide the compass widget
 * - NodeProvider; delegate of nodes datasource, a node means a POI location
 * - show nodes on camera layer and compass widget from VisibleNodeProvider
 *
 * (3 days)
 * 2. Camera Preview (Supporting on Camera and Camera2)
 *
 * (2 days)
 * 3. Compass widget
 * - NodeProvider;
 * - show nodes in compass widget
 *
 * (5 days)
 * 4. Node Projection Calculation module
 * - show the POI node base on screen size
 * - customize the outside of screen style
 * - customize the inside of screen style
 *
 * (1 day)
 * 5. Filter of POI visibility
 *
 * (1 day)
 * 6. Filter of Goto POI
 *
 * */
public class MainActivity extends AppCompatActivity implements CompassView.CompassListener {

    private CameraView mCameraView;
    private ProjectionView mProjectionView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
//                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        setContentView(R.layout.activity_camera);

        CameraLogger.setLogLevel(CameraLogger.LEVEL_VERBOSE);

        mCameraView = findViewById(R.id.camera_view);
        mCameraView.setLifecycleOwner(this);

        CompassView compassView = findViewById(R.id.compass_view);
        compassView.setLifecycleOwner(this);
        compassView.setCompassListener(this);

        mProjectionView = findViewById(R.id.projection_view);
    }

    public void buttonFilterAction(View view) {
        FilterActivity.start(this);
    }

    public void buttonGoAction(View view) {

    }

    @Override
    public void onOrientationChanged() {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float[] rotationMatrixFromVector = new float[16];
        float[] projectionMatrix = new float[16];
        float[] rotatedProjectionMatrix = new float[16];

        SensorManager.getRotationMatrixFromVector(rotationMatrixFromVector, event.values);

        if (mCameraView != null) {
            projectionMatrix = mCameraView.getProjectionMatrix();
        }

        Matrix.multiplyMM(rotatedProjectionMatrix, 0, projectionMatrix, 0, rotationMatrixFromVector, 0);
        mProjectionView.updateRotatedProjectionMatrix(rotatedProjectionMatrix);

        updateLocation();
    }

    private void updateLocation() {
        Location xianLocation = new Location("MockGps");
        xianLocation.setLatitude(34.21027777);
        xianLocation.setLongitude(108.83777777);
        mProjectionView.updateDeviceLocation(xianLocation);
    }
}
