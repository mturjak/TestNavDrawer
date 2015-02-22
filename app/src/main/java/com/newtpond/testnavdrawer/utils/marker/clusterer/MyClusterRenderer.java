package com.newtpond.testnavdrawer.utils.marker.clusterer;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

/**
 * Created by martinturjak on 2/18/15.
 */
public class MyClusterRenderer extends DefaultClusterRenderer<Moment> {

    public MyClusterRenderer(Context context, GoogleMap map,
                           ClusterManager<Moment> clusterManager) {
        super(context, map, clusterManager);
    }

    @Override
    protected void onBeforeClusterItemRendered(Moment item, MarkerOptions markerOptions) {
        //markerOptions.icon(item.getIcon());
        //markerOptions.snippet(item.getSnippet());
        markerOptions.title(item.getTitle());
        super.onBeforeClusterItemRendered(item, markerOptions);
    }

    @Override
    protected boolean shouldRenderAsCluster(Cluster cluster) {
        return cluster.getSize() > 1;
    }
}
