package master.infant.gpscamera.preview;

import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

class AspectRatio implements Comparable<AspectRatio> {

    final static Map<String, AspectRatio> sCache = new HashMap<>();

    private final int mX, mY;

    private AspectRatio(int x, int y) {
        mX = x;
        mY = y;
    }

    public static AspectRatio of(int x, int y) {
        int gcd = gcd(x, y);
        x /= gcd;
        y /= gcd;
        String key = x + ":" + y;
        AspectRatio cached = sCache.get(key);
        if (cached == null) {
            cached = new AspectRatio(x, y);
            sCache.put(key, cached);
        }
        return cached;
    }

    public static AspectRatio parse(@NonNull String str) {
        String[] parts = str.split(":");
        if (parts.length != 2) {
            throw new NumberFormatException("Illegal AspectRatio String. Should be x:y");
        }
        int x = Integer.valueOf(parts[0]);
        int y = Integer.valueOf(parts[1]);
        return of(x, y);
    }

    static int gcd(int a, int b) {
        while (b != 0) {
            int c = b;
            b = a % b;
            a = c;
        }
        return a;
    }

    float toFloat() {
        return (float) mX / mY;
    }

    public int getX() {
        return mX;
    }

    public int getY() {
        return mY;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AspectRatio)) return false;

        AspectRatio that = (AspectRatio) o;

        if (mX != that.mX) return false;
        return mY == that.mY;
    }

    @Override
    public int hashCode() {
        int result = mX;
        result = 31 * result + mY;
        return result;
    }

    @Override
    public int compareTo(@NonNull AspectRatio another) {
        if (equals(another)) {
            return 0;
        } else if (toFloat() - another.toFloat() > 0) {
            return 1;
        }
        return -1;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AspectRatio{");
        sb.append("mX=").append(mX);
        sb.append(", mY=").append(mY);
        sb.append('}');
        return sb.toString();
    }

    boolean meatches(Size size) {
        int gcd = gcd(size.getWidth(), size.getHeight());
        int x = size.getWidth() / gcd;
        int y = size.getHeight() / gcd;
        return mX == x && mY == y;
    }

    AspectRatio inverse() {
        return AspectRatio.of(mY, mX);
    }
}
