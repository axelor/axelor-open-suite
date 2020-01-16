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
package com.axelor.apps.bankpayment.ebics.certificate;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.openssl.PEMParser;

/**
 * Key store loader. This class loads a key store from a given path and allow to get private keys
 * and certificates for a given alias. The PKCS12 key store type is recommended to be used
 *
 * @author hachani
 */
public class KeyStoreManager {

  /**
   * Loads a certificate for a given alias
   *
   * @param alias the certificate alias
   * @return the certificate
   * @throws KeyStoreException
   */
  public final X509Certificate getCertificate(String alias) throws KeyStoreException {
    X509Certificate cert;

    cert = (X509Certificate) keyStore.getCertificate(alias);

    if (cert == null) {
      throw new IllegalArgumentException("alias " + alias + " not found in the KeyStore");
    }

    return cert;
  }

  /**
   * Loads a private key for a given alias
   *
   * @param alias the certificate alias
   * @return the private key
   * @throws GeneralSecurityException
   */
  public final PrivateKey getPrivateKey(String alias) throws GeneralSecurityException {
    PrivateKey key;

    key = (PrivateKey) keyStore.getKey(alias, password);
    if (key == null) {
      throw new IllegalArgumentException("private key not found for alias " + alias);
    }

    return key;
  }

  /**
   * Loads a key store from a given path and password
   *
   * @param path the key store path
   * @param password the key store password
   * @throws GeneralSecurityException
   * @throws IOException
   */
  public void load(String path, char[] password) throws GeneralSecurityException, IOException {
    keyStore = KeyStore.getInstance("PKCS12", "BC");
    this.password = password;
    load(path);
  }

  /**
   * Loads a key store and cache the loaded one
   *
   * @param path the key store path.
   * @throws GeneralSecurityException
   * @throws IOException
   */
  private void load(String path) throws GeneralSecurityException, IOException {
    if (path.equals("")) {
      this.keyStore.load(null, null);
    } else {
      this.keyStore.load(new FileInputStream(path), password);
      this.certs = read(this.keyStore);
    }
  }

  /**
   * Reads a certificate from an input stream for a given provider
   *
   * @param input the input stream
   * @param provider the certificate provider
   * @return the certificate
   * @throws CertificateException
   * @throws IOException
   */
  public X509Certificate read(InputStream input, Provider provider)
      throws CertificateException, IOException {
    X509Certificate certificate;

    certificate =
        (X509Certificate)
            CertificateFactory.getInstance("X.509", provider).generateCertificate(input);

    if (certificate == null) {
      try (final PEMParser reader = new PEMParser(new InputStreamReader(input))) {
        final X509CertificateHolder certificateHolder = (X509CertificateHolder) reader.readObject();
        certificate = new JcaX509CertificateConverter().getCertificate(certificateHolder);
      }
    }

    return certificate;
  }

  /**
   * Returns the public key of a given certificate.
   *
   * @param input the given certificate
   * @return The RSA public key of the given certificate
   * @throws GeneralSecurityException
   * @throws IOException
   */
  public RSAPublicKey getPublicKey(InputStream input) throws GeneralSecurityException, IOException {
    X509Certificate cert;
    cert = read(input, keyStore.getProvider());
    return (RSAPublicKey) cert.getPublicKey();
  }

  /**
   * Writes the given certificate into the key store.
   *
   * @param alias the certificate alias
   * @param input the given certificate.
   * @throws GeneralSecurityException
   * @throws IOException
   */
  public void setCertificateEntry(String alias, InputStream input)
      throws GeneralSecurityException, IOException {
    keyStore.setCertificateEntry(alias, read(input, keyStore.getProvider()));
  }

  /**
   * Saves the key store to a given output stream.
   *
   * @param output the output stream.
   */
  public void save(OutputStream output) throws GeneralSecurityException, IOException {
    keyStore.store(output, password);
  }

  /**
   * Returns the certificates contained in the key store.
   *
   * @return the certificates contained in the key store.
   */
  public Map<String, X509Certificate> getCertificates() {
    return certs;
  }

  /**
   * Reads all certificate existing in a given key store
   *
   * @param keyStore the key store
   * @return A <code>Map</code> of certificate, the key of the map is the certificate alias
   * @throws KeyStoreException
   */
  public Map<String, X509Certificate> read(KeyStore keyStore) throws KeyStoreException {
    Map<String, X509Certificate> certificates;
    Enumeration<String> enumeration;

    certificates = new HashMap<String, X509Certificate>();
    enumeration = keyStore.aliases();
    while (enumeration.hasMoreElements()) {
      String alias;

      alias = enumeration.nextElement();
      certificates.put(alias, (X509Certificate) keyStore.getCertificate(alias));
    }

    return certificates;
  }

  // --------------------------------------------------------------------
  // DATA MEMBERS
  // --------------------------------------------------------------------

  private KeyStore keyStore;
  private char[] password;
  private Map<String, X509Certificate> certs;
}
