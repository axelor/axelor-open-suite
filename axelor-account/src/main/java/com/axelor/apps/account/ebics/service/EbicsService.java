package com.axelor.apps.account.ebics.service;

import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;

import org.jdom.JDOMException;

import com.axelor.apps.account.db.EbicsUser;
import com.axelor.apps.account.ebics.client.DefaultConfiguration;
import com.axelor.apps.account.ebics.client.EbicsProduct;
import com.axelor.apps.account.ebics.client.EbicsSession;
import com.axelor.apps.account.ebics.client.KeyManagement;
import com.axelor.exception.AxelorException;


public class EbicsService {
	
	public String makeDN(String name, String email, String country, String organization)
	{
		StringBuffer		buffer;
		
		buffer = new StringBuffer();
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
	
	
	public RSAPublicKey getPublicKey(String modulus, String exponent) throws NoSuchAlgorithmException, InvalidKeySpecException{
		
		RSAPublicKeySpec spec = new RSAPublicKeySpec( new BigInteger(modulus), new BigInteger(exponent));
		KeyFactory factory = KeyFactory.getInstance("RSA");
		RSAPublicKey pub = (RSAPublicKey) factory.generatePublic(spec);
		/*Signature verifier = Signature.getInstance("SHA1withRSA");
		verifier.initVerify(pub);
		boolean okay = verifier.verify(signature);*/
		
		
		return pub;
	}
	
	public RSAPrivateKey getPrivateKey(byte[] encoded) throws NoSuchAlgorithmException, InvalidKeySpecException{
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return (RSAPrivateKey) kf.generatePrivate(keySpec);
	}
	
	
	/**
	   * Sends an INI request to the ebics bank server
	   * @param userId the user ID
	   * @param product the application product
	 * @throws AxelorException 
	 * @throws JDOMException 
	 * @throws IOException 
	   */
	  public void sendINIRequest(EbicsUser ebicsUser, EbicsProduct product) throws AxelorException, IOException, JDOMException {
	    EbicsSession		session;
	    KeyManagement		keyManager;

	    // log ::: configuration.getLogger().info(Messages.getString("ini.request.send", Constants.APPLICATION_BUNDLE_NAME, userId));

	    
	    ebicsUser.setIsInitialized(false);

	    if (ebicsUser.getIsInitialized()) {
	      //configuration.getLogger().info(Messages.getString("user.already.initialized", Constants.APPLICATION_BUNDLE_NAME, userId));
	      return;
	    }

	    session = new EbicsSession(ebicsUser, new DefaultConfiguration() );
	    session.setProduct(product);
	    keyManager = new KeyManagement(session);
	    //configuration.getTraceManager().setTraceDirectory(configuration.getTransferTraceDirectory(user));

	  //  try {
	      keyManager.sendINI(null);
	  //  } catch (Exception e) {
	    	//throw new AxelorException(e.getCause(), IException.CONFIGURATION_ERROR);
	      //configuration.getLogger().error(Messages.getString("ini.send.error", Constants.APPLICATION_BUNDLE_NAME, userId), e);
	      //return;
	    //}

	    ebicsUser.setIsInitialized(true);
	    //configuration.getLogger().info(Messages.getString("ini.send.success", Constants.APPLICATION_BUNDLE_NAME, userId));
	  }

	  /**
	   * Sends a HIA request to the ebics server.
	   * @param userId the user ID.
	   * @param product the application product.
	   */
	  public void sendHIARequest(EbicsUser user) {
	    EbicsSession		session;
	    KeyManagement		keyManager;

	    //configuration.getLogger().info(Messages.getString("hia.request.send", Constants.APPLICATION_BUNDLE_NAME, userId));
	    user.setIsInitializedHIA(false);
	    if (user.getIsInitializedHIA()) {
	      //configuration.getLogger().info(Messages.getString("user.already.hia.initialized", Constants.APPLICATION_BUNDLE_NAME, userId));
	      return;
	    }
	    session = new EbicsSession(user, new DefaultConfiguration());
	    //session.setProduct(product);
	    keyManager = new KeyManagement(session);
	    //configuration.getTraceManager().setTraceDirectory(configuration.getTransferTraceDirectory(user));

	    try {
	      keyManager.sendHIA(null);
	    } catch (Exception e) {
	      //configuration.getLogger().error(Messages.getString("hia.send.error", Constants.APPLICATION_BUNDLE_NAME, userId), e);
	      return;
	    }

	    user.setIsInitializedHIA(true);
	    //configuration.getLogger().info(Messages.getString("hia.send.success", Constants.APPLICATION_BUNDLE_NAME, userId));
	  }

	  /**
	   * Sends a HPB request to the ebics server.
	   * @param userId the user ID.
	   * @param product the application product.
	   */
	 /* public void sendHPBRequest(EbicsUser user) {
	    EbicsSession		session;
	    KeyManagement		keyManager;

	    //configuration.getLogger().info(Messages.getString("hpb.request.send", Constants.APPLICATION_BUNDLE_NAME, userId));

	    session = new EbicsSession(user);
	    //session.setProduct(product);
	    keyManager = new KeyManagement(session);

	    //configuration.getTraceManager().setTraceDirectory(configuration.getTransferTraceDirectory(user));

	    try {
	      keyManager.sendHPB();
	    } catch (Exception e) {
	     // configuration.getLogger().error(Messages.getString("hpb.send.error", Constants.APPLICATION_BUNDLE_NAME, userId), e);
	      return;
	    }

	    //configuration.getLogger().info(Messages.getString("hpb.send.success", Constants.APPLICATION_BUNDLE_NAME, userId));
	  }*/

	  /**
	   * Sends the SPR order to the bank.
	   * @param userId the user ID
	   * @param product the session product
	   */
	/*  public void revokeSubscriber(EbicsUser user) {
	    EbicsSession		session;
	    KeyManagement		keyManager;

	    //configuration.getLogger().info(Messages.getString("spr.request.send", Constants.APPLICATION_BUNDLE_NAME, userId));

	    session = new EbicsSession(user, null);
	    //session.setProduct(product);
	    keyManager = new KeyManagement(session);

	    //configuration.getTraceManager().setTraceDirectory(configuration.getTransferTraceDirectory(user));

	    try {
	      keyManager.lockAccess();
	    } catch (Exception e) {
	      //configuration.getLogger().error(Messages.getString("spr.send.error", Constants.APPLICATION_BUNDLE_NAME, userId), e);
	      return;
	    }

	    //configuration.getLogger().info(Messages.getString("spr.send.success", Constants.APPLICATION_BUNDLE_NAME, userId));
	  }*/

	  /**
	   * Sends a file to the ebics bank sever
	   * @param path the file path to send
	   * @param userId the user ID that sends the file.
	   * @param product the application product.
	   */
	 /* public void sendFile(String path, String userId, Product product) {
	    FileTransfer		transferManager;
	    EbicsSession		session;

	    session = new EbicsSession(users.get(userId), configuration);
	    session.addSessionParam("FORMAT", "pain.xxx.cfonb160.dct");
	    session.addSessionParam("TEST", "true");
	    session.addSessionParam("EBCDIC", "false");
	    session.setProduct(product);
	    transferManager = new FileTransfer(session);

	    configuration.getTraceManager().setTraceDirectory(configuration.getTransferTraceDirectory(users.get(userId)));

	    try {
	      transferManager.sendFile(IOUtils.getFileContent(path), OrderType.FUL);
	    } catch (IOException e) {
	      configuration.getLogger().error(Messages.getString("upload.file.error", Constants.APPLICATION_BUNDLE_NAME, path), e);
	    } catch (EbicsException e) {
	      configuration.getLogger().error(Messages.getString("upload.file.error", Constants.APPLICATION_BUNDLE_NAME, path), e);
	    }
	  }*/

	  /*public void fetchFile(String path,
	                        String userId,
	                        Product product,
	                        OrderType orderType,
	                        boolean isTest,
	                        Date start,
	                        Date end)
	  {
	    FileTransfer		transferManager;
	    EbicsSession		session;

	    session = new EbicsSession(users.get(userId), configuration);
	    session.addSessionParam("FORMAT", "pain.xxx.cfonb160.dct");
	    if (isTest) {
	      session.addSessionParam("TEST", "true");
	    }
	    session.setProduct(product);
	    transferManager = new FileTransfer(session);

	    configuration.getTraceManager().setTraceDirectory(configuration.getTransferTraceDirectory(users.get(userId)));

	    try {
	      transferManager.fetchFile(orderType, start, end, new FileOutputStream(path));
	    } catch (IOException e) {
	      configuration.getLogger().error(Messages.getString("download.file.error", Constants.APPLICATION_BUNDLE_NAME), e);
	    } catch (EbicsException e) {
	      configuration.getLogger().error(Messages.getString("download.file.error", Constants.APPLICATION_BUNDLE_NAME), e);
	    }
	  }*/

}
