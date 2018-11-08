package master.infant.gpscamera;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

/**
 * Below is a single app base on related AR GPS location features.
 *
 * (2 days)
 * 1. Camera Widget
 * - setCameraPreviewEnabled(boolean); show or hide the camera preview layer
 * - setCompassEnabled(boolean); show or hide the compass widget
 * - NodeProvider; delegate of nodes datasource, a node means a POI location
 * - show nodes on camera layer and compass widget from VisibleNodeProvider
 *
 * (3 days)
 * 2. Camera Preview (Supporting on Camera and Camera2)
 *
 * (2 days)
 * 3. Compass widget
 * - NodeProvider;
 * - show nodes in compass widget
 *
 * (5 days)
 * 4. Node Projection Calculation module
 * - show the POI node base on screen size
 * - customize the outside of screen style
 * - customize the inside of screen style
 *
 * (1 day)
 * 5. Filter of POI visibility
 *
 * (1 day)
 * 6. Filter of Goto POI
 *
 * */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
