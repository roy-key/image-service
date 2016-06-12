package utils;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by roykey on 11/06/2016.
 */
public class ExecutorUtil {

    private static final Logger logger = Logger.getRootLogger();

    public static void execute(List<Runnable> imageDownloaderRunnables, List<Runnable> imageManipulatorsRunnables, List<Runnable> imagePersistersRunnables, int numOfThreads){

        ExecutorService downloaderExecutor = InitUtil.initExecutorService(numOfThreads);
        ExecutorService manipulatorExecutor = InitUtil.initExecutorService(numOfThreads);
        ExecutorService persisterExecutor = InitUtil.initExecutorService(numOfThreads);

        executeRunnables(imageDownloaderRunnables, downloaderExecutor);
        executeRunnables(imageManipulatorsRunnables, manipulatorExecutor);
        executeRunnables(imagePersistersRunnables, persisterExecutor);

        downloaderExecutor.shutdown();
        manipulatorExecutor.shutdown();
        persisterExecutor.shutdown();

        try {
            downloaderExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            manipulatorExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            persisterExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }

    }

    public static void executeRunnables(List<Runnable> runnableList, ExecutorService executorService) {
        runnableList.forEach(executorService::execute);
    }
}
