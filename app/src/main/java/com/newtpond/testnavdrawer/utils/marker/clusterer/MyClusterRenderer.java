package com.newtpond.testnavdrawer.utils.marker.clusterer;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

/**
 * Created by martinturjak on 2/18/15.
 */
public class MyClusterRenderer extends DefaultClusterRenderer<MapClusterItem> {

    public MyClusterRenderer(Context context, GoogleMap map,
                           ClusterManager<MapClusterItem> clusterManager) {
        super(context, map, clusterManager);
    }

    @Override
    protected void onBeforeClusterItemRendered(MapClusterItem item, MarkerOptions markerOptions) {
        //markerOptions.icon(item.getIcon());
        //markerOptions.snippet(item.getSnippet());
        markerOptions.title(item.getTitle());
        super.onBeforeClusterItemRendered(item, markerOptions);
    }
}
