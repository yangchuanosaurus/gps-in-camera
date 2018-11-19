package master.infant.gpscamera.compass;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

import master.infant.gpscamera.BuildConfig;

import static android.content.Context.SENSOR_SERVICE;

/* reference from: https://github.com/AlexandreLouisnard/android-compass */
public class LogicCompass implements SensorEventListener {

    private static final String TAG = LogicCompass.class.getSimpleName();

    // Constants
    private static final float ROTATION_VECTOR_SMOOTHING_FACTOR = 0.5f;
    private static final float GEOMAGNETIC_SMOOTHING_FACTOR = 0.4f;
    private static final float GRAVITY_SMOOTHING_FACTOR = 0.1f;

    // Context
    private final Context mContext;

    // Sensors
    private final SensorManager mSensorManager;
    private final Sensor mRotationVectorSensor;
    private final Sensor mMagnetometerSensor;
    private final Sensor mAccelerometerSensor;
    // RotationVectorSensor is more precise than Magnetic+Accelerometer, but on some devices it is not working
    private boolean mUseRotationVectorSensor = false;

    // Orientation
    @SuppressWarnings("FieldCanBeLocal")
    private float mAzimuthDegrees;
    private float mPitchDegrees;
    private float mRollDegrees;
    private float[] mRotationVector = new float[5];
    private float[] mGeomagnetic = new float[3];
    private float[] mGravity = new float[3];

    // Listener
    private final CompassListener mCompassListener;
    // The minimum difference in degrees with the last orientation value for the CompassListener to be notified
    private float mAzimuthSensibility;
    private float mPitchSensibility;
    private float mRollSensibility;
    // The last orientation value sent to the CompassListener
    private float mLastAzimuthDegrees;
    private float mLastPitchDegrees;
    private float mLastRollDegrees;

    public interface CompassListener {
        /**
         * Called whenever the device orientation has changed, providing azimuth, pitch and roll values taking into account the screen orientation of the device.
         * @param azimuth the azimuth of the device (East of the magnetic North = counterclockwise), in degrees, from 0° to 360°.
         * @param pitch the pitch (vertical inclination) of the device, in degrees, from -180° to 180°.<br>
         *              Angle of rotation about the x axis. This value represents the angle between a plane parallel to the device's screen and a plane parallel to the ground.<br>
         *              Equals 0° if the device top and bottom edges are on the same level.<br>
         *              Equals -90° if the device top edge is up and the device bottom edge is down (such as when holding the device to take a picture towards the horizon).<br>
         *              Equals 90° if the device top edge is down and the device bottom edge is up.
         * @param roll the roll (horizontal inclination) of the device, in degrees, from -90° to 90°.<br>
         *             Angle of rotation about the y axis. This value represents the angle between a plane perpendicular to the device's screen and a plane perpendicular to the ground.<br>
         *             Equals 0° if the device left and right edges are on the same level.<br>
         *             Equals -90° if the device right edge is up and the device left edge is down.<br>
         *             Equals 90° if the device right edge is down and the device left edge is up.
         */
        void onOrientationChanged(float azimuth, float pitch, float roll);
    }

    private LogicCompass(Context context, CompassListener compassListener) {
        mContext = context;
        // Sensors
        mSensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        mMagnetometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mRotationVectorSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        // Listener
        mCompassListener = compassListener;
    }

    public static LogicCompass newInstance(Context context, CompassListener compassListener) {
        LogicCompass compass = new LogicCompass(context, compassListener);
        if (compass.hasRequiredSensors()) {
            return compass;
        } else {
            return null;
        }
    }

    private boolean hasRequiredSensors() {
        if (mRotationVectorSensor != null) {
            if (BuildConfig.DEBUG) Log.d(TAG, "Sensor.TYPE_ROTATION_VECTOR found");
            return true;
        } else if (mMagnetometerSensor != null && mAccelerometerSensor != null) {
            if (BuildConfig.DEBUG) Log.d(TAG, "Sensor.TYPE_MAGNETIC_FIELD and Sensor.TYPE_ACCELEROMETER found");
            return true;
        } else {
            if (BuildConfig.DEBUG) Log.d(TAG, "The device does not have the required sensors");
            return false;
        }
    }

