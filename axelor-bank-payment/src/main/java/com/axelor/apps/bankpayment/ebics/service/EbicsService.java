/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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

import com.axelor.app.AppSettings;
import com.axelor.apps.bankpayment.db.BankOrderFileFormat;
import com.axelor.apps.bankpayment.db.EbicsPartner;
import com.axelor.apps.bankpayment.db.EbicsPartnerService;
import com.axelor.apps.bankpayment.db.EbicsRequestLog;
import com.axelor.apps.bankpayment.db.EbicsUser;
import com.axelor.apps.bankpayment.db.repo.EbicsPartnerRepository;
import com.axelor.apps.bankpayment.db.repo.EbicsRequestLogRepository;
import com.axelor.apps.bankpayment.db.repo.EbicsUserRepository;
import com.axelor.apps.bankpayment.exception.BankPaymentExceptionMessage;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.auth.db.User;
import com.axelor.i18n.I18n;
import com.axelor.libs.ebics.client.EbicsProduct;
import com.axelor.libs.ebics.client.EbicsSession;
import com.axelor.libs.ebics.client.FileTransfer;
import com.axelor.libs.ebics.client.KeyManagement;
import com.axelor.libs.ebics.client.OrderType;
import com.axelor.libs.ebics.dto.EbicsLibRequestLog;
import com.axelor.libs.ebics.dto.EbicsLibUser;
import com.axelor.libs.ebics.exception.EbicsLibException;
import com.axelor.libs.ebics.exception.ReturnCode;
import com.axelor.libs.ebics.io.IOUtils;
import com.axelor.libs.ebics.xml.DefaultResponseElement;
import com.axelor.libs.ebics.xml.KeyManagementResponseElement;
import com.axelor.meta.MetaFiles;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jdom.JDOMException;

public class EbicsService {

  @Inject private EbicsUserRepository userRepo;

  @Inject private EbicsRequestLogRepository logRepo;

  @Inject private EbicsUserService userService;

  @Inject private MetaFiles metaFiles;

  private static final String HTTP_PROXY_HOST = "http.proxy.host";
  private static final String HTTP_PROXY_PORT = "http.proxy.port";
  private static final String HTTP_PROXY_AUTH_USER = "http.proxy.auth.user";
  private static final String HTTP_PROXY_AUTH_PASSWORD = "http.proxy.auth.password";

  private EbicsProduct defaultProduct;

  static {
    org.apache.xml.security.Init.init();
    java.security.Security.addProvider(new BouncyCastleProvider());
  }

  @Inject
  public EbicsService() {

    AppSettings settings = AppSettings.get();
    String name = settings.get("application.name") + " " + settings.get("application.version");
    String language = settings.get("application.locale");
    String instituteID = settings.get("application.author");

    defaultProduct = new EbicsProduct(name, language, instituteID);
  }

  public String makeDN(EbicsUser ebicsUser) {

    String email = null;
    String companyName = defaultProduct.getInstituteID();
    User user = ebicsUser.getAssociatedUser();

    if (user != null) {
      email = user.getEmail();
      if (user.getActiveCompany() != null) {
        companyName = user.getActiveCompany().getName();
      }
    }

    return makeDN(ebicsUser.getName(), email, "FR", companyName);
  }

  private String makeDN(String name, String email, String country, String organization) {

    StringBuffer buffer = new StringBuffer();
    buffer.append("CN=" + name);

    if (country != null) {
      buffer.append(", " + "C=" + country.toUpperCase());
    }
    if (organization != null) {
      buffer.append(", " + "O=" + organization);
    }
    if (email != null) {
      buffer.append(", " + "E=" + email);
    }

    return buffer.toString();
  }

  public RSAPublicKey getPublicKey(String modulus, String exponent)
      throws NoSuchAlgorithmException, InvalidKeySpecException {

    RSAPublicKeySpec spec = new RSAPublicKeySpec(new BigInteger(modulus), new BigInteger(exponent));
    KeyFactory factory = KeyFactory.getInstance("RSA");
    RSAPublicKey pub = (RSAPublicKey) factory.generatePublic(spec);
    /*Signature verifier = Signature.getInstance("SHA1withRSA");
    verifier.initVerify(pub);
    boolean okay = verifier.verify(signature);*/

    return pub;
  }

