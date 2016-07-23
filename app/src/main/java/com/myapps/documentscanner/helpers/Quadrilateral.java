package com.myapps.documentscanner.helpers;

import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;

/**
 * Created by ANIRUDDHA.
 */
public class Quadrilateral {
    public MatOfPoint contour;
    public Point[] points;

    public Quadrilateral(MatOfPoint contour, Point[] points) {
        this.contour = contour;
        this.points = points;
    }
}

