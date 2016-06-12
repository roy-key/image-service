package model;

import java.awt.image.BufferedImage;

/**
 * Created by roykey on 09/06/2016.
 */
public class ImageBean {

    private BufferedImage filteredImage;
    private BufferedImage originalImage;

    private String url;
    private String directoryPath;
    private String md5;

    private ManipulatedData manipulatedData;

    public ImageBean() {
    }

    public ImageBean(BufferedImage originalImage, BufferedImage filteredImage, String url, String directoryPath, String md5) {
        this.originalImage = originalImage;
        this.filteredImage = filteredImage;
        this.url = url;
        this.directoryPath = directoryPath;
        this.md5 = md5;
    }

    public BufferedImage getOriginalImage() {
        return originalImage;
    }

    public void setOriginalImage(BufferedImage originalImage) {
        this.originalImage = originalImage;
    }

    public BufferedImage getFilteredImage() {
        return filteredImage;
    }

    public void setFilteredImage(BufferedImage filteredImage) {
        this.filteredImage = filteredImage;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDirectoryPath() {
        return directoryPath;
    }

    public void setDirectoryPath(String directoryPath) {
        this.directoryPath = directoryPath;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public ManipulatedData getManipulatedData() {
        return manipulatedData;
    }

    public void setManipulatedData(ManipulatedData manipulatedData) {
        this.manipulatedData = manipulatedData;
    }
}
