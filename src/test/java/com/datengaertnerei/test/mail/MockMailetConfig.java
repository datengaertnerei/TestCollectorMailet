package com.datengaertnerei.test.mail;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.mailet.MailetConfig;
import org.apache.mailet.MailetContext;

public class MockMailetConfig implements MailetConfig {

  Map<String, String> params = new HashMap<>();

  public MockMailetConfig() {
    params.put("url", "jdbc:derby:MailDB;create=true");
    params.put("driver", "org.apache.derby.jdbc.EmbeddedDriver");
    params.put("user", "APP");
    params.put("password", "APP");
  }

  @Override
  public String getInitParameter(String name) {
    return params.get(name);
  }

  @Override
  public Iterator<String> getInitParameterNames() {
    return params.keySet().iterator();
  }

  @Override
  public MailetContext getMailetContext() {
    return null;
  }

  @Override
  public String getMailetName() {
    return null;
  }
}
