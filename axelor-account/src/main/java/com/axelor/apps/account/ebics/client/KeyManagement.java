package com.axelor.apps.account.ebics.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.interfaces.RSAPublicKey;

import org.jdom.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.EbicsBank;
import com.axelor.apps.account.ebics.certificate.KeyStoreManager;
import com.axelor.apps.account.ebics.certificate.KeyUtil;
import com.axelor.apps.account.ebics.interfaces.ContentFactory;
import com.axelor.apps.account.ebics.io.ByteArrayContentFactory;
import com.axelor.apps.account.ebics.service.EbicsUserService;
import com.axelor.apps.account.ebics.utils.Utils;
import com.axelor.apps.account.ebics.xml.HIARequestElement;
import com.axelor.apps.account.ebics.xml.HPBRequestElement;
import com.axelor.apps.account.ebics.xml.HPBResponseOrderDataElement;
import com.axelor.apps.account.ebics.xml.INIRequestElement;
import com.axelor.apps.account.ebics.xml.KeyManagementResponseElement;
import com.axelor.apps.account.ebics.xml.SPRRequestElement;
import com.axelor.apps.account.ebics.xml.SPRResponseElement;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;


/**
 * Everything that has to do with key handling.
 * If you have a totally new account use <code>sendINI()</code> and <code>sendHIA()</code> to send you newly created keys to the bank.
 * Then wait until the bank activated your keys.
 * If you are migrating from FTAM. Just send HPB, your EBICS account should be usable without delay.
 *
 * @author Hachani
 *
 */
public class KeyManagement {
 
  /**
   * Constructs a new <code>KeyManagement</code> instance
   * with a given ebics session
   * @param session the ebics session
   */
  public KeyManagement(EbicsSession session) {
    this.session = session;
  }
  private final Logger log = LoggerFactory.getLogger( getClass() );
  /**
   * Sends the user's signature key (A005) to the bank.
   * After successful operation the user is in state "initialized".
   * @param orderId the order ID. Let it null to generate a random one.
   * @throws EbicsException server generated error message
   * @throws IOException communication error
 * @throws AxelorException 
 * @throws JDOMException 
   */
  public void sendINI(String orderId) throws IOException, AxelorException, JDOMException {
    INIRequestElement			request;
    KeyManagementResponseElement	response;
    HttpRequestSender			sender;
    int					httpCode;
    sender = new HttpRequestSender(session);
    log.debug("HttpRequestSender OK");
    request = new INIRequestElement(session, orderId);
    log.debug("INIRequestElement OK");
    request.build();
    log.debug("build OK");
    request.validate();
    log.debug("validate OK");
    //session.getConfiguration().getTraceManager().trace(request);
    httpCode = sender.send(new ByteArrayContentFactory(request.prettyPrint()));
    log.debug("send OK");
    EbicsUtils.checkHttpCode(httpCode);
    log.debug("checkHttpCode OK");
    response = new KeyManagementResponseElement(sender.getResponseBody(), "INIResponse");
    log.debug("KeyManagementResponseElement OK");
    response.build();
    log.debug("build OK");
    //session.getConfiguration().getTraceManager().trace(response);
    response.report();
    log.debug("report OK");
  }

  /**
   * Sends the public part of the protocol keys to the bank.
   * @param orderId the order ID. Let it null to generate a random one.
   * @throws IOException communication error
 * @throws JDOMException 
   * @throws EbicsException server generated error message
   */
  public void sendHIA(String orderId) throws IOException, AxelorException, JDOMException {
    HIARequestElement			request;
    KeyManagementResponseElement	response;
    HttpRequestSender			sender;
    int					httpCode;

    sender = new HttpRequestSender(session);
    request = new HIARequestElement(session, orderId);
    request.build();
    request.validate();
//    session.getConfiguration().getTraceManager().trace(request);
    httpCode = sender.send(new ByteArrayContentFactory(request.prettyPrint()));
    EbicsUtils.checkHttpCode(httpCode);
    response = new KeyManagementResponseElement(sender.getResponseBody(), "HIAResponse");
    response.build();
//    session.getConfiguration().getTraceManager().trace(response);
    response.report();
  }

