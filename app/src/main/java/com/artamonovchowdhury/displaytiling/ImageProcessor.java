package com.artamonovchowdhury.displaytiling;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

public class ImageProcessor {


    private int myX, otherX;
    private double[] serverDims;
    private double[] clientDims;
    private double myDens, otherDens;
    private String dir;
    private Bitmap myBitmap = null, otherBitmap = null;
    Context context;

    public ImageProcessor(double[] myDims, double[] otherDims, String dir, Context context) {
        this.serverDims = myDims;
        this.clientDims = otherDims;
        myDens = myDims[4];
        otherDens = otherDims[4];
        this.dir = dir;
        this.context = context;
    }

    public Bitmap[] processImage(Bitmap bitmap) {
        //int[] overallDims = getOverallDims();
        //this.img = getResizedBitmap(bitmap, getOverallDims());
        return cutBitmap(bitmap);


    }

    private Bitmap[] cutBitmap(Bitmap img) {
        double serverDimensionsX = serverDims[0];
        double serverDimensionsY = serverDims[1];
        double serverDimensionsXdp = serverDims[2];
        double serverDimensionsYdp = serverDims[3];
        double serverDPI = serverDims[5];
        double serverDimensionsXinch = serverDimensionsX / serverDPI;
        double serverDimensionsYinch = serverDimensionsY / serverDPI;
        Log.i("myLogs", "ImageProcessor: IP serverDimensionsX " + serverDimensionsX);
        Log.i("myLogs", "ImageProcessor: IP serverDimensionsY " + serverDimensionsY);
        Log.i("myLogs", "ImageProcessor: IP serverDimensionsXdp " + serverDimensionsXdp);
        Log.i("myLogs", "ImageProcessor: IP serverDimensionsYdp " + serverDimensionsYdp);
        Log.i("myLogs", "ImageProcessor: IP serverDPI " + serverDPI);
        Log.i("myLogs", "ImageProcessor: IP serverDimensionsXinch " + serverDimensionsXinch);
        Log.i("myLogs", "ImageProcessor: IP serverDimensionsYinch " + serverDimensionsYinch);

        double clientDimensionsX = clientDims[0];
        double clientDimensionsY = clientDims[1];
        double clientDimensionsXdp = clientDims[2];
        double clientDimensionsYdp = clientDims[3];
        double clientDPI = clientDims[5];
        double clientDimensionsXinch = clientDimensionsX / clientDPI;
        double clientDimensionsYinch = clientDimensionsY / clientDPI;

        Log.i("myLogs", "ImageProcessor: IP clientDimensionsX " + clientDimensionsX);
        Log.i("myLogs", "ImageProcessor: IP clientDimensionsY " + clientDimensionsY);
        Log.i("myLogs", "ImageProcessor: IP clientDimensionsXdp " + clientDimensionsXdp);
        Log.i("myLogs", "ImageProcessor: IP clientDimensionsYdp " + clientDimensionsYdp);
        Log.i("myLogs", "ImageProcessor: IP clientDPI " + clientDPI);
        Log.i("myLogs", "ImageProcessor: IP clientDimensionsXinch " + clientDimensionsXinch);
        Log.i("myLogs", "ImageProcessor: IP clientDimensionsYinch " + clientDimensionsYinch);

        double cutPercentage;
        Bitmap myBitmapScaled = null, otherBitmapScaled = null;
        Bitmap[] cutBMs = new Bitmap[2];

        if (dir.equals("RIGHT") || dir.equals("LEFT")) {

            cutPercentage = serverDimensionsXinch / (serverDimensionsXinch + clientDimensionsXinch);
            int imgHeight = img.getHeight(), imgWidth = img.getWidth();

            if (dir.equals("LEFT")) {
                if (serverDimensionsYinch <= clientDimensionsYinch) {
                    //SMALL --> BIG

                    /** createBitmap method creates a new bitMap
                     *  @param img the source,
                     *  @param imgWidth*(1-cutPercentage)   initial image X position on a server side,
                     *  @param 0                            initial image Y position on a server side,
                     *  @param imgWidth-(int)(imgWidth*(1-cutPercentage))  desired width
                     *  @param imgHeight                                   desired height
                     *  @return Bitmap                       a copy of a subset of the source bitmap or the source bitmap itself
                     *
                     * createScaledBitmap method involves scaling if it's needed
                     *  @param myBitmap the source,
                     *  @param serverDimensionsX   desired width
                     *  @param serverDimensionsY   desired height
                     *  @param false               filter
                     *  @return Bitmap             new scaled bitmap or input bitmap without any changes
                     *
                     *  @author Andrii Artamonov
                     */


                    myBitmap = Bitmap.createBitmap(img, (int) (imgWidth * (1 - cutPercentage)), 0, imgWidth - (int) (imgWidth * (1 - cutPercentage)), imgHeight);
                        myBitmapScaled = Bitmap.createScaledBitmap(myBitmap, (int) serverDimensionsX, (int) serverDimensionsY, false);
                    Log.i("myLogs", "ImageProcessor: IP myBitmapScaledX " + myBitmapScaled.getWidth());
                    Log.i("myLogs", "ImageProcessor: IP myBitmapScaledY " + myBitmapScaled.getHeight());

                    otherBitmap = Bitmap.createBitmap(img, 0, 0, (int) (imgWidth * (1 - cutPercentage)), imgHeight);
                    otherBitmapScaled = Bitmap.createScaledBitmap(otherBitmap, (int) clientDimensionsX, (int) (serverDimensionsYdp * otherDens), false);
                    Log.i("myLogs", "ImageProcessor: IP otherBitmapScaledX " + otherBitmapScaled.getWidth());
                    Log.i("myLogs", "ImageProcessor: IP otherBitmapScaledY " + otherBitmapScaled.getHeight());
                } else {
                    //BIG --> SMALL

                    myBitmap = Bitmap.createBitmap(img, (int) (imgWidth * (1 - cutPercentage)), 0, imgWidth - (int) (imgWidth * (1 - cutPercentage)), imgHeight);
                    myBitmapScaled = Bitmap.createScaledBitmap(myBitmap, (int) serverDimensionsX, (int) (clientDimensionsYdp * myDens), false);
                    Log.i("myLogs", "ImageProcessor: IP myBitmapScaledX " + myBitmapScaled.getWidth());
                    Log.i("myLogs", "ImageProcessor: IP myBitmapScaledY " + myBitmapScaled.getHeight());

                    otherBitmap = Bitmap.createBitmap(img, 0, 0, (int) (imgWidth * (1 - cutPercentage)), imgHeight);
                    otherBitmapScaled = Bitmap.createScaledBitmap(otherBitmap, (int) clientDimensionsX, (int) clientDimensionsY, false);

                    Log.i("myLogs", "ImageProcessor: IP otherBitmapScaledX " + otherBitmapScaled.getWidth());
                    Log.i("myLogs", "ImageProcessor: IP otherBitmapScaledY " + otherBitmapScaled.getHeight());
                }
            } else if (dir.equals("RIGHT")) {
                if (serverDimensionsYinch <= clientDimensionsYinch) {
                    //SMALL --> BIG

                    myBitmap = Bitmap.createBitmap(img, 0, 0, (int) (imgWidth * cutPercentage), imgHeight);
                    myBitmapScaled = Bitmap.createScaledBitmap(myBitmap, (int) serverDimensionsX, (int) serverDimensionsY, false);
                    Log.i("myLogs", "ImageProcessor: IP myBitmapScaledX " + myBitmapScaled.getWidth());
                    Log.i("myLogs", "ImageProcessor: IP myBitmapScaledY " + myBitmapScaled.getHeight());

                    otherBitmap = Bitmap.createBitmap(img, (int) (imgWidth * cutPercentage), 0, imgWidth - (int) (imgWidth * cutPercentage), imgHeight);
                    otherBitmapScaled = Bitmap.createScaledBitmap(otherBitmap, (int) clientDimensionsX, (int) (serverDimensionsYdp * otherDens), false);

                    Log.i("myLogs", "ImageProcessor: IP otherBitmapScaledX " + otherBitmapScaled.getWidth());
                    Log.i("myLogs", "ImageProcessor: IP otherBitmapScaledY " + otherBitmapScaled.getHeight());
                } else {
                    //BIG --> SMALL

                    myBitmap = Bitmap.createBitmap(img, 0, 0, (int) (imgWidth * cutPercentage), imgHeight);
                    myBitmapScaled = Bitmap.createScaledBitmap(myBitmap, (int) serverDimensionsX, (int) (clientDimensionsYdp * myDens), false);
                    Log.i("myLogs", "ImageProcessor: IP myBitmapScaledX " + myBitmapScaled.getWidth());
                    Log.i("myLogs", "ImageProcessor: IP myBitmapScaledY " + myBitmapScaled.getHeight());

                    otherBitmap = Bitmap.createBitmap(img, (int) (imgWidth * cutPercentage), 0, imgWidth - (int) (imgWidth * cutPercentage), imgHeight);
                    otherBitmapScaled = Bitmap.createScaledBitmap(otherBitmap, (int) clientDimensionsX, (int) clientDimensionsY, false);
                    Log.i("myLogs", "ImageProcessor: IP otherBitmapScaledX " + otherBitmapScaled.getWidth());
                    Log.i("myLogs", "ImageProcessor: IP otherBitmapScaledY " + otherBitmapScaled.getHeight());
                }
            }
        }

        cutBMs[0] = myBitmapScaled;
        cutBMs[1] = otherBitmapScaled;
        return cutBMs;
    }

    public double[] getImageViewDims() {
        if (myX >= otherX) {
            return clientDims;
        } else {
            return serverDims;
        }
    }
}
