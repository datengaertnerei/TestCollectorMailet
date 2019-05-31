package com.datengaertnerei.test.mail;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Stream;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.apache.james.core.MailAddress;
import org.apache.mailet.Attribute;
import org.apache.mailet.AttributeName;
import org.apache.mailet.Mail;
import org.apache.mailet.PerRecipientHeaders;
import org.apache.mailet.PerRecipientHeaders.Header;

public class MockMail implements Mail {

  /** serialization UID */
  private static final long serialVersionUID = -1720971762080523187L;

  private String name;
  private MimeMessage message;
  private Collection<MailAddress> recipients;
  private InternetAddress sender;
  private String state;
  private String errormsg;

  public MockMail() {
    // nothing to do here
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String newName) {
    name = newName;
  }

  @Override
  public MimeMessage getMessage() throws MessagingException {
    return message;
  }

  @Override
  public Collection<MailAddress> getRecipients() {
    return recipients;
  }

  @Override
  public void setRecipients(Collection<MailAddress> recipients) {
    this.recipients = recipients;
  }

  @Override
  public MailAddress getSender() {
    MailAddress result = null;
    try {
      result = new MailAddress(sender);
    } catch (AddressException e) {
      // ignore
    }
    return result;
  }

  @Override
  public Mail duplicate() throws MessagingException {
    throw new MessagingException("not implemented");
  }

  @Override
  public String getState() {
    return state;
  }

  @Override
  public String getRemoteHost() {
    return null;
  }

  @Override
  public String getRemoteAddr() {
    return null;
  }

  @Override
  public String getErrorMessage() {
    return errormsg;
  }

  @Override
  public void setErrorMessage(String msg) {
    errormsg = msg;
  }

  @Override
  public void setMessage(MimeMessage message) throws MessagingException {
    this.message = message;
    this.sender = (InternetAddress) message.getSender();
  }

  @Override
  public void setState(String state) {
    this.state = state;
  }

  @Override
  public Serializable getAttribute(String name) {
    return name;
  }

  @Override
  public Iterator<String> getAttributeNames() {
    return null;
  }

  @Override
  public boolean hasAttributes() {
    return false;
  }

  @Override
  public Serializable removeAttribute(String name) {
    return null;
  }

  @Override
  public void removeAllAttributes() {
    // not implemented
  }

  @Override
  public Serializable setAttribute(String name, Serializable object) {
    return null;
  }

  @Override
  public void addSpecificHeaderForRecipient(Header header, MailAddress recipient) {
    // not implemented
  }

  @Override
  public PerRecipientHeaders getPerRecipientSpecificHeaders() {
    return null;
  }

  @Override
  public long getMessageSize() throws MessagingException {
    return 0;
  }

  @Override
  public Date getLastUpdated() {
    return null;
  }

  @Override
  public void setLastUpdated(Date lastUpdated) {
    // not implemented
  }

  @Override
  public Stream<Attribute> attributes() { 
    return null;
  }

  @Override
  public Optional<Attribute> getAttribute(AttributeName name) { 
    return null;
  }

  @Override
  public Stream<AttributeName> attributeNames() { 
    return null;
  }

  @Override
  public Optional<Attribute> removeAttribute(
      AttributeName attributeName) { 
    return null;
  }

  @Override
  public Optional<Attribute> setAttribute(Attribute attribute) { 
    return null;
  }
}
