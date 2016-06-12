import model.ImageBean;
import org.junit.Assert;
import org.junit.Test;
import runnables.ImageDownloader;
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
public class ImageDownloaderTest extends BaseImageTest{

    @Test
    public void a_download_all_working_one_thread(){

        String[] imageUrls = getImgUrlsStrings();

        int numOfThreads = 1;
        int length = imageUrls.length;

        List<Runnable> runnables = setupTest(imageUrls, numOfThreads);
        actualTest(length, runnables);

    }

    @Test
    public void b_download_all_working_three_thread(){

        String[] imageUrls = getImgUrlsStrings();

        int numOfThreads = 3;
        int length = imageUrls.length;
        List<Runnable> runnables = setupTest(imageUrls, numOfThreads);
        actualTest(length, runnables);


    }

    @Test
    public void c_download_not_all_working_three_thread(){

        String[] imageUrls = new String[]{
                "http://carbl.com/im/2013101/07/Suzuki-Swift-5d-600x324.jpg",
                "http://carbl.com/im/2013/06/Cadilalc-CTS-2014-600x324.jpg",
                "http://carbl.com/im/2013/06/Chevrolet-Camaro-600x324.jpg",
                "http://media.zenfs.com/en-US/blogs/omgcelebnews/30566e0a-26e1-4bfc-b2ee-b0dd3bc1ee35_620_TomCruise_072213.jpg"};

        int numOfThreads = 3;
        int length = imageUrls.length;
        List<Runnable> runnables = setupTest(imageUrls, numOfThreads);
        actualTestNotWorking(length, runnables);

    }


    @Test
    public void d_download_empty_urls_array_three_thread(){

        String[] imageUrls = new String[]{};

        int numOfThreads = 3;
        int length = imageUrls.length;
        List<Runnable> runnables = setupTest(imageUrls, numOfThreads);
        actualTest(length, runnables);
    }

    private List<Runnable> setupTest(String[] imageUrls, int numOfThreads) {

        LinkedBlockingQueue<String> imgUrlsBlockingQueue = InitUtil.initImgUrlsBlockingQueue(imageUrls);
        LinkedBlockingQueue<ImageBean> downloadedImagesBlockingQueue = InitUtil.initLinkedBlockingQueue();

        ReentrantLock downloaderReentrantLock = new ReentrantLock();
        final Condition downloaderNotFull  = downloaderReentrantLock.newCondition();

        List<Runnable> imageDownloaderRunnables = InitUtil.initImageDownloaderRunnables(
                imgUrlsBlockingQueue,
                downloadedImagesBlockingQueue,
                downloaderReentrantLock,
                downloaderNotFull,
                numOfThreads);

        ExecutorService downloaderExecutor = InitUtil.initExecutorService(numOfThreads);
        ExecutorUtil.executeRunnables(imageDownloaderRunnables, downloaderExecutor);
        downloaderExecutor.shutdown();
        try {
            downloaderExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            Assert.assertFalse(e.getMessage(), false);
        }

        return imageDownloaderRunnables;

    }

    private void actualTest(int imageUrlsLength, List<Runnable> imageDownloaderRunnables) {

        LinkedBlockingQueue<ImageBean> downloadedImagesBlockingQueueResTemp = null;

        for (Runnable imageDownloaderRunnable : imageDownloaderRunnables) {
            ImageDownloader imageDownloader = (ImageDownloader) imageDownloaderRunnable;
            LinkedBlockingQueue<ImageBean> downloadedImagesBlockingQueueRes = imageDownloader.getDownloadedImagesBlockingQueue();
            if (downloadedImagesBlockingQueueResTemp  == null){
                downloadedImagesBlockingQueueResTemp = downloadedImagesBlockingQueueRes;
            }
            Assert.assertEquals(imageUrlsLength, downloadedImagesBlockingQueueRes.size());
            Assert.assertEquals(0, imageDownloader.getDownloaderReentrantLock().getHoldCount());
            Assert.assertFalse(imageDownloader.getDownloaderReentrantLock().isHeldByCurrentThread());
        }

        Assert.assertNotNull(downloadedImagesBlockingQueueResTemp);
        for (ImageBean imageBean : downloadedImagesBlockingQueueResTemp) {
            Assert.assertNotNull(imageBean.getOriginalImage());

        }
    }

    private void actualTestNotWorking(int imageUrlsLength, List<Runnable> imageDownloaderRunnables) {
        boolean found = false;
        LinkedBlockingQueue<ImageBean> downloadedImagesBlockingQueueResTemp = null;
        for (Runnable imageDownloaderRunnable : imageDownloaderRunnables) {
            ImageDownloader imageDownloader = (ImageDownloader) imageDownloaderRunnable;
            LinkedBlockingQueue<ImageBean> downloadedImagesBlockingQueueRes = imageDownloader.getDownloadedImagesBlockingQueue();
            if (downloadedImagesBlockingQueueResTemp  == null){
                downloadedImagesBlockingQueueResTemp = downloadedImagesBlockingQueueRes;
            }
            Assert.assertEquals(imageUrlsLength, downloadedImagesBlockingQueueRes.size());
            Assert.assertEquals(0, imageDownloader.getDownloaderReentrantLock().getHoldCount());
            Assert.assertFalse(imageDownloader.getDownloaderReentrantLock().isHeldByCurrentThread());
        }

        Assert.assertNotNull(downloadedImagesBlockingQueueResTemp);
        for (ImageBean imageBean : downloadedImagesBlockingQueueResTemp) {
            if (imageBean.getOriginalImage() == null){
                found = true;
            }
        }

        Assert.assertTrue(found);
    }
}
