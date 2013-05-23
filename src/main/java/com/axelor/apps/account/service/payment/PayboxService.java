package com.axelor.apps.account.service.payment;


import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.List;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.codec.Base64;
import org.bouncycastle.util.io.pem.PemReader;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.account.service.AccountCustomerService;
import com.axelor.apps.account.service.debtrecovery.ReminderService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.account.db.PaymentVoucher;
import com.axelor.apps.tool.StringTool;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class PayboxService {
	
	private static final Logger LOG = LoggerFactory.getLogger(PayboxService.class);
	
	@Inject
	private ReminderService rs;
	
	@Inject
	private AccountCustomerService acs;
	
	private final String CHARSET = "UTF-8";
	
	private final String HASH_ENCRYPTION_ALGORITHM = "SHA1withRSA";
	
	private final String ENCRYPTION_ALGORITHM = "RSA";
		
	/**
	 * Procédure permettant de réaliser un paiement avec Paybox
	 * @param paymentVoucher
	 * 				Une saisie paiement
	 * @throws AxelorException
	 * @throws UnsupportedEncodingException 
	 */
	public String paybox(PaymentVoucher paymentVoucher) throws AxelorException, UnsupportedEncodingException  {
		
		this.checkPayboxPaymentVoucherFields(paymentVoucher);
		
		Company company = paymentVoucher.getCompany();
		this.checkPayboxPaymentVoucherFields(company);
		
		BigDecimal paidAmount = paymentVoucher.getPaidAmount();
		Partner payerPartner = paymentVoucher.getPartner();
//		this.checkPayboxPartnerFields(payerPartner);
		this.checkPaidAmount(payerPartner, company, paidAmount);
		this.checkPaidAmount(paymentVoucher);
		
		String payboxUrl = company.getPayboxUrl();
		String pbxSite = company.getPayboxSite();
		String pbxRang = company.getPayboxRang();
		String pbxDevise = company.getPayboxDevise();
		String pbxTotal = paidAmount.setScale(2).toString().replace(".","");
		String pbxCmd = paymentVoucher.getRef();  // Identifiant de la saisie paiement
		String pbxPorteur = this.getPartnerEmail(paymentVoucher);
		String pbxRetour = company.getPayboxRetour();
//		String pbxEffectue = this.encodeUrl(this.replaceVariableInUrl(company.getPayboxRetourUrlEffectue(), paymentVoucher));
		String pbxEffectue = this.replaceVariableInUrl(company.getPayboxRetourUrlEffectue(), paymentVoucher);
		String pbxRefuse = this.replaceVariableInUrl(company.getPayboxRetourUrlRefuse(), paymentVoucher);
		String pbxAnnule = this.replaceVariableInUrl(company.getPayboxRetourUrlAnnule(), paymentVoucher);
		String pbxIdentifiant = company.getPayboxIdentifiant();
		String pbxHash = company.getPayboxHashSelect();
		String pbxHmac = company.getPayboxHmac();
		
		//Date à laquelle l'empreinte HMAC a été calculée (format ISO8601)
		String pbxTime = ISODateTimeFormat.dateHourMinuteSecond().print(new DateTime());
		
		// Permet de restreindre les modes de paiement
		String pbxTypepaiement = "CARTE";
		String pbxTypecarte = "CB";
		
		String message = this.buildMessage(pbxSite, pbxRang, pbxIdentifiant, pbxTotal, pbxDevise, pbxCmd, pbxPorteur, 
				pbxRetour, pbxEffectue, pbxRefuse, pbxAnnule, pbxHash, pbxTime, pbxTypepaiement, pbxTypecarte);
				
		
		LOG.debug("Message : {}",message);
		
		String messageHmac = this.getHmacSignature(message, pbxHmac, pbxHash);
		
		LOG.debug("Message HMAC : {}",messageHmac);

		String messageEncode = this.buildMessage(
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
		
		
		String url = payboxUrl + messageEncode + "&PBX_HMAC="+ messageHmac;
		
		LOG.debug("Url : {}",url);
		
		return url;		
	}
	
	
	
	public String buildMessage(String pbxSite, String pbxRang,String pbxIdentifiant,String pbxTotal,String pbxDevise,String pbxCmd,String pbxPorteur,String pbxRetour,
			String pbxEffectue,String pbxRefuse,String pbxAnnule,String pbxHash,String pbxTime,String pbxTypepaiement,String pbxTypecarte)  {
		return String.format("PBX_SITE=%s&PBX_RANG=%s&PBX_IDENTIFIANT=%s&PBX_TOTAL=%s&PBX_DEVISE=%s" +
				"&PBX_CMD=%s&PBX_PORTEUR=%s&PBX_RETOUR=%s&PBX_EFFECTUE=%s&PBX_REFUSE=%s&PBX_ANNULE=%s&PBX_HASH=%s&PBX_TIME=%s&PBX_TYPEPAIEMENT=%s&PBX_TYPECARTE=%s",
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
	 * @param url
	 * @param paymentVoucher
	 * @return
	 */
	public String replaceVariableInUrl(String url, PaymentVoucher paymentVoucher)  {
		
		return url.replaceAll("%idPV", paymentVoucher.getId().toString());
		
	}
	
	/**
	 * Fonction convertissant l'url en url encodé
	 * @param url
	 * @return
	 */
	public String encodeUrl(String url)  {
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
	
	
	public String getPartnerEmail(PaymentVoucher paymentVoucher) throws AxelorException  {
		
		Partner partner = paymentVoucher.getPartner();
		Company company = paymentVoucher.getCompany();
		
		if(partner.getEmail() != null && !partner.getEmail().isEmpty())  {
			return partner.getEmail();
		}
		else if(paymentVoucher.getEmail() != null && !paymentVoucher.getEmail().isEmpty())  {
			return paymentVoucher.getEmail();
		}
		else  {
			this.checkPayboxDefaultEmail(company);
			return company.getPayboxDefaultEmail();
		}
	}
	
	
	/**
	 * Procédure permettant de vérifier le paramètrage propre à Paybox dans la société
	 * @param company
	 * @throws AxelorException
	 */
	public void checkPayboxPaymentVoucherFields(Company company) throws AxelorException  {
		if (company.getPayboxSite() == null || company.getPayboxSite().isEmpty())  {
			throw new AxelorException(String.format("%s :\n Veuillez paramétrer un Numéro de site dans la société %s.", 
					GeneralService.getExceptionAccountingMsg(), company.getName()), IException.CONFIGURATION_ERROR);
		}
		if (company.getPayboxRang() == null || company.getPayboxRang().isEmpty())  {
			throw new AxelorException(String.format("%s :\n Veuillez paramétrer un Numéro de rang dans la société %s.", 
					GeneralService.getExceptionAccountingMsg(), company.getName()), IException.CONFIGURATION_ERROR);
		}
		if (company.getPayboxDevise() == null || company.getPayboxDevise().isEmpty())  {
			throw new AxelorException(String.format("%s :\n Veuillez paramétrer une Devise des transactions dans la société %s.", 
					GeneralService.getExceptionAccountingMsg(), company.getName()), IException.CONFIGURATION_ERROR);
		}
		if (company.getPayboxRetour() == null || company.getPayboxRetour().isEmpty())  {
			throw new AxelorException(String.format("%s :\n Veuillez paramétrer une Liste des variables à retourner par Paybox dans la société %s.", 
					GeneralService.getExceptionAccountingMsg(), company.getName()), IException.CONFIGURATION_ERROR);
		}
		if (company.getPayboxRetourUrlEffectue() == null || company.getPayboxRetourUrlEffectue().isEmpty())  {
			throw new AxelorException(String.format("%s :\n Veuillez paramétrer une Url retourner par Paybox une fois le paiement effectué %s.", 
					GeneralService.getExceptionAccountingMsg(), company.getName()), IException.CONFIGURATION_ERROR);
		}
		if (company.getPayboxRetourUrlRefuse() == null || company.getPayboxRetourUrlRefuse().isEmpty())  {
			throw new AxelorException(String.format("%s :\n Veuillez paramétrer une Url retourner par Paybox une fois le paiement refusé %s.", 
					GeneralService.getExceptionAccountingMsg(), company.getName()), IException.CONFIGURATION_ERROR);
		}
		if (company.getPayboxRetourUrlAnnule() == null || company.getPayboxRetourUrlAnnule().isEmpty())  {
			throw new AxelorException(String.format("%s :\n Veuillez paramétrer une Url retourner par Paybox une fois le paiement annulé %s.", 
					GeneralService.getExceptionAccountingMsg(), company.getName()), IException.CONFIGURATION_ERROR);
		}
		if (company.getPayboxIdentifiant() == null || company.getPayboxIdentifiant().isEmpty())  {
			throw new AxelorException(String.format("%s :\n Veuillez paramétrer un Identifiant interne dans la société %s.", 
					GeneralService.getExceptionAccountingMsg(), company.getName()), IException.CONFIGURATION_ERROR);
		}
		if (company.getPayboxHashSelect() == null || company.getPayboxHashSelect().isEmpty())  {
			throw new AxelorException(String.format("%s :\n Veuillez selectionner un Type d'algorithme de hachage utilisé lors du calcul de l'empreinte dans la société %s.", 
					GeneralService.getExceptionAccountingMsg(), company.getName()), IException.CONFIGURATION_ERROR);
		}
		if (company.getPayboxHmac() == null || company.getPayboxHmac().isEmpty())  {
			throw new AxelorException(String.format("%s :\n Veuillez paramétrer une Signature calculée avec la clé secrète dans la société %s.", 
					GeneralService.getExceptionAccountingMsg(), company.getName()), IException.CONFIGURATION_ERROR);
		}
		if (company.getPayboxUrl() == null || company.getPayboxUrl().isEmpty())  {
			throw new AxelorException(String.format("%s :\n Veuillez paramétrer une Url de l'environnement dans la société %s.", 
					GeneralService.getExceptionAccountingMsg(), company.getName()), IException.CONFIGURATION_ERROR);
		}
		if (company.getPayboxPublicKeyPath() == null || company.getPayboxPublicKeyPath().isEmpty())  {
			throw new AxelorException(String.format("%s :\n Veuillez paramétrer un Chemin de la clé publique Paybox dans la société %s.", 
					GeneralService.getExceptionAccountingMsg(), company.getName()), IException.CONFIGURATION_ERROR);
		}
	}
	
	
	/**
	 * Procédure permettant de vérifier que les champs de la saisie paiement necessaire à Paybox sont bien remplis
	 * @param paymentVoucher
	 * @throws AxelorException
	 */
	public void checkPayboxPaymentVoucherFields(PaymentVoucher paymentVoucher) throws AxelorException  {
		if (paymentVoucher.getPaidAmount() == null)  {
			throw new AxelorException(String.format("%s :\n Veuillez paramétrer un Montant réglé pour la saisie paiement %s.", 
					GeneralService.getExceptionAccountingMsg(), paymentVoucher.getRef()), IException.CONFIGURATION_ERROR);
		}
	}
	
	
	
	public void checkPayboxDefaultEmail(Company company) throws AxelorException  {
		if (company.getPayboxDefaultEmail() == null || company.getPayboxDefaultEmail().isEmpty())  {
			throw new AxelorException(String.format("%s :\n Veuillez paramétrer un Email de back-office Axelor pour Paybox pour la société %s.", 
					GeneralService.getExceptionAccountingMsg(), company.getName()), IException.CONFIGURATION_ERROR);
		}
	}
	
	/**
	 * Procédure permettant de vérifier que le montant réglé par Paybox n'est pas supérieur au solde du payeur
	 * @param partner
	 * @param paidAmount
	 * @throws AxelorException
	 */
	public void checkPaidAmount(Partner partner, Company company, BigDecimal paidAmount) throws AxelorException  {
		AccountingSituation accountingSituation = acs.getAccountingSituation(partner, company);
			
		BigDecimal partnerBalance = accountingSituation.getBalanceCustAccount();
		
		if(paidAmount.compareTo(partnerBalance) > 0)  {
			throw new AxelorException(String.format("%s :\n Le montant réglé pour la saisie paiement par CB ne doit pas être supérieur au solde du payeur.", 
					GeneralService.getExceptionAccountingMsg()), IException.CONFIGURATION_ERROR);
		}

	}
	
	
	public void checkPaidAmount(PaymentVoucher paymentVoucher) throws AxelorException  {
		
		if(paymentVoucher.getRemainingAmount().compareTo(BigDecimal.ZERO) > 0 )  {
			throw new AxelorException(String.format("%s :\n Attention - Vous ne pouvez pas régler un montant supérieur aux factures selectionnées.", 
					GeneralService.getExceptionAccountingMsg()), IException.INCONSISTENCY);
		}
	

	}
	
	
	/**
	 * Procédure permettant de vérifier que le paramétrage des champs necessaire à Paybox d'un tiers est bien réalisé
	 * @param partner
	 * @throws AxelorException
	 */
	public void checkPayboxPartnerFields(Partner partner) throws AxelorException  {
		if (partner.getEmail() == null || partner.getEmail().isEmpty())  {
			throw new AxelorException(String.format("%s :\n Veuillez paramétrer un Email pour le tiers %s.", 
					GeneralService.getExceptionAccountingMsg(), partner.getName()), IException.CONFIGURATION_ERROR);
		}
	}
	
	
	/**
	 * Fonction calculant la signature HMAC des paramètres
	 * @param data
	 * 			La chaine contenant les paramètres
	 * @param hmacKey
	 * 			La clé HMAC
	 * @param algorithm
	 * 			L'algorithme utilisé (SHA512, ...)
	 * @return
	 * @throws AxelorException 
	 */
	public String getHmacSignature(String data, String hmacKey, String algorithm) throws AxelorException  {
		try {
			
			byte[] bytesKey = DatatypeConverter.parseHexBinary(hmacKey);
			SecretKeySpec secretKey = new SecretKeySpec(bytesKey, "Hmac"+algorithm);
			Mac mac = Mac.getInstance("Hmac"+algorithm);
			mac.init(secretKey);
			
			byte[] macData = mac.doFinal( data.getBytes(this.CHARSET) );
	
//			final byte[] hex = new Hex().encode( macData );
//			return new String( hex, this.CHARSET );
//			LOG.debug("Message HMAC 2 : {}",new String( hex, this.CHARSET ));
			
	        String s =  StringTool.getHexString(macData);
			
			return s.toUpperCase();
			
		} catch (InvalidKeyException e) {
			throw new AxelorException(String.format("%s :\n %s", GeneralService.getExceptionAccountingMsg(),e), IException.INCONSISTENCY);
		} catch (NoSuchAlgorithmException e) {
			throw new AxelorException(String.format("%s :\n %s", GeneralService.getExceptionAccountingMsg(),e), IException.INCONSISTENCY);
		} catch (UnsupportedEncodingException e) {
			throw new AxelorException(String.format("%s :\n %s", GeneralService.getExceptionAccountingMsg(),e), IException.INCONSISTENCY);
		}
	}
	
	
	/**
	 * Méthode permettant d'ajouter une adresse email à un contact
	 * @param contact
	 * 			Un contact
	 * @param email
	 * 			Une adresse email
	 * @param toSaveOk
	 * 			L'adresse email doit-elle être enregistré pour le contact
	 */
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void addPayboxEmail(Partner partner, String email, boolean toSaveOk)  {
		if(toSaveOk)  {
			partner.setEmail(email);
			partner.save();
		}
	}
	
	
	/**
	 * 
	 * @param signature
	 * 			La signture contenu dans l'url
	 * @param varUrl
	 * 			Liste des variables contenu dans l'url, privé de la dernière : la signature
	 * @param company
	 * 			La société
	 * @return
	 * @throws Exception
	 */
	public boolean checkPaybox(String signature, List<String> varUrl, Company company) throws Exception  {

		boolean result =  this.checkPaybox(signature, varUrl, company.getPayboxPublicKeyPath());
		
		LOG.debug("Resultat de la verification de signature : {}",result);
		
		return result;
	}
	

	/**
	 * 
	 * @param signature
	 * 			La signature contenu dans l'url
	 * @param urlParam
	 * 			Liste des paramètres contenus dans l'url, privé du dernier : la signature
	 * @param pubKeyPath
	 * 			Le chemin de la clé publique Paybox
	 * @return
	 * @throws Exception
	 */
	public boolean checkPaybox(String signature, List<String> urlParam, String pubKeyPath) throws Exception {

    	String payboxParams = StringUtils.join(urlParam, "&");
     	LOG.debug("Liste des variables Paybox signées : {}",payboxParams);

//	 		Déjà décodée par le framework
//	     	String decoded = URLDecoder.decode(sign, this.CHARSET);
     	
        byte[] sigBytes = Base64.decode(signature.getBytes(this.CHARSET));

        // lecture de la cle publique
        PublicKey pubKey = this.getPubKey(pubKeyPath);
        
        /** 
         * Dans le cas où le clé est au format .der
         *
         * PublicKey pubKey = this.getPubKeyDer(pubKeyPath);
 		 */	
        
        // verification signature
        return this.verify(payboxParams.getBytes(), sigBytes, this.HASH_ENCRYPTION_ALGORITHM, pubKey);
        
     }
	 
	 
	 
    
    /** Chargement de la cle AU FORMAT pem
     * Alors ajouter la dépendance dans le fichier pom.xml :
     * <dependency>
	 *	  <groupId>org.bouncycastle</groupId>
	 *	  <artifactId>bcprov-jdk15on</artifactId>
	 *	  <version>1.47</version>
	 *	</dependency>
	 *
	 * Ainsi que l'import : import org.bouncycastle.util.io.pem.PemReader;
     * 
     * @param pubKeyFile
     * @return
     * @throws Exception
     */
     private PublicKey getPubKey(String pubKeyPath) throws Exception  {
        
 		PemReader reader = new PemReader(new FileReader(pubKeyPath));
 	
 		byte[] pubKey = reader.readPemObject().getContent();
 		
 		reader.close();
 		
 		KeyFactory keyFactory = KeyFactory.getInstance(this.ENCRYPTION_ALGORITHM);
 		
 		X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(pubKey);
         
 		return keyFactory.generatePublic(pubKeySpec);
 		
     }
    
    
   /** Chargement de la cle AU FORMAT der
     * Utliser la commande suivante pour 'convertir' la clé 'pem' en 'der'
     * openssl rsa -inform PEM -in pubkey.pem -outform DER -pubin -out pubkey.der
     * 
     * @param pubKeyFile
     * @return
     * @throws Exception
     */
    @Deprecated
	private PublicKey getPubKeyDer(String pubKeyPath) throws Exception  {
    	
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
      * @param dataBytes
      * @param sigBytes
      * @param pubKey
      * @return
      * @throws Exception
      */
     private boolean verify( byte[] dataBytes, byte[] sigBytes, String sigAlg, PublicKey pubKey) throws Exception  {
         Signature signature = Signature.getInstance(sigAlg);
         signature.initVerify(pubKey);
         signature.update(dataBytes);
         return signature.verify(sigBytes);
     }
     
     
     
}
