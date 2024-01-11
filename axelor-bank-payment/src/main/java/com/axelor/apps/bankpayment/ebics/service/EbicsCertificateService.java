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
package com.axelor.apps.bankpayment.ebics.service;

import com.axelor.apps.bankpayment.db.EbicsBank;
import com.axelor.apps.bankpayment.db.EbicsCertificate;
import com.axelor.apps.bankpayment.db.EbicsUser;
import com.axelor.apps.bankpayment.db.repo.EbicsCertificateRepository;
import com.axelor.apps.bankpayment.ebics.client.EbicsUtils;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.DateService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaStore;
import com.axelor.meta.schema.views.Selection.Option;
import com.axelor.utils.date.DateTool;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.security.NoSuchProviderException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.net.ssl.SSLSession;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ManagedHttpClientConnection;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.PEMWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EbicsCertificateService {

  private static final String CONTEXT_CERTI_KEY = "CONTEXT_CERTI_KEY";

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Inject private EbicsCertificateRepository certRepo;

  @Inject private AppBaseService appBaseService;

  @Inject protected DateService dateService;

  public static byte[] getCertificateContent(EbicsBank bank, String type) throws AxelorException {

    EbicsCertificate cert = getEbicsCertificate(bank, type);

    if (cert != null) {
      return cert.getCertificate();
    }

    if (bank.getUrl() != null && type.equals(EbicsCertificateRepository.TYPE_SSL)) {
      return Beans.get(EbicsCertificateService.class).getSSLCertificate(bank);
    }

    throw new AxelorException(
        bank,
        TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
        I18n.get("No bank certificate of type %s found"),
        type);
  }

  public static X509Certificate getCertificate(byte[] certificate, String type)
      throws AxelorException {

    ByteArrayInputStream instream = new ByteArrayInputStream(certificate);
    X509Certificate cert;
    try {
      cert =
          (X509Certificate)
              CertificateFactory.getInstance("X.509", "BC").generateCertificate(instream);
    } catch (CertificateException | NoSuchProviderException e) {
      throw new AxelorException(
          e.getCause(),
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get("Error in bank certificate of type %s"),
          type);
    }

    return cert;
  }

  public static X509Certificate getBankCertificate(EbicsBank bank, String type)
      throws AxelorException {

    byte[] certificate = getCertificateContent(bank, type);

    if (certificate == null) {
      return null;
    }

    return getCertificate(certificate, type);
  }

  private byte[] getSSLCertificate(EbicsBank bank) {

    try {
      final URL bankUrl = new URL(bank.getUrl());
      log.debug("Bank url protocol: {}", bankUrl.getProtocol());
      log.debug("Bank url host: {}", bankUrl.getHost());
      log.debug("Bank url port: {}", bankUrl.getPort());
      HttpHost targethost =
          new HttpHost(bankUrl.getHost(), bankUrl.getPort(), bankUrl.getProtocol());

      HttpClientBuilder builder = HttpClients.custom();

      EbicsUtils.setProxy(builder);

      builder.addInterceptorLast(
          new HttpResponseInterceptor() {
            @Override
            public void process(HttpResponse response, HttpContext context)
                throws HttpException, IOException {
              ManagedHttpClientConnection routedConnection =
                  (ManagedHttpClientConnection)
                      context.getAttribute(HttpCoreContext.HTTP_CONNECTION);
              SSLSession sslSession = routedConnection.getSSLSession();
              if (sslSession != null) {
                Certificate[] certificates = sslSession.getPeerCertificates();
                context.setAttribute(CONTEXT_CERTI_KEY, certificates);
              }
            }
          });

      CloseableHttpClient httpclient = builder.build();
      HttpContext context = new BasicHttpContext();
      HttpPost httpPost = new HttpPost(bankUrl.getPath());

      CloseableHttpResponse response = httpclient.execute(targethost, httpPost, context);

      if (response.getStatusLine() != null && response.getStatusLine().getStatusCode() == 200) {
        Certificate[] peerCertificates = (Certificate[]) context.getAttribute(CONTEXT_CERTI_KEY);
        for (int i = 0; i < peerCertificates.length; i++) {
          Certificate certificate = peerCertificates[i];
          if (certificate instanceof X509Certificate) {
            X509Certificate cert = (X509Certificate) certificate;
            createCertificate(cert, bank, EbicsCertificateRepository.TYPE_SSL);
            return certificate.getEncoded();
          }
        }
      }

    } catch (Exception e) {
      TraceBackService.trace(e);
    }

    return null;
  }

  public EbicsCertificate updateCertificate(
      X509Certificate certificate, EbicsCertificate cert, boolean cleanPrivateKey)
      throws CertificateEncodingException, IOException, AxelorException {

    String sha = DigestUtils.sha256Hex(certificate.getEncoded());
    log.debug("sha256 HEX : {}", sha);
    log.debug("certificat : {}", new String(certificate.getEncoded()));
    log.debug("certificat size : {}", certificate.getEncoded().length);

    cert.setValidFrom(DateTool.toLocalDate(certificate.getNotBefore()));
    cert.setValidTo(DateTool.toLocalDate(certificate.getNotAfter()));
    cert.setIssuer(certificate.getIssuerDN().getName());
    cert.setSubject(certificate.getSubjectDN().getName());
    cert.setCertificate(certificate.getEncoded());
    RSAPublicKey publicKey = (RSAPublicKey) certificate.getPublicKey();
    cert.setPublicKeyExponent(publicKey.getPublicExponent().toString(16));
    cert.setPublicKeyModulus(publicKey.getModulus().toString(16));
    cert.setSerial(certificate.getSerialNumber().toString(16));
    cert.setPemString(convertToPEMString(certificate));

    if (cleanPrivateKey) {
      cert.setPrivateKey(null);
    }

    sha = sha.toUpperCase();
    cert.setSha2has(sha);
    computeFullName(cert);

    return cert;
  }

  @Transactional(rollbackOn = {Exception.class})
  public EbicsCertificate createCertificate(
      X509Certificate certificate, EbicsBank bank, String type)
      throws CertificateEncodingException, IOException, AxelorException {

    EbicsCertificate cert = getEbicsCertificate(bank, type);
    if (cert == null) {
      log.debug("Creating bank certicate for bank: {}, type: {}", bank.getName(), type);
      cert = new EbicsCertificate();
      cert.setEbicsBank(bank);
      cert.setTypeSelect(type);
    }

    cert = updateCertificate(certificate, cert, true);

    return certRepo.save(cert);
  }

  private static EbicsCertificate getEbicsCertificate(EbicsBank bank, String type) {

    if (bank == null) {
      return null;
    }

    for (EbicsCertificate cert : bank.getEbicsCertificateList()) {
      if (cert.getTypeSelect().equals(type)) {
        return cert;
      }
    }

    return null;
  }

  public void computeFullName(EbicsCertificate entity) throws AxelorException {

    StringBuilder fullName = new StringBuilder();
    Option item =
        MetaStore.getSelectionItem(
            "bankpayment.ebics.certificate.type.select", entity.getTypeSelect());
    if (item != null) {
      fullName.append(I18n.get(item.getTitle()));
    }

    LocalDate date = entity.getValidFrom();
    if (date != null) {
      DateTimeFormatter dateFormat = dateService.getDateFormat();
      fullName.append(":" + date.format(dateFormat));
      date = entity.getValidTo();
      if (date != null) {
        fullName.append("-" + date.format(dateFormat));
      }
    }

    String issuer = entity.getIssuer();
    if (issuer != null) {
      fullName.append(":" + issuer);
    }

    entity.setFullName(fullName.toString());
  }

  public String convertToPEMString(X509Certificate x509Cert) throws IOException {

    StringWriter sw = new StringWriter();
    try (PEMWriter pw = new PEMWriter(sw)) {
      pw.writeObject(x509Cert);
    }

    return sw.toString();
  }

  public X509Certificate convertToCertificate(String pemString)
      throws IOException, CertificateException {

    X509Certificate certificate;
    StringReader reader = new StringReader(pemString);
    try (final PEMParser pr = new PEMParser(reader)) {
      final X509CertificateHolder certificateHolder = (X509CertificateHolder) pr.readObject();
      certificate = new JcaX509CertificateConverter().getCertificate(certificateHolder);
    }

    return certificate;
  }

  public byte[] convertToDER(String pemString) throws IOException, CertificateException {

    X509Certificate cert = convertToCertificate(pemString);

    return cert.getEncoded();
  }

  @Transactional
  public void updateEditionDate(EbicsUser user) {

    LocalDateTime now = appBaseService.getTodayDateTime().toLocalDateTime();

    EbicsCertificate certificate = user.getA005Certificate();
    if (certificate != null) {
      certificate.setInitLetterEditionDate(now);
      certRepo.save(certificate);
    }

    certificate = user.getE002Certificate();
    if (certificate != null) {
      certificate.setInitLetterEditionDate(now);
      certRepo.save(certificate);
    }

    certificate = user.getX002Certificate();
    if (certificate != null) {
      certificate.setInitLetterEditionDate(now);
      certRepo.save(certificate);
    }
  }
}
