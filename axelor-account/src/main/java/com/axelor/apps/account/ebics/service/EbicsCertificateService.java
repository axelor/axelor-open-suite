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
package com.axelor.apps.account.ebics.service;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.EbicsBank;
import com.axelor.apps.account.db.EbicsCertificate;
import com.axelor.apps.account.db.repo.EbicsCertificateRepository;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;


public class EbicsCertificateService {
	
	private final Logger log = LoggerFactory.getLogger(EbicsCertificateService.class);
	
	@Inject
	private EbicsCertificateRepository certRepo;
	
	public static byte[] getCertificateContent(EbicsBank bank, String type) throws AxelorException {
		 
		 EbicsCertificate cert = getEbicsCertificate(bank, type);
		 
		 if (cert != null) {
			 return cert.getCertificate();
		 }
		
		 if (bank.getUrl() != null && type.equals("ssl")) {
			 return Beans.get(EbicsCertificateService.class).getSSLCertificate(bank);
		 }

		 throw new AxelorException(I18n.get("No bank certificate of type %s found"), IException.CONFIGURATION_ERROR, type);
	}
	
	public static X509Certificate getCertificate(EbicsBank bank, String type) throws AxelorException {
		
		byte[] certificate = getCertificateContent(bank, type);
		ByteArrayInputStream instream = new ByteArrayInputStream(certificate);
		X509Certificate cert;
		try {
			cert = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(instream);
		} catch (CertificateException e) {
			throw new AxelorException(I18n.get("Error in bank certificate of type %s"), IException.CONFIGURATION_ERROR, type);
		}
		
		return cert;
	}
	
	private byte[] getSSLCertificate(EbicsBank bank) throws AxelorException {
		
		try {
			URL url = new URL(bank.getUrl());
			log.debug("Bank url protocol: {}", url.getProtocol());
			log.debug("Bank url host: {}", url.getHost());
			log.debug("Bank url port: {}", url.getPort());
			
			url = new URL(url.getProtocol() + "://" + url.getHost() + ":" + url.getPort());
			
			SSLContext sslCtx = SSLContext.getInstance("TLS");
			sslCtx.init(null, new TrustManager[]{ new X509TrustManager() {
				
				private X509Certificate[] accepted;
				
				@Override
				public void checkClientTrusted(X509Certificate[] arg0, String arg1)
						throws CertificateException {
					
				}
	
				@Override
				public void checkServerTrusted(X509Certificate[] arg0, String arg1)
						throws CertificateException {
					accepted = arg0;
					
				}
	
				@Override
				public X509Certificate[] getAcceptedIssuers() {
					return accepted;
				}
				
			}}, null);
			
			HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
			
			HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
	
				@Override
				public boolean verify(String arg0, SSLSession arg1) {
					return true;
				}
	
			});
			
			connection.setSSLSocketFactory(sslCtx.getSocketFactory());
			log.debug("SSL connection response code: {}", connection.getResponseCode());
			log.debug("SSL connection response message: {}", connection.getResponseMessage());
			
			if (connection.getResponseCode() == 200) {
			    Certificate[] certificates = connection.getServerCertificates();
			    for (int i = 0; i < certificates.length; i++) {
			        Certificate certificate = certificates[i];
			        if (certificate instanceof X509Certificate) {
			        	createCertificate((X509Certificate) certificate, bank, "ssl");
			        	return certificate.getEncoded();
			        }
			    }
			}
	
			connection.disconnect();
			
		} catch(Exception e) {
			e.printStackTrace();
		}

		
		throw new AxelorException(I18n.get("Error in getting ssl certificate"), IException.CONFIGURATION_ERROR);
		
	}
	
	@Transactional
	public void createCertificate(X509Certificate certificate, EbicsBank bank, String type) throws CertificateEncodingException {
		
		EbicsCertificate cert = getEbicsCertificate(bank, type);
		if (cert == null) {
			cert = new EbicsCertificate();
		}
		cert.setEbicsBank(bank);
		cert.setTypeSelect(type);
		cert.setValidFrom(new LocalDate(certificate.getNotBefore()));
		cert.setValidTo(new LocalDate(certificate.getNotAfter()));
		cert.setIssuer(certificate.getIssuerDN().getName());
		cert.setSubject(certificate.getSubjectDN().getName());
		cert.setCertificate(certificate.getEncoded());
		
		certRepo.save(cert);
	}
	
	private static EbicsCertificate getEbicsCertificate(EbicsBank bank, String type) {
		 
		for (EbicsCertificate cert : bank.getEbicsCertificateList()) {
			 if (cert.getTypeSelect().equals(type)) {
				return cert;
			 }
		 }
		
		return null;
		
	}
}