  /**
   * Sends an INI request to the ebics bank server
   *
   * @param userId the user ID
   * @param product the application product
   * @throws AxelorException
   * @throws JDOMException
   * @throws IOException
   */
  @Transactional(rollbackOn = {Exception.class})
  public ReturnCode sendINIRequest(EbicsUser ebicsUser, EbicsProduct product)
      throws AxelorException {

    AppSettings appSettings = AppSettings.get();
    String proxyHost = appSettings.get(HTTP_PROXY_HOST);
    int proxyPort = appSettings.getInt(HTTP_PROXY_PORT, 0);
    String userName = appSettings.get(HTTP_PROXY_AUTH_USER);
    String userPassword = appSettings.get(HTTP_PROXY_AUTH_PASSWORD);

    if (ebicsUser.getStatusSelect()
        != EbicsUserRepository.STATUS_WAITING_SENDING_SIGNATURE_CERTIFICATE) {
      return ReturnCode.EBICS_OK;
    }

    try {
      userService.getNextOrderId(ebicsUser);

      EbicsLibUser ebicsLibUser = EbicsLibConvertUtils.convertEbicsUser(ebicsUser);

      EbicsSession session = new EbicsSession(ebicsLibUser);
      if (product == null) {
        product = defaultProduct;
      }
      session.setProduct(product);

      KeyManagement keyManager = new KeyManagement(session);
      DefaultResponseElement response =
          keyManager.sendINI(proxyHost, proxyPort, userName, userPassword);

      // retrieve logs from lib
      EbicsLibConvertUtils.createEbicsRequestLogsFromResponse(response);

      ReturnCode returnCode = checkResponseErrors(response);
      if (returnCode.isOk()) {
        ebicsUser.setStatusSelect(EbicsUserRepository.STATUS_WAITING_AUTH_AND_ENCRYPT_CERTIFICATES);
        userRepo.save(ebicsUser);
      }
      return returnCode;

    } catch (Exception e) {
      TraceBackService.trace(e);
      throw new AxelorException(
          e, TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, I18n.get(e.getMessage()));
    }
  }

  /**
   * Sends a HIA request to the ebics server.
   *
   * @param userId the user ID.
   * @param product the application product.
   * @throws AxelorException
   */
  @Transactional(rollbackOn = {Exception.class})
  public ReturnCode sendHIARequest(EbicsUser ebicsUser, EbicsProduct product)
      throws AxelorException {

    AppSettings appSettings = AppSettings.get();
    String proxyHost = appSettings.get(HTTP_PROXY_HOST);
    int proxyPort = appSettings.getInt(HTTP_PROXY_PORT, 0);
    String userName = appSettings.get(HTTP_PROXY_AUTH_USER);
    String userPassword = appSettings.get(HTTP_PROXY_AUTH_PASSWORD);

    if (ebicsUser.getStatusSelect()
        != EbicsUserRepository.STATUS_WAITING_AUTH_AND_ENCRYPT_CERTIFICATES) {
      return ReturnCode.EBICS_OK;
    }
    userService.getNextOrderId(ebicsUser);

    EbicsLibUser ebicsLibUser = EbicsLibConvertUtils.convertEbicsUser(ebicsUser);
    EbicsSession session = new EbicsSession(ebicsLibUser);
    if (product == null) {
      product = defaultProduct;
    }
    session.setProduct(product);
    KeyManagement keyManager = new KeyManagement(session);

    try {
      DefaultResponseElement response =
          keyManager.sendHIA(proxyHost, proxyPort, userName, userPassword);
      // retrieve logs from lib
      EbicsLibConvertUtils.createEbicsRequestLogsFromResponse(response);
      ReturnCode returnCode = checkResponseErrors(response);
      if (returnCode.isOk()) {
        ebicsUser.setStatusSelect(EbicsUserRepository.STATUS_ACTIVE_CONNECTION);
        userRepo.save(ebicsUser);
      }
      return returnCode;

    } catch (IOException | EbicsLibException e) {
      TraceBackService.trace(e);
      throw new AxelorException(
          e, TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, I18n.get(e.getMessage()));
    }
  }

