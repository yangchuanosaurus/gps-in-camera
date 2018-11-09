package master.infant.gpscamera.preview;

import android.support.annotation.NonNull;

class Size implements Comparable<Size> {

    private final int mWidth;
    private final int mHeight;

    Size(int width, int height) {
        this.mWidth = width;
        this.mHeight = height;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    Size copy() {
        return new Size(mWidth, mHeight);
    }

    Size flip() {
        return new Size(mHeight, mWidth);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Size)) return false;

        Size size = (Size) o;

        if (mWidth != size.mWidth) return false;
        return mHeight == size.mHeight;
    }

    @Override
    public int hashCode() {
        int result = mWidth;
        result = 31 * result + mHeight;
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Size{");
        sb.append("width=").append(mWidth);
        sb.append(", height=").append(mHeight);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public int compareTo(@NonNull Size another) {
        return mWidth * mHeight - another.mWidth * another.mHeight;
    }
}
