/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
package com.axelor.apps.bankpayment.ebics.client;

import com.axelor.app.AppSettings;
import com.axelor.apps.bankpayment.db.EbicsBank;
import com.axelor.apps.bankpayment.db.repo.EbicsCertificateRepository;
import com.axelor.apps.bankpayment.ebics.interfaces.ContentFactory;
import com.axelor.apps.bankpayment.ebics.service.EbicsCertificateService;
import com.axelor.apps.bankpayment.exception.IExceptionMessage;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLException;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple HTTP request sender and receiver. The send returns a HTTP code that should be analyzed
 * before proceeding ebics request response parse.
 *
 * @author hachani
 */
public class HttpRequestSender {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  /**
   * Constructs a new <code>HttpRequestSender</code> with a given ebics session.
   *
   * @param session the ebics session
   */
  public HttpRequestSender(EbicsSession session) {
    this.session = session;
  }

  /**
   * Sends the request contained in the <code>ContentFactory</code>. The <code>ContentFactory</code>
   * will deliver the request as an <code>InputStream</code>.
   *
   * @param request the ebics request
   * @return the HTTP return code
   * @throws AxelorException
   */
  public final int send(ContentFactory request) throws IOException, AxelorException {

    EbicsBank bank = session.getUser().getEbicsPartner().getEbicsBank();
    String url = bank.getUrl();
    if (url == null || !url.startsWith("http://") && !url.startsWith("https://")) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(IExceptionMessage.EBICS_INVALID_BANK_URL));
    }

    if (bank.getProtocolSelect().equals("ssl")) {
      return sendSSL(request, bank);
    } else {
      return sendTLS(request, bank);
    }
  }

  public final int sendSSL(ContentFactory request, EbicsBank bank)
      throws AxelorException, IOException {
    String url = bank.getUrl();
    X509Certificate certificate =
        EbicsCertificateService.getBankCertificate(bank, EbicsCertificateRepository.TYPE_SSL);
    DefaultHttpClient client = getSecuredHttpClient(certificate, url);
    String proxyConfiguration = AppSettings.get().get("http.proxy.host");

    if (proxyConfiguration != null && !proxyConfiguration.equals("")) {
      setProxy(client);
    }

    InputStream input = request.getContent();
    int retCode = -1;
    log.debug("Bank url: {}", url);
    HttpPost post = new HttpPost(url);
    ContentType type = ContentType.TEXT_XML;
    HttpEntity entity = new InputStreamEntity(input, retCode, type);
    post.setEntity(entity);

    try {
      HttpResponse responseHttp = client.execute(post);
      retCode = responseHttp.getStatusLine().getStatusCode();
      log.debug("Http reason phrase: {}", responseHttp.getStatusLine().getReasonPhrase());
      response = new InputStreamContentFactory(responseHttp.getEntity().getContent());
    } catch (IOException e) {
      e.printStackTrace();
      throw new AxelorException(
          e.getCause(),
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get("Connection error: %s"),
          e.getMessage());
    }

    return retCode;
  }

  public final int sendTLS(ContentFactory request, EbicsBank bank) throws IOException {
    HttpClient httpClient;
    PostMethod method;
    RequestEntity requestEntity;
    InputStream input;
    int retCode;
    httpClient = new HttpClient();
    String proxyConfiguration = AppSettings.get().get("http.proxy.host");

    if (proxyConfiguration != null && !proxyConfiguration.equals("")) {
      setProxy(httpClient);
    }
    input = request.getContent();
    method = new PostMethod(bank.getUrl());
    method.getParams().setSoTimeout(30000);
    requestEntity = new InputStreamRequestEntity(input);
    method.setRequestEntity(requestEntity);
    method.setRequestHeader("Content-type", "text/xml; charset=ISO-8859-1");
    retCode = -1;
    retCode = httpClient.executeMethod(method);
    response = new InputStreamContentFactory(method.getResponseBodyAsStream());

    return retCode;
  }

  private void setProxy(DefaultHttpClient client) {

    String proxyHost;
    int proxyPort;

    proxyHost = AppSettings.get().get("http.proxy.host").trim();
    proxyPort = Integer.parseInt(AppSettings.get().get("http.proxy.port").trim());
    if (!AppSettings.get().get("http.proxy.user").equals("")) {
      String user;
      String pwd;
      Credentials credentials;
      AuthScope authscope;

      user = AppSettings.get().get("http.proxy.user").trim();
      pwd = AppSettings.get().get("http.proxy.password").trim();
      credentials = new UsernamePasswordCredentials(user, pwd);
      authscope = new AuthScope(proxyHost, proxyPort);
      client.getCredentialsProvider().setCredentials(authscope, credentials);
    }
  }

  private void setProxy(HttpClient httpClient) {

    String proxyHost = AppSettings.get().get("http.proxy.host").trim();
    Integer proxyPort = Integer.parseInt(AppSettings.get().get("http.proxy.port").trim());
    HostConfiguration hostConfig = httpClient.getHostConfiguration();
    hostConfig.setProxy(proxyHost, proxyPort);
    if (!AppSettings.get().get("http.proxy.user").equals("")) {
      String user;
      String pwd;
      org.apache.commons.httpclient.UsernamePasswordCredentials credentials;
      org.apache.commons.httpclient.auth.AuthScope authscope;

      user = AppSettings.get().get("http.proxy.user").trim();
      pwd = AppSettings.get().get("http.proxy.password").trim();
      credentials = new org.apache.commons.httpclient.UsernamePasswordCredentials(user, pwd);
      authscope = new org.apache.commons.httpclient.auth.AuthScope(proxyHost, proxyPort);
      httpClient.getState().setProxyCredentials(authscope, credentials);
    }
  }

  private DefaultHttpClient getSecuredHttpClient(Certificate cert, String bankURL)
      throws AxelorException {

    HttpParams httpParams = new BasicHttpParams();
    HttpConnectionParams.setConnectionTimeout(httpParams, 30000);
    HttpConnectionParams.setSoTimeout(httpParams, 30000);
    DefaultHttpClient client = new DefaultHttpClient(httpParams);

    try {
      Scheme https = null;
      if (cert != null) {
        log.debug("SSL certificate exist");
        URL url = new URL(bankURL);
        log.debug("Url host: {}", url.getHost());
        KeyStore keystore = KeyStore.getInstance("jks");
        char[] password = "NoPassword".toCharArray();
        keystore.load(null, password);
        keystore.setCertificateEntry(url.getHost(), cert);
        SSLSocketFactory factory = new SSLSocketFactory(keystore);
        try {
          factory.getHostnameVerifier().verify(url.getHost(), (X509Certificate) cert);
          https = new Scheme("https", 443, new SSLSocketFactory(keystore));
        } catch (SSLException e) {
          log.debug("Error in ssl certifcate host name verification");
          https = new Scheme("https", 443, SSLSocketFactory.getSocketFactory());
        }

      } else {
        log.debug("SSL certificate not exist");
        https = new Scheme("https", 443, SSLSocketFactory.getSocketFactory());
      }
      client.getConnectionManager().getSchemeRegistry().register(https);
    } catch (Exception e) {
      e.printStackTrace();
      throw new AxelorException(
          e.getCause(), TraceBackRepository.TYPE_TECHNICAL, I18n.get("Error adding certificate"));
    }

    return client;
  }

  /**
   * Returns the content factory of the response body
   *
   * @return the content factory of the response.
   */
  public ContentFactory getResponseBody() {
    return response;
  }

  //////////////////////////////////////////////////////////////////
  // DATA MEMBERS
  //////////////////////////////////////////////////////////////////

  private EbicsSession session;
  private ContentFactory response;
}
