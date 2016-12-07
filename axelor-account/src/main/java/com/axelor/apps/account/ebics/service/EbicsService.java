package com.axelor.apps.account.ebics.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Date;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jdom.JDOMException;

import com.axelor.app.AppSettings;
import com.axelor.apps.account.db.EbicsBank;
import com.axelor.apps.account.db.EbicsPartner;
import com.axelor.apps.account.db.EbicsUser;
import com.axelor.apps.account.db.repo.EbicsBankRepository;
import com.axelor.apps.account.db.repo.EbicsUserRepository;
import com.axelor.apps.account.ebics.client.EbicsProduct;
import com.axelor.apps.account.ebics.client.EbicsSession;
import com.axelor.apps.account.ebics.client.FileTransfer;
import com.axelor.apps.account.ebics.client.KeyManagement;
import com.axelor.apps.account.ebics.client.OrderType;
import com.axelor.apps.account.ebics.io.IOUtils;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;


public class EbicsService {
	
	@Inject
	private EbicsUserRepository userRepo;
	
	@Inject
	private EbicsBankRepository bankRepo;
	
	@Inject
	private MetaFiles metaFiles;
	
	private EbicsProduct defaultProduct;
	
	static {
	    org.apache.xml.security.Init.init();
	    java.security.Security.addProvider(new BouncyCastleProvider());
	}
	
	@Inject
	public EbicsService() {
		
		AppSettings settings = AppSettings.get();
		String name = settings.get("application.name") + "-" + settings.get("application.version");
		String language = settings.get("application.locale");
		String instituteID = settings.get("application.author");
		
		defaultProduct = new EbicsProduct(name, language, instituteID);
	}
	
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
	
	
	public RSAPublicKey getPublicKey(String modulus, String exponent) throws NoSuchAlgorithmException, InvalidKeySpecException {
		
		RSAPublicKeySpec spec = new RSAPublicKeySpec( new BigInteger(modulus), new BigInteger(exponent));
		KeyFactory factory = KeyFactory.getInstance("RSA");
		RSAPublicKey pub = (RSAPublicKey) factory.generatePublic(spec);
		/*Signature verifier = Signature.getInstance("SHA1withRSA");
		verifier.initVerify(pub);
		boolean okay = verifier.verify(signature);*/
		
		return pub;
	}
	
	
	
