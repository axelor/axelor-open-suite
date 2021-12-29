/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
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
package com.axelor.apps.portal.service.paybox;

import com.axelor.app.AppSettings;
import com.axelor.apps.base.db.AppPortal;
import com.axelor.apps.base.db.repo.AppPortalRepository;
import com.axelor.common.ObjectUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.lang.invoke.MethodHandles;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.core.MultivaluedMap;
import javax.xml.bind.DatatypeConverter;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.util.io.pem.PemReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PayboxServiceImpl implements PayboxService {

  /** Hash method used for hmac, send to paybox as a parameter. */
  private static final String HASH_METHOD = "Sha512";

  /** HMAC method used to sign paybox parameters. */
  private static final String HMAC_METHOD = "HmacSHA512";

  /** Public Key encryption algorithm. */
  private static final String ENCRYPTION_ALGORITHM = "RSA";

  /** Signature hash encryption algorithm. */
  private static final String HASH_ENCRYPTION_ALGORITHM = "SHA1withRSA";

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected AppPortalRepository appRepo;

  @Inject
  public PayboxServiceImpl(AppPortalRepository appRep) {
    this.appRepo = appRep;
  }

  @Override
  public String buildUrl(
      Long amountInCents,
      String orderReference,
      String email,
      String successURL,
      String failureURL) {
    final Map<String, String> params = new LinkedHashMap<>();

    AppPortal app = appRepo.all().fetchOne();
    final String baseUrl = AppSettings.get().getBaseURL();

    params.put(PayboxConstants.PBX_SITE, app.getPayboxSite());
    params.put(PayboxConstants.PBX_RANG, app.getPayboxRank());
    params.put(PayboxConstants.PBX_IDENTIFIANT, app.getPayboxUserName());
    params.put(PayboxConstants.PBX_PAYBOX, app.getPayboxPaybox());
    params.put(PayboxConstants.PBX_BACKUP1, app.getPayboxBackup1());
    params.put(PayboxConstants.PBX_BACKUP2, app.getPayboxBackup2());
    params.put(PayboxConstants.PBX_TOTAL, String.valueOf(amountInCents));
    params.put(PayboxConstants.PBX_DEVISE, String.valueOf(app.getPayboxCurrency()));
    params.put(PayboxConstants.PBX_CMD, orderReference);
    params.put(PayboxConstants.PBX_PORTEUR, email);
    params.put(PayboxConstants.PBX_RETOUR, "reference:R;error:E;transaction:S;sign:K");
    params.put(PayboxConstants.PBX_EFFECTUE, successURL);
    params.put(PayboxConstants.PBX_REFUSE, failureURL);
    params.put(PayboxConstants.PBX_ANNULE, failureURL);
    params.put(PayboxConstants.PBX_ATTENTE, successURL);
    params.put(PayboxConstants.PBX_REPONDRE_A, baseUrl + "/ws/public/paybox-validate");
    return buildPayboxUrl(app.getPayboxUrl(), params);
  }

  @Override
  public boolean checkSignature(MultivaluedMap<String, String> params) {
    AppPortal app = appRepo.all().fetchOne();
    final String signKey = "sign";
    Map<String, String> paramsWOSign =
        params.keySet().stream()
            .filter(key -> !signKey.equals(key))
            .collect(Collectors.toMap(Function.identity(), params::getFirst));

    final String payboxParams = join(paramsWOSign, false);
    final List<String> signValues = params.get(signKey);
    final String sign = ObjectUtils.notEmpty(signValues) ? signValues.get(0) : null;
    return checkSignature(payboxParams, sign, app.getPayboxPublicKey());
  }

  @Override
  public void checkError(String errorCode) throws AxelorException {
    String message = null;
    switch (errorCode) {
      case PayboxErrorConstants.CODE_ERROR_CONNECTION_FAILED:
        message = I18n.get("Connection to the authorization center failed.");
        break;
      case PayboxErrorConstants.CODE_ERROR_PAYMENT_REFUSED:
        message = I18n.get("Payment refused by the authorization center.");
        break;
      case PayboxErrorConstants.CODE_ERROR_PAYBOX:
        message = I18n.get("Paybox error");
        break;
      case PayboxErrorConstants.CODE_ERROR_WRONG_USER_NAME_OR_CRYPTOGRAM:
        message = I18n.get("Invalid user number or visual cryptogram.");
        break;
      case PayboxErrorConstants.CODE_ERROR_ACCESS_DENIED:
        message = I18n.get("Access denied or incorrect site / rank / username.");
        break;
      case PayboxErrorConstants.CODE_ERROR_WRONG_EXPIRATION_DATE:
        message = I18n.get("Incorrect expiry date.");
        break;
      case PayboxErrorConstants.CODE_ERROR_SUBSCRIPTION_CREATION:
        message = I18n.get("Error creating a subscription.");
        break;
      case PayboxErrorConstants.CODE_ERROR_UNKNOWN_CURRENCY:
        message = I18n.get("Unknown currency.");
        break;
      case PayboxErrorConstants.CODE_ERROR_WRONG_AMOUNT:
        message = I18n.get("Incorrect amount.");
        break;
      case PayboxErrorConstants.CODE_ERROR_ALREADY_DONE:
        message = I18n.get("Payment already made.");
        break;
      case PayboxErrorConstants.CODE_ERROR_CARD_NOT_ALLOWED:
        message = I18n.get("Card not valid.");
        break;
      case PayboxErrorConstants.CODE_ERROR_RESERVED:
      case PayboxErrorConstants.CODE_ERROR_RESERVED_2:
        message = I18n.get("Reserved.");
        break;
      case PayboxErrorConstants.CODE_ERROR_BLOCKED_IP:
        message = I18n.get("Country code of the IP address of the buyer's browser unauthorized.");
        break;
      case PayboxErrorConstants.CODE_ERROR_3DSECURE:
        message = I18n.get("Operation without 3DSecure authentication, blocked by the filter.");
        break;
      default:
        message = I18n.get("Unknown error");
        break;
    }
    throw new AxelorException(TraceBackRepository.CATEGORY_INCONSISTENCY, message);
  }

  protected String buildPayboxUrl(final String url, final Map<String, String> params) {
    // PBX_TIME, PBX_HASH and PBX_HMAC are removed before re-insertion to guarantee
    // that all of them are placed at the end of the uri and with relevant values.
    params.remove(PayboxConstants.PBX_TIME);
    params.remove(PayboxConstants.PBX_HASH);
    params.remove(PayboxConstants.PBX_HMAC);

    params.put(
        PayboxConstants.PBX_TIME,
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
    params.put(PayboxConstants.PBX_HASH, HASH_METHOD);
    params.put(PayboxConstants.PBX_HMAC, generateHMAC(join(params, false)));

    final StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append(url);
    stringBuilder.append('?');
    stringBuilder.append(join(params, true));

    return stringBuilder.toString();
  }

  /**
   * Assemble all the parameters to make a key-value string.
   *
   * <p>Joins all parameters together
   *
   * @param params params key/value map.
   * @param urlencode if true values are urlencoded.
   * @return joined key and values in a single string.
   */
  private String join(final Map<String, String> params, final boolean urlencode) {
    final String[] elems = new String[params.size()];
    int index = 0;

    for (final Entry<String, String> set : params.entrySet()) {
      elems[index++] = addKeyValueElement(urlencode, set);
    }

    return StringUtils.join(elems, '&');
  }

  /**
   * Creates a key=value http GET parameter using set parameter.
   *
   * @param urlencode If true value will be urlencoded
   * @param set the key/value set
   * @return the key=value generated string.
   */
  private String addKeyValueElement(final boolean urlencode, final Entry<String, String> set) {
    final StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append(set.getKey());
    stringBuilder.append('=');

    if (urlencode) {
      try {
        stringBuilder.append(
            URLEncoder.encode(set.getValue(), StandardCharsets.UTF_8.name().toLowerCase()));
      } catch (final UnsupportedEncodingException e) {
        log.error(e.getMessage());
        TraceBackService.trace(e);
      }
    } else {
      stringBuilder.append(set.getValue());
    }

    return stringBuilder.toString();
  }

  /**
   * Generates hmac signature of a message.
   *
   * @param payboxParameters parameters concatenation in the pattern
   *     key1=value1&key2=value2&...&keyN=valueN of all transmited parameter, not urlencoded
   * @return the signature (it will be added as value of the latest parameter).
   */
  private String generateHMAC(final String payboxParameters) {
    Mac mac;
    String result;
    AppPortal app = appRepo.all().fetchOne();
    try {
      final byte[] bytesKey = DatatypeConverter.parseHexBinary(app.getPayboxKey());
      final SecretKeySpec secretKey = new SecretKeySpec(bytesKey, HMAC_METHOD);
      mac = Mac.getInstance(HMAC_METHOD);
      mac.init(secretKey);
      final String charSet = StandardCharsets.UTF_8.name();
      final byte[] macData = mac.doFinal(payboxParameters.getBytes(charSet));
      final byte[] hex = new Hex().encode(macData);
      result = new String(hex, charSet);
    } catch (final NoSuchAlgorithmException e) {
      result = "";
      TraceBackService.trace(e);
    } catch (final InvalidKeyException e) {
      result = "";
      TraceBackService.trace(e);
    } catch (final UnsupportedEncodingException e) {
      result = "";
      TraceBackService.trace(e);
    }

    return result.toUpperCase();
  }

  protected boolean checkSignature(final String message, final String sign, final String key) {
    boolean result;

    try {
      result = verify(message, sign, getKey(key));
    } catch (InvalidKeyException
        | NoSuchAlgorithmException
        | SignatureException
        | InvalidKeySpecException
        | IOException e) {
      TraceBackService.trace(e);
      result = false;
    }

    return result;
  }

  /**
   * Operates validation of signature among message and public key.
   *
   * @param message the message
   * @param sign the raw signature, must be still urlencoded.
   * @param publicKey the public key.
   * @return true, if signature successfully validated.
   * @throws NoSuchAlgorithmException the no such algorithm exception
   * @throws InvalidKeyException the invalid key exception
   * @throws SignatureException the signature exception
   * @throws UnsupportedEncodingException the unsupported encoding exception
   */
  private static boolean verify(final String message, final String sign, final PublicKey publicKey)
      throws NoSuchAlgorithmException, InvalidKeyException, SignatureException,
          UnsupportedEncodingException {
    final Signature sig = Signature.getInstance(HASH_ENCRYPTION_ALGORITHM);
    final Charset utf8 = StandardCharsets.UTF_8;
    sig.initVerify(publicKey);
    sig.update(message.getBytes(utf8));

    final Base64 b64 = new Base64();
    final byte[] bytes = b64.decode(URLDecoder.decode(sign, utf8.name()).getBytes(utf8));
    return sig.verify(bytes);
  }

  /**
   * Get public key at specified path.
   *
   * @param key public key.
   * @return public key object.
   * @throws NoSuchAlgorithmException the no such algorithm exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws InvalidKeySpecException the invalid key spec exception
   */
  private PublicKey getKey(final String key)
      throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
    final KeyFactory keyFactory = KeyFactory.getInstance(ENCRYPTION_ALGORITHM);
    final PemReader reader = new PemReader(new StringReader(key));
    final byte[] pubKey = reader.readPemObject().getContent();
    final X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(pubKey);

    PublicKey generatePublic = keyFactory.generatePublic(publicKeySpec);
    reader.close();

    return generatePublic;
  }
}
