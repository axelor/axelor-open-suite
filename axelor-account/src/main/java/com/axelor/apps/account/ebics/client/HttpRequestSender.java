/*
 * Copyright (c) 1990-2012 kopiLeft Development SARL, Bizerte, Tunisia
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1 as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * $Id$
 */

package com.axelor.apps.account.ebics.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import com.axelor.app.AppSettings;
import com.axelor.apps.account.ebics.interfaces.ContentFactory;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;


/**
 * A simple HTTP request sender and receiver.
 * The send returns a HTTP code that should be analyzed
 * before proceeding ebics request response parse.
 *
 * @author hachani
 *
 */
public class HttpRequestSender {

  /**
   * Constructs a new <code>HttpRequestSender</code> with a
   * given ebics session.
   * @param session the ebics session
   */
  public HttpRequestSender(EbicsSession session) {
    this.session = session;
  }

  /**
   * Sends the request contained in the <code>ContentFactory</code>.
   * The <code>ContentFactory</code> will deliver the request as
   * an <code>InputStream</code>.
   *
   * @param request the ebics request
   * @return the HTTP return code
 * @throws AxelorException 
   */
  public final int send(ContentFactory request, File certFile) throws IOException, AxelorException {
    HttpClient			httpClient;
    String                      proxyConfiguration;
    InputStream			input;
    int				retCode;

    httpClient = new HttpClient();
    DefaultHttpClient client = getSecuredHttpClient(certFile);
    
    proxyConfiguration =  AppSettings.get().get("http.proxy.host");

    if (proxyConfiguration != null && !proxyConfiguration.equals("")) {
      HostConfiguration		hostConfig;
      String			proxyHost;
      int			proxyPort;

      hostConfig = httpClient.getHostConfiguration();
      proxyHost =  AppSettings.get().get("http.proxy.host").trim();
      proxyPort = Integer.parseInt(AppSettings.get().get("http.proxy.port").trim());
      hostConfig.setProxy(proxyHost, proxyPort);
      if (! AppSettings.get().get("http.proxy.user").equals("")) {
		String				user;
		String				pwd;
		UsernamePasswordCredentials	credentials;
		AuthScope			authscope;
	
		user =  AppSettings.get().get("http.proxy.user").trim();
		pwd =  AppSettings.get().get("http.proxy.password").trim();
		credentials = new UsernamePasswordCredentials(user, pwd);
		authscope = new AuthScope(proxyHost, proxyPort);
		httpClient.getState().setProxyCredentials(authscope, credentials);
      }
    }
    
    input = request.getContent();
    retCode = -1;
    HttpPost post = new HttpPost(session.getUser().getEbicsPartner().getEbicsBank().getUrl());
    ContentType type = ContentType.TEXT_XML;
    HttpEntity entity = new InputStreamEntity(input, retCode, type);
    post.setEntity(entity);
    HttpResponse responseHttp = client.execute(post);
    retCode = responseHttp.getStatusLine().getStatusCode();
    response = new InputStreamContentFactory(responseHttp.getEntity().getContent());
    return retCode;
  }

  private DefaultHttpClient getSecuredHttpClient(File certFile) throws AxelorException {

	DefaultHttpClient client = new DefaultHttpClient();
	
	try {
	    KeyStore keystore  = KeyStore.getInstance("jks");
	    char[] password = "NoPassword".toCharArray();
	    keystore.load(null, password);
	    InputStream instream = new FileInputStream(certFile);
	    Certificate cert = CertificateFactory.getInstance("X.509").generateCertificate(instream);
	    keystore.setCertificateEntry("certficate.host", cert);
	    Scheme https = new Scheme("https", 443,  new SSLSocketFactory(keystore));
	    client.getConnectionManager().getSchemeRegistry().register(https);
    } catch(Exception e) {
    	e.printStackTrace();
    	throw new AxelorException(I18n.get("Error adding certificate"), IException.TECHNICAL);
    }
	
	return client;
}

  /**
   * Returns the content factory of the response body
   * @return the content factory of the response.
   */
  public ContentFactory getResponseBody() {
    return response;
  }

  //////////////////////////////////////////////////////////////////
  // DATA MEMBERS
  //////////////////////////////////////////////////////////////////

  private EbicsSession				session;
  private ContentFactory			response;
}
