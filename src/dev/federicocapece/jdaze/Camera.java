package dev.federicocapece.jdaze;

import java.awt.*;

/**
 * The main camera used by the renderer.
 * For now there can be only one camera.
 */
public class Camera extends GameObject {
    //TODO: javadoc

    /**
     * This is protected to avoid creating new cameras.
     */
    protected Camera() {
        position.set(0,0);
    }


    /**
     * The scale of the camera
     */
    private float scale = 1;


    /**
     * Get the scale of the camera as a float
     * <pre>
     * Examples:
     * With scale = 1 the camera will render pixel 1:1
     * With scale = 2 the camera will be zoomed in and each pixel will render to 4 pixels
     * With scale = .5 the camera will be zoomed in and each 4 pixel will be rendered as 1 pixel
     * </pre>
     * @return the scale
     */
    public float getScale() {
        return scale;
    }


    /**
     * Zoom out the camera by X Multiplier
     * @param multiplier
     */
    public void zoomIn(float multiplier){
        if(multiplier == 0) throw new ArithmeticException("Invalid multiplier (0 not accepted)");
        scale *= multiplier;
    }

    /**
     * Zoom out the camera by X Multiplier
     * @param multiplier
     */
    public void zoomOut(float multiplier){
        if(multiplier == 0) throw new ArithmeticException("Invalid multiplier (0 not accepted)");
        scale /= multiplier;
    }


    /**
     * Zoom in the camera by X2
     */
    public void zoomIn(){
        zoomIn(2);
    }

    /**
     * Zoom out the camera by X2
     */
    public void zoomOut(){
        zoomIn(.5f);
    }


    /**
     * Add a float to the current scale multiplier.
     * This can be useful if you want to gradually zoom in by using deltaTime
     * @param zoom the new zoom to add the the scale
     */
    public void addZoom(float zoom){
        float newScale = zoom + scale;
        if(newScale == 0) throw new ArithmeticException("Invalid zoom (0 not accepted)");
        scale = newScale;
    }

}
