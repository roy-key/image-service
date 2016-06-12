package runnables;

import model.ImageBean;
import model.ImageFilter;
import model.ManipulatedData;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by roykey on 09/06/2016.
 */
public class ImageManipulator implements Runnable {

    private static final Logger logger = Logger.getRootLogger();

    private LinkedBlockingQueue<ImageBean> downloadedImagesBlockingQueue;
    private LinkedBlockingQueue<ImageBean> manipulatedImagesBlockingQueue;
    private AtomicInteger capacity;
    private ManipulatedData manipulatedData;
    private ReentrantLock downloaderReentrantLock;
    private ReentrantLock manipulatorReentrantLock;
    private Condition downloaderNotFull;
    private Condition manipulatorNotFull;

    public ImageManipulator(LinkedBlockingQueue<ImageBean> downloadedImagesBlockingQueue,
                            LinkedBlockingQueue<ImageBean> manipulatedImagesBlockingQueue,
                            AtomicInteger capacity,
                            ManipulatedData manipulatedData,
                            ReentrantLock downloaderReentrantLock,
                            ReentrantLock manipulatorReentrantLock,
                            Condition downloaderNotFull,
                            Condition manipulatorNotFull) {

        this.downloadedImagesBlockingQueue = downloadedImagesBlockingQueue;
        this.manipulatedImagesBlockingQueue = manipulatedImagesBlockingQueue;
        this.capacity = capacity;
        this.downloaderReentrantLock = downloaderReentrantLock;
        this.manipulatorReentrantLock = manipulatorReentrantLock;
        this.downloaderNotFull = downloaderNotFull;
        this.manipulatorNotFull = manipulatorNotFull;
        this.manipulatedData = manipulatedData;
    }

    @Override
    public void run() {
        while (capacity.get() > 0) {
            downloaderReentrantLock.lock();
            if (capacity.get() > 0) {

                ImageBean imageBean = downloadedImagesBlockingQueue.poll();

                if (imageBean != null) {

                    capacity.decrementAndGet();
                    if (capacity.get() == 0) {
                        downloaderNotFull.signalAll();
                    }
                    downloaderReentrantLock.unlock();

                    if (imageBean.getOriginalImage() != null) {

                        BufferedImage originalImage = imageBean.getOriginalImage();

                        BufferedImage resizeImage = resizeImage(originalImage, manipulatedData.getPxWidth(), manipulatedData.getPxHeight());
                        BufferedImage filteredImage = applyFilterOnImage(manipulatedData.getFilter(), resizeImage);

                        imageBean.setFilteredImage(filteredImage);

                        imageBean.setManipulatedData(manipulatedData);
                    }

                    manipulatedImagesBlockingQueue.add(imageBean);

                    signalAllPersisters();

                } else {
                    try {
                        downloaderNotFull.await();
                        downloaderReentrantLock.unlock();
                    } catch (InterruptedException e) {
                        logger.log(Level.ERROR, e.getMessage(), e);
                    }
                }
            }
        }

        logger.log(Level.INFO, "Manipulator: " + Thread.currentThread().getId() + "  Ended Gracefully");
    }

    private void signalAllPersisters() {
        manipulatorReentrantLock.lock();
        manipulatorNotFull.signalAll();
        manipulatorReentrantLock.unlock();
    }


    private BufferedImage resizeImage(BufferedImage originalBufferedImage,
                                      int pxWidth,
                                      int pxHeight) {

        BufferedImage resizedImage = new BufferedImage(pxWidth, pxHeight, originalBufferedImage.getType());
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalBufferedImage, 0, 0, pxWidth, pxHeight, null);
        g.dispose();
        return resizedImage;
    }

    private BufferedImage applyFilterOnImage(ImageFilter imageFilter, BufferedImage image) {

        int width = image.getWidth();
        int height = image.getHeight();

        switch (imageFilter) {
            case GRAYSCALE:
                image = applyGrayscaleFilter(image, width, height);
                break;
        }

        return image;

    }

    private BufferedImage applyGrayscaleFilter(BufferedImage image, int width, int height) {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {

                Color c = new Color(image.getRGB(j, i));
                int red = (int) (c.getRed() * 0.299);
                int green = (int) (c.getGreen() * 0.587);
                int blue = (int) (c.getBlue() * 0.114);
                Color newColor = new Color(red + green + blue,

                        red + green + blue, red + green + blue);

                image.setRGB(j, i, newColor.getRGB());
            }
        }

        return image;
    }

    public LinkedBlockingQueue<ImageBean> getManipulatedImagesBlockingQueue() {
        return manipulatedImagesBlockingQueue;
    }

    public ReentrantLock getManipulatorReentrantLock() {
        return manipulatorReentrantLock;
    }
}
