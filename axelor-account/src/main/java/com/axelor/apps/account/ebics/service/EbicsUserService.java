package com.axelor.apps.account.ebics.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Signature;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import java.time.LocalDateTime;

import com.axelor.apps.account.db.EbicsRequestLog;
import com.axelor.apps.account.db.EbicsUser;
import com.axelor.apps.account.db.repo.EbicsRequestLogRepository;
import com.axelor.apps.account.ebics.utils.Utils;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class EbicsUserService {
	
	@Inject
	EbicsService ebicsService;
	
	@Inject
	EbicsRequestLogRepository requestLogRepo;
	
	
	public byte[] sign(EbicsUser ebicsUser, byte[] digest) throws IOException, GeneralSecurityException {
		
		Signature signature = Signature.getInstance("SHA256WithRSA", BouncyCastleProvider.PROVIDER_NAME);
	    signature.initSign(ebicsService.getPrivateKey( ebicsUser.getA005PrivateKey() ));
	    signature.update(removeOSSpecificChars(digest));
	    return signature.sign();
	
	}
	
	public byte[] authenticate(EbicsUser ebicsUser, byte[] digest) throws GeneralSecurityException {
	    Signature signature;
	    signature = Signature.getInstance("SHA256WithRSA", BouncyCastleProvider.PROVIDER_NAME);
	    signature.initSign( ebicsService.getPrivateKey( ebicsUser.getX002PrivateKey() )) ;
	    signature.update(digest);
	    return signature.sign();
	  }
	
	public static byte[] removeOSSpecificChars(byte[] buf) {
	    ByteArrayOutputStream		output;
	    
	    output = new ByteArrayOutputStream();
	    for (int i = 0; i < buf.length; i++) {
	      switch (buf[i]) {
	      case '\r':
	      case '\n':
	      case 0x1A: // CTRL-Z / EOF
		// ignore this character
		break;

	      default:
		output.write(buf[i]);
	      }
	    }
	    
	    return output.toByteArray();
	}
	
	public byte[] decrypt(EbicsUser user, byte[] encryptedData, byte[] transactionKey)
		    throws AxelorException, GeneralSecurityException, IOException
	  {
	    Cipher			cipher;
	    int				blockSize;
	    ByteArrayOutputStream	outputStream;

	    cipher = Cipher.getInstance("RSA/NONE/PKCS1Padding", BouncyCastleProvider.PROVIDER_NAME);
	    cipher.init(Cipher.DECRYPT_MODE, ebicsService.getPrivateKey(user.getE002PrivateKey()));
	    blockSize = cipher.getBlockSize();
	    outputStream = new ByteArrayOutputStream();
	    for (int j = 0; j * blockSize < transactionKey.length; j++) {
	      outputStream.write(cipher.doFinal(transactionKey, j * blockSize, blockSize));
	    }
	    
	    return decryptData(encryptedData, outputStream.toByteArray());
	}
	
	private byte[] decryptData(byte[] input, byte[] key) throws AxelorException { 
		 return Utils.decrypt(input, new SecretKeySpec(key, "EAS"));
	}
	
	@Transactional
	public void logRequest(EbicsUser ebicsUser, String requestType, String responseCode) {
		
		EbicsRequestLog requestLog = new EbicsRequestLog();
		requestLog.setEbicsUser(ebicsUser);
		requestLog.setRequestTime(LocalDateTime.now());
		requestLog.setRequestType(requestType);
		requestLog.setResponseCode(responseCode);
		
		requestLogRepo.save(requestLog);
	}
	
	

}