    public void start(float azimuthSensibility, float pitchSensibility, float rollSensibility) {
        mAzimuthSensibility = azimuthSensibility;
        mPitchSensibility = pitchSensibility;
        mRollSensibility = rollSensibility;
        if (mRotationVectorSensor != null) {
            mSensorManager.registerListener(this, mRotationVectorSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (mMagnetometerSensor != null) {
            mSensorManager.registerListener(this, mMagnetometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (mAccelerometerSensor != null) {
            mSensorManager.registerListener(this, mAccelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    public void start() {
        start(0, 0, 0);
    }

    public void stop() {
        mAzimuthSensibility = 0;
        mPitchSensibility = 0;
        mRollSensibility = 0;
        mSensorManager.unregisterListener(this);
    }

    private float[] exponentialSmoothing(float[] newValue, float[] lastValue, float alpha) {
        float[] output = new float[newValue.length];
        if (lastValue == null) {
            return newValue;
        }
        for (int i=0; i<newValue.length; i++) {
            output[i] = lastValue[i] + alpha * (newValue[i] - lastValue[i]);
        }
        return output;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        synchronized (this) {
            // Get the orientation array with Sensor.TYPE_ROTATION_VECTOR if possible (more precise), otherwise with Sensor.TYPE_MAGNETIC_FIELD and Sensor.TYPE_ACCELEROMETER combined
            float orientation[] = new float[3];
            if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
                // Only use rotation vector sensor if it is working on this device
                if (!mUseRotationVectorSensor) {
                    if (BuildConfig.DEBUG) Log.d(TAG, "Using Sensor.TYPE_ROTATION_VECTOR (more precise compass data)");
                    mUseRotationVectorSensor = true;
                }
                // Smooth values
                mRotationVector = exponentialSmoothing(event.values, mRotationVector, ROTATION_VECTOR_SMOOTHING_FACTOR);
                // Calculate the rotation matrix
                float[] rotationMatrix = new float[9];
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
                // Calculate the orientation
                SensorManager.getOrientation(rotationMatrix, orientation);
            } else if (!mUseRotationVectorSensor &&
                    (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD || event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)) {
                if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                    mGeomagnetic = exponentialSmoothing(event.values, mGeomagnetic, GEOMAGNETIC_SMOOTHING_FACTOR);
                }
                if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                    mGravity = exponentialSmoothing(event.values, mGravity, GRAVITY_SMOOTHING_FACTOR);
                }
                // Calculate the rotation and inclination matrix
                float rotationMatrix[] = new float[9];
                float inclinationMatrix[] = new float[9];
                SensorManager.getRotationMatrix(rotationMatrix, inclinationMatrix, mGravity, mGeomagnetic);
                // Calculate the orientation
                SensorManager.getOrientation(rotationMatrix, orientation);
            } else {
                return;
            }

            // Calculate azimuth, pitch and roll values from the orientation[] array
            // Correct values depending on the screen rotation
            final int screenRotation = (((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay()).getRotation();
            mAzimuthDegrees = (float) Math.toDegrees(orientation[0]);
            if (screenRotation == Surface.ROTATION_0) {
                mPitchDegrees = (float) Math.toDegrees(orientation[1]);
                mRollDegrees = (float) Math.toDegrees(orientation[2]);
                if (mRollDegrees >= 90 || mRollDegrees <= -90) {
                    mAzimuthDegrees += 180;
                    mPitchDegrees = mPitchDegrees > 0 ? 180 - mPitchDegrees : -180 - mPitchDegrees;
                    mRollDegrees = mRollDegrees > 0 ? 180 - mRollDegrees : -180 - mRollDegrees;
                }
            } else if (screenRotation == Surface.ROTATION_90) {
                mAzimuthDegrees += 90;
                mPitchDegrees = (float) Math.toDegrees(orientation[2]);
                mRollDegrees = (float) -Math.toDegrees(orientation[1]);
            } else if (screenRotation == Surface.ROTATION_180) {
                mAzimuthDegrees += 180;
                mPitchDegrees = (float) -Math.toDegrees(orientation[1]);
                mRollDegrees = (float) -Math.toDegrees(orientation[2]);
                if (mRollDegrees >= 90 || mRollDegrees <= -90) {
                    mAzimuthDegrees += 180;
                    mPitchDegrees = mPitchDegrees > 0 ? 180 - mPitchDegrees : -180 - mPitchDegrees;
                    mRollDegrees = mRollDegrees > 0 ? 180 - mRollDegrees : -180 - mRollDegrees;
                }
            } else if (screenRotation == Surface.ROTATION_270) {
                mAzimuthDegrees += 270;
                mPitchDegrees = (float) -Math.toDegrees(orientation[2]);
                mRollDegrees = (float) Math.toDegrees(orientation[1]);
            }

            // Force azimuth value between 0° and 360°.
            mAzimuthDegrees = (mAzimuthDegrees + 360) % 360;

            // Notify the compass listener if needed
            if (Math.abs(mAzimuthDegrees - mLastAzimuthDegrees) >= mAzimuthSensibility
                    || Math.abs(mPitchDegrees - mLastPitchDegrees) >= mPitchSensibility
                    || Math.abs(mRollDegrees - mLastRollDegrees) >= mRollSensibility
                    || mLastAzimuthDegrees == 0) {
                mLastAzimuthDegrees = mAzimuthDegrees;
                mLastPitchDegrees = mPitchDegrees;
                mLastRollDegrees = mRollDegrees;
                mCompassListener.onOrientationChanged(mAzimuthDegrees, mPitchDegrees, mRollDegrees);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
