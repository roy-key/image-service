package db;

import model.ImageBean;
import exceptions.CorticaImageException;

/**
 * Created by roykey on 09/06/2016.
 */
public interface ImageDAO {

    void saveImageIntoDB(ImageBean image) throws CorticaImageException;
    int getSizeOfTable() throws CorticaImageException;
}
