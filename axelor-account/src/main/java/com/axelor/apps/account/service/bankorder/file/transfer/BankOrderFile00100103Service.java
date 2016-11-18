package com.axelor.apps.account.service.bankorder.file.transfer;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.BankOrder;
import com.axelor.apps.account.db.BankOrderLine;
import com.axelor.apps.account.service.bankorder.file.BankOrderFileService;
import com.axelor.apps.account.xsd.pain_001_001_03.AccountIdentification4Choice;
import com.axelor.apps.account.xsd.pain_001_001_03.ActiveOrHistoricCurrencyAndAmount;
import com.axelor.apps.account.xsd.pain_001_001_03.AmountType3Choice;
import com.axelor.apps.account.xsd.pain_001_001_03.BranchAndFinancialInstitutionIdentification4;
import com.axelor.apps.account.xsd.pain_001_001_03.CashAccount16;
import com.axelor.apps.account.xsd.pain_001_001_03.CreditTransferTransactionInformation10;
import com.axelor.apps.account.xsd.pain_001_001_03.CustomerCreditTransferInitiationV03;
import com.axelor.apps.account.xsd.pain_001_001_03.Document;
import com.axelor.apps.account.xsd.pain_001_001_03.FinancialInstitutionIdentification7;
import com.axelor.apps.account.xsd.pain_001_001_03.GroupHeader32;
import com.axelor.apps.account.xsd.pain_001_001_03.ObjectFactory;
import com.axelor.apps.account.xsd.pain_001_001_03.PartyIdentification32;
import com.axelor.apps.account.xsd.pain_001_001_03.PaymentIdentification1;
import com.axelor.apps.account.xsd.pain_001_001_03.PaymentInstructionInformation3;
import com.axelor.apps.account.xsd.pain_001_001_03.PaymentMethod3Code;
import com.axelor.apps.account.xsd.pain_001_001_03.PaymentTypeInformation19;
import com.axelor.apps.account.xsd.pain_001_001_03.RemittanceInformation5;
import com.axelor.apps.account.xsd.pain_001_001_03.ServiceLevel8Choice;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;

public class BankOrderFile00100103Service extends BankOrderFileService  {

