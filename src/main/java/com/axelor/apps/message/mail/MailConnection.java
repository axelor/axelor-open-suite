/**
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the “License”); you may not use
 * this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://license.axelor.com/.
 *
 * The License is based on the Mozilla Public License Version 1.1 but
 * Sections 14 and 15 have been added to cover use of software over a
 * computer network and provide for limited attribution for the
 * Original Developer. In addition, Exhibit A has been modified to be
 * consistent with Exhibit B.
 *
 * Software distributed under the License is distributed on an “AS IS”
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is part of "Axelor Business Suite", developed by
 * Axelor exclusively.
 *
 * The Original Developer is the Initial Developer. The Initial Developer of
 * the Original Code is Axelor.
 *
 * All portions of the code written by Axelor are
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
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
