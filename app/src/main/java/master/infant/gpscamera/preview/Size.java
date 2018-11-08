package master.infant.gpscamera.preview;

import android.support.annotation.NonNull;

class Size implements Comparable<Size> {

    private final int width;
    private final int height;

    Size(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    Size copy() {
        return new Size(width, height);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Size)) return false;

        Size size = (Size) o;

        if (width != size.width) return false;
        return height == size.height;
    }

    @Override
    public int hashCode() {
        int result = width;
        result = 31 * result + height;
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Size{");
        sb.append("width=").append(width);
        sb.append(", height=").append(height);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public int compareTo(@NonNull Size another) {
        return width * height - another.width * another.height;
    }
}
