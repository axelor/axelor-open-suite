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

import com.axelor.apps.bankpayment.db.EbicsBank;
import com.axelor.apps.bankpayment.db.EbicsCertificate;
import com.axelor.apps.bankpayment.db.EbicsPartner;
import com.axelor.apps.bankpayment.db.EbicsUser;
import com.axelor.apps.bankpayment.db.repo.EbicsCertificateRepository;
import com.axelor.apps.bankpayment.db.repo.EbicsPartnerRepository;
import com.axelor.apps.bankpayment.db.repo.EbicsUserRepository;
import com.axelor.apps.bankpayment.ebics.service.EbicsCertificateService;
import com.axelor.inject.Beans;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * Simple manager for EBICS certificates.
 *
 * @author hacheni
 */
public class CertificateManager {

  public CertificateManager(EbicsUser user) {
    this.user = user;
    generator = new X509Generator();
  }

  /**
   * Creates the certificates for the user
   *
   * @throws GeneralSecurityException
   * @throws IOException
   */
  public void create() throws GeneralSecurityException, IOException {

    Calendar calendar;

    calendar = Calendar.getInstance();
    calendar.add(
        Calendar.YEAR, user.getEbicsPartner().getEbicsBank().getCertValidityPeriodSelect());

    org.apache.xml.security.Init.init();
    java.security.Security.addProvider(new BouncyCastleProvider());

    EbicsPartner ebicsPartner = user.getEbicsPartner();

    if ((user.getUserTypeSelect() == EbicsUserRepository.USER_TYPE_TRANSPORT
            && ebicsPartner.getEbicsTypeSelect() == EbicsPartnerRepository.EBICS_TYPE_TS)
        || ebicsPartner.getEbicsTypeSelect() == EbicsPartnerRepository.EBICS_TYPE_T) {
      createA005Certificate(new Date(calendar.getTimeInMillis()));
    }
    createX002Certificate(new Date(calendar.getTimeInMillis()));
    createE002Certificate(new Date(calendar.getTimeInMillis()));
    setUserCertificates();
  }

  /**
   * Sets the user certificates
   *
   * @throws IOException
   * @throws CertificateEncodingException
   */
  private void setUserCertificates() throws IOException, CertificateEncodingException {

    EbicsPartner ebicsPartner = user.getEbicsPartner();

    if ((user.getUserTypeSelect() == EbicsUserRepository.USER_TYPE_TRANSPORT
            && ebicsPartner.getEbicsTypeSelect() == EbicsPartnerRepository.EBICS_TYPE_TS)
        || ebicsPartner.getEbicsTypeSelect() == EbicsPartnerRepository.EBICS_TYPE_T) {
      user.setA005Certificate(
          updateCertificate(
              a005Certificate,
              user.getA005Certificate(),
              a005PrivateKey.getEncoded(),
              EbicsCertificateRepository.TYPE_SIGNATURE));
    }

    user.setX002Certificate(
        updateCertificate(
            x002Certificate,
            user.getX002Certificate(),
            x002PrivateKey.getEncoded(),
            EbicsCertificateRepository.TYPE_AUTHENTICATION));

    user.setE002Certificate(
        updateCertificate(
            e002Certificate,
            user.getE002Certificate(),
            e002PrivateKey.getEncoded(),
            EbicsCertificateRepository.TYPE_ENCRYPTION));
  }

  private EbicsCertificate updateCertificate(
      X509Certificate certificate, EbicsCertificate cert, byte[] privateKey, String type)
      throws CertificateEncodingException, IOException {

    if (cert == null) {
      cert = new EbicsCertificate();
      cert.setTypeSelect(type);
    }

    EbicsCertificateService certificateService = Beans.get(EbicsCertificateService.class);

    cert = certificateService.updateCertificate(certificate, cert, true);

    cert.setPrivateKey(privateKey);

    return cert;
  }

  /**
   * Creates the signature certificate.
   *
   * @param the expiration date of a the certificate.
   * @throws GeneralSecurityException
   * @throws IOException
   */
  public void createA005Certificate(Date end) throws GeneralSecurityException, IOException {
    KeyPair keypair = KeyUtil.makeKeyPair(X509Constants.EBICS_KEY_SIZE);

    EbicsBank ebicsBank = user.getEbicsPartner().getEbicsBank();

    a005Certificate =
        generator.generateA005Certificate(
            keypair,
            user.getDn(),
            new Date(),
            end,
            ebicsBank.getUseX509ExtensionBasicConstraints(),
            ebicsBank.getUseX509ExtensionSubjectKeyIdentifier(),
            ebicsBank.getUseX509ExtensionAuthorityKeyIdentifier(),
            ebicsBank.getUseX509ExtensionExtendedKeyUsage());
    a005PrivateKey = keypair.getPrivate();
  }

