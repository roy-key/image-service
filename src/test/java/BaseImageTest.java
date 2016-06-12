import exceptions.CorticaImageException;
import org.junit.Assert;
import utils.ImageFileUtil;

import java.io.File;

/**
 * Created by roykey on 12/06/2016.
 */
abstract public class BaseImageTest {

    protected String[] getImgUrlsStrings() {
        String [] imageUrls = new String[0];
        try {
            imageUrls = ImageFileUtil.readImageUrlsFromFile(new File("src/main/resources/input.images.txt"));
        } catch (CorticaImageException e) {
            Assert.assertFalse(e.getMessage(), true);
        }
        return imageUrls;
    }

}
