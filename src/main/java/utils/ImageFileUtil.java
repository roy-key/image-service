package utils;

import exceptions.CorticaImageException;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by roykey on 12/06/2016.
 */
public class ImageFileUtil {

    public static String[] readImageUrlsFromFile(File file) throws CorticaImageException {

        // create token1
        String token1 = "";

        // create Scanner inFile1
        Scanner inFile1 = null;
        try {
            inFile1 = new Scanner(file).useDelimiter(",\\s*");
        } catch (FileNotFoundException e) {
            throw new CorticaImageException(e.getMessage(), e);
        }

        List<String> temps = new ArrayList<String>();

        while (inFile1.hasNext()) {
            token1 = inFile1.next();
            temps.add(token1);
        }
        inFile1.close();

        String[] tempsArray = temps.toArray(new String[0]);

        return tempsArray;
    }
}
