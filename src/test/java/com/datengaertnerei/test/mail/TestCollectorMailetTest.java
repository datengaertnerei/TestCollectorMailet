package com.datengaertnerei.test.mail;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import com.datengaertnerei.test.mail.TestCollectorMailet;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mailet.Mail;
import org.apache.mailet.MailetConfig;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestCollectorMailetTest {
  private static final String DROP_TABLE_MAILCONTAINER = "DROP TABLE MAILCONTAINER";

  private static Log log = LogFactory.getLog(TestCollectorMailetTest.class);

  private static final String TEST_RECIPIENT = "tester@mail.test";
  private static final String TEST_SENDER = "system@mail.test";
  private static final String TEST_BODY = "Testmail";
  private static final String TEST_SUBJECT = "TestSubject";

  private TestCollectorMailet sut;

  @BeforeAll
  public static void setUp() {

    try (Connection conn = getConnection()) {
      execSql(DROP_TABLE_MAILCONTAINER, conn);
    } catch (ClassNotFoundException | SQLException e) {
      // ignore initial drop, table may not exist
    }
  }

  @AfterAll
  public static void tearDown() {

    try (Connection conn = getConnection()) {
      execSql(DROP_TABLE_MAILCONTAINER, conn);
    } catch (SQLException | ClassNotFoundException e) {
      // tear down should work, fail if drop table does not work
      fail(e);
    }
  }

  private static List<String[]> querySql(String sqlStatement)
      throws ClassNotFoundException, SQLException {
    Connection conn = getConnection();

    List<String[]> result = new LinkedList<>();

    try (Statement statement = conn.createStatement()) {
      // execute the SQL statement
      ResultSet rs = statement.executeQuery(sqlStatement);
      while (rs.next()) {
        String[] record = new String[5];
        record[0] = rs.getString("SENDER");
        record[1] = rs.getString("RECIPIENTS");
        record[2] = rs.getString("SUBJECT");
        record[3] = rs.getString("HEADERS");
        record[4] = rs.getString("BODY");
        result.add(record);
      }

      log.info(String.format("Statement \"%s\" executed", sqlStatement));

    } catch (SQLException e) {

      log.error("", e);

    } finally {
      if (conn != null) {
        conn.close();
      }
    }

    return result;
  }

  public static Connection getConnection() throws ClassNotFoundException, SQLException {
    Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
    Connection conn = DriverManager.getConnection("jdbc:derby:MailDB;create=true", "APP", "APP");
    return conn;
  }

  private static void execSql(String sqlStatement, Connection conn) throws SQLException {
    try (Statement statement = conn.createStatement()) {
      // execute the SQL statement
      statement.execute(sqlStatement);
      log.debug(String.format("Statement \"%s\" executed", sqlStatement));

    } catch (SQLException e) {
      // test setup, statement is definitely closed, rethrow exception
      throw e;
    }
  }

  @BeforeEach
  public void prepareTest() throws MessagingException {
    sut = new TestCollectorMailet();
    MailetConfig newConfig = new MockMailetConfig();
    sut.init(newConfig);
  }

  @Test
  final void testServiceMail() {
    Mail mail = new MockMail();
    MimeMessage msg = new MimeMessage(Session.getInstance(new Properties()));

    try {
      msg.setText(TEST_BODY, "UTF8");
      msg.setSubject(TEST_SUBJECT);
      msg.setSender(new InternetAddress(TEST_SENDER));
      InternetAddress[] address = {new InternetAddress(TEST_RECIPIENT)};
      msg.setRecipients(Message.RecipientType.TO, address);
      mail.setMessage(msg);
      sut.service(mail);

      List<String[]> result = querySql("SELECT * FROM MAILCONTAINER");
      assertEquals(1, result.size());
      assertEquals(TEST_SENDER, result.get(0)[0]);
      assertEquals(TEST_RECIPIENT, result.get(0)[1]);
      assertEquals(TEST_SUBJECT, result.get(0)[2]);
      assertEquals(TEST_BODY, result.get(0)[4]);
    } catch (MessagingException | ClassNotFoundException | SQLException e) {
      fail(e);
    }
  }
}
