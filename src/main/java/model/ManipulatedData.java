package model;

/**
 * Created by roykey on 10/06/2016.
 */
public class ManipulatedData {

    private int pxWidth;
    private int pxHeight;
    private ImageFilter filter;

    public ManipulatedData(int pxWidth, int pxHeight, ImageFilter filter) {
        this.pxWidth = pxWidth;
        this.pxHeight = pxHeight;
        this.filter = filter;
    }

    public int getPxWidth() {
        return pxWidth;
    }

    public void setPxWidth(int pxWidth) {
        this.pxWidth = pxWidth;
    }

    public int getPxHeight() {
        return pxHeight;
    }

    public void setPxHeight(int pxHeight) {
        this.pxHeight = pxHeight;
    }

    public ImageFilter getFilter() {
        return filter;
    }

    public void setFilter(ImageFilter filter) {
        this.filter = filter;
    }
}
