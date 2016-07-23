package com.myapps.documentscanner;

/*
 * based on code originally at http://www.androidhive.info/2013/09/android-fullscreen-image-slider-with-swipe-and-pinch-zoom-gestures/
 */

import android.app.Activity;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.myapps.documentscanner.views.TouchImageView;

import java.util.ArrayList;

public class FullScreenImageAdapter extends PagerAdapter {

    private static final String TAG = "FullScreenImageAdapter";
    private Activity _activity;
    private ArrayList<String> _imagePaths;
    private int maxTexture;
    private ImageLoader mImageLoader;
    private ImageSize mTargetSize;

    // constructor
    public FullScreenImageAdapter(Activity activity,
                                  ArrayList<String> imagePaths) {
        this._activity = activity;
        this._imagePaths = imagePaths;
    }

    @Override
    public int getCount() {
        return this._imagePaths.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        TouchImageView imgDisplay;

        LayoutInflater inflater = (LayoutInflater) _activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewLayout = inflater.inflate(R.layout.layout_fullscreen_image, container,
                false);

        imgDisplay = (TouchImageView) viewLayout.findViewById(R.id.imgDisplay);

        String imagePath = _imagePaths.get(position);
        mImageLoader.displayImage("file:///"+imagePath, imgDisplay, mTargetSize);

        container.addView(viewLayout);

        return viewLayout;
    }

    private static double Log( double n , double base ) {
        return Math.log(n) / Math.log(base);
    }

    public String getPath(int position) {
        return _imagePaths.get(position);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((RelativeLayout) object);
    }

    public void setMaxTexture(int maxTexture, ImageSize targetSize) {
        this.maxTexture = maxTexture;
        mTargetSize = targetSize;
    }

    public void setImageLoader(ImageLoader imageLoader) {
        mImageLoader = imageLoader;
    }
}
