package runnables;

import exceptions.CorticaImageException;
import model.ImageBean;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by roykey on 09/06/2016.
 */
public class ImageDownloader implements Runnable {

    protected static final Logger logger = Logger.getRootLogger();

    private LinkedBlockingQueue<ImageBean> downloadedImagesBlockingQueue;
    private LinkedBlockingQueue<String> imgUrlsBlockingQueue;
    private ReentrantLock downloaderReentrantLock;
    private Condition downloaderNotFull;

    public ImageDownloader(LinkedBlockingQueue<String> imgUrlsBlockingQueue,
                           LinkedBlockingQueue<ImageBean> downloadedImagesBlockingQueue,
                           ReentrantLock downloaderReentrantLock,
                           Condition downloaderNotFull) {

        this.downloadedImagesBlockingQueue = downloadedImagesBlockingQueue;
        this.imgUrlsBlockingQueue = imgUrlsBlockingQueue;
        this.downloaderReentrantLock = downloaderReentrantLock;
        this.downloaderNotFull = downloaderNotFull;

    }

    @Override
    public void run() {
        while (!this.imgUrlsBlockingQueue.isEmpty()) {
            BufferedImage bufferedImage;
            try {

                String imgUrl = this.imgUrlsBlockingQueue.take();

                bufferedImage = downloadImage(imgUrl);

                ImageBean imageEntity = new ImageBean();

                imageEntity.setUrl(imgUrl);
                imageEntity.setOriginalImage(bufferedImage);

                this.downloadedImagesBlockingQueue.add(imageEntity);


            } catch (CorticaImageException e) {

                logger.log(Level.ERROR, e.getMessage());
                this.downloadedImagesBlockingQueue.add(new ImageBean()); // dummy object

            } catch (InterruptedException e) {
                logger.log(Level.ERROR, e.getMessage());
            } finally {
                signalAllManipulators();
            }
        }

        logger.log(Level.INFO, "Downloader: " + Thread.currentThread().getId() + "  Ended Gracefully");
    }

    private BufferedImage downloadImage(String imgUrl) throws CorticaImageException {
        BufferedImage image;
        try {
            URL url = new URL(imgUrl);
            image = ImageIO.read(url);
            return image;
        } catch (IOException e) {
            throw new CorticaImageException("Image url" + imgUrl + "failed to download due to " + e.getMessage(), e);
        }
    }

    private void signalAllManipulators() {
        downloaderReentrantLock.lock();
        downloaderNotFull.signalAll();
        downloaderReentrantLock.unlock();
    }

    public LinkedBlockingQueue<ImageBean> getDownloadedImagesBlockingQueue() {
        return downloadedImagesBlockingQueue;
    }

    public ReentrantLock getDownloaderReentrantLock() {
        return downloaderReentrantLock;
    }
}
