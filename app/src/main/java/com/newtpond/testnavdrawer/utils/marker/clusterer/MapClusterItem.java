package com.newtpond.testnavdrawer.utils.marker.clusterer;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;


public class MapClusterItem implements ClusterItem {
    private final LatLng mPosition;
    private final String mTitle;

    public MapClusterItem(double lat, double lng) {
        mPosition = new LatLng(lat, lng);
        mTitle = "p(" + String.valueOf(lat) + "; " + String.valueOf(lng) + ")";
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    public String getTitle() {
        return mTitle;
    }
}