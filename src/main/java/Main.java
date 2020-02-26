import PostgresDB.DBConnectionClass;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import javax.imageio.IIOException;
import java.io.IOException;
import java.sql.SQLException;

public class Main {
    private static final Logger log = Logger.getLogger(Main.class);

    public static void main(String[] args) {


        try {
            ConsoleApp app = new ConsoleApp();

        } catch (IOException e) {
            log.error("IOException: ", e);
            e.printStackTrace();
        } catch (SQLException e) {
            log.error("SQLException: ", e);
            e.printStackTrace();
        }


    }
}
