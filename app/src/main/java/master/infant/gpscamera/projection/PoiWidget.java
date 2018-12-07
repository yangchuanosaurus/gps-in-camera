package master.infant.gpscamera.projection;

import android.content.Context;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import master.infant.gpscamera.R;

public class PoiWidget {

    private final Location mLocation;
    private PoiView mView;

    public PoiWidget(Location location) {
        mLocation = location;
    }

    public View createPoiView(Context context, ViewGroup root) {
        mView = (PoiView) LayoutInflater.from(context).inflate(R.layout.view_poi, root, false);

        return mView;
    }

    public Location getLocation() {
        return mLocation;
    }

    public PoiView getView() {
        return mView;
    }
}
