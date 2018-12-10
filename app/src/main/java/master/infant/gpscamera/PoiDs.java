package master.infant.gpscamera;

import android.location.Location;

import java.util.List;

public interface PoiDs {
    List<Location> getPoiList();
    Location getDeviceLocation();
}
