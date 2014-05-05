package krb.test.mssql;

import com.microsoft.sqlserver.jdbc.SQLServerDriver;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Martin Simka
 */
public class Main {

    // Dotan's test instance
    private static final String JDBC_URL = "jdbc:sqlserver://dpaz-win2008.msdomain.mw.lab.eng.bos.redhat.com;DatabaseName=krbusr01;integratedSecurity=true;authenticationScheme=JavaKerberos";


    public static void main(String[] args) throws Exception {
            Logger logger = Logger.getLogger("com.microsoft.sqlserver.jdbc.internals.KerbAuthentication");
            logger.setLevel(Level.FINER);

            System.setProperty("java.security.krb5.realm", "msdomain.mw.lab.eng.bos.redhat.com");
            System.setProperty("java.security.krb5.kdc", "DC1.msdomain.mw.lab.eng.bos.redhat.com");
            System.setProperty("java.security.krb5.conf", new File("krb5.conf").getAbsolutePath());
            System.setProperty("sun.security.krb5.debug", "true");

            Properties props = new Properties();
            //props.put("user", "KRBUSR01@MSDOMAIN.MW.LAB.ENG.BOS.REDHAT.COM");

            DriverManager.registerDriver(new SQLServerDriver());
            Connection conn = DriverManager.getConnection(JDBC_URL, props);
            printUserName(conn);

            conn.close();
    }

    private static void printUserName(Connection conn) throws SQLException {
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT CURRENT_USER;");
            while (rs.next())
                System.out.println("User is: " + rs.getString(1));
            rs.close();
        } finally {
            if (stmt != null)
                stmt.close();
        }
    }
}
