package master.infant.gpscamera.preview;

import android.support.annotation.NonNull;

import java.util.List;

public interface SizeSelector {
    /**
     * Returns a list of acceptable sizes from the given input.
     * The final size will be the first element in the output list.
     *
     * @param source input list
     * @return output list
     */
    @NonNull
    List<Size> select(@NonNull List<Size> source);
}
