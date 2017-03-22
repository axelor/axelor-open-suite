/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2017 Axelor (<http://axelor.com>).
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
package com.axelor.apps.bankpayment.ebics.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Signature;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jdom.JDOMException;
import org.joda.time.LocalDateTime;

import com.axelor.apps.bankpayment.db.EbicsPartner;
import com.axelor.apps.bankpayment.db.EbicsRequestLog;
import com.axelor.apps.bankpayment.db.EbicsUser;
import com.axelor.apps.bankpayment.db.repo.EbicsRequestLogRepository;
import com.axelor.apps.bankpayment.db.repo.EbicsUserRepository;
import com.axelor.apps.bankpayment.ebics.client.EbicsRootElement;
import com.axelor.apps.bankpayment.ebics.client.EbicsUtils;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class EbicsUserService {
	
	@Inject
	private EbicsService ebicsService;
	
	@Inject
	private EbicsRequestLogRepository requestLogRepo;
	
	@Inject
	private EbicsUserRepository ebicsUserRepo;
	
	
	public byte[] sign(EbicsUser ebicsUser, byte[] digest) throws IOException, GeneralSecurityException {
		
		Signature signature = Signature.getInstance("SHA256WithRSA", BouncyCastleProvider.PROVIDER_NAME);
	    signature.initSign(ebicsService.getPrivateKey( ebicsUser.getA005Certificate().getPrivateKey() ));
	    signature.update(removeOSSpecificChars(digest));
	    return signature.sign();
	
	}
	
	public byte[] authenticate(EbicsUser ebicsUser, byte[] digest) throws GeneralSecurityException {
	    Signature signature;
	    signature = Signature.getInstance("SHA256WithRSA", BouncyCastleProvider.PROVIDER_NAME);
	    signature.initSign( ebicsService.getPrivateKey( ebicsUser.getX002Certificate().getPrivateKey() )) ;
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
	    cipher.init(Cipher.DECRYPT_MODE, ebicsService.getPrivateKey(user.getE002Certificate().getPrivateKey()));
	    blockSize = cipher.getBlockSize();
	    outputStream = new ByteArrayOutputStream();
	    for (int j = 0; j * blockSize < transactionKey.length; j++) {
	      outputStream.write(cipher.doFinal(transactionKey, j * blockSize, blockSize));
	    }
	    
	    return decryptData(encryptedData, outputStream.toByteArray());
	}
	
	private byte[] decryptData(byte[] input, byte[] key) throws AxelorException { 
		 return EbicsUtils.decrypt(input, new SecretKeySpec(key, "EAS"));
	}
	
	@Transactional
	public void logRequest(long ebicsUserId, String requestType, String responseCode, EbicsRootElement[] rootElements) {
		
		EbicsRequestLog requestLog = new EbicsRequestLog();
		requestLog.setEbicsUser(ebicsUserRepo.find(ebicsUserId));
		LocalDateTime time = new LocalDateTime();
		requestLog.setRequestTime(time);
		requestLog.setRequestType(requestType);
		requestLog.setResponseCode(responseCode);
		
		try {
			trace(requestLog, rootElements);
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		requestLogRepo.save(requestLog);
	}
	
	@Transactional
	public String getNextOrderId(EbicsUser user) throws AxelorException {
		
		String orderId = user.getNextOrderId();
		
		if (orderId == null) {
			EbicsPartner partner = user.getEbicsPartner();
			EbicsUser otherUser = ebicsUserRepo.all().filter("self.ebicsPartner = ?1 and self.id != ?2 and self.nextOrderId != null", partner, user.getId()).order("nextOrderId").fetchOne();
			
			char firstLetter = 'A';
			if (otherUser != null) {
				String otherOrderId = otherUser.getNextOrderId();
				firstLetter =  otherOrderId.charAt(0);
				firstLetter++;
			}
			
			orderId = String.valueOf(firstLetter) + "000";
			user.setNextOrderId(orderId);
			ebicsUserRepo.save(user);
		}
		else {
			orderId = getNextOrderNumber(orderId);
			user.setNextOrderId(orderId);
			ebicsUserRepo.save(user);
		}
		
		
		return orderId;
	}

	private String getNextOrderNumber(String orderId) throws AxelorException {
		
		Integer orderNo = Integer.parseInt(orderId.substring(1)) + 1;
		
		if (orderNo > 999) {
			throw new AxelorException(I18n.get("Maximum order limit reach"),1);
		}
		
		return orderId.substring(0,1) + String.format("%03d", orderNo);
	}

	
	private void trace(EbicsRequestLog requestLog, EbicsRootElement[] rootElements) throws AxelorException, JDOMException, IOException {
		
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		rootElements[0].save(bout);
		requestLog.setRequestTraceText(bout.toString());
		bout.close();
		
		bout = new ByteArrayOutputStream();
		rootElements[1].save(bout);
		requestLog.setResponseTraceText(bout.toString());
		bout.close();
		
	}

	

}
