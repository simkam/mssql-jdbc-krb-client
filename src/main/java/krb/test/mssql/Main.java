package krb.test.mssql;

import javax.security.auth.Subject;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import java.io.File;
import java.security.PrivilegedExceptionAction;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * @author Martin Simka
 */
public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());
    private static final String JDBC_URL = "jdbc:sqlserver://db06.msdomain.mw.lab.eng.bos.redhat.com;DatabaseName=krbusr01;integratedSecurity=true;authenticationScheme=JavaKerberos";

    public static void main(String[] args) throws Exception {
        Logger mslogger = Logger.getLogger("com.microsoft");
        mslogger.setLevel(Level.ALL);
        FileHandler handler = new FileHandler("log.txt", false);
        handler.setFormatter(new SimpleFormatter());
        handler.setLevel(Level.ALL);
        mslogger.addHandler(handler);
        logger.addHandler(handler);


        System.setProperty("java.security.krb5.realm", "MSDOMAIN.MW.LAB.ENG.BOS.REDHAT.COM");
        System.setProperty("java.security.krb5.kdc", "DC1.msdomain.mw.lab.eng.bos.redhat.com");
        System.setProperty("java.security.krb5.conf", new File("krb5.conf").getAbsolutePath());
        System.setProperty("sun.security.krb5.debug", "true");

        class Krb5LoginConfiguration extends Configuration {

            private final AppConfigurationEntry[] configList = new AppConfigurationEntry[1];

            public Krb5LoginConfiguration() {
                Map<String, String> options = new HashMap<String, String>();
                options.put("storeKey", "false");
                options.put("useKeyTab", "true");
                options.put("keyTab", "krbusr01.keytab");
                options.put("principal", "KRBUSR01@MSDOMAIN.MW.LAB.ENG.BOS.REDHAT.COM");
                options.put("doNotPrompt", "true");
                options.put("useTicketCache", "true");
                options.put("ticketCache", "/tmp/krbcc_1000");
                options.put("refreshKrb5Config", "true");
                options.put("isInitiator", "true");
                options.put("addGSSCredential", "true");
                configList[0] = new AppConfigurationEntry(
                        "org.jboss.security.negotiation.KerberosLoginModule",
                        AppConfigurationEntry.LoginModuleControlFlag.REQUIRED,
                        options);
            }

            @Override
            public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
                return configList;
            }
        }

        Configuration.setConfiguration(new Krb5LoginConfiguration());
        final LoginContext lc = new LoginContext("test");
        lc.login();
        Subject subject = lc.getSubject();

        try {
            Connection conn = Subject.doAs(subject, new PrivilegedExceptionAction<Connection>() {
                @Override
                public Connection run() throws Exception {
                    return DriverManager.getConnection(JDBC_URL);
                }
            });

            printUserName(conn);
            conn.close();


            conn = Subject.doAs(subject, new PrivilegedExceptionAction<Connection>() {
                @Override
                public Connection run() throws Exception {
                    return DriverManager.getConnection(JDBC_URL);
                }
            });

            printUserName(conn);
            conn.close();
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "", t);
        }
    }

    private static void printUserName(Connection conn) throws SQLException {
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT SYSTEM_USER;");
            while (rs.next()) {
                String username = rs.getString(1);
                System.out.println("User is: " + username);
                logger.log(Level.INFO, "User is: " + username);
            }
            rs.close();
        } finally {
            if (stmt != null)
                stmt.close();
        }
    }
}


