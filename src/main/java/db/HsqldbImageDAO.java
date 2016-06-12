package db;

import model.ImageBean;
import exceptions.CorticaImageException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by roykey on 09/06/2016.
 */
public class HsqldbImageDAO extends ConfigDB implements ImageDAO {

    private static final String DB_NAME = "jdbc:hsqldb:mem:images_db";
    private static final String USERNAME = "sa";
    private static final String PASSWORD = "";
    private static final String DRIVER = "com.mysql.jdbc.Driver";

    public HsqldbImageDAO() throws CorticaImageException {
        super(DB_NAME, USERNAME, PASSWORD, DRIVER);
        try {
            createTable();
        } catch (SQLException e) {
            throw new CorticaImageException("Cannot create DB: " + e.getMessage(), e);
        }
    }

    @Override
    public void saveImageIntoDB(ImageBean imageBean) throws CorticaImageException {
        try {
            insertIntoTable(imageBean.getDirectoryPath(), imageBean.getUrl(), imageBean.getMd5());
        } catch (SQLException e) {
            throw new CorticaImageException("Cannot save file to DB: " + e.getMessage(), e);
        }
    }

    @Override
    public int getSizeOfTable() throws CorticaImageException {
        String query = "SELECT * FROM images; ";
        Statement st = createStatement();
		ResultSet result = null;
        int size = 0;

        try {
			result = st.executeQuery(query);
			while (result.next()) { // process results one row at a time
                size++;
            }

		}
		catch (Exception e) {
			_logger.error(e.getLocalizedMessage());
			throw new CorticaImageException("Failed to get image form DB " + e.getLocalizedMessage(), e);
		}
		finally {
			closeStatement(st, result);
		}

        return size;
    }

    public void createTable() throws SQLException {
        String query = "CREATE TABLE IF NOT EXISTS images "
                + "(downloadDate DATE, filepath VARCHAR(2000),"
                + "url VARCHAR(2000), md5 VARCHAR(32))";
        try (Statement s = createStatement()) {
            s.executeUpdate(query);
        }
    }

    public void insertIntoTable(String directoryPath, String imageUrl, String imageMD5) throws SQLException {

        String query = "INSERT INTO images (downloadDate, filepath, url, md5) "
                + " VALUES(now(), '"+ directoryPath +"', '" + imageUrl + "','" + imageMD5 + "')";

        try (Statement s = createStatement()) {
            s.executeUpdate(query);
        }
    }
}
