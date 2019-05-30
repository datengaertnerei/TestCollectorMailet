# TestCollectorMailet
Apache James mailet for e-mail test automation

1. Build project
2. Install [Apache James](https://james.apache.org/)
3. Copy project and JDBC driver jar files to conf/lib of your Apache James installation
4. Edit conf/mailetcontainer.xml using your own JDBC setup

```
       <mailet match="All" class="com.datengaertnerei.test.mail.TestCollectorMailet">
         <url>jdbc:derby://localhost:1527/MailDB;create=true</url>
	 <driver>org.apache.derby.jdbc.ClientDriver</driver>
	 <user>APP</user>
	 <password>APP</password>
       </mailet>
```
