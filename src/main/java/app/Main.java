package app;

import exceptions.CorticaImageException;
import model.ImageBean;
import model.ImageFilter;
import model.ManipulatedData;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import utils.ExecutorUtil;
import utils.ImageFileUtil;
import utils.InitUtil;
import utils.PropertyUtil;

import java.io.File;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by roykey on 09/06/2016.
 */
public class Main {

    private static final Logger logger = Logger.getRootLogger();
    
    public static void main(String[] args) throws CorticaImageException {

        String[] imageUrls = ImageFileUtil.readImageUrlsFromFile(new File("src/main/resources/input.images.txt"));
        Properties properties = PropertyUtil.readPropertieFile("src/main/resources/config.properties");

        int pxWidth = Integer.valueOf(properties.getProperty("pxWidth"));
        int pxHeight = Integer.valueOf(properties.getProperty("pxHeight"));
        int numOfThreads = Integer.valueOf(properties.getProperty("numOfThreads"));

        int capacity = imageUrls.length;

        LinkedBlockingQueue<String> imgUrlsBlockingQueue = InitUtil.initImgUrlsBlockingQueue(imageUrls);

        LinkedBlockingQueue<ImageBean> downloadedImagesBlockingQueue = InitUtil.initLinkedBlockingQueue();
        LinkedBlockingQueue<ImageBean> manipulatedImagesBlockingQueue = InitUtil.initLinkedBlockingQueue();

        ReentrantLock downloaderReentrantLock = new ReentrantLock();
        final Condition downloaderNotFull  = downloaderReentrantLock.newCondition();

        List<Runnable> imageDownloaderRunnables = InitUtil.initImageDownloaderRunnables(
                imgUrlsBlockingQueue,
                downloadedImagesBlockingQueue,
                downloaderReentrantLock,
                downloaderNotFull,
                numOfThreads);


        ReentrantLock manipulatorReentrantLock = new ReentrantLock();
        final Condition manipulatorNotFull  = manipulatorReentrantLock.newCondition();
        ManipulatedData manipulatedData = new ManipulatedData(pxWidth, pxHeight, ImageFilter.GRAYSCALE);

        List<Runnable> imageManipulatorsRunnables = InitUtil.initImageManipulatorsRunnables(
                downloadedImagesBlockingQueue,
                manipulatedImagesBlockingQueue,
                downloaderReentrantLock,
                downloaderNotFull,
                manipulatorReentrantLock,
                manipulatorNotFull,
                manipulatedData,
                capacity,
                numOfThreads);


        List<Runnable> imagePersistersRunnables = InitUtil.initImagePersisterRunnables(
                manipulatedImagesBlockingQueue,
                manipulatorReentrantLock,
                manipulatorNotFull,
                capacity,
                numOfThreads);

        ExecutorUtil.execute(imageDownloaderRunnables, imageManipulatorsRunnables, imagePersistersRunnables, numOfThreads);

        logger.log(Level.INFO, "Main Done");

    }
}
