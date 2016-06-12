import db.HsqldbImageDAO;
import exceptions.CorticaImageException;
import model.ImageBean;
import model.ImageFilter;
import model.ManipulatedData;
import org.junit.Assert;
import org.junit.Test;
import runnables.ImagePersister;
import utils.ExecutorUtil;
import utils.InitUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by roykey on 12/06/2016.
 */
public class ImagePersisterTest {


    public static final int PX_WIDTH = 200;
    public static final int PX_HEIGHT = 200;

    @Test
    public void a_persister_all_working_three_thread() {


        String[] imageUrls = new String[]{
                "http://carbl.com/im/2013/07/Suzuki-Swift-5d-600x324.jpg"};

        int numOfThreads = 3;

        List<Runnable> runnables = setupTest(imageUrls, numOfThreads);
        actualTest(runnables);

    }

    private List<Runnable> setupTest(String[] imageUrls, int numOfThreads) {

        int capacity = imageUrls.length;

        LinkedBlockingQueue<String> imgUrlsBlockingQueue = InitUtil.initImgUrlsBlockingQueue(imageUrls);

        LinkedBlockingQueue<ImageBean> downloadedImagesBlockingQueue = InitUtil.initLinkedBlockingQueue();
        LinkedBlockingQueue<ImageBean> manipulatedImagesBlockingQueue = InitUtil.initLinkedBlockingQueue();

        ReentrantLock downloaderReentrantLock = new ReentrantLock();
        final Condition downloaderNotFull = downloaderReentrantLock.newCondition();

        List<Runnable> imageDownloaderRunnables = InitUtil.initImageDownloaderRunnables(
                imgUrlsBlockingQueue,
                downloadedImagesBlockingQueue,
                downloaderReentrantLock,
                downloaderNotFull,
                numOfThreads);


        ReentrantLock manipulatorReentrantLock = new ReentrantLock();
        final Condition manipulatorNotFull = manipulatorReentrantLock.newCondition();
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


        List<Runnable> imagePersistersRunnables = null;
        try {
            imagePersistersRunnables = InitUtil.initImagePersisterRunnables(
                    manipulatedImagesBlockingQueue,
                    manipulatorReentrantLock,
                    manipulatorNotFull,
                    capacity,
                    numOfThreads);
        } catch (CorticaImageException e) {
            Assert.assertFalse(e.getMessage(), false);
        }

        ExecutorUtil.execute(imageDownloaderRunnables, imageManipulatorsRunnables, imagePersistersRunnables, numOfThreads);

        return imagePersistersRunnables;
    }


    private void actualTest(List<Runnable> imagePersistersRunnables) {

        HsqldbImageDAO hsqlImageDAOTemp = null;

        for (Runnable runnable : imagePersistersRunnables) {

            ImagePersister imagePersistersRunnable = (ImagePersister) runnable;

            LinkedBlockingQueue<ImageBean> manipulatedImagesBlockingQueue = imagePersistersRunnable.getManipulatedImagesBlockingQueue();

            Assert.assertEquals(0, manipulatedImagesBlockingQueue.size());
            Assert.assertEquals(0, imagePersistersRunnable.getManipulatedReentrantLock().getHoldCount());
            Assert.assertFalse(imagePersistersRunnable.getManipulatedReentrantLock().isHeldByCurrentThread());

            if (hsqlImageDAOTemp == null) {
                hsqlImageDAOTemp = imagePersistersRunnable.getHsqlImageDAO();
            }

        }

        Assert.assertNotNull(hsqlImageDAOTemp);
        try {
            int sizeOfTable = hsqlImageDAOTemp.getSizeOfTable();
            Assert.assertEquals(1, sizeOfTable);
        } catch (CorticaImageException e) {
            Assert.assertFalse(e.getMessage(), false);
        }

        String s = "GRAYSCALE_200*200_Suzuki-Swift-5d-600x324.jpg";
        try {
            File input = new File(s);
            BufferedImage read = ImageIO.read(input);

            Assert.assertEquals(PX_WIDTH, read.getWidth());
            Assert.assertEquals(PX_HEIGHT, read.getHeight());

            boolean delete = input.delete();
            if (!delete) {
                Assert.assertFalse(input.getName() + " wasn't deleted", false);
            }

        } catch (IOException e){
            Assert.assertFalse(e.getMessage(), false);
        }
    }
}