  /**
   * Sends a HPB request to the ebics server.
   *
   * @param userId the user ID.
   * @param product the application product.
   * @throws AxelorException
   */
  @Transactional(rollbackOn = {Exception.class})
  public Map<String, Object> sendHPBRequest(EbicsUser user, EbicsProduct product)
      throws AxelorException {

    AppSettings appSettings = AppSettings.get();
    String proxyHost = appSettings.get(HTTP_PROXY_HOST);
    int proxyPort = appSettings.getInt(HTTP_PROXY_PORT, 0);
    String userName = appSettings.get(HTTP_PROXY_AUTH_USER);
    String userPassword = appSettings.get(HTTP_PROXY_AUTH_PASSWORD);

    EbicsLibUser ebicsLibUser = EbicsLibConvertUtils.convertEbicsUser(user);

    EbicsSession session = new EbicsSession(ebicsLibUser);
    if (product == null) {
      product = defaultProduct;
    }
    session.setProduct(product);

    KeyManagement keyManager = new KeyManagement(session);
    try {
      DefaultResponseElement response =
          keyManager.sendHPB(proxyHost, proxyPort, userName, userPassword);
      // retrieve logs from lib
      EbicsLibConvertUtils.createEbicsRequestLogsFromResponse(response);
      ReturnCode returnCode = checkResponseErrors(response);
      X509Certificate[] certificateFromResponse =
          keyManager.createCertificateFromResponse(
              (KeyManagementResponseElement) response,
              proxyHost,
              proxyPort,
              userName,
              userPassword);

      return Map.of("certificate", certificateFromResponse, "returnCode", returnCode);
    } catch (Exception e) {
      TraceBackService.trace(e);
      throw new AxelorException(
          e, TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, I18n.get(e.getMessage()));
    }
  }

  /**
   * Sends the SPR order to the bank.
   *
   * @param userId the user ID
   * @param product the session product
   * @throws AxelorException
   */
  @Transactional(rollbackOn = {Exception.class})
  public ReturnCode sendSPRRequest(EbicsUser ebicsUser, EbicsProduct product)
      throws AxelorException {

    AppSettings appSettings = AppSettings.get();
    String proxyHost = appSettings.get(HTTP_PROXY_HOST);
    int proxyPort = appSettings.getInt(HTTP_PROXY_PORT, 0);
    String userName = appSettings.get(HTTP_PROXY_AUTH_USER);
    String userPassword = appSettings.get(HTTP_PROXY_AUTH_PASSWORD);

    EbicsLibUser ebicsLibUser = EbicsLibConvertUtils.convertEbicsUser(ebicsUser);
    EbicsSession session = new EbicsSession(ebicsLibUser);
    if (product == null) {
      product = defaultProduct;
    }
    session.setProduct(product);

    KeyManagement keyManager = new KeyManagement(session);
    try {
      DefaultResponseElement response =
          keyManager.lockAccess(proxyHost, proxyPort, userName, userPassword);
      // retrieve logs from lib
      EbicsLibConvertUtils.createEbicsRequestLogsFromResponse(response);

      ReturnCode returnCode = checkResponseErrors(response);
      if (returnCode.isOk()) {
        ebicsUser.setStatusSelect(EbicsUserRepository.STATUS_WAITING_SENDING_SIGNATURE_CERTIFICATE);
        userService.getNextOrderId(ebicsUser);
        userRepo.save(ebicsUser);
      }

      return returnCode;
    } catch (Exception e) {
      TraceBackService.trace(e);
      throw new AxelorException(
          e, TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, I18n.get(e.getMessage()));
    }
  }

  /**
   * Send a file to the EBICS bank server.
   *
   * @param transportUser
   * @param signatoryUser
   * @param product
   * @param file
   * @param format
   * @param signature
   * @throws AxelorException
   */
  public void sendFULRequest(
      EbicsUser transportUser,
      EbicsUser signatoryUser,
      EbicsProduct product,
      File file,
      BankOrderFileFormat format,
      File signature)
      throws AxelorException {
    Preconditions.checkNotNull(transportUser);
    Preconditions.checkNotNull(transportUser.getEbicsPartner());
    Preconditions.checkNotNull(format);
    List<EbicsPartnerService> ebicsPartnerServiceList =
        transportUser.getEbicsPartner().getBoEbicsPartnerServiceList();
    String ebicsCodification;

    if (ebicsPartnerServiceList == null || ebicsPartnerServiceList.isEmpty()) {
      ebicsCodification = format.getOrderFileFormatSelect();
    } else {
      ebicsCodification = findEbicsCodification(transportUser.getEbicsPartner(), format);
    }

    sendFULRequest(transportUser, signatoryUser, product, file, ebicsCodification, signature);
  }

