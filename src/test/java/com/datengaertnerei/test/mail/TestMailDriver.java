package com.datengaertnerei.test.mail;

import java.util.Date;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class TestMailDriver {

  public static void main(String[] args) {
    // Common variables
    String host = "localhost";
    String from = "noreply@datengaertnerei.com";
    String to = "billy.boyington@email.test";

    // Set properties
    Properties props = new Properties();
    props.put("mail.smtp.host", host);
    props.put("mail.debug", "true");

    // Get session
    Session session = Session.getInstance(props);

    try {
      // Instantiate a message
      Message msg = new MimeMessage(session);

      // Set the FROM message
      msg.setFrom(new InternetAddress(from));

      // The recipients can be more than one so we use an array but you can
      // use 'new InternetAddress(to)' for only one address.
      InternetAddress[] address = {new InternetAddress(to)};
      msg.setRecipients(Message.RecipientType.TO, address);

      // Set the message subject and date we sent it.
      msg.setSubject("Email from JavaMail test");
      msg.setSentDate(new Date());

      // Set message content
      msg.setText("This is the text for this simple demo using JavaMail.");

      // Send the message
      Transport.send(msg);
    } catch (MessagingException mex) {
      mex.printStackTrace();
    }
  }
}
