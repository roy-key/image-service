package runnables;

import db.HsqldbImageDAO;
import model.ImageBean;
import exceptions.CorticaImageException;
import model.ManipulatedData;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by roykey on 09/06/2016.
 */
public class ImagePersister implements Runnable {

    private static final Logger logger = Logger.getRootLogger();

    private LinkedBlockingQueue<ImageBean> manipulatedImagesBlockingQueue;
    private AtomicInteger capacity;
    private ReentrantLock manipulatedReentrantLock;
    private Condition manipulatedNotFull;
    private HsqldbImageDAO hsqlImageDAO;

    public ImagePersister(LinkedBlockingQueue<ImageBean> manipulatedImagesBlockingQueue,
                          AtomicInteger capacity,
                          ReentrantLock manipulatedReentrantLock,
                          Condition manipulatedNotFull) throws CorticaImageException {

        this.manipulatedImagesBlockingQueue = manipulatedImagesBlockingQueue;
        this.capacity = capacity;
        this.manipulatedReentrantLock = manipulatedReentrantLock;
        this.manipulatedNotFull = manipulatedNotFull;
        this.hsqlImageDAO = new HsqldbImageDAO();

    }

    @Override
    public void run() {
        while (capacity.get() > 0) {
            try {
                manipulatedReentrantLock.lock();
                if (capacity.get() > 0) {

                    ImageBean imageBean = manipulatedImagesBlockingQueue.poll();

                    if (imageBean != null) {

                        capacity.decrementAndGet();
                        if (capacity.get() == 0) {
                            manipulatedNotFull.signalAll(); //prevents the deadlock where t1 handles the last one, while others await
                        }
                        manipulatedReentrantLock.unlock();

                        if (imageBean.getManipulatedData() != null) {

                            BufferedImage filteredImage = imageBean.getFilteredImage();

                            setMd5ToImageBean(imageBean, filteredImage);
                            setDirectoryPathToImageBean(imageBean);

                            saveImageToFile(imageBean);

                            hsqlImageDAO.saveImageIntoDB(imageBean);
                        }

                    }
                    /* its null on this occasions:
                     * 1. the persister thread scheduled before the manipulator put anything in the queue, hence we need to wait.
                     * Thread will be awake if:
                     * 1. Manipulator thread inserted thing to the queue.
                     * 2. Last persister thread finished is work.
                    */
                    else {
                        try {
                            manipulatedNotFull.await();
                            manipulatedReentrantLock.unlock();

                        } catch (InterruptedException e) {
                            logger.log(Level.ERROR, e.getMessage(), e);
                        }
                    }
                }
            } catch (CorticaImageException e) {
                logger.log(Level.ERROR, e.getMessage(), e);
            }
        }

        logger.log(Level.INFO, "Persister: " + Thread.currentThread().getId() + "  Ended Gracefully");
    }


    private void saveImageToFile(ImageBean imageBean) throws CorticaImageException {
        File outputfile = new File(imageBean.getDirectoryPath());
        try {
            ImageIO.write(imageBean.getFilteredImage(), "jpg", outputfile);
        } catch (IOException e) {
            throw new CorticaImageException("Image " + imageBean.getUrl() + "failed to be save to file due to : " + e.getMessage(), e);
        }
    }

    private void setDirectoryPathToImageBean(ImageBean imageBean) {
        String pathToFile = getPathToFile(imageBean);
        imageBean.setDirectoryPath(pathToFile);
    }


    private String getPathToFile(ImageBean imageBean) {

        ManipulatedData manipulatedData = imageBean.getManipulatedData();
        int pxWidth = manipulatedData.getPxWidth();
        int pxHeight = manipulatedData.getPxHeight();
        String filter = manipulatedData.getFilter().name();

        String imgUrl = imageBean.getUrl();
        return filter + "_" + pxWidth + "*" + pxHeight + "_" + imgUrl.substring(imgUrl.lastIndexOf('/') + 1, imgUrl.length());
    }

    private void setMd5ToImageBean(ImageBean imageBean, BufferedImage bufferedImage) throws CorticaImageException {

        String md5 = calculateMD5OfImage(imageBean, bufferedImage);
        imageBean.setMd5(md5);
    }

    private String calculateMD5OfImage(ImageBean imageBean, BufferedImage bufferedImage) throws CorticaImageException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            ImageIO.write(bufferedImage, "jpg", outputStream);
        } catch (IOException e) {
            throw new CorticaImageException("Image " + imageBean.getUrl() + "failed to create a ByteArrayOutputStream from BufferedImage" + e.getMessage(), e);
        }
        byte[] data = outputStream.toByteArray();

        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new CorticaImageException("ImageUtil failed to digest a MD5 hash" + e.getMessage(), e);
        }
        md.update(data);

        return new BigInteger(1, md.digest()).toString(16);
    }

    public HsqldbImageDAO getHsqlImageDAO() {
        return hsqlImageDAO;
    }

    public LinkedBlockingQueue<ImageBean> getManipulatedImagesBlockingQueue() {
        return manipulatedImagesBlockingQueue;
    }

    public ReentrantLock getManipulatedReentrantLock() {
        return manipulatedReentrantLock;
    }
}