  private String findEbicsCodification(EbicsPartner ebicsPartner, BankOrderFileFormat format)
      throws AxelorException {
    Preconditions.checkNotNull(ebicsPartner);
    Preconditions.checkNotNull(format);

    if (ebicsPartner.getBoEbicsPartnerServiceList() != null) {
      for (EbicsPartnerService service : ebicsPartner.getBoEbicsPartnerServiceList()) {
        if (format.equals(service.getBankOrderFileFormat())) {
          return service.getEbicsCodification();
        }
      }
    }

    throw new AxelorException(
        TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
        I18n.get(BankPaymentExceptionMessage.EBICS_NO_SERVICE_CONFIGURED),
        ebicsPartner.getPartnerId(),
        format.getName());
  }

  /**
   * Sends a file to the ebics bank sever
   *
   * @param path the file path to send
   * @param userId the user ID that sends the file.
   * @param product the application product.
   * @throws AxelorException
   */
  private void sendFULRequest(
      EbicsUser transportUser,
      EbicsUser signatoryUser,
      EbicsProduct product,
      File file,
      String format,
      File signature)
      throws AxelorException {

    AppSettings appSettings = AppSettings.get();
    String proxyHost = appSettings.get(HTTP_PROXY_HOST);
    int proxyPort = appSettings.getInt(HTTP_PROXY_PORT, 0);
    String userName = appSettings.get(HTTP_PROXY_AUTH_USER);
    String userPassword = appSettings.get(HTTP_PROXY_AUTH_PASSWORD);

    EbicsLibUser ebicsLibTransportUser = EbicsLibConvertUtils.convertEbicsUser(transportUser);
    EbicsLibUser ebicsLibsignatoryUser = EbicsLibConvertUtils.convertEbicsUser(signatoryUser);

    EbicsSession session = new EbicsSession(ebicsLibTransportUser, ebicsLibsignatoryUser);
    boolean test = isTest(transportUser);
    if (test) {
      session.addSessionParam("TEST", "true");
    }
    if (file == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, "File is required to send FUL request");
    }
    EbicsPartner ebicsPartner = transportUser.getEbicsPartner();

