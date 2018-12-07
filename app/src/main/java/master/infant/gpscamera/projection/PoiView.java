package master.infant.gpscamera.projection;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import master.infant.gpscamera.R;

public class PoiView extends View {

    private Paint mBkgPaint;

    private RectF mRoundRect;
    private RectF mRect;
    private Path mBkgPath;
    private final static int RADIUS_ROUND = 14;

    private Direction mDirection;

    public enum Direction {
        Left, Right, Inside
    }

    public PoiView(Context context) {
        this(context, null);
    }

    public PoiView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PoiView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mDirection = Direction.Left;

        mBkgPaint = new Paint();
        mBkgPaint.setColor(ContextCompat.getColor(context, R.color.black_50));
        mBkgPaint.setStyle(Paint.Style.FILL);

        mRoundRect = new RectF();
        mRect = new RectF();
        mBkgPath = new Path();
    }

    public void updateDirection(Direction direction) {
        mDirection = direction;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int radius = Math.min(getWidth(), getHeight()) / 2;
        if (radius == 0) return;

        switch (mDirection) {
            case Left: onDirectionLeft(canvas, radius); break;
            case Right: onDirectionRight(canvas, radius); break;
            case Inside: onDirectionInside(canvas, radius); break;
        }
    }

    private void onDirectionInside(Canvas canvas, int radius) {
        mRoundRect.set(0, radius, getWidth(), getHeight());
        mRect.set(0, radius, getWidth(), radius + RADIUS_ROUND);

        mBkgPath.reset();
        mBkgPath.addCircle(getWidth() / 2.0f,  radius, radius, Path.Direction.CW);
        mBkgPath.addRect(mRect, Path.Direction.CW);
        mBkgPath.addRoundRect(mRoundRect, RADIUS_ROUND, RADIUS_ROUND, Path.Direction.CW);

        canvas.drawPath(mBkgPath, mBkgPaint);
    }

    private void onDirectionLeft(Canvas canvas, int radius) {
        mRoundRect.set(0, radius, getWidth(), getHeight());
        mRect.set(0, radius, getWidth(), radius + RADIUS_ROUND);

        mBkgPath.reset();
        mBkgPath.addCircle(getWidth() / 2.0f,  radius, radius, Path.Direction.CW);
//        mBkgPath.addRect(mRect, Path.Direction.CW);
//        mBkgPath.addRoundRect(mRoundRect, RADIUS_ROUND, RADIUS_ROUND, Path.Direction.CW);

        canvas.drawPath(mBkgPath, mBkgPaint);
    }

    private void onDirectionRight(Canvas canvas, int radius) {
        mRoundRect.set(0, radius, getWidth(), getHeight());
        mRect.set(0, radius, getWidth(), radius + RADIUS_ROUND);

        mBkgPath.reset();
//        mBkgPath.addCircle(getWidth() / 2.0f,  radius, radius, Path.Direction.CW);
        mBkgPath.addRect(mRect, Path.Direction.CW);
        mBkgPath.addRoundRect(mRoundRect, RADIUS_ROUND, RADIUS_ROUND, Path.Direction.CW);

        canvas.drawPath(mBkgPath, mBkgPaint);
    }
}