  /**
   * Creates the authentication certificate.
   *
   * @param the expiration date of a the certificate.
   * @throws GeneralSecurityException
   * @throws IOException
   */
  public void createX002Certificate(Date end) throws GeneralSecurityException, IOException {
    KeyPair keypair = KeyUtil.makeKeyPair(X509Constants.EBICS_KEY_SIZE);

    EbicsBank ebicsBank = user.getEbicsPartner().getEbicsBank();

    x002Certificate =
        generator.generateX002Certificate(
            keypair,
            user.getDn(),
            new Date(),
            end,
            ebicsBank.getUseX509ExtensionBasicConstraints(),
            ebicsBank.getUseX509ExtensionSubjectKeyIdentifier(),
            ebicsBank.getUseX509ExtensionAuthorityKeyIdentifier(),
            ebicsBank.getUseX509ExtensionExtendedKeyUsage());
    x002PrivateKey = keypair.getPrivate();
  }

  /**
   * Creates the encryption certificate.
   *
   * @param the expiration date of a the certificate.
   * @throws GeneralSecurityException
   * @throws IOException
   */
  public void createE002Certificate(Date end) throws GeneralSecurityException, IOException {
    KeyPair keypair = KeyUtil.makeKeyPair(X509Constants.EBICS_KEY_SIZE);

    EbicsBank ebicsBank = user.getEbicsPartner().getEbicsBank();

    e002Certificate =
        generator.generateE002Certificate(
            keypair,
            user.getDn(),
            new Date(),
            end,
            ebicsBank.getUseX509ExtensionBasicConstraints(),
            ebicsBank.getUseX509ExtensionSubjectKeyIdentifier(),
            ebicsBank.getUseX509ExtensionAuthorityKeyIdentifier(),
            ebicsBank.getUseX509ExtensionExtendedKeyUsage());
    e002PrivateKey = keypair.getPrivate();
  }

  /**
   * Saves the certificates in PKCS12 format
   *
   * @param path the certificates path
   * @param pwdCallBack the password call back
   * @throws GeneralSecurityException
   * @throws IOException
   */
  public void save(String path) throws GeneralSecurityException, IOException {
    char[] pwd = null;
    if (user.getPassword() != null) {
      pwd = user.getPassword().toCharArray();
    }
    writePKCS12Certificate(path + "/" + user.getUserId(), pwd);
  }

  /**
   * Loads user certificates from a given key store
   *
   * @param path the key store path
   * @param pwdCallBack the password call back
   * @throws GeneralSecurityException
   * @throws IOException
   */
  public void load(String path) throws GeneralSecurityException, IOException {
    KeyStoreManager loader;

    loader = new KeyStoreManager();

    if (user.getPassword() != null) {
      loader.load(path, user.getPassword().toCharArray());
    } else {
      loader.load(path, null);
    }

    a005Certificate = loader.getCertificate(user.getUserId() + "-A005");
    x002Certificate = loader.getCertificate(user.getUserId() + "-X002");
    e002Certificate = loader.getCertificate(user.getUserId() + "-E002");

    a005PrivateKey = loader.getPrivateKey(user.getUserId() + "-A005");
    x002PrivateKey = loader.getPrivateKey(user.getUserId() + "-X002");
    e002PrivateKey = loader.getPrivateKey(user.getUserId() + "-E002");
    setUserCertificates();
  }

  /**
   * Writes a the generated certificates into a PKCS12 key store.
   *
   * @param filename the key store file name
   * @param password the key password
   * @throws IOException
   */
  public void writePKCS12Certificate(String filename, char[] password)
      throws GeneralSecurityException, IOException {
    if (filename == null || "".equals(filename)) {
      throw new IOException("The file name cannot be empty");
    }

    if (!filename.toLowerCase().endsWith(".p12")) {
      filename += ".p12";
    }

    FileOutputStream fos = new FileOutputStream(filename);
    writePKCS12Certificate(password, fos);
    fos.close();
  }

  /**
   * Writes a the generated certificates into a PKCS12 key store.
   *
   * @param password the key store password
   * @param fos the output stream
   * @throws GeneralSecurityException
   * @throws IOException
   */
  public void writePKCS12Certificate(char[] password, OutputStream fos)
      throws GeneralSecurityException, IOException {
    KeyStore keystore;

    keystore = KeyStore.getInstance("PKCS12", new BouncyCastleProvider());
    keystore.load(null, null);
    keystore.setKeyEntry(
        user.getUserId() + "-A005",
        a005PrivateKey,
        password,
        new X509Certificate[] {a005Certificate});
    keystore.setKeyEntry(
        user.getUserId() + "-X002",
        x002PrivateKey,
        password,
        new X509Certificate[] {x002Certificate});
    keystore.setKeyEntry(
        user.getUserId() + "-E002",
        e002PrivateKey,
        password,
        new X509Certificate[] {e002Certificate});
    keystore.store(fos, password);
  }

  // --------------------------------------------------------------------
  // DATA MEMBERS
  // --------------------------------------------------------------------

  private X509Generator generator;
  private EbicsUser user;

  private X509Certificate a005Certificate;
  private X509Certificate e002Certificate;
  private X509Certificate x002Certificate;

  private PrivateKey a005PrivateKey;
  private PrivateKey x002PrivateKey;
  private PrivateKey e002PrivateKey;
}