  /**
   * Sends encryption and authentication keys to the bank.
   * This order is only allowed for a new user at the bank side that has been created by copying the A005 key.
   * The keys will be activated immediately after successful completion of the transfer.
   * @param orderId the order ID. Let it null to generate a random one.
   * @throws IOException communication error
   * @throws GeneralSecurityException data decryption error
 * @throws AxelorException 
 * @throws JDOMException 
   * @throws EbicsException server generated error message
   */
  public void sendHPB() throws IOException, GeneralSecurityException, AxelorException, JDOMException {
    HPBRequestElement			request;
    KeyManagementResponseElement	response;
    HttpRequestSender			sender;
    HPBResponseOrderDataElement		orderData;
    ContentFactory			factory;
    KeyStoreManager			keystoreManager;
    RSAPublicKey			e002PubKey;
    RSAPublicKey			x002PubKey;
    int					httpCode;
    
    sender = new HttpRequestSender(session);
    request = new HPBRequestElement(session);
    request.build();
    request.validate();
    httpCode = sender.send(new ByteArrayContentFactory(request.prettyPrint()));
    Utils.checkHttpCode(httpCode);
    response = new KeyManagementResponseElement(sender.getResponseBody(), "HBPResponse");
    response.build();
    response.report();
    EbicsUserService userService = Beans.get(EbicsUserService.class);
    factory = new ByteArrayContentFactory(Utils.unzip(userService.decrypt(session.getUser(), response.getOrderData(), response.getTransactionKey())));
    orderData = new HPBResponseOrderDataElement(factory);
    orderData.build();
    keystoreManager = new KeyStoreManager();
    keystoreManager.load("" , session.getUser().getPassword().toCharArray() );
    e002PubKey = keystoreManager.getPublicKey(new ByteArrayInputStream(orderData.getBankE002Certificate()));
    x002PubKey = keystoreManager.getPublicKey(new ByteArrayInputStream(orderData.getBankX002Certificate()));
    EbicsBank bank = session.getUser().getEbicsPartner().getEbicsBank();
//    bank.setE002Key(e002PubKey.getEncoded());
//    bank.setX002Key(x002PubKey.getEncoded());
    bank.setE002Digest(KeyUtil.getKeyDigest(e002PubKey));
    bank.setX002Digest(KeyUtil.getKeyDigest(x002PubKey));
    bank.setE002KeyExponent(e002PubKey.getPublicExponent().toString());
    bank.setE002KeyModulus(e002PubKey.getModulus().toString());
    bank.setX002KeyExponent(x002PubKey.getPublicExponent().toString());
    bank.setX002KeyModulus(x002PubKey.getModulus().toString());
    keystoreManager.setCertificateEntry(session.getBankID() + "-E002", new ByteArrayInputStream(orderData.getBankE002Certificate()));
    keystoreManager.setCertificateEntry(session.getBankID() + "-X002", new ByteArrayInputStream(orderData.getBankX002Certificate()));
  }

  /**
   * Sends the SPR order to the bank.
   * After that you have to start over with sending INI and HIA.
   * @throws IOException Communication exception
 * @throws AxelorException 
 * @throws JDOMException 
   * @throws EbicsException Error message generated by the bank.
   */
  
  public void lockAccess() throws IOException, AxelorException, JDOMException {
    HttpRequestSender			sender;
    SPRRequestElement			request;
    SPRResponseElement			response;
    int					httpCode;

    sender = new HttpRequestSender(session);
    request = new SPRRequestElement(session);
    request.build();
    request.validate();
    httpCode = sender.send(new ByteArrayContentFactory(request.prettyPrint()));
    Utils.checkHttpCode(httpCode);
    response = new SPRResponseElement(sender.getResponseBody());
    response.build();
    response.report();
  }

  // --------------------------------------------------------------------
  // DATA MEMBERS
  // --------------------------------------------------------------------

  private EbicsSession 				session;
}
