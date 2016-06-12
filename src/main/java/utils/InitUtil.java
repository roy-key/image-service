package utils;

import exceptions.CorticaImageException;
import model.ImageBean;
import model.ManipulatedData;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import runnables.ImageDownloader;
import runnables.ImageManipulator;
import runnables.ImagePersister;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by roykey on 11/06/2016.
 */
public class InitUtil {

    private static final Logger logger = Logger.getRootLogger();


    public static LinkedBlockingQueue<String> initImgUrlsBlockingQueue(String[] imageUrls) {

        LinkedBlockingQueue<String> imgUrlsBlockingQueue = new LinkedBlockingQueue<>();

        for (String imageUrl : imageUrls) {
            imgUrlsBlockingQueue.add(imageUrl);
        }

        return imgUrlsBlockingQueue;
    }

    public static LinkedBlockingQueue<ImageBean> initLinkedBlockingQueue() {
        return new LinkedBlockingQueue<>();
    }

    public static ExecutorService initExecutorService(int numOfThreads) {
        return Executors.newFixedThreadPool(numOfThreads);
    }


    public static List<Runnable> initImageDownloaderRunnables(LinkedBlockingQueue<String> imgUrlsBlockingQueue,
                                                              LinkedBlockingQueue<ImageBean> downloadedImagesBlockingQueue,
                                                              ReentrantLock downloaderReentrantLock,
                                                              Condition downloaderNotFull,
                                                              int numOfThreads) {

        List<Runnable> imageDownloaderRunnables = new ArrayList<>(numOfThreads);
        for (int i = 0; i < numOfThreads; i++) {
            Runnable worker = new ImageDownloader(
                    imgUrlsBlockingQueue,
                    downloadedImagesBlockingQueue,
                    downloaderReentrantLock,
                    downloaderNotFull);
            imageDownloaderRunnables.add(worker);
        }
        return imageDownloaderRunnables;
    }


    public static List<Runnable> initImageManipulatorsRunnables(LinkedBlockingQueue<ImageBean> downloadedImagesBlockingQueue,
                                                                LinkedBlockingQueue<ImageBean> manipulatedImagesBlockingQueue,
                                                                ReentrantLock downloaderReentrantLock,
                                                                Condition downloaderNotFull,
                                                                ReentrantLock manipulatorReentrantLock,
                                                                Condition manipulatorNotFull,
                                                                ManipulatedData manipulatedData,
                                                                int capacity,
                                                                int numOfThreads) {

        AtomicInteger manipulatorCapacity = new AtomicInteger(capacity);

        List<Runnable> imageManipulatorsRunnables = new ArrayList<>(numOfThreads);

        for (int i = 0; i < numOfThreads; i++) {
            Runnable worker = new ImageManipulator(
                    downloadedImagesBlockingQueue,
                    manipulatedImagesBlockingQueue,
                    manipulatorCapacity,
                    manipulatedData,
                    downloaderReentrantLock,
                    manipulatorReentrantLock,
                    downloaderNotFull,
                    manipulatorNotFull);
            imageManipulatorsRunnables.add(worker);
        }

        return imageManipulatorsRunnables;
    }

    public static List<Runnable> initImagePersisterRunnables(LinkedBlockingQueue<ImageBean> manipulatedImagesBlockingQueue,
                                                             ReentrantLock manipulatorReentrantLock,
                                                             Condition manipulatorNotFull,
                                                             int capacity,
                                                             int numOfThreads) throws CorticaImageException {

        AtomicInteger persisterCapacity = new AtomicInteger(capacity);
        List<Runnable> imagePersistersRunnables = new ArrayList<>(numOfThreads);

        for (int i = 0; i < numOfThreads; i++) {
            try {
                Runnable worker = new ImagePersister(manipulatedImagesBlockingQueue,
                        persisterCapacity,
                        manipulatorReentrantLock,
                        manipulatorNotFull);
                imagePersistersRunnables.add(worker);

            } catch (CorticaImageException e) {
                logger.log(Level.FATAL, e.getMessage(), e);
                throw e;
            }
        }

        return imagePersistersRunnables;
    }
}
