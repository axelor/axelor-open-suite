package com.axelor.apps.account.service.bankorder.file.transfer;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.BankOrder;
import com.axelor.apps.account.db.BankOrderLine;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.bankorder.file.BankOrderFileService;
import com.axelor.apps.account.service.bankorder.file.cfonb.CfonbToolService;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

public class BankOrderFileAFB320Service extends BankOrderFileService  {

	private final Logger log = LoggerFactory.getLogger( getClass() );

	protected String registrationCode;
	protected CfonbToolService cfonbToolService;
	protected PartnerService partnerService;
	protected int sequence = 1;
	protected final int NB_CHAR_PER_LINE = 320;
	protected boolean isMultiCurrency = false;
	protected boolean isMultiDate = false;

	
	@Inject
	public BankOrderFileAFB320Service(BankOrder bankOrder) throws AxelorException  {
		
		super(bankOrder);
		
		this.partnerService = Beans.get(PartnerService.class);
		this.registrationCode = this.partnerService.getSIRENNumber(senderCompany.getPartner());  // Add it on company
		this.cfonbToolService = Beans.get(CfonbToolService.class);
		fileExtension = FILE_EXTENSION_TXT;

	}
	
	
	/**
	 * Method to create an XML file for SEPA transfer pain.001.001.02
	 * 
	 * @throws AxelorException
	 * @throws DatatypeConfigurationException
	 * @throws JAXBException
	 * @throws IOException
	 */
	@Override
	public File generateFile() throws JAXBException, IOException, AxelorException, DatatypeConfigurationException  {

		List<String> records = Lists.newArrayList();
		
		records.add(this.createSenderRecord());
		
		for(BankOrderLine bankOrderLine : bankOrderLineList)  {

			records.add(this.createDetailRecord(bankOrderLine));
			
//			if(!isChequePayment)  {  // TODO option sur les lignes d'ordres bancaires (case à cocher)
				records.add(this.createDependentReceiverBankRecord(bankOrderLine));
//			}
			if(!Strings.isNullOrEmpty(bankOrderLine.getReceiverReference()))  {
				records.add(this.createOptionnalFurtherInformationRecord(bankOrderLine));
			}
		}
		
		records.add(this.createTotalRecord());
		
		fileToCreate = records;

		return super.generateFile();
	}

	
	/**
	 * Method to create a sender record for international transfer AFB320
	 * @param company
	 * @param dateTime
	 * @return
	 * @throws AxelorException
	 */
	private String createSenderRecord() throws AxelorException  {

		try  {
			// Zone 1 : Code enregistrement
			String senderRecord = cfonbToolService.createZone("1", "03", cfonbToolService.STATUS_MANDATORY, cfonbToolService.FORMAT_NUMERIC, 2);
			
			// Zone 2 : Code opération
			senderRecord += cfonbToolService.createZone("2", "RF", cfonbToolService.STATUS_MANDATORY, cfonbToolService.FORMAT_ALPHA_NUMERIC, 2);
			
			// Zone 3 : Numéro séquentiel
			senderRecord += cfonbToolService.createZone("3", Integer.toString(sequence), cfonbToolService.STATUS_MANDATORY, cfonbToolService.FORMAT_NUMERIC, 6);
			
			// Zone 4 : Date de création
			senderRecord += cfonbToolService.createZone("4", this.validationDateTime.toString("yyyyMMdd"), cfonbToolService.STATUS_MANDATORY, cfonbToolService.FORMAT_NUMERIC, 8);
			
			// Zone 5 : Raison sociale émetteur
			senderRecord += cfonbToolService.createZone("5", senderCompany.getName(), cfonbToolService.STATUS_MANDATORY, cfonbToolService.FORMAT_ALPHA_NUMERIC, 35); 
			
			// Zone 6 : Adresse de l'émetteur
			senderRecord += cfonbToolService.createZone("6", senderCompany.getName(), cfonbToolService.STATUS_OPTIONAL, cfonbToolService.FORMAT_ALPHA_NUMERIC, 3*35);  //TODO check if not null
			
			// Zone 7 : N° SIRET de l'émetteur
			senderRecord += cfonbToolService.createZone("7", registrationCode, cfonbToolService.STATUS_DEPENDENT, cfonbToolService.FORMAT_ALPHA_NUMERIC, 14);
			
			// Zone 8 : Référence remise 
			senderRecord += cfonbToolService.createZone("8", bankOrderSeq, cfonbToolService.STATUS_MANDATORY, cfonbToolService.FORMAT_ALPHA_NUMERIC, 16);
			
			// Zone 9 : Code BIC banque d'exécution
			senderRecord += cfonbToolService.createZone("9", "", cfonbToolService.STATUS_OPTIONAL, cfonbToolService.FORMAT_ALPHA_NUMERIC, 11);
			
			// Zone 10 : Type identifiant du compte à débiter à la banque d'éxécution ("1" : IBAN, "2" : Identifiant national, "0" : Autre )
			senderRecord += cfonbToolService.createZone("10", "1", cfonbToolService.STATUS_MANDATORY, cfonbToolService.FORMAT_NUMERIC, 1);
			
			// Zone 11 : Identifiant du compte à débiter à la banque d'éxécution 
			senderRecord += cfonbToolService.createZone("11", senderBankDetails.getIban(), cfonbToolService.STATUS_MANDATORY, cfonbToolService.FORMAT_ALPHA_NUMERIC, 34);
			
			// Zone 12 : Code devise du compte à débiter à la banque d'éxécution 
			senderRecord += cfonbToolService.createZone("12", senderCompany.getCurrency().getCode(), cfonbToolService.STATUS_MANDATORY, cfonbToolService.FORMAT_ALPHA_NUMERIC, 3);
			
			// Zone 13 : Identification du contrat/client 
			senderRecord += cfonbToolService.createZone("13", "", cfonbToolService.STATUS_OPTIONAL, cfonbToolService.FORMAT_ALPHA_NUMERIC, 16); // TODO
			
			// Zone 14 : Type identifiant du compte émetteur ("1" : IBAN, "2" : Identifiant national, "0" : Autre )
			senderRecord += cfonbToolService.createZone("14", "1", cfonbToolService.STATUS_DEPENDENT, cfonbToolService.FORMAT_ALPHA_NUMERIC, 1);
			
			// Zone 15 : Identifiant du compte émetteur 
			senderRecord += cfonbToolService.createZone("15", senderBankDetails.getIban(), cfonbToolService.STATUS_DEPENDENT, cfonbToolService.FORMAT_ALPHA_NUMERIC, 34);
			
			// Zone 16 : Code devise du compte émetteur (Norme ISO)
			senderRecord += cfonbToolService.createZone("16", senderCompany.getCurrency().getCode(), cfonbToolService.STATUS_DEPENDENT, cfonbToolService.FORMAT_ALPHA_NUMERIC, 3);
			
			// Zone 17-1 : Zone non utilisée 
			senderRecord += cfonbToolService.createZone("17-1", "", cfonbToolService.STATUS_NOT_USED, cfonbToolService.FORMAT_ALPHA_NUMERIC, 4);
			
			// Zone 17-2 : Zone non utilisée 
			senderRecord += cfonbToolService.createZone("17-2", "", cfonbToolService.STATUS_NOT_USED, cfonbToolService.FORMAT_ALPHA_NUMERIC, 1);
			
			// Zone 17-3 : Qualifiant de la date ("203" (date d'exécution demandée) valeur par défaut, "227" soumis à accord contractuel avec la banque)
			senderRecord += cfonbToolService.createZone("17-3", paymentMode.getBankOrderFileFormat().getQualifyingOfDate(), cfonbToolService.STATUS_OPTIONAL, cfonbToolService.FORMAT_ALPHA_NUMERIC, 3);
			
			// Zone 17-4 : Zone réservée 
			senderRecord += cfonbToolService.createZone("17-4", "", cfonbToolService.STATUS_NOT_USED, cfonbToolService.FORMAT_ALPHA_NUMERIC, 8);
			
			// Zone 18 : Zone non utilisée 
			senderRecord += cfonbToolService.createZone("18", "", cfonbToolService.STATUS_NOT_USED, cfonbToolService.FORMAT_ALPHA_NUMERIC, 1);
			
			// Zone 19 : Indice type de remises :
			// "1" : mono date et mono devise : La date et la devise sont prises dans l'enregistrement "En-tête".
			// "2" : mono date et multi devises : La date est prise dans l'enregistrement "En-tête" et la "devise" dans les enregistrements "Détail de l’opération".
			// "3" : multi dates et mono devise : la date est prise dans les enregistrements "Détail de l’opération"et la devise dans l'enregistrement "En-tête".
			// "4" : multi dates et multi devises : la date et la devise sont prises dans les enregistrements "Détail de l’opération".
			// NB : La valeur par défaut est "1". La possibilité d'utiliser les autres valeurs doit être vérifiée auprès de la banque d'acheminement. 
			String zone19 = cfonbToolService.createZone("19", "1", cfonbToolService.STATUS_OPTIONAL, cfonbToolService.FORMAT_ALPHA_NUMERIC, 1);
			senderRecord += zone19;
			
			// Zone 20 : Date :
			// Cette donnée est obligatoire pour les remises mono-date (zone 19 de l'"Entête" = "1" ou "2"), pour les autres remises, elle ne doit pas être renseignée. 
			String zone20 = "";	
			if(zone19.equals("1") || zone19.equals("2"))  {
				zone20 = bankOrderDate.toString("yyyyMMdd");
			}
			else  {  isMultiDate = true;  }
			senderRecord += cfonbToolService.createZone("20", zone20, cfonbToolService.STATUS_DEPENDENT, cfonbToolService.FORMAT_NUMERIC, 8);
	
			// Zone 21 : Code devise des ordres de paiements 
			// Norme ISO :
			// Cette donnée est obligatoire pour les remises mono-devise (zone 19 de l'"Entête" = "1" ou "3"), pour les autres remises, elle ne doit pas être renseignée. 
			String zone21 = "";
			if(zone19.equals("1") || zone19.equals("3"))  {
				zone21 = currency.getCode();												
			}
			else  {  isMultiCurrency = true;  }
			senderRecord += cfonbToolService.createZone("21", zone21, cfonbToolService.STATUS_DEPENDENT, cfonbToolService.FORMAT_ALPHA_NUMERIC, 3);
	
			cfonbToolService.toUpperCase(senderRecord);
			
			cfonbToolService.testLength(senderRecord, NB_CHAR_PER_LINE);
			
			return senderRecord;
		
		} catch (AxelorException e) {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.BANK_ORDER_WRONG_SENDER_RECORD), bankOrderSeq), e, IException.MISSING_FIELD);
		}
	}
	
	
	/**
	 * Method to create a recipient record for international transfer AFB320
	 * @param company
	 * @param dateTime
	 * @return
	 * @throws AxelorException
	 */
	private String createDetailRecord(BankOrderLine bankOrderLine) throws AxelorException   {
 
		try {
			// Zone 1 : Code enregistrement
			String detailRecord = cfonbToolService.createZone("1", "04", cfonbToolService.STATUS_MANDATORY, cfonbToolService.FORMAT_NUMERIC, 2);
		
			// Zone 2 : Code opération
			detailRecord += cfonbToolService.createZone("2", "RF", cfonbToolService.STATUS_MANDATORY, cfonbToolService.FORMAT_ALPHA_NUMERIC, 2);
			
			// Zone 3 : Numéro séquentiel
			detailRecord += cfonbToolService.createZone("3", Integer.toString(sequence), cfonbToolService.STATUS_MANDATORY, cfonbToolService.FORMAT_NUMERIC, 6);
			
			// Zone 4 : Type identifiant du compte du bénéficiaire ("1" : IBAN, "2" : Identifiant national, "0" : Autre )
			detailRecord += cfonbToolService.createZone("4", "1", cfonbToolService.STATUS_DEPENDENT, cfonbToolService.FORMAT_ALPHA_NUMERIC, 1);
			
			// Zone 5 : Identifiant du compte du bénéficiaire
			detailRecord += cfonbToolService.createZone("5", bankOrderLine.getReceiverBankDetails().getIban(), cfonbToolService.STATUS_DEPENDENT, cfonbToolService.FORMAT_ALPHA_NUMERIC, 34);  
			
			// Zone 6 : Nom du bénéficiaire
			detailRecord += cfonbToolService.createZone("6", bankOrderLine.getPartner().getFullName(), cfonbToolService.STATUS_MANDATORY, cfonbToolService.FORMAT_ALPHA_NUMERIC, 35); 
			
			// Zone 7 : Adresse du bénéficiaire (Obligatoire si mode de règlement par chèque (zone 18 = "1" ou "2") 
			// Si le nom du bénéficiaire contient plus de 35 caractères, utiliser le début de la première zone pour le compléter et le reste de cette zone pour indiquer le début de l'adresse)
			detailRecord += cfonbToolService.createZone("7", "", cfonbToolService.STATUS_DEPENDENT, cfonbToolService.FORMAT_ALPHA_NUMERIC, 3*35); //TODO Prendre l'adresse de facturation (et la recopier sur les lignes d'ordres bancaires)
			
			// Zone 8 : Identification nationale du bénéficiaire (Cette zone n'est pas utilisée)
			detailRecord += cfonbToolService.createZone("8", "", cfonbToolService.STATUS_OPTIONAL, cfonbToolService.FORMAT_ALPHA_NUMERIC, 17);
			
			// Zone 9 : Code pays du bénéficiaire (Norme ISO)
			String countryCode = partnerService.getDefaultAddress(bankOrderLine.getPartner()).getAddressL7Country().getCode();
			detailRecord += cfonbToolService.createZone("9", countryCode, cfonbToolService.STATUS_MANDATORY, cfonbToolService.FORMAT_ALPHA_NUMERIC, 2);
			
			// Zone 10 : Référence de l'opération 
			detailRecord += cfonbToolService.createZone("10", bankOrderLine.getSequence(), cfonbToolService.STATUS_MANDATORY, cfonbToolService.FORMAT_ALPHA_NUMERIC, 16);
			
			// Zone 11 : Qualifiant du montant de l'ordre ("T" : Montant exprimé dans la devise du transfert)
			detailRecord += cfonbToolService.createZone("11", "T", cfonbToolService.STATUS_MANDATORY, cfonbToolService.FORMAT_ALPHA_NUMERIC, 1);
			
			// Zone 12 : Zone réservée 
			detailRecord += cfonbToolService.createZone("12", "", cfonbToolService.STATUS_NOT_USED, cfonbToolService.FORMAT_ALPHA_NUMERIC, 4);
			
			// Zone 13 : Montant de l'ordre (Le montant comporte le nombre de décimales indiqué dans la zone "Nombre de décimales" du même enregistrement)
			detailRecord += cfonbToolService.createZone("13", cfonbToolService.normalizeNumber(bankOrderLine.getAmount()), cfonbToolService.STATUS_MANDATORY, cfonbToolService.FORMAT_NUMERIC, 14);
			
			// Zone 14 : Nombre de décimales 
			detailRecord += cfonbToolService.createZone("14", "2", cfonbToolService.STATUS_MANDATORY, cfonbToolService.FORMAT_NUMERIC, 1);
			
			// Zone 15 : Zone réservée 
			detailRecord += cfonbToolService.createZone("15", "", cfonbToolService.STATUS_NOT_USED, cfonbToolService.FORMAT_ALPHA_NUMERIC, 1);
			
			// Zone 16 : Code motif économique (3 caract. numériques ou valeur "NNN" )
			detailRecord += cfonbToolService.createZone("16", "NNN", cfonbToolService.STATUS_OPTIONAL, cfonbToolService.FORMAT_ALPHA_NUMERIC, 3);
			
			// Zone 17 : Zone réservée  
			detailRecord += cfonbToolService.createZone("17", "", cfonbToolService.STATUS_NOT_USED, cfonbToolService.FORMAT_ALPHA_NUMERIC, 2);
			
			// Zone 18 : Mode de règlement ("0" = Virement ou autre sauf chèque, "1" ou "2" = par chèque)
			detailRecord += cfonbToolService.createZone("18", "0", cfonbToolService.STATUS_MANDATORY, cfonbToolService.FORMAT_ALPHA_NUMERIC, 1); // TODO option sur les lignes d'ordres bancaires (case à cocher)
			
			// Zone 19 : Code imputation des frais ("13" = Bénéficiaire (BEN), "14" = Emetteur et Bénéficiaire (SHA), "15" = Emetteur (OUR))
			detailRecord += cfonbToolService.createZone("19", "15", cfonbToolService.STATUS_MANDATORY, cfonbToolService.FORMAT_NUMERIC, 2); //TODO option
			
			// Zone 23 : Zone réservée 
			detailRecord += cfonbToolService.createZone("23", "", cfonbToolService.STATUS_NOT_USED, cfonbToolService.FORMAT_ALPHA_NUMERIC, 57);
			
			// Zone 24-1 : Qualifiant de la date ("203" (date d'exécution demandée))
			detailRecord += cfonbToolService.createZone("24-1", paymentMode.getBankOrderFileFormat().getQualifyingOfDate(), cfonbToolService.STATUS_OPTIONAL, cfonbToolService.FORMAT_ALPHA_NUMERIC, 3);
			
			// Zone 24-2 : Date ( Cette donnée est obligatoire pour les remises multi dates (zone 19 de l'"Entête" = "3" ou "4"), pour les autres remises, elle ne doit pas être renseignée.)
			String zone24_2 = "";
			if(isMultiDate)  {
	//			zone24_2 = bankOrderLine.getDate();  //TODO
			}
			detailRecord += cfonbToolService.createZone("24-2", zone24_2, cfonbToolService.STATUS_DEPENDENT, cfonbToolService.FORMAT_NUMERIC, 8);
			
			// Zone 25 : Code devise du transfert (Cette donnée est obligatoire pour les remises multi devise (zone 19 de l'"Entête" = "2" ou "4"), pour les autres remises, elle ne doit pas être renseignée.
			String zone25 = "";
			if(isMultiCurrency)  {  
	//			zone25 = bankOrderLine.getCurrency(); //TODO
			}
			detailRecord += cfonbToolService.createZone("25", zone25, cfonbToolService.STATUS_DEPENDENT, cfonbToolService.FORMAT_ALPHA_NUMERIC, 3);
	
			cfonbToolService.toUpperCase(detailRecord);
			
			cfonbToolService.testLength(detailRecord, NB_CHAR_PER_LINE);
			
			return detailRecord;
		
		} catch (AxelorException e) {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.BANK_ORDER_WRONG_MAIN_DETAIL_RECORD), bankOrderLine.getSequence()), e, IException.MISSING_FIELD);
		}
	}
	
	/**
	 * Method to create a dependent receiver bank record for international transfer AFB320
	 * @param company
	 * @param dateTime
	 * @return
	 * @throws AxelorException
	 */
	private String createDependentReceiverBankRecord(BankOrderLine bankOrderLine) throws AxelorException  {

		try {
		
			BankDetails receiverBankDetails = bankOrderLine.getReceiverBankDetails();
			
			// Zone 1 : Code enregistrement
			String totalRecord = cfonbToolService.createZone("1", "05", cfonbToolService.STATUS_MANDATORY, cfonbToolService.FORMAT_NUMERIC, 2);
			
			// Zone 2 : Code opération
			totalRecord += cfonbToolService.createZone("2", "RF", cfonbToolService.STATUS_MANDATORY, cfonbToolService.FORMAT_ALPHA_NUMERIC, 2);
			
			// Zone 3 : Numéro séquentiel 
			totalRecord += cfonbToolService.createZone("3", Integer.toString(sequence), cfonbToolService.STATUS_MANDATORY, cfonbToolService.FORMAT_NUMERIC, 6);
			
			// Zone 4 : Nom de la banque du bénéficiaire (A ne renseigner que si le code BIC de la banque du bénéficiaire est absent. 
			// Si cette zone est renseignée ainsi que le code BIC, elle est ignorée par la banque sauf en cas d'anomalie sur le code BIC.)
			totalRecord += cfonbToolService.createZone("4", receiverBankDetails.getBankAddress(), cfonbToolService.STATUS_DEPENDENT, cfonbToolService.FORMAT_ALPHA_NUMERIC, 35);
			
			// Zone 5 : Localisation de l'agence (Si le nom de la banque contient plus de 35 caractères, utiliser le début de la première zone 
			// pour le compléter et le reste de cette zone pour indiquer le début de l'adresse)
			totalRecord += cfonbToolService.createZone("5", "", cfonbToolService.STATUS_DEPENDENT, cfonbToolService.FORMAT_ALPHA_NUMERIC, 3*35);  //TODO update bank details model
			
			// Zone 6 : Code BIC de la banque du bénéficiaire (Si ce code est renseigné, c'est lui qui est utilisé pour identifier la banque du bénéficiaire. 
			// C'est cette option qui est préconisée pour identifier la banque du bénéficiaire. )
			totalRecord += cfonbToolService.createZone("6", receiverBankDetails.getBic(), cfonbToolService.STATUS_OPTIONAL, cfonbToolService.FORMAT_ALPHA_NUMERIC, 11);
			
			// Zone 7 : Code pays de la banque du bénéficiaire (Norme ISO.)
			totalRecord += cfonbToolService.createZone("7", "", cfonbToolService.STATUS_OPTIONAL, cfonbToolService.FORMAT_ALPHA_NUMERIC, 2);  //TODO update bank details model
			
			// Zone 8 : Zone réservée 
			totalRecord += cfonbToolService.createZone("8", "", cfonbToolService.STATUS_NOT_USED, cfonbToolService.FORMAT_ALPHA_NUMERIC, 157);
			
			cfonbToolService.toUpperCase(totalRecord);
			
			cfonbToolService.testLength(totalRecord, NB_CHAR_PER_LINE);
			
			return totalRecord;
		
		} catch (AxelorException e) {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.BANK_ORDER_WRONG_BENEFICIARY_BANK_DETAIL_RECORD), bankOrderLine.getSequence()), e.getCause(), IException.MISSING_FIELD);
		}
		
		
	}
	
	
	/**
	 * Method to create an optional further information record for international transfer AFB320
	 * @param company
	 * @param dateTime
	 * @return
	 * @throws AxelorException
	 */
	private String createOptionnalFurtherInformationRecord(BankOrderLine bankOrderLine) throws AxelorException  {

		try {
		
			BankDetails receiverBankDetails = bankOrderLine.getReceiverBankDetails();
			
			// Zone 1 : Code enregistrement
			String totalRecord = cfonbToolService.createZone("1", "07", cfonbToolService.STATUS_MANDATORY, cfonbToolService.FORMAT_NUMERIC, 2);
			
			// Zone 2 : Code opération
			totalRecord += cfonbToolService.createZone("2", "RF", cfonbToolService.STATUS_MANDATORY, cfonbToolService.FORMAT_ALPHA_NUMERIC, 2);
			
			// Zone 3 : Numéro séquentiel 
			totalRecord += cfonbToolService.createZone("3", Integer.toString(sequence), cfonbToolService.STATUS_MANDATORY, cfonbToolService.FORMAT_NUMERIC, 6);
			
			// Zone 4 : Motif du règlement :
			// Ces 4 zones de 35 caractères sont à la disposition du donneur d'ordre et à destination du bénéficiaire. Pour faciliter l'identification des références transmises
			// dans ces zones, le donneur d'ordre peut utiliser les mots clé suivant : /INV/, /IPI/, /RFB/, /ROC/ 
			totalRecord += cfonbToolService.createZone("4", this.computePaymentReason(bankOrderLine), cfonbToolService.STATUS_MANDATORY, cfonbToolService.FORMAT_ALPHA_NUMERIC, 4*35);
			
			// Zone 5 : Zone non utilisée 
			totalRecord += cfonbToolService.createZone("5", "", cfonbToolService.STATUS_NOT_USED, cfonbToolService.FORMAT_ALPHA_NUMERIC, 1);  //TODO update bank details model
			
			// Zone 6 : Zone non utilisée 
			totalRecord += cfonbToolService.createZone("6", receiverBankDetails.getBic(), cfonbToolService.STATUS_NOT_USED, cfonbToolService.FORMAT_ALPHA_NUMERIC, 16);
			
			// Zone 7 : Zone non utilisée 
			totalRecord += cfonbToolService.createZone("7", "", cfonbToolService.STATUS_NOT_USED, cfonbToolService.FORMAT_ALPHA_NUMERIC, 8);  
			
			// Zone 8 : Zone non utilisée 
			totalRecord += cfonbToolService.createZone("8", "", cfonbToolService.STATUS_NOT_USED, cfonbToolService.FORMAT_ALPHA_NUMERIC, 12);
			
			// Zone 9 : Instructions particulières 
			// Ces instructions particulières sont destinées à la banque d'exécution et éventuellement à la banque du bénéficiaire.
			// Elles sont soumises à accord contractuel. Lorsqu'elles sont utilisées elles doivent respecter les règles d'utilisations ci-dessous (2)
			totalRecord += cfonbToolService.createZone("9", this.computeSpecialInstructions(bankOrderLine), cfonbToolService.STATUS_OPTIONAL, cfonbToolService.FORMAT_ALPHA_NUMERIC, 3*35);
			
			// Zone 10 : Zone réservée 
			totalRecord += cfonbToolService.createZone("8", "", cfonbToolService.STATUS_NOT_USED, cfonbToolService.FORMAT_ALPHA_NUMERIC, 28);

			cfonbToolService.toUpperCase(totalRecord);
			
			cfonbToolService.testLength(totalRecord, NB_CHAR_PER_LINE);
			
			return totalRecord;
		
		} catch (AxelorException e) {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.BANK_ORDER_WRONG_FURTHER_INFORMATION_DETAIL_RECORD), bankOrderLine.getSequence()), e, IException.MISSING_FIELD);
		}
		
		
	}
	
	
	/**
	 * 
	 *	Ces 4 zones de 35 caractères sont à la disposition du donneur d'ordre et à destination du bénéficiaire. Pour faciliter l'identification des références transmises
	 *	dans ces zones, le donneur d'ordre peut utiliser les mots clé suivant : /INV/, /IPI/, /RFB/, /ROC/ 
	 *	
	 *	
	 *	Mot clé |  Signification   													|  Suivi de :
	 *	---------------------------------------------------------------------------------------------------------------------------
	 *	INV 	|	Facture (Invoice) 												|	Date
	 *			|																	|	Référence
	 *			|																	|	Détail éventuel de la facture
	 *			|																	|	(ces trois zones séparées par des blancs)
	 *	IPI 	|	International Payment Instruction (pied de facture normalisé) 	|	Référence de 20 caractères maximum
	 *	RFB 	|	Référence pour le bénéficiaire 									|	Référence de 20 caractères maximum
	 *	ROC 	|	Référence du donneur d'ordre 									|	Référence dans la limite de la longueur disponible
	 *		
	 *	Les mots clé sont placés entre deux "/". Si un mot clé n'est pas placé en début d'une des zones de 35 caractères il	doit être précédé par un double "/" ("//").
	 *	Exemple 1 :
	 *		/INV/20040423 1234567 36 BOITES DE GATEAUX
	 *	 	/RFB/AKC2847312
	 *	
	 *	Exemple 2 :
	 *		/INV/20040423 1234567 36 BOITES DE GATEAUX//RFB/AKC2847312 
	 * @param bankOrderLine
	 * @return
	 */
	public String computePaymentReason(BankOrderLine bankOrderLine)  {
		
		String paymentReason = "";
		
		if(!Strings.isNullOrEmpty(bankOrderLine.getReceiverReference()))  {
			paymentReason += bankOrderLine.getReceiverReference();
		}
		if(!Strings.isNullOrEmpty(bankOrderLine.getReceiverLabel()))  {
			if(!Strings.isNullOrEmpty(paymentReason))  {  paymentReason += "/";  }
			paymentReason += bankOrderLine.getReceiverLabel();
		}
		return paymentReason;
		
	}
	
	
	public String computeSpecialInstructions(BankOrderLine bankOrderLine)  {
		
		String specialInstructions = "";
		
		if(!Strings.isNullOrEmpty(bankOrderLine.getReceiverReference()))  {
			specialInstructions += bankOrderLine.getSpecialInstructions();
		}
		return specialInstructions;
		
	}
	
	
	/**
	 * Method to create a total record for internationnal transfer AFB320
	 * @param company
	 * @param dateTime
	 * @return
	 * @throws AxelorException
	 */
	private String createTotalRecord() throws AxelorException  {

		try  {
			// Zone 1 : Code enregistrement
			String totalRecord = cfonbToolService.createZone("1", "08", cfonbToolService.STATUS_MANDATORY, cfonbToolService.FORMAT_NUMERIC, 2);
			
			// Zone 2 : Code opération
			totalRecord += cfonbToolService.createZone("2", "RF", cfonbToolService.STATUS_MANDATORY, cfonbToolService.FORMAT_ALPHA_NUMERIC, 2);
			
			// Zone 3 : Numéro séquentiel 
			totalRecord += cfonbToolService.createZone("3", Integer.toString(sequence), cfonbToolService.STATUS_MANDATORY, cfonbToolService.FORMAT_NUMERIC, 6);
			
			// Zone 4 : Date de création
			totalRecord += cfonbToolService.createZone("4", this.validationDateTime.toString("yyyyMMdd"), cfonbToolService.STATUS_MANDATORY, cfonbToolService.FORMAT_NUMERIC, 8);
			
			// Zone 5 : Zone réservée 
			totalRecord += cfonbToolService.createZone("5", "", cfonbToolService.STATUS_NOT_USED, cfonbToolService.FORMAT_ALPHA_NUMERIC, 4*35);
			
			// Zone 6 : N° SIRET de l'émetteur
			totalRecord += cfonbToolService.createZone("6", registrationCode, cfonbToolService.STATUS_DEPENDENT, cfonbToolService.FORMAT_NUMERIC, 14);
			
			// Zone 7 : Référence remise 
			totalRecord += cfonbToolService.createZone("7", bankOrderSeq, cfonbToolService.STATUS_MANDATORY, cfonbToolService.FORMAT_ALPHA_NUMERIC, 16);
			
			// Zone 8 : Zone réservée 
			totalRecord += cfonbToolService.createZone("8", "", cfonbToolService.STATUS_NOT_USED, cfonbToolService.FORMAT_ALPHA_NUMERIC, 11);
			
			// Zone 9 : Type identifiant du compte à débiter à la banque d'éxécution ("1" : IBAN, "2" : Identifiant national, "0" : Autre )
			totalRecord += cfonbToolService.createZone("9", "1", cfonbToolService.STATUS_MANDATORY, cfonbToolService.FORMAT_NUMERIC, 1);
			
			// Zone 10 : Identifiant du compte à débiter à la banque d'éxécution 
			totalRecord += cfonbToolService.createZone("10", senderBankDetails.getIban(), cfonbToolService.STATUS_MANDATORY, cfonbToolService.FORMAT_ALPHA_NUMERIC, 34);
			
			// Zone 11 : Code devise du compte à débiter à la banque d'éxécution 
			totalRecord += cfonbToolService.createZone("11", senderCompany.getCurrency().getCode(), cfonbToolService.STATUS_MANDATORY, cfonbToolService.FORMAT_ALPHA_NUMERIC, 3);
			
			// Zone 12 : Identification du contrat/client 
			totalRecord += cfonbToolService.createZone("12", "", cfonbToolService.STATUS_OPTIONAL, cfonbToolService.FORMAT_ALPHA_NUMERIC, 16);  // TODO
			
			// Zone 13 : TOTAL DE CONTROLE 
			totalRecord += cfonbToolService.createZone("13", cfonbToolService.normalizeNumber(totalAmount), cfonbToolService.STATUS_MANDATORY, cfonbToolService.FORMAT_NUMERIC, 18);
			
			// Zone 14 : Zone réservée 
			totalRecord += cfonbToolService.createZone("14", "", cfonbToolService.STATUS_NOT_USED, cfonbToolService.FORMAT_ALPHA_NUMERIC, 49);
	
			cfonbToolService.toUpperCase(totalRecord);
			
			cfonbToolService.testLength(totalRecord, NB_CHAR_PER_LINE);
			
			return totalRecord;
			
		} catch (AxelorException e) {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.BANK_ORDER_WRONG_TOTAL_RECORD), bankOrderSeq), e, IException.MISSING_FIELD);
		}
	}
}
