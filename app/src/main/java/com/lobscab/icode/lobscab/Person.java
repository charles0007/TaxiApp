package com.lobscab.icode.lobscab;

/**
 * Created by Icode on 6/12/2017.
 */

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class Person implements ClusterItem {
    public final String name;
    public final String mTitle;
    public final String mSnippet;
    public final int profilePhoto;
    private final LatLng mPosition;

    public Person(LatLng position, String name, int pictureResource, String snippet,String title) {
        this.name = name;
        profilePhoto = pictureResource;
        mPosition = position;
        mSnippet=snippet;
        mTitle=title;
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    @Override
    public String getTitle() {
        return mTitle;
    }

    @Override
    public String getSnippet() {
        return mSnippet;
    }
}