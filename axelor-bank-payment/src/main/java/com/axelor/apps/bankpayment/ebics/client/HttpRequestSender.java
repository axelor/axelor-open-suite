/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.bankpayment.ebics.client;

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

import com.axelor.apps.bankpayment.db.EbicsBank;
import com.axelor.apps.bankpayment.db.repo.EbicsCertificateRepository;
import com.axelor.apps.bankpayment.ebics.interfaces.ContentFactory;
import com.axelor.apps.bankpayment.ebics.service.EbicsCertificateService;
import com.axelor.apps.bankpayment.exception.BankPaymentExceptionMessage;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;
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
          I18n.get(BankPaymentExceptionMessage.EBICS_INVALID_BANK_URL));
    }

    return sendRequest(request, bank);
  }

  public final int sendRequest(ContentFactory request, EbicsBank bank)
      throws AxelorException, IOException {
    String bankUrl = bank.getUrl();

    X509Certificate certificate = null;
    final boolean isSSL = EbicsCertificateRepository.TYPE_SSL.equals(bank.getProtocolSelect());

    if (isSSL) {
      certificate =
          EbicsCertificateService.getBankCertificate(bank, EbicsCertificateRepository.TYPE_SSL);
    }

    HttpClientBuilder builder = getSecuredHttpClient(isSSL, certificate, bankUrl);

    int retCode = -1;
    log.debug("Bank url: {}", bankUrl);
    HttpPost post = new HttpPost(bankUrl);
    HttpEntity entity = new InputStreamEntity(request.getContent(), ContentType.TEXT_XML);
    post.setEntity(entity);

    try {
      CloseableHttpResponse httpResponse = builder.build().execute(post);
      retCode = httpResponse.getStatusLine().getStatusCode();
      log.debug("Http reason phrase: {}", httpResponse.getStatusLine().getReasonPhrase());
      response = new InputStreamContentFactory(httpResponse.getEntity().getContent());

    } catch (Exception e) {
      log.error(e.getMessage());
      throw new AxelorException(
          e.getCause(),
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get("Connection error: %s"),
          e.getMessage());
    }

    return retCode;
  }

  private HttpClientBuilder getSecuredHttpClient(boolean isSSL, Certificate cert, String bankURL)
      throws AxelorException {
    RequestConfig requestConfig =
        RequestConfig.custom()
            .setConnectionRequestTimeout(0)
            .setConnectTimeout(30000)
            .setSocketTimeout(30000)
            .build();

    HttpClientBuilder clientBuilder =
        HttpClientBuilder.create().setDefaultRequestConfig(requestConfig);

    try {
      SSLConnectionSocketFactory sslConnectionFactory =
          isSSL
              ? getSSLConnectionFactory(cert, bankURL)
              : SSLConnectionSocketFactory.getSocketFactory();
      Registry<ConnectionSocketFactory> registry =
          RegistryBuilder.<ConnectionSocketFactory>create()
              .register("https", sslConnectionFactory)
              .register("http", PlainConnectionSocketFactory.INSTANCE)
              .build();
      clientBuilder.setConnectionManager(new PoolingHttpClientConnectionManager(registry));
    } catch (Exception e) {
      log.error(e.getMessage());
      throw new AxelorException(
          e.getCause(),
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get("Error adding certificate"));
    }

    EbicsUtils.setProxy(clientBuilder);

    return clientBuilder;
  }

  private SSLConnectionSocketFactory getSSLConnectionFactory(Certificate cert, String bankURL)
      throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException,
          KeyManagementException, UnrecoverableKeyException {
    SSLConnectionSocketFactory sslConnectionFactory;

    if (cert == null) {
      log.debug("SSL certificate not exist");
      return SSLConnectionSocketFactory.getSocketFactory();
    }

    log.debug("SSL certificate exist");

    KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
    char[] password = "NoPassword".toCharArray();
    keystore.load(null, password);
    URL url = new URL(bankURL);
    keystore.setCertificateEntry(url.getHost(), cert);
    SSLContext sslContext =
        SSLContexts.custom()
            .loadKeyMaterial(keystore, password)
            .loadTrustMaterial(null, new TrustSelfSignedStrategy())
            .build();
    try {
      sslConnectionFactory = new SSLConnectionSocketFactory(sslContext);
      new DefaultHostnameVerifier().verify(url.getHost(), (X509Certificate) cert);

    } catch (SSLException e) {
      log.debug("Error in ssl certifcate host name verification");
      sslConnectionFactory = SSLConnectionSocketFactory.getSocketFactory();
    }

    return sslConnectionFactory;
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