	private final Logger log = LoggerFactory.getLogger( getClass() );

	
	@Inject
	public BankOrderFile00100103Service(BankOrder bankOrder)  {
		
		super(bankOrder);
		
		context = "com.axelor.apps.account.xsd.pain_001_001_03";
		fileExtension = FILE_EXTENSION_XML;

	}
	
	
	/**
	 * Method to create an XML file for SEPA transfer pain.001.001.03
	 * 
	 * @throws AxelorException
	 * @throws DatatypeConfigurationException
	 * @throws JAXBException
	 * @throws IOException
	 */
	@Override
	public File generateFile() throws JAXBException, IOException, AxelorException, DatatypeConfigurationException  {

		DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();

		ObjectFactory factory = new ObjectFactory();

		ServiceLevel8Choice svcLvl = factory.createServiceLevel8Choice();
		svcLvl.setCd("SEPA");

		PaymentTypeInformation19 pmtTpInf = factory.createPaymentTypeInformation19();
		pmtTpInf.setSvcLvl(svcLvl);

		// Payer
		PartyIdentification32 dbtr = factory.createPartyIdentification32();
		dbtr.setNm(senderBankDetails.getOwnerName());

		// IBAN
		AccountIdentification4Choice iban = factory.createAccountIdentification4Choice();
		iban.setIBAN(senderBankDetails.getIban());

		CashAccount16 dbtrAcct = factory.createCashAccount16();
		dbtrAcct.setId(iban);

		// BIC
		FinancialInstitutionIdentification7 finInstnId = factory.createFinancialInstitutionIdentification7();
		finInstnId.setBIC(senderBankDetails.getBic());

		BranchAndFinancialInstitutionIdentification4 dbtrAgt = factory.createBranchAndFinancialInstitutionIdentification4();
		dbtrAgt.setFinInstnId(finInstnId);

		PaymentInstructionInformation3 pmtInf = factory.createPaymentInstructionInformation3();
		pmtInf.setPmtMtd(PaymentMethod3Code.TRF);
		pmtInf.setPmtTpInf(pmtTpInf);
		
		/**
		 * RequestedExecutionDate
		 * Definition : Date at which the initiating party asks the Debtor's Bank to process the payment. This is the
		 * date on which the debtor's account(s) is (are) to be debited.
		 * XML Tag : <ReqdExctnDt>
		 * Occurrences : [1..1]
		 * Format : YYYY-MM-DD
		 * Rules : date is limited to maximum one year in the future. 
		 */
		pmtInf.setReqdExctnDt(datatypeFactory.newXMLGregorianCalendar(bankOrderDate.toString("yyyy-MM-dd")));
		pmtInf.setDbtr(dbtr);
		pmtInf.setDbtrAcct(dbtrAcct);
		pmtInf.setDbtrAgt(dbtrAgt);

		CreditTransferTransactionInformation10 cdtTrfTxInf = null; PaymentIdentification1 pmtId = null;
		AmountType3Choice amt = null; ActiveOrHistoricCurrencyAndAmount instdAmt = null;
		PartyIdentification32 cbtr = null; CashAccount16 cbtrAcct = null;
		BranchAndFinancialInstitutionIdentification4 cbtrAgt = null;
		RemittanceInformation5 rmtInf = null;
		
		for (BankOrderLine bankOrderLine : bankOrderLineList)  { 

			BankDetails receiverBankDetails = bankOrderLine.getReceiverBankDetails();

			// Reference
			pmtId = factory.createPaymentIdentification1();
			pmtId.setEndToEndId(bankOrderLine.getReceiverReference());

			// Amount
			instdAmt = factory.createActiveOrHistoricCurrencyAndAmount();
			instdAmt.setCcy(bankOrderCurrency.getCode());
			instdAmt.setValue(bankOrderLine.getBankOrderAmount());

			amt = factory.createAmountType3Choice();
			amt.setInstdAmt(instdAmt);

			// Receiver
			cbtr = factory.createPartyIdentification32();
			cbtr.setNm(receiverBankDetails.getOwnerName());

			// IBAN
			iban = factory.createAccountIdentification4Choice();
			iban.setIBAN(receiverBankDetails.getIban());

			cbtrAcct = factory.createCashAccount16();
			cbtrAcct.setId(iban);

			// BIC
			finInstnId = factory.createFinancialInstitutionIdentification7();
			finInstnId.setBIC(receiverBankDetails.getBic());

			cbtrAgt = factory.createBranchAndFinancialInstitutionIdentification4();
			cbtrAgt.setFinInstnId(finInstnId);

			rmtInf = factory.createRemittanceInformation5();

			rmtInf.getUstrd().add(bankOrderLine.getReceiverLabel());

			// Transaction
			cdtTrfTxInf = factory.createCreditTransferTransactionInformation10();
			cdtTrfTxInf.setPmtId(pmtId);
			cdtTrfTxInf.setAmt(amt);
			cdtTrfTxInf.setCdtr(cbtr);
			cdtTrfTxInf.setCdtrAcct(cbtrAcct);
			cdtTrfTxInf.setCdtrAgt(cbtrAgt);
			cdtTrfTxInf.setRmtInf(rmtInf);

			pmtInf.getCdtTrfTxInf().add(cdtTrfTxInf);
		}

		// Header
		GroupHeader32 grpHdr = factory.createGroupHeader32();
		
		/**
		 * CreationDateTime
		 * Definition : Date and Time at which a (group of) payment instruction(s) was created by the instructing party.
		 * XML Tag : <CreDtTm>
		 * Occurrences : [1..1]
		 * Format : YYYY-MM-DDThh:mm:ss 
		 */
		grpHdr.setCreDtTm(datatypeFactory.newXMLGregorianCalendar(validationDateTime.toString("yyyy-MM-dd'T'HH:mm:ss")));
		grpHdr.setNbOfTxs(Integer.toString(nbOfLines));
		grpHdr.setCtrlSum(arithmeticTotal);
		grpHdr.setInitgPty(dbtr);

		// Parent
		CustomerCreditTransferInitiationV03 customerCreditTransferInitiationV03 = factory.createCustomerCreditTransferInitiationV03();
		customerCreditTransferInitiationV03.setGrpHdr(grpHdr);
		customerCreditTransferInitiationV03.getPmtInf().add(pmtInf);
		
		// Document
		Document xml = factory.createDocument();
		xml.setCstmrCdtTrfInitn(customerCreditTransferInitiationV03);

		fileToCreate = factory.createDocument(xml);
		
		return super.generateFile();
	}
	
	
}
