package master.infant.gpscamera.compass;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.location.Location;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import master.infant.gpscamera.PoiDs;
import master.infant.gpscamera.R;

public class DialPlateView extends View {

    private static final String TAG = DialPlateView.class.getSimpleName();

    private Paint mDialPlatePaint;
    private Paint mDialPlateBorderPaint;
    private Paint mLineNorthPaint;

    private Paint mTextPaint;

    private Paint mPoiPaint;

    private static final int BORDER_WIDTH = 6;
    private static final int MARGIN_LETTER_DIALPLATE = 10;

    private static final int RADIUS_OF_POI_POINT = 4;

    float mCenterX, mCenterY;
    int mRadius;

    OnDrawCallback mOnDrawCallback;
    private PoiDs mPoiDataSource;

    interface OnDrawCallback {
        void onDrawReady();
    }

    public DialPlateView(Context context) {
        this(context, null);
    }

    public DialPlateView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DialPlateView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setTextSize(30);

        //DialPlateView dialPlateView = new DialPlateView(context);
        mDialPlatePaint = new Paint();
        mDialPlatePaint.setAntiAlias(true);
        mDialPlatePaint.setColor(ContextCompat.getColor(context, R.color.compass_dial_plate));
        mDialPlatePaint.setStyle(Paint.Style.FILL);

        mDialPlateBorderPaint = new Paint();
        mDialPlateBorderPaint.setAntiAlias(true);
        mDialPlateBorderPaint.setColor(ContextCompat.getColor(context, R.color.compass_border));
        mDialPlateBorderPaint.setStrokeWidth(BORDER_WIDTH);
        mDialPlateBorderPaint.setStyle(Paint.Style.STROKE);

        mLineNorthPaint = new Paint();
        mLineNorthPaint.setAntiAlias(true);
        mLineNorthPaint.setColor(ContextCompat.getColor(context, R.color.compass_line));
        mLineNorthPaint.setStrokeWidth(1);
        mLineNorthPaint.setStyle(Paint.Style.STROKE);

        mPoiPaint = new Paint();
        mPoiPaint.setAntiAlias(true);
        mPoiPaint.setColor(Color.GREEN);
        mPoiPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.d(TAG, "onDraw");

        int width = getMeasuredWidth();
        int height = getMeasuredHeight();

        if (width == 0 || height == 0) return;

        int[] letterSize = drawDirections(canvas, width, height);
        drawDialPlate(canvas, letterSize, width, height);

        if (mPoiDataSource != null) {

            double maxDistance = 0;
            for (Location location : mPoiDataSource.getPoiList()) {
                double distance = mPoiDataSource.getDeviceLocation().distanceTo(location);
                maxDistance = Math.max(distance, maxDistance);
            }
            double radius = mRadius * 0.7;
            for (Location location : mPoiDataSource.getPoiList()) {

                double distance = mPoiDataSource.getDeviceLocation().distanceTo(location);
                double distanceOfCircle = distance * radius / maxDistance;
                double angle = location.bearingTo(mPoiDataSource.getDeviceLocation());
                if (angle < 0) angle = 360 + angle;

                Log.d(TAG, "onDraw distance=" + distance + ", angle=" + angle);
                double x = mCenterX + distanceOfCircle * Math.cos(angle);
                double y = mCenterY + distanceOfCircle * Math.sin(angle);
                // todo
                canvas.drawCircle((float) x, (float) y, RADIUS_OF_POI_POINT, mPoiPaint);
            }
        }

        if (mOnDrawCallback != null) mOnDrawCallback.onDrawReady();
    }

    void setPoiDataSource(PoiDs poiDataSource) {
        mPoiDataSource = poiDataSource;
        invalidate();
    }

    void update() {
        invalidate();
    }

    private int[] drawDirections(Canvas canvas, int width, int height) {
        int[] letterSize = drawLetter(canvas, width, height, Direction.North);

        drawLetter(canvas, width, height, Direction.South);
        drawLetter(canvas, width, height, Direction.West);
        drawLetter(canvas, width, height, Direction.East);

        return letterSize;
    }

    private void drawDialPlate(Canvas canvas, int[] letterSize, int width, int height) {
        float cx = width / 2.0f, cy = height / 2.0f;
        mCenterX = cx;
        mCenterY = cy;

        int letterWidth = Math.max(letterSize[0], letterSize[1]) + MARGIN_LETTER_DIALPLATE;
        int radius = Math.max(width, height) / 2 - letterWidth;
        mRadius = radius;
        canvas.drawCircle(cx, cy, radius, mDialPlatePaint);

        int outsideBorderRadius = radius - BORDER_WIDTH / 2;
        canvas.drawCircle(cx, cy, outsideBorderRadius, mDialPlateBorderPaint);

        int innerRadius = radius / 3;
        canvas.drawCircle(cx, cy, innerRadius, mLineNorthPaint);

        int middleRadius = innerRadius * 2;
        canvas.drawCircle(cx, cy, middleRadius, mLineNorthPaint);

        canvas.drawLine(letterWidth, cy, width - letterWidth, cy, mLineNorthPaint);
        canvas.drawLine(cx, letterWidth, cx, height - letterWidth, mLineNorthPaint);
    }

    private int[] drawLetter(Canvas canvas, int width, int height, Direction direction) {
        canvas.save();

        Rect rect = new Rect();
        mTextPaint.getTextBounds(direction.letter, 0, 1, rect);

        int x = 0, y = 0;

        switch (direction) {
            case North: x = (width - rect.width()) / 2; y = rect.height(); break;
            case South: x = (width - rect.width()) / 2; y = height; break;
            case West: x = 0; y = (height + rect.height()) / 2; break;
            case East: x = width - rect.height(); y = (height + rect.width()) / 2; break;
        }
        canvas.translate(x, y);
        mTextPaint.setStyle(Paint.Style.FILL);

//        canvas.drawText(direction.letter, 0, 0, mTextPaint);
//        mTextPaint.setStyle(Paint.Style.STROKE);
//        canvas.drawRect(rect, mTextPaint);

        canvas.translate(-x, -y);

        canvas.rotate(direction.degrees, x + rect.exactCenterX(), y + rect.exactCenterY());
        mTextPaint.setStyle(Paint.Style.FILL);
        canvas.drawText(direction.letter, x, y, mTextPaint);

        canvas.restore();

        return new int[] {rect.width(), rect.height()};
    }

    enum Direction {
        North("N", 0), South("S", 180), West("W", -90), East("E", 90);

        final String letter;
        final float degrees;

        Direction(String value, float degrees) {
            letter = value;
            this.degrees = degrees;
        }
    }
}
