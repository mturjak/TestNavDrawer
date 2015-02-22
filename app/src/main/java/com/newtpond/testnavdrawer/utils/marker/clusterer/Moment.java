package com.newtpond.testnavdrawer.utils.marker.clusterer;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;
import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

@ParseClassName("Moment")
public class Moment extends ParseObject implements ClusterItem {
    public Moment() {
        /* default constructor */
    }

    @Override
    public LatLng getPosition() {
        ParseGeoPoint p = getParseGeoPoint("location");
        return new LatLng(p.getLatitude(), p.getLongitude());
    }

    public String getTitle() {
        return getString("message");
    }
}