import model.ImageBean;
import model.ImageFilter;
import model.ManipulatedData;
import org.junit.Assert;
import org.junit.Test;
import runnables.ImageManipulator;
import utils.ExecutorUtil;
import utils.InitUtil;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by roykey on 12/06/2016.
 */
public class ImageManipulatorTest extends BaseImageTest {

    public static final int PX_WIDTH = 200;
    public static final int PX_HEIGHT = 200;

    @Test
    public void a_manipulator_all_working_three_thread() {

        String[] imageUrls = getImgUrlsStrings();

        int numOfThreads = 1;
        int length = imageUrls.length;

        List<Runnable> runnables = setupTest(imageUrls, numOfThreads);
        actualTest(length, runnables);

    }

    private List<Runnable> setupTest(String[] imageUrls, int numOfThreads) {

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
        ManipulatedData manipulatedData = new ManipulatedData(PX_WIDTH, PX_HEIGHT, ImageFilter.GRAYSCALE);

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

        ExecutorService downloaderExecutor = InitUtil.initExecutorService(numOfThreads);
        ExecutorService manipulatorExecutor = InitUtil.initExecutorService(numOfThreads);

        ExecutorUtil.executeRunnables(imageDownloaderRunnables, downloaderExecutor);
        ExecutorUtil.executeRunnables(imageManipulatorsRunnables, manipulatorExecutor);

        downloaderExecutor.shutdown();
        manipulatorExecutor.shutdown();

        try {

            downloaderExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            manipulatorExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

        } catch (InterruptedException e) {
            Assert.assertFalse(e.getMessage(), true);
        }

        return imageManipulatorsRunnables;

    }


    private void actualTest(int imageUrlsLength, List<Runnable> imageManipulatorsRunnables) {

        LinkedBlockingQueue<ImageBean> manipulatorImagesBlockingQueueResTemp = null;

        for (Runnable runnable : imageManipulatorsRunnables) {

            ImageManipulator imageManipulatorsRunnable = (ImageManipulator) runnable;

            LinkedBlockingQueue<ImageBean> manipulatedImagesBlockingQueue = imageManipulatorsRunnable.getManipulatedImagesBlockingQueue();
            if (manipulatorImagesBlockingQueueResTemp == null) {
                manipulatorImagesBlockingQueueResTemp = manipulatedImagesBlockingQueue;
            }
            Assert.assertEquals(imageUrlsLength, manipulatedImagesBlockingQueue.size());
            Assert.assertEquals(0, imageManipulatorsRunnable.getManipulatorReentrantLock().getHoldCount());
            Assert.assertFalse(imageManipulatorsRunnable.getManipulatorReentrantLock().isHeldByCurrentThread());
        }

        Assert.assertNotNull(manipulatorImagesBlockingQueueResTemp);
        for (ImageBean imageBean : manipulatorImagesBlockingQueueResTemp) {
            Assert.assertNotNull(imageBean.getManipulatedData());

        }
    }
}
