package master.infant.gpscamera.preview;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SizeSelectors {

    public interface Filter {
        boolean accepts(Size size);
    }

    public static SizeSelector withFilter(@NonNull Filter filter) {
        return new FilterSelector(filter);
    }

    public static SizeSelector maxWidth(final int width) {
        return withFilter(new Filter() {
            @Override
            public boolean accepts(Size size) {
                return size.getWidth() <= width;
            }
        });
    }

    public static SizeSelector minWidth(final int width) {
        return withFilter(new Filter() {
            @Override
            public boolean accepts(Size size) {
                return size.getWidth() >= width;
            }
        });
    }

    public static SizeSelector maxHeight(final int height) {
        return withFilter(new Filter() {
            @Override
            public boolean accepts(Size size) {
                return size.getHeight() <= height;
            }
        });
    }

    public static SizeSelector minHeight(final int height) {
        return withFilter(new Filter() {
            @Override
            public boolean accepts(Size size) {
                return size.getHeight() >= height;
            }
        });
    }

    public static SizeSelector aspectRatio(AspectRatio ratio, final float delta) {
        final float desired = ratio.toFloat();
        return withFilter(new Filter() {
            @Override
            public boolean accepts(Size size) {
                float candidate = AspectRatio.of(size.getWidth(), size.getHeight()).toFloat();
                return candidate >= desired - delta && candidate <= desired + delta;
            }
        });
    }

    public static SizeSelector biggest() {
        return new SizeSelector() {
            @NonNull
            @Override
            public List<Size> select(@NonNull List<Size> source) {
                Collections.sort(source);
                Collections.reverse(source);
                return source;
            }
        };
    }

    public static SizeSelector smallest() {
        return new SizeSelector() {
            @NonNull
            @Override
            public List<Size> select(@NonNull List<Size> source) {
                Collections.sort(source);
                return source;
            }
        };
    }

    public static SizeSelector maxArea(final int area) {
        return withFilter(new Filter() {
            @Override
            public boolean accepts(Size size) {
                return size.getHeight() * size.getWidth() <= area;
            }
        });
    }

    public static SizeSelector minArea(final int area) {
        return withFilter(new Filter() {
            @Override
            public boolean accepts(Size size) {
                return size.getHeight() * size.getWidth() >= area;
            }
        });
    }

    public static SizeSelector and(SizeSelector... selectors) {
        return new AndSelector(selectors);
    }

    public static SizeSelector or(SizeSelector... selectors) {
        return new OrSelector(selectors);
    }

    private static class FilterSelector implements SizeSelector {

        private Filter constraint;

        private FilterSelector(@NonNull Filter constraint) {
            this.constraint = constraint;
        }

        @NonNull
        @Override
        public List<Size> select(@NonNull List<Size> source) {
            List<Size> sizes = new ArrayList<>();
            for (Size size : source) {
                if (constraint.accepts(size)) {
                    sizes.add(size);
                }
            }
            return sizes;
        }
    }

    private static class AndSelector implements SizeSelector {

        private SizeSelector[] values;

        private AndSelector(SizeSelector... values) {
            this.values = values;
        }

        @NonNull
        @Override
        public List<Size> select(@NonNull List<Size> source) {
            List<Size> tmp = source;
            for (SizeSelector selector : values) {
                tmp = selector.select(tmp);
            }
            return tmp;
        }
    }

    private static class OrSelector implements SizeSelector {

        private SizeSelector[] values;

        private OrSelector(SizeSelector... values) {
            this.values = values;
        }

        @Override
        @NonNull
        public List<Size> select(@NonNull List<Size> source) {
            List<Size> tmp = null;
            for (SizeSelector selector : values) {
                tmp = selector.select(source);
                if (!tmp.isEmpty()) break;
            }

            return tmp == null ? new ArrayList<Size>() : tmp;
        }
    }
}
