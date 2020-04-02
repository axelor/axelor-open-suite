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
package com.axelor.apps.account.service.payment;

import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.account.db.PayboxConfig;
import com.axelor.apps.account.db.PaymentVoucher;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.AccountingSituationService;
import com.axelor.apps.account.service.config.PayboxConfigService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.tool.StringTool;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.persist.Transactional;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.UnsupportedEncodingException;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.codec.Base64;
import org.bouncycastle.openssl.PEMReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PayboxService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected PayboxConfigService payboxConfigService;
  protected PartnerRepository partnerRepository;

  protected final String CHARSET = "UTF-8";
  protected final String HASH_ENCRYPTION_ALGORITHM = "SHA1withRSA";
  protected final String ENCRYPTION_ALGORITHM = "RSA";

  public PayboxService(
      PayboxConfigService payboxConfigService, PartnerRepository partnerRepository) {

    this.payboxConfigService = payboxConfigService;
    this.partnerRepository = partnerRepository;
  }

  /**
   * Procédure permettant de réaliser un paiement avec Paybox
   *
   * @param paymentVoucher Une saisie paiement
   * @throws AxelorException
   * @throws UnsupportedEncodingException
   */
  public String paybox(PaymentVoucher paymentVoucher)
      throws AxelorException, UnsupportedEncodingException {

    this.checkPayboxPaymentVoucherFields(paymentVoucher);

    Company company = paymentVoucher.getCompany();

    BigDecimal paidAmount = paymentVoucher.getPaidAmount();
    Partner payerPartner = paymentVoucher.getPartner();
    //		this.checkPayboxPartnerFields(payerPartner);
    this.checkPaidAmount(payerPartner, company, paidAmount);
    this.checkPaidAmount(paymentVoucher);

    PayboxConfig payboxConfig = payboxConfigService.getPayboxConfig(company);

    // Vérification du remplissage du chemin de la clé publique Paybox
    payboxConfigService.getPayboxPublicKeyPath(payboxConfig);

    String payboxUrl = payboxConfigService.getPayboxUrl(payboxConfig);
    String pbxSite = payboxConfigService.getPayboxSite(payboxConfig);
    String pbxRang = payboxConfigService.getPayboxRang(payboxConfig);
    String pbxDevise = payboxConfigService.getPayboxDevise(payboxConfig);
    String pbxTotal = paidAmount.setScale(2).toString().replace(".", "");
    String pbxCmd = paymentVoucher.getRef(); // Identifiant de la saisie paiement
    String pbxPorteur = this.getPartnerEmail(paymentVoucher);
    String pbxRetour = payboxConfigService.getPayboxRetour(payboxConfig);
    //		String pbxEffectue =
    // this.encodeUrl(this.replaceVariableInUrl(accountConfigService.getPayboxRetourUrlEffectue(accountConfig), paymentVoucher));
    String pbxEffectue =
        this.replaceVariableInUrl(
            payboxConfigService.getPayboxRetourUrlEffectue(payboxConfig), paymentVoucher);
    String pbxRefuse =
        this.replaceVariableInUrl(
            payboxConfigService.getPayboxRetourUrlRefuse(payboxConfig), paymentVoucher);
    String pbxAnnule =
        this.replaceVariableInUrl(
            payboxConfigService.getPayboxRetourUrlAnnule(payboxConfig), paymentVoucher);
    String pbxIdentifiant = payboxConfigService.getPayboxIdentifiant(payboxConfig);
    String pbxHash = payboxConfigService.getPayboxHashSelect(payboxConfig);
    String pbxHmac = payboxConfigService.getPayboxHmac(payboxConfig);

    // Date à laquelle l'empreinte HMAC a été calculée (format ISO8601)
    String pbxTime = ZonedDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);

    // Permet de restreindre les modes de paiement
    String pbxTypepaiement = "CARTE";
    String pbxTypecarte = "CB";

    String message =
        this.buildMessage(
            pbxSite,
            pbxRang,
            pbxIdentifiant,
            pbxTotal,
            pbxDevise,
            pbxCmd,
            pbxPorteur,
            pbxRetour,
            pbxEffectue,
            pbxRefuse,
            pbxAnnule,
            pbxHash,
            pbxTime,
            pbxTypepaiement,
            pbxTypecarte);

    log.debug("Message : {}", message);

    String messageHmac = this.getHmacSignature(message, pbxHmac, pbxHash);

    log.debug("Message HMAC : {}", messageHmac);

    String messageEncode =
        this.buildMessage(
            URLEncoder.encode(pbxSite, this.CHARSET),
            URLEncoder.encode(pbxRang, this.CHARSET),
            URLEncoder.encode(pbxIdentifiant, this.CHARSET),
            pbxTotal,
            URLEncoder.encode(pbxDevise, this.CHARSET),
            URLEncoder.encode(pbxCmd, this.CHARSET),
            URLEncoder.encode(pbxPorteur, this.CHARSET),
            URLEncoder.encode(pbxRetour, this.CHARSET),
            URLEncoder.encode(pbxEffectue, this.CHARSET),
            URLEncoder.encode(pbxRefuse, this.CHARSET),
            URLEncoder.encode(pbxAnnule, this.CHARSET),
            URLEncoder.encode(pbxHash, this.CHARSET),
            URLEncoder.encode(pbxTime, this.CHARSET),
            URLEncoder.encode(pbxTypepaiement, this.CHARSET),
            URLEncoder.encode(pbxTypecarte, this.CHARSET));

    String url = payboxUrl + messageEncode + "&PBX_HMAC=" + messageHmac;

    log.debug("Url : {}", url);

    return url;
  }

  public String buildMessage(
      String pbxSite,
      String pbxRang,
      String pbxIdentifiant,
      String pbxTotal,
      String pbxDevise,
      String pbxCmd,
      String pbxPorteur,
      String pbxRetour,
      String pbxEffectue,
      String pbxRefuse,
      String pbxAnnule,
      String pbxHash,
      String pbxTime,
      String pbxTypepaiement,
      String pbxTypecarte) {
    return String.format(
        "PBX_SITE=%s&PBX_RANG=%s&PBX_IDENTIFIANT=%s&PBX_TOTAL=%s&PBX_DEVISE=%s"
            + "&PBX_CMD=%s&PBX_PORTEUR=%s&PBX_RETOUR=%s&PBX_EFFECTUE=%s&PBX_REFUSE=%s&PBX_ANNULE=%s&PBX_HASH=%s&PBX_TIME=%s&PBX_TYPEPAIEMENT=%s&PBX_TYPECARTE=%s",
        pbxSite,
        pbxRang,
        pbxIdentifiant,
        pbxTotal,
        pbxDevise,
        pbxCmd,
        pbxPorteur,
        pbxRetour,
        pbxEffectue,
        pbxRefuse,
        pbxAnnule,
        pbxHash,
        pbxTime,
        pbxTypepaiement,
        pbxTypecarte);
  }

  /**
   * Fonction remplaçant le paramètre %id par le numéro d'id de la saisie paiement
   *
   * @param url
   * @param paymentVoucher
   * @return
   */
  public String replaceVariableInUrl(String url, PaymentVoucher paymentVoucher) {

    return url.replaceAll("%idPV", paymentVoucher.getId().toString());
  }

  /**
   * Fonction convertissant l'url en url encodé
   *
   * @param url
   * @return
   */
  public String encodeUrl(String url) {
    String newUrl = url.replaceAll("\\%", "%25");
    newUrl = newUrl.replaceAll("\\?", "%3F");
    newUrl = newUrl.replaceAll("\\/", "%2F");
    newUrl = newUrl.replaceAll("\\:", "%3A");
    newUrl = newUrl.replaceAll("\\#", "%23");
    newUrl = newUrl.replaceAll("\\&", "%26");
    newUrl = newUrl.replaceAll("\\=", "%3D");
    newUrl = newUrl.replaceAll("\\+", "%2B");
    newUrl = newUrl.replaceAll("\\$", "%24");
    newUrl = newUrl.replaceAll("\\,", "%2C");
    newUrl = newUrl.replaceAll(" ", "%20");
    newUrl = newUrl.replaceAll("\\;", "%3B");
    newUrl = newUrl.replaceAll("\\<", "%3C");
    newUrl = newUrl.replaceAll("\\>", "%3E");
    newUrl = newUrl.replaceAll("\\~", "%7E");
    newUrl = newUrl.replaceAll("\\.", "%2E");
    return newUrl;
    //		return url;

  }

  public String getPartnerEmail(PaymentVoucher paymentVoucher) throws AxelorException {

    Partner partner = paymentVoucher.getPartner();
    Company company = paymentVoucher.getCompany();

    if (partner.getEmailAddress().getAddress() != null
        && !partner.getEmailAddress().getAddress().isEmpty()) {
      return partner.getEmailAddress().getAddress();
    } else if (paymentVoucher.getEmail() != null && !paymentVoucher.getEmail().isEmpty()) {
      return paymentVoucher.getEmail();
    } else {
      return payboxConfigService.getPayboxDefaultEmail(
          payboxConfigService.getPayboxConfig(company));
    }
  }

  /**
   * Procédure permettant de vérifier que les champs de la saisie paiement necessaire à Paybox sont
   * bien remplis
   *
   * @param paymentVoucher
   * @throws AxelorException
   */
  public void checkPayboxPaymentVoucherFields(PaymentVoucher paymentVoucher)
      throws AxelorException {
    if (paymentVoucher.getPaidAmount() == null
        || paymentVoucher.getPaidAmount().compareTo(BigDecimal.ZERO) > 1) {
      throw new AxelorException(
          paymentVoucher,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.PAYBOX_1),
          I18n.get(com.axelor.apps.base.exceptions.IExceptionMessage.EXCEPTION),
          paymentVoucher.getRef());
    }
  }

  /**
   * Procédure permettant de vérifier que le montant réglé par Paybox n'est pas supérieur au solde
   * du payeur
   *
   * @param partner
   * @param paidAmount
   * @throws AxelorException
   */
  public void checkPaidAmount(Partner partner, Company company, BigDecimal paidAmount)
      throws AxelorException {
    AccountingSituation accountingSituation =
        Beans.get(AccountingSituationService.class).getAccountingSituation(partner, company);

    BigDecimal partnerBalance = accountingSituation.getBalanceCustAccount();

    if (paidAmount.compareTo(partnerBalance) > 0) {
      throw new AxelorException(
          partner,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.PAYBOX_2),
          I18n.get(com.axelor.apps.base.exceptions.IExceptionMessage.EXCEPTION));
    }
  }

  public void checkPaidAmount(PaymentVoucher paymentVoucher) throws AxelorException {
    if (paymentVoucher.getRemainingAmount().compareTo(BigDecimal.ZERO) > 0) {
      throw new AxelorException(
          paymentVoucher,
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(IExceptionMessage.PAYBOX_3),
          I18n.get(com.axelor.apps.base.exceptions.IExceptionMessage.EXCEPTION));
    }
  }

  /**
   * Procédure permettant de vérifier que le paramétrage des champs necessaire à Paybox d'un tiers
   * est bien réalisé
   *
   * @param partner
   * @throws AxelorException
   */
  public void checkPayboxPartnerFields(Partner partner) throws AxelorException {
    if (partner.getEmailAddress().getAddress() == null
        || partner.getEmailAddress().getAddress().isEmpty()) {
      throw new AxelorException(
          partner,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.PAYBOX_4),
          I18n.get(com.axelor.apps.base.exceptions.IExceptionMessage.EXCEPTION),
          partner.getName());
    }
  }

  /**
   * Fonction calculant la signature HMAC des paramètres
   *
   * @param data La chaine contenant les paramètres
   * @param hmacKey La clé HMAC
   * @param algorithm L'algorithme utilisé (SHA512, ...)
   * @return
   * @throws AxelorException
   */
  public String getHmacSignature(String data, String hmacKey, String algorithm)
      throws AxelorException {
    try {

      byte[] bytesKey = DatatypeConverter.parseHexBinary(hmacKey);
      SecretKeySpec secretKey = new SecretKeySpec(bytesKey, "Hmac" + algorithm);
      Mac mac = Mac.getInstance("Hmac" + algorithm);
      mac.init(secretKey);

      byte[] macData = mac.doFinal(data.getBytes(this.CHARSET));

      //			final byte[] hex = new Hex().encode( macData );
      //			return new String( hex, this.CHARSET );
      //			LOG.debug("Message HMAC 2 : {}",new String( hex, this.CHARSET ));

      String s = StringTool.getHexString(macData);

      return s.toUpperCase();

    } catch (InvalidKeyException | NoSuchAlgorithmException | UnsupportedEncodingException e) {
      throw new AxelorException(
          e.getCause(),
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          "%s :\n %s",
          I18n.get(com.axelor.apps.base.exceptions.IExceptionMessage.EXCEPTION),
          e);
    }
  }

  /**
   * Méthode permettant d'ajouter une adresse email à un contact
   *
   * @param contact Un contact
   * @param email Une adresse email
   * @param toSaveOk L'adresse email doit-elle être enregistré pour le contact
   */
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public void addPayboxEmail(Partner partner, String email, boolean toSaveOk) {
    if (toSaveOk) {

      partner.getEmailAddress().setAddress(email);
      partnerRepository.save(partner);
    }
  }

  /**
   * @param signature La signture contenu dans l'url
   * @param varUrl Liste des variables contenu dans l'url, privé de la dernière : la signature
   * @param company La société
   * @return
   * @throws Exception
   */
  public boolean checkPaybox(String signature, List<String> varUrl, Company company)
      throws Exception {

    boolean result =
        this.checkPaybox(
            signature,
            varUrl,
            company.getAccountConfig().getPayboxConfig().getPayboxPublicKeyPath());

    log.debug("Resultat de la verification de signature : {}", result);

    return result;
  }

  /**
   * @param signature La signature contenu dans l'url
   * @param urlParam Liste des paramètres contenus dans l'url, privé du dernier : la signature
   * @param pubKeyPath Le chemin de la clé publique Paybox
   * @return
   * @throws Exception
   */
  public boolean checkPaybox(String signature, List<String> urlParam, String pubKeyPath)
      throws Exception {

    String payboxParams = StringUtils.join(urlParam, "&");
    log.debug("Liste des variables Paybox signées : {}", payboxParams);

    //	 		Déjà décodée par le framework
    //	     	String decoded = URLDecoder.decode(sign, this.CHARSET);

    byte[] sigBytes = Base64.decode(signature.getBytes(this.CHARSET));

    // lecture de la cle publique
    PublicKey pubKey = this.getPubKey(pubKeyPath);

    /**
     * Dans le cas où le clé est au format .der
     *
     * <p>PublicKey pubKey = this.getPubKeyDer(pubKeyPath);
     */

    // verification signature
    return this.verify(payboxParams.getBytes(), sigBytes, this.HASH_ENCRYPTION_ALGORITHM, pubKey);
  }

  /**
   * Chargement de la cle AU FORMAT pem Alors ajouter la dépendance dans le fichier pom.xml :
   * <dependency> <groupId>org.bouncycastle</groupId> <artifactId>bcprov-jdk15on</artifactId>
   * <version>1.47</version> </dependency>
   *
   * <p>Ainsi que l'import : import org.bouncycastle.util.io.pem.PemReader;
   *
   * @param pubKeyFile
   * @return
   * @throws Exception
   */
  private PublicKey getPubKey(String pubKeyPath) throws Exception {

    PEMReader reader = new PEMReader(new FileReader(pubKeyPath));

    byte[] pubKey = ((X509EncodedKeySpec) reader.readObject()).getEncoded();

    reader.close();

    KeyFactory keyFactory = KeyFactory.getInstance(this.ENCRYPTION_ALGORITHM);

    X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(pubKey);

    return keyFactory.generatePublic(pubKeySpec);
  }

  /**
   * Chargement de la cle AU FORMAT der Utliser la commande suivante pour 'convertir' la clé 'pem'
   * en 'der' openssl rsa -inform PEM -in pubkey.pem -outform DER -pubin -out pubkey.der
   *
   * @param pubKeyFile
   * @return
   * @throws Exception
   */
  @Deprecated
  private PublicKey getPubKeyDer(String pubKeyPath) throws Exception {

    FileInputStream fis = new FileInputStream(pubKeyPath);
    DataInputStream dis = new DataInputStream(fis);

    byte[] pubKeyBytes = new byte[fis.available()];

    dis.readFully(pubKeyBytes);
    fis.close();
    dis.close();

    KeyFactory keyFactory = KeyFactory.getInstance(this.ENCRYPTION_ALGORITHM);

    // extraction cle
    X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(pubKeyBytes);
    return keyFactory.generatePublic(pubSpec);
  }

  /**
   * verification signature RSA des donnees avec cle publique
   *
   * @param dataBytes
   * @param sigBytes
   * @param pubKey
   * @return
   * @throws Exception
   */
  private boolean verify(byte[] dataBytes, byte[] sigBytes, String sigAlg, PublicKey pubKey)
      throws Exception {
    Signature signature = Signature.getInstance(sigAlg);
    signature.initVerify(pubKey);
    signature.update(dataBytes);
    return signature.verify(sigBytes);
  }
}
