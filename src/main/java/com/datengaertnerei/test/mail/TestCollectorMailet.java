package com.datengaertnerei.test.mail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.Enumeration;
import javax.activation.DataHandler;
import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mailet.Mail;
import org.apache.mailet.MailetConfig;
import org.apache.mailet.base.GenericMailet;

public class TestCollectorMailet extends GenericMailet {
  private static Log log = LogFactory.getLog(TestCollectorMailet.class);

  private Connection conn;
  private PreparedStatement preparedStatement;

  @Override
  public void service(Mail mail) throws MessagingException {

    if (null == preparedStatement) {
      setupDatabase();
    }
    MimeMessage msg = mail.getMessage();

    DataHandler dh = msg.getDataHandler();
    String bodyString = "";
    if (dh.getContentType().startsWith("text/plain")) {
      Object body;
      try {
        body = dh.getContent();
        bodyString = body.toString();
      } catch (IOException e) {
        log.error("Could not extract message body", e);
      }
    }

    byte[] msgContent = null;
    try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
      msg.writeTo(os);
      msgContent = os.toByteArray();
    } catch (IOException e) {
      log.error("Could not prepare message content", e);
    }

    String sender = mail.getMaybeSender().get().asString();
    String recipients = transferRecipients(msg);
    String headers = transferHeaders(msg);
    persistMessage(sender, recipients, headers, msg, bodyString, msgContent);
  }

  private synchronized void persistMessage(
      String sender,
      String recipients,
      String headers,
      MimeMessage msg,
      String bodyString,
      byte[] msgContent)
      throws MessagingException {
    log.debug("Save message to database");
    try (ByteArrayInputStream is = new ByteArrayInputStream(msgContent)) {
      preparedStatement.setInt(1, msg.hashCode());
      preparedStatement.setString(2, sender);
      preparedStatement.setString(3, recipients);
      preparedStatement.setString(4, msg.getSubject());
      preparedStatement.setString(5, headers);
      preparedStatement.setString(6, bodyString);
      preparedStatement.setBlob(7, is);
      long timestamp =
          msg.getReceivedDate() == null ? new Date().getTime() : msg.getReceivedDate().getTime();
      preparedStatement.setTimestamp(8, new java.sql.Timestamp(timestamp));
      // execute insert SQL statement
      preparedStatement.executeUpdate();
    } catch (IOException | SQLException e) {
      log.error("Could not persist message", e);
    }
  }

  private String transferHeaders(MimeMessage msg) throws MessagingException {
    Enumeration<String> allHeaderLines = msg.getAllHeaderLines();
    StringBuilder headers = new StringBuilder();
    while (allHeaderLines.hasMoreElements()) {
      headers.append(allHeaderLines.nextElement()).append(System.lineSeparator());
    }
    return headers.toString();
  }

  private String transferRecipients(MimeMessage msg) throws MessagingException {
    Address[] recipientList = msg.getAllRecipients();
    if (null != recipientList) {
      StringBuilder recipients = new StringBuilder();
      for (Address recipient : recipientList) {
        recipients.append(recipient.toString()).append(";");
      }
      recipients.deleteCharAt(recipients.length() - 1);
      return recipients.toString();
    } else {
      return "NoRecipients";
    }
  }

  @Override
  public void destroy() {

    if (null != conn) {
      try {
        preparedStatement.close();
        conn.close();
      } catch (SQLException e) {
        log.error("Could not close database connection", e);
      }
    }

    super.destroy();
  }

  @Override
  public void init(MailetConfig newConfig) throws MessagingException {
    super.init(newConfig);
    log.debug("Initializing mailet...");

    setupDatabase();
  }

  /** Set up the database with init parameters, create table if needed, prepare insert statement */
  void setupDatabase() {
    String driver = getInitParameter("driver");
    String jdbcUrl = getInitParameter("url");
    String user = getInitParameter("user");
    String password = getInitParameter("password");

    try {
      Class.forName(driver);
      conn = DriverManager.getConnection(jdbcUrl, user, password);
      log.info("Using database: " + jdbcUrl);

      ensureTableExists();

      String insertTableSql =
          "INSERT INTO MAILCONTAINER"
              + "(ID, SENDER, RECIPIENTS, SUBJECT, HEADERS, BODY, CONTENT, RECEIVED) VALUES"
              + "(?,?,?,?,?,?,?,?)";
      preparedStatement = conn.prepareStatement(insertTableSql);

    } catch (SQLException | ClassNotFoundException e) {
      log.error("Could not init mailet: ", e);
    }
  }

  private void ensureTableExists() {
    String createTableSql =
        "CREATE TABLE MAILCONTAINER("
            + "ID INT NOT NULL, "
            + "SENDER VARCHAR(256) NOT NULL, "
            + "RECIPIENTS VARCHAR(1024) NOT NULL, "
            + "SUBJECT VARCHAR(1024) NOT NULL, "
            + "HEADERS VARCHAR(4096) NOT NULL, "
            + "BODY CLOB NOT NULL, "
            + "CONTENT BLOB NOT NULL, "
            + "RECEIVED TIMESTAMP NOT NULL "
            + ")";

    execSql(createTableSql, conn);
  }

  void execSql(String sqlStatement, Connection conn) {

    try (Statement statement = conn.createStatement()) {
      // execute the SQL statement
      statement.execute(sqlStatement);
      log.debug(String.format("Statement \"%s\" executed", sqlStatement));

    } catch (SQLException e) {
      log.error("", e);
    }
  }
}
