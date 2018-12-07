package master.infant.gpscamera.projection;

import android.content.Context;
import android.location.Location;
import android.opengl.Matrix;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

public class ProjectionView extends RelativeLayout {

    private static final String TAG = ProjectionView.class.getSimpleName();

    public ProjectionView(Context context) {
        this(context, null);
    }

    public ProjectionView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ProjectionView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private Location mCurrentLocation;
    private Location mChengDuLocation;
    private Location mDaTongLocation;

    private Location mDeviceLocation;

    private View mChengDuView;

    private List<PoiWidget> poiViewList;

    private float[] rotatedProjectionMatrix = new float[16];

    public void updateRotatedProjectionMatrix(float[] rotatedProjectionMatrix) {
        this.rotatedProjectionMatrix = rotatedProjectionMatrix;
        this.invalidate();
    }

    // Xi'an: 34.21027777, 108.83777777
    // ChengDu: 30.56305555555, 104.0697222222
    // Datong: 40.019444444, 113.179722222
    private void init(Context context) {

        mCurrentLocation = new Location("MockGps");
        mCurrentLocation.setLatitude(34.21027777);
        mCurrentLocation.setLongitude(108.83777777);

        mChengDuLocation = new Location("MockGps");
        mChengDuLocation.setLatitude(30.56305555555);
        mChengDuLocation.setLongitude(104.0697222222);

        mDaTongLocation = new Location("MockGps");
        mDaTongLocation.setLatitude(40.019444444);
        mDaTongLocation.setLongitude(113.179722222);

        poiViewList = new ArrayList<>();
        installPoiPoints();
    }

//    public void install(POI poi) {
//
//        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(100, 100);
//        params.leftMargin = -100;
//        params.topMargin = -100;
//
//        mChengDuView = new View(getContext());
//        mChengDuView.setBackgroundColor(Color.YELLOW);
//        addView(mChengDuView, params);
//    }

    private void installPoiPoints() {
        poiViewList.clear();

        List<Location> poiList = new ArrayList<>();
        poiList.add(mChengDuLocation);
        //poiList.add(mDaTongLocation);

        for (Location location : poiList) {
            PoiWidget poiView = new PoiWidget(location);
            poiViewList.add(poiView);

            View view = poiView.createPoiView(getContext(), this);
            addView(view);
        }
    }

    public void updateDeviceLocation(Location deviceLocation) {
        mDeviceLocation = deviceLocation;

        for (PoiWidget poiView : poiViewList) {
            float[] currentLocationInECEF = LocationHelper.WSG84toECEF(mDeviceLocation);
            float[] pointInECEF = LocationHelper.WSG84toECEF(poiView.getLocation());
            float[] pointInENU = LocationHelper.ECEFtoENU(mDeviceLocation, currentLocationInECEF, pointInECEF);

            float[] cameraCoordinateVector = new float[4];
            Matrix.multiplyMV(cameraCoordinateVector, 0, rotatedProjectionMatrix, 0, pointInENU, 0);

            PoiView view = poiView.getView();
            PoiView.Direction poiDirection = PoiView.Direction.Left;
            if (cameraCoordinateVector[2] < 0) {
                float x  = (0.5f + cameraCoordinateVector[0]/cameraCoordinateVector[3]) * getWidth();
                float y = (0.5f - cameraCoordinateVector[1]/cameraCoordinateVector[3]) * getHeight();

                Log.d("Albert", "place markland x=" + x + ", y=" + y);

                poiDirection = PoiView.Direction.Inside;

                if (x < 0) {
                    x = 0;
                    poiDirection = PoiView.Direction.Left;
                } else if (x + view.getWidth() > getWidth()) {
                    x = getWidth() - view.getWidth();
                    poiDirection = PoiView.Direction.Right;
                }

                if (y < 0) {
                    y = 0;
                } else if (y + view.getHeight() > getHeight()) {
                    y = getHeight() - view.getHeight();
                }

                view.animate().translationX(x).translationY(y).start();

            } else {
                float x  = (0.5f + cameraCoordinateVector[0]/cameraCoordinateVector[3]) * getWidth();
                float y = (0.5f - cameraCoordinateVector[1]/cameraCoordinateVector[3]) * getHeight();

                Log.d("Albert", "2 place markland x=" + x + ", y=" + y);

                if (x < 0) {
                    x = getWidth() - view.getWidth();
                    poiDirection = PoiView.Direction.Right;
                } else {
                    x = 0;
                    poiDirection = PoiView.Direction.Left;
                }

                if (y < 0) {
                    y = 0;
                }

                if (y + view.getHeight() > getHeight()) {
                    y = getHeight() - view.getHeight();
                }

                view.setTranslationX(x);
                view.setTranslationY(y);
            }

            view.updateDirection(poiDirection);
        }
    }
}