    if (ebicsPartner.getEbicsTypeSelect() == EbicsPartnerRepository.EBICS_TYPE_TS) {
      if (signature == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            "Signature file is required to send FUL request");
      }
      if (signatoryUser == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            "Signatory user is required to send FUL request");
      }
    }

    session.addSessionParam("EBCDIC", "false");
    session.addSessionParam("FORMAT", format);

    if (product == null) {
      product = defaultProduct;
    }
    session.setProduct(product);
    FileTransfer transferManager = new FileTransfer(session);

    try {
      List<DefaultResponseElement> ebicsResponses = null;
      if (ebicsPartner.getEbicsTypeSelect() == EbicsPartnerRepository.EBICS_TYPE_TS) {
        ebicsResponses =
            transferManager.sendFile(
                IOUtils.getFileContent(file.getAbsolutePath()),
                OrderType.FUL,
                IOUtils.getFileContent(signature.getAbsolutePath()),
                proxyHost,
                proxyPort,
                userName,
                userPassword);
      } else {
        ebicsResponses =
            transferManager.sendFile(
                IOUtils.getFileContent(file.getAbsolutePath()),
                OrderType.FUL,
                null,
                proxyHost,
                proxyPort,
                userName,
                userPassword);
      }

      ebicsResponses.forEach(EbicsLibConvertUtils::createEbicsRequestLogsFromResponse);
      userService.getNextOrderId(transportUser);
      checkResponseListErrors(ebicsResponses);
    } catch (IOException | AxelorException | EbicsLibException e) {
      TraceBackService.trace(e);
      throw new AxelorException(
          e, TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, I18n.get(e.getMessage()));
    }

    try {
      if (ebicsPartner.getUsePSR()) {
        sendFDLRequest(
            transportUser,
            product,
            null,
            null,
            ebicsPartner.getpSRBankStatementFileFormat().getStatementFileFormatSelect());
      }
    } catch (AxelorException e) {
      TraceBackService.trace(e);
    }
  }

  public File sendFDLRequest(
      EbicsUser user, EbicsProduct product, Date start, Date end, String fileFormat)
      throws AxelorException {

    return fetchFile(OrderType.FDL, user, product, start, end, fileFormat);
  }

  public File sendHTDRequest(EbicsUser user, EbicsProduct product, Date start, Date end)
      throws AxelorException {

    return fetchFile(OrderType.HTD, user, product, start, end, null);
  }

  public File sendPTKRequest(EbicsUser user, EbicsProduct product, Date start, Date end)
      throws AxelorException {

    return fetchFile(OrderType.PTK, user, product, start, end, null);
  }

  public File sendHPDRequest(EbicsUser user, EbicsProduct product, Date start, Date end)
      throws AxelorException {

    return fetchFile(OrderType.HPD, user, product, start, end, null);
  }

  private File fetchFile(
      OrderType orderType,
      EbicsUser user,
      EbicsProduct product,
      Date start,
      Date end,
      String fileFormat)
      throws AxelorException {

    AppSettings appSettings = AppSettings.get();
    String proxyHost = appSettings.get(HTTP_PROXY_HOST);
    int proxyPort = appSettings.getInt(HTTP_PROXY_PORT, 0);
    String userName = appSettings.get(HTTP_PROXY_AUTH_USER);
    String userPassword = appSettings.get(HTTP_PROXY_AUTH_PASSWORD);

    EbicsLibUser ebicsLibUser = EbicsLibConvertUtils.convertEbicsUser(user);

    EbicsSession session = new EbicsSession(ebicsLibUser);
    File file = null;
    try {
      boolean test = isTest(user);
      if (test) {
        session.addSessionParam("TEST", "true");
      }
      if (fileFormat != null) {
        session.addSessionParam("FORMAT", fileFormat);
      }
      if (product == null) {
        product = defaultProduct;
      }
      session.setProduct(product);

      FileTransfer transferManager = new FileTransfer(session);

      file = File.createTempFile(user.getName(), "." + orderType.getOrderType());
      List<DefaultResponseElement> ebicsResponses =
          transferManager.fetchFile(
              orderType,
              start,
              end,
              new FileOutputStream(file),
              proxyHost,
              proxyPort,
              userName,
              userPassword);

      ebicsResponses.forEach(EbicsLibConvertUtils::createEbicsRequestLogsFromResponse);

      addResponseFile(user, file);

      userService.getNextOrderId(user);
      checkResponseListErrors(ebicsResponses);

    } catch (EbicsLibException | IOException e) {
      TraceBackService.trace(e);
      throw new AxelorException(
          e, TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, I18n.get(e.getMessage()));
    }

    return file;
  }

  private boolean isTest(EbicsUser user) {

    EbicsPartner partner = user.getEbicsPartner();

    return partner.getTestMode();
  }

  @Transactional(rollbackOn = {Exception.class})
  public void addResponseFile(EbicsUser user, File file) throws IOException {

    EbicsRequestLog requestLog =
        logRepo.all().filter("self.ebicsUser = ?1", user).order("-id").fetchOne();
    if (requestLog != null && file != null && file.length() > 0) {
      requestLog.setResponseFile(metaFiles.upload(file));
      logRepo.save(requestLog);
    }
  }

  protected void checkResponseListErrors(List<DefaultResponseElement> ebicsResponses)
      throws AxelorException {
    // technical error message
    for (DefaultResponseElement ebicsResponse : ebicsResponses) {
      checkTechnicalErrorInResponse(ebicsResponse);
    }

    // functional error message
    Optional<ReturnCode> errorReturnCode =
        ebicsResponses.stream()
            .map(DefaultResponseElement::getReturnCode)
            .filter(returnCode -> !returnCode.isOk())
            .findFirst();
    if (errorReturnCode.isPresent()) {
      AxelorException exception =
          new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(errorReturnCode.map(ReturnCode::getText).orElse("")));
      TraceBackService.trace(exception);
      throw exception;
    }
  }

  protected ReturnCode checkResponseErrors(DefaultResponseElement ebicsResponse)
      throws AxelorException {
    // technical error message
    checkTechnicalErrorInResponse(ebicsResponse);

    // functional error message, do not throw exception to avoid rollback
    return Optional.ofNullable(ebicsResponse.getReturnCode()).orElse(ReturnCode.EBICS_OK);
  }

  protected void checkTechnicalErrorInResponse(DefaultResponseElement ebicsResponse)
      throws AxelorException {
    Optional<EbicsLibRequestLog> ebicsRequestError =
        Optional.ofNullable(ebicsResponse.getEbicsLibRequestLog())
            .filter(ebicsLibRequestLog -> ebicsLibRequestLog.getErrorMessage() != null).stream()
            .findFirst();

    if (ebicsRequestError.isPresent()) {
      AxelorException exception =
          new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(ebicsRequestError.map(EbicsLibRequestLog::getErrorMessage).orElse("")));
      TraceBackService.trace(exception);
      throw exception;
    }
  }
}
