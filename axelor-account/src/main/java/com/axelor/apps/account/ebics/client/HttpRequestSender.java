/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2017 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.ebics.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLException;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.app.AppSettings;
import com.axelor.apps.account.db.EbicsBank;
import com.axelor.apps.account.ebics.interfaces.ContentFactory;
import com.axelor.apps.account.ebics.service.EbicsCertificateService;
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
 
  private final Logger log = LoggerFactory.getLogger(HttpRequestSender.class);
	
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
  public final int send(ContentFactory request) throws IOException, AxelorException {
    HttpClient			httpClient;
    String                      proxyConfiguration;
    InputStream			input;
    int				retCode;
    
    httpClient = new HttpClient();
    EbicsBank bank = session.getUser().getEbicsPartner().getEbicsBank();
    X509Certificate certificate = EbicsCertificateService.getBankCertificate(bank, "ssl");
    DefaultHttpClient client = getSecuredHttpClient(certificate, bank.getUrl());
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
    HttpPost post = new HttpPost(bank.getUrl());
    ContentType type = ContentType.TEXT_XML;
    HttpEntity entity = new InputStreamEntity(input, retCode, type);
    post.setEntity(entity);
    HttpResponse responseHttp = client.execute(post);
    retCode = responseHttp.getStatusLine().getStatusCode();
    log.debug("Http reason phrase: {}" , responseHttp.getStatusLine().getReasonPhrase());
    response = new InputStreamContentFactory(responseHttp.getEntity().getContent());
    return retCode;
  }

  private DefaultHttpClient getSecuredHttpClient(Certificate cert, String bankURL) throws AxelorException {

	DefaultHttpClient client = new DefaultHttpClient();
	
	try {
	    Scheme https  = null;
	    if (cert != null) {
	    	log.debug("SSL certificate exist");
	    	URL url = new URL(bankURL);
	    	log.debug("Url host: {}", url.getHost());
	    	KeyStore keystore  = KeyStore.getInstance("jks");
	 	    char[] password = "NoPassword".toCharArray();
	 	    keystore.load(null, password);
	    	keystore.setCertificateEntry(url.getHost(), cert);
	    	SSLSocketFactory factory = new SSLSocketFactory(keystore);
	    	try {
	    		factory.getHostnameVerifier().verify(url.getHost(), (X509Certificate)cert);
	    		https = new Scheme("https", 443,  new SSLSocketFactory(keystore));
	    	}
	    	catch(SSLException e) {
	    		log.debug("Error in ssl certifcate host name verification");
	    		https = new Scheme("https", 443, SSLSocketFactory.getSocketFactory());
	    	}
	    	
	    }
	    else {
	    	log.debug("SSL certificate not exist");
	    	https = new Scheme("https", 443, SSLSocketFactory.getSocketFactory());
	    }
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
