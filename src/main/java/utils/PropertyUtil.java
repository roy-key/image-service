package utils;

import exceptions.CorticaImageException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by roykey on 12/06/2016.
 */
public class PropertyUtil {

    public static Properties readPropertieFile(String propertyPath) throws CorticaImageException {
        Properties prop = new Properties();

        try {

            FileInputStream input = new FileInputStream(propertyPath);
            prop.load(input);

        } catch (FileNotFoundException e) {
            throw new CorticaImageException(e.getMessage(), e);
        } catch (IOException e) {
            throw new CorticaImageException(e.getMessage(), e);
        }

        return prop;

    }
}
