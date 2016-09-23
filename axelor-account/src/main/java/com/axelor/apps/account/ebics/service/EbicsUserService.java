package com.axelor.apps.account.ebics.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.crypto.dsig.SignedInfo;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.axelor.apps.account.db.EbicsUser;
import com.google.inject.Inject;

public class EbicsUserService {
	
	@Inject
	EbicsService ebicsService;
	
	
	public byte[] sign(EbicsUser ebicsUser, byte[] digest) throws IOException, GeneralSecurityException {
		
		Signature signature = Signature.getInstance("SHA256WithRSA", BouncyCastleProvider.PROVIDER_NAME);
	    signature.initSign(ebicsService.getPrivateKey( ebicsUser.getA005PrivateKey() ));
	    signature.update(removeOSSpecificChars(digest));
	    return signature.sign();
	
	}
	
	public byte[] authenticate(EbicsUser ebicsUser, byte[] digest) throws GeneralSecurityException {
	    Signature			signature;

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

}
