package com.myapps.documentscanner;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.widget.Toast;

import com.myapps.documentscanner.uicamera.GraphicOverlay;
import com.google.android.gms.vision.text.Line;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;


import java.util.List;

/**
 * Graphic instance for rendering TextBlock position, size, and ID within an associated graphic
 * overlay view.
 */
public class OcrGraphic extends GraphicOverlay.Graphic {

    private int mId;

    private static final int TEXT_COLOR = Color.WHITE;

    private static Paint sRectPaint;
    private static Paint sTextPaint;
    private final TextBlock mText;
    private int tblock;


    OcrGraphic(GraphicOverlay overlay, TextBlock text) {
        super(overlay);

        mText = text;

        if (sRectPaint == null) {
            sRectPaint = new Paint();
            sRectPaint.setColor(TEXT_COLOR);
            sRectPaint.setStyle(Paint.Style.STROKE);
            sRectPaint.setStrokeWidth(4.0f);
        }

        if (sTextPaint == null) {
            sTextPaint = new Paint();
            sTextPaint.setColor(TEXT_COLOR);
            sTextPaint.setTextSize(54.0f);
        }
        // Redraw the overlay, as this graphic has been added.
        postInvalidate();
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        this.mId = id;
    }

    public TextBlock getTextBlock() {
        return mText;
    }

    /**
     * Checks whether a point is within the bounding box of this graphic.
     * The provided point should be relative to this graphic's containing overlay.
     *
     * @param x An x parameter in the relative context of the canvas.
     * @param y A y parameter in the relative context of the canvas.
     * @return True if the provided point is contained within this graphic's bounding box.
     */
    public boolean contains(float x, float y) {
        TextBlock text = mText;
        if (text == null) {
            return false;
        }
        RectF rect = new RectF(text.getBoundingBox());
        rect.left = translateX(rect.left);
        rect.top = translateY(rect.top);
        rect.right = translateX(rect.right);
        rect.bottom = translateY(rect.bottom);
        return (rect.left < x && rect.right > x && rect.top < y && rect.bottom > y);
    }

    /**
     * Draws the text block annotations for position, size, and raw value on the supplied canvas.
     */
    @Override
    public void draw(final Canvas canvas) {
        final TextBlock text = mText;
        if (text == null) {
            return;
        }

        // Draws the bounding box around the TextBlock.
        final RectF rect = new RectF(text.getBoundingBox());
        rect.left = translateX(rect.left);
        rect.top = translateY(rect.top);
        rect.right = translateX(rect.right);
        rect.bottom = translateY(rect.bottom);
        canvas.drawRect(rect, sRectPaint);

        OcrCaptureActivity oca = new OcrCaptureActivity();
        tblock = oca.getBlock();
        Log.d("OcrGraphic", "Selected tblock option: " + Integer.toString(tblock));

        switch (tblock) {

            case 0:
                //Draw the text blocks without breaking the text

                canvas.drawText(text.getValue(), rect.left, rect.bottom, sTextPaint);
                break;

            case 2:
                // Break the text into multiple lines and draw each one according to its own bounding box.

                List<? extends Text> textComponents1 = text.getComponents();
                for (Text currentText : textComponents1) {
                    //float left = translateX(currentText.getBoundingBox().left);
                    //float bottom = translateY(currentText.getBoundingBox().bottom);
                    //canvas.drawText(currentText.getValue(), left, bottom, sTextPaint);

                    List<? extends Text> lineComponents = currentText.getComponents();
                    for (Text currentText1 : lineComponents) {
                        float left = translateX(currentText1.getBoundingBox().left);
                        float bottom = translateY(currentText1.getBoundingBox().bottom);
                        canvas.drawText(currentText1.getValue(), left, bottom, sTextPaint);
                    }
                }
                break;

            default:
                // Break the lines into its component words and draw each one according to its own bounding box.

                List<? extends Text> textComponents = text.getComponents();
                for (Text currentText : textComponents) {
                    float left = translateX(currentText.getBoundingBox().left);
                    float bottom = translateY(currentText.getBoundingBox().bottom);
                    canvas.drawText(currentText.getValue(), left, bottom, sTextPaint);
                }
                break;
        }
    }
}
