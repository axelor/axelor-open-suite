/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2012-2014 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.message.mail;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;

import com.google.common.base.Preconditions;


public class MailConnection {

	String protocol;
	String host;
	String port;
	String userName;
	String password;
	String aliasName;

	Session session;
	Properties properties;
	Store store;

	public MailConnection(String protocol, String host, String port, String userName, String password) throws MessagingException {
		this(protocol, host, port, userName, null, password);
	}

	public MailConnection(String protocol, String host, String port, String userName, String aliasName, String password) throws MessagingException {
		Preconditions.checkNotNull(userName, "User name can not be null.");
		Preconditions.checkNotNull(password, "Password can not be null.");

		this.protocol = protocol;
		this.host = host;
		this.port = port;
		this.userName = userName;
		this.password = password;
		this.aliasName = aliasName;
		this.loadConnection();
	}

	public Session getSession() {
		return session;
	}

	public Properties getProperties() {
		return properties;
	}

	public String getProtocol() {
		return protocol;
	}

	public Store getStore() {
		return store;
	}

	public String getUserName() {
		return userName;
	}

	public String getPassword() {
		return password;
	}

	public String getAliasName() {
		return aliasName;
	}

	/**
	 * Returns a Properties object which is configured for a POP3/IMAP server
	 *
	 * @return a Properties object
	 */
	protected Properties getServerProperties() {
		Properties properties = new Properties();

		// server setting
		properties.put(String.format("mail.%s.host", protocol), host);
		properties.put(String.format("mail.%s.port", protocol), port);
		properties.put(String.format("mail.%s.auth", protocol), "true");

		// SSL setting
		properties.setProperty(String.format("mail.%s.socketFactory.class", protocol),"javax.net.ssl.SSLSocketFactory");
		properties.setProperty(String.format("mail.%s.socketFactory.fallback", protocol),"false");
		properties.setProperty(String.format("mail.%s.socketFactory.port", protocol),String.valueOf(port));

		return properties;
	}

	protected void loadConnection() throws MessagingException {
		this.properties = getServerProperties();
        this.session = Session.getDefaultInstance(properties, new mailAuth());

        // connects to the message store
        try {
        	this.store = this.session.getStore(protocol);
        	store.connect(userName, password);
        } catch(NoSuchProviderException e) {
        }
	}

	class mailAuth extends Authenticator {

		@Override
		protected PasswordAuthentication getPasswordAuthentication() {

			return new PasswordAuthentication(userName, password);

		}
	}

}