	public RSAPrivateKey getPrivateKey(byte[] encoded) throws NoSuchAlgorithmException, InvalidKeySpecException {
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
	  @Transactional
	  public void sendINIRequest(EbicsUser ebicsUser, EbicsProduct product) throws AxelorException, IOException, JDOMException {
	    EbicsSession		session;
	    KeyManagement		keyManager;

	    if (ebicsUser.getStatusSelect() != EbicsUserRepository.STATUS_WAITING_SENDING_SIGNATURE_CERTIFICATE) {
		      return;
	    }
	    
	    session = new EbicsSession(ebicsUser);
	    if (product == null) {
	    	product = defaultProduct;
	    }
	    session.setProduct(product);
	    
	    keyManager = new KeyManagement(session);
        keyManager.sendINI(null, getCertificate(ebicsUser));
	    
        ebicsUser.setStatusSelect(EbicsUserRepository.STATUS_WAITING_AUTH_AND_ENCRYPT_CERTIFICATES);
	    userRepo.save(ebicsUser);
	  }

	  /**
	   * Sends a HIA request to the ebics server.
	   * @param userId the user ID.
	   * @param product the application product.
	   */
	  @Transactional
	  public void sendHIARequest(EbicsUser ebicsUser, EbicsProduct product) {
	    EbicsSession		session;
	    KeyManagement		keyManager;

	    if (ebicsUser.getStatusSelect() != EbicsUserRepository.STATUS_WAITING_AUTH_AND_ENCRYPT_CERTIFICATES) {
	      return;
	    }
	    session = new EbicsSession(ebicsUser);
	    if (product == null) {
	    	product = defaultProduct;
	    }
	    session.setProduct(product);
	    keyManager = new KeyManagement(session);

	    try {
			keyManager.sendHIA(null , getCertificate(ebicsUser));
		} catch (IOException | AxelorException | JDOMException e) {
			e.printStackTrace();
		}

	    ebicsUser.setStatusSelect(EbicsUserRepository.STATUS_ACTIVE_CONNECTION);
	    userRepo.save(ebicsUser);
	  }

	  /**
	   * Sends a HPB request to the ebics server.
	   * @param userId the user ID.
	   * @param product the application product.
	   */
	  @Transactional
	  public void sendHPBRequest(EbicsUser user, EbicsProduct product) {
	    EbicsSession		session;
	    KeyManagement		keyManager;

	    session = new EbicsSession(user);
	    if (product == null) {
	    	product = defaultProduct;
	    }
	    session.setProduct(product);
	    keyManager = new KeyManagement(session);

	    try {
	      keyManager.sendHPB(getCertificate(user));
	      bankRepo.save(user.getEbicsPartner().getEbicsBank());
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }

	  }

	  /**
	   * Sends the SPR order to the bank.
	   * @param userId the user ID
	   * @param product the session product
	   */
	  public void revokeSubscriber(EbicsUser user, EbicsProduct product) {
	    EbicsSession		session;
	    KeyManagement		keyManager;

	    session = new EbicsSession(user);
	    if (product == null) {
	    	product = defaultProduct;
	    }
	    session.setProduct(product);
	    keyManager = new KeyManagement(session);

	    try {
	      keyManager.lockAccess(getCertificate(user));
	    } catch (Exception e) {
	      e.printStackTrace();
	      return;
	    }

	  }

	  /**
	   * Sends a file to the ebics bank sever
	   * @param path the file path to send
	   * @param userId the user ID that sends the file.
	   * @param product the application product.
	 * @throws AxelorException 
	   */
	  public void sendFULRequest(EbicsUser user, EbicsProduct product, File file) throws AxelorException {
		  
	    FileTransfer transferManager;
	    EbicsSession session;

	    session = new EbicsSession(user);
	    session.addSessionParam("FORMAT", "pain.xxx.cfonb160.dct");
	    boolean test = isTest(user, false);
	    if (test) {
	    	session.addSessionParam("TEST", "true");
	    	file = MetaFiles.getPath(user.getEbicsPartner().getEbicsBank().getTestFile()).toFile(); 
	    }
	    else if (file == null) {
	    	throw new AxelorException("File is required to send FUL request", 1);
	    }
	    session.addSessionParam("EBCDIC", "false");
	    if (product == null) {
	    	product = defaultProduct;
	    }
	    session.setProduct(product);
	    transferManager = new FileTransfer(session);
	    
	    try {
	      transferManager.sendFile(IOUtils.getFileContent(file.getAbsolutePath()), OrderType.FUL, getCertificate(user));
	    } catch (IOException | AxelorException e) {
	    	e.printStackTrace();
	    }
	  }

	 public File sendFDLRequest( EbicsUser user,
	                        EbicsProduct product,
	                        Date start,
	                        Date end) throws IOException, AxelorException
	  {
	    FileTransfer		transferManager;
	    EbicsSession		session;

	    session = new EbicsSession(user);
	    boolean test = isTest(user, true);
	    if (test) {
	    	session.addSessionParam("TEST", "true");
	    }
	    session.addSessionParam("FORMAT", "pain.xxx.cfonb160.dct");
	    if (product == null) {
	    	product = defaultProduct;
	    }
	    session.setProduct(product);
	    transferManager = new FileTransfer(session);
	    
	    File file = File.createTempFile(user.getName(), "fdl");

	    transferManager.fetchFile(OrderType.FDL, start, end, new FileOutputStream(file), getCertificate(user));
	    
	    if (test) {
	    	 updateTestFile(user, file);
	    }
	    
	    return file;
	  }
	
	private boolean isTest(EbicsUser user, boolean download) throws AxelorException {
		
		EbicsPartner partner = user.getEbicsPartner();
		
		if (partner != null) {
			EbicsBank bank = partner.getEbicsBank();
			if (bank != null) {
				if (bank.getTestMode()) {
					if (download) {
						return true;
					}
					if (bank.getTestFile() != null) {
						return true;
					}
				}
				else {
					return false;
				}
			}
		}
		 
		throw new  AxelorException(I18n.get("Ebics bank configuration error"), 1);
	}
	
	@Transactional
	public void updateTestFile(EbicsUser user, File file) throws IOException {
		
		EbicsBank bank = user.getEbicsPartner().getEbicsBank();
		
		if (bank.getTestFile() != null) {
			metaFiles.upload(file, bank.getTestFile());
		}
		else {
			bank.setTestFile(metaFiles.upload(file));
			bankRepo.save(bank);
		}
		
	}

	public void sendHTDRequest( EbicsUser user,
             EbicsProduct product,
             Date start,
             Date end) throws IOException, AxelorException
	 {
		FileTransfer		transferManager;
		EbicsSession		session;
		
		session = new EbicsSession(user);
		session.addSessionParam("FORMAT", "pain.xxx.cfonb160.dct");
		session.addSessionParam("ClientDataDownload", "true");
		boolean test = isTest(user, true);
		if (test) {
			session.addSessionParam("TEST", "true");
		}
		if (product == null) {
			product = defaultProduct;
		}
		session.setProduct(product);
		transferManager = new FileTransfer(session);
		
		File file = File.createTempFile(user.getName(), "htd");
		
		transferManager.fetchFile(OrderType.HTD, start, end, new FileOutputStream(file), getCertificate(user));
		
		if (test) {
			updateTestFile(user, file);
		}
	 }
	 
	 
	 private File getCertificate(EbicsUser user) {
		 
		 EbicsBank bank = user.getEbicsPartner().getEbicsBank();
		 MetaFile cert = bank.getCertificate();

		 return MetaFiles.getPath(cert).toFile();
	 }

}
