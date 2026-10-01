/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.bankpayment.service.bankorder.file.transfer;

import com.axelor.apps.bankpayment.db.BankOrder;
import com.axelor.apps.bankpayment.db.BankOrderLine;
import com.axelor.apps.bankpayment.service.bankorder.file.BankOrderFileService;
import com.axelor.apps.bankpayment.xsd.sepa.pain_001_001_09.AccountIdentification4Choice;
import com.axelor.apps.bankpayment.xsd.sepa.pain_001_001_09.ActiveOrHistoricCurrencyAndAmount;
import com.axelor.apps.bankpayment.xsd.sepa.pain_001_001_09.AmountType4Choice;
import com.axelor.apps.bankpayment.xsd.sepa.pain_001_001_09.BranchAndFinancialInstitutionIdentification6;
import com.axelor.apps.bankpayment.xsd.sepa.pain_001_001_09.CashAccount38;
import com.axelor.apps.bankpayment.xsd.sepa.pain_001_001_09.CreditTransferTransaction34;
import com.axelor.apps.bankpayment.xsd.sepa.pain_001_001_09.CustomerCreditTransferInitiationV09;
import com.axelor.apps.bankpayment.xsd.sepa.pain_001_001_09.DateAndDateTime2Choice;
import com.axelor.apps.bankpayment.xsd.sepa.pain_001_001_09.Document;
import com.axelor.apps.bankpayment.xsd.sepa.pain_001_001_09.FinancialInstitutionIdentification18;
import com.axelor.apps.bankpayment.xsd.sepa.pain_001_001_09.GenericFinancialIdentification1;
import com.axelor.apps.bankpayment.xsd.sepa.pain_001_001_09.GroupHeader85;
import com.axelor.apps.bankpayment.xsd.sepa.pain_001_001_09.ObjectFactory;
import com.axelor.apps.bankpayment.xsd.sepa.pain_001_001_09.PartyIdentification135;
import com.axelor.apps.bankpayment.xsd.sepa.pain_001_001_09.PaymentIdentification6;
import com.axelor.apps.bankpayment.xsd.sepa.pain_001_001_09.PaymentInstruction30;
import com.axelor.apps.bankpayment.xsd.sepa.pain_001_001_09.PaymentMethod3Code;
import com.axelor.apps.bankpayment.xsd.sepa.pain_001_001_09.PaymentTypeInformation26;
import com.axelor.apps.bankpayment.xsd.sepa.pain_001_001_09.RemittanceInformation16;
import com.axelor.apps.bankpayment.xsd.sepa.pain_001_001_09.ServiceLevel8Choice;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Bank;
import com.axelor.apps.base.db.BankDetails;
import com.google.common.base.Strings;
import jakarta.inject.Inject;
import java.io.File;
import java.time.format.DateTimeFormatter;
import javax.xml.datatype.DatatypeFactory;

public class BankOrderFile00100109Service extends BankOrderFileService {

  protected static final String BIC_NOT_PROVIDED = "NOTPROVIDED";

  @Inject
  public BankOrderFile00100109Service(BankOrder bankOrder) {

    super(bankOrder);

    context = "com.axelor.apps.bankpayment.xsd.sepa.pain_001_001_09";
    fileExtension = FILE_EXTENSION_XML;
  }

  /**
   * Method to create an XML file for SEPA transfer pain.001.001.09
   *
   * @throws AxelorException
   */
  @Override
  public File generateFile() throws AxelorException {
    DatatypeFactory datatypeFactory = DatatypeFactory.newDefaultInstance();

    ObjectFactory factory = new ObjectFactory();

    ServiceLevel8Choice svcLvl = factory.createServiceLevel8Choice();
    svcLvl.setCd("SEPA");

    PaymentTypeInformation26 pmtTpInf = factory.createPaymentTypeInformation26();
    pmtTpInf.getSvcLvl().add(svcLvl);

    // Payer
    PartyIdentification135 dbtr = factory.createPartyIdentification135();
    dbtr.setNm(senderBankDetails.getOwnerName());

    // IBAN
    AccountIdentification4Choice iban = factory.createAccountIdentification4Choice();
    iban.setIBAN(senderBankDetails.getIban());

    CashAccount38 dbtrAcct = factory.createCashAccount38();
    dbtrAcct.setId(iban);

    // BIC
    FinancialInstitutionIdentification18 finInstnId =
        factory.createFinancialInstitutionIdentification18();

    fillBic(finInstnId, senderBankDetails.getBank());

    BranchAndFinancialInstitutionIdentification6 dbtrAgt =
        factory.createBranchAndFinancialInstitutionIdentification6();
    dbtrAgt.setFinInstnId(finInstnId);

    PaymentInstruction30 pmtInf = factory.createPaymentInstruction30();
    pmtInf.setPmtInfId(bankOrderSeq);
    pmtInf.setPmtMtd(PaymentMethod3Code.TRF);
    pmtInf.setPmtTpInf(pmtTpInf);

    /**
     * RequestedExecutionDate Definition : Date at which the initiating party asks the Debtor's Bank
     * to process the payment. This is the date on which the debtor's account(s) is (are) to be
     * debited. XML Tag : <ReqdExctnDt> Occurrences : [1..1] Format : YYYY-MM-DD Rules : date is
     * limited to maximum one year in the future.
     */
    DateAndDateTime2Choice requestedExecutionDate = factory.createDateAndDateTime2Choice();
    requestedExecutionDate.setDt(
        datatypeFactory.newXMLGregorianCalendar(
            bankOrderDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))));
    pmtInf.setReqdExctnDt(requestedExecutionDate);
    pmtInf.setDbtr(dbtr);
    pmtInf.setDbtrAcct(dbtrAcct);
    pmtInf.setDbtrAgt(dbtrAgt);

    CreditTransferTransaction34 cdtTrfTxInf = null;
    PaymentIdentification6 pmtId = null;
    AmountType4Choice amt = null;
    ActiveOrHistoricCurrencyAndAmount instdAmt = null;
    PartyIdentification135 cbtr = null;
    CashAccount38 cbtrAcct = null;
    BranchAndFinancialInstitutionIdentification6 cbtrAgt = null;
    RemittanceInformation16 rmtInf = null;

    for (BankOrderLine bankOrderLine : bankOrderLineList) {

      BankDetails receiverBankDetails = bankOrderLine.getReceiverBankDetails();

      // Reference
      pmtId = factory.createPaymentIdentification6();
      //			pmtId.setInstrId(bankOrderLine.getSequence());
      pmtId.setEndToEndId(bankOrderLine.getSequence());

      // Amount
      instdAmt = factory.createActiveOrHistoricCurrencyAndAmount();
      instdAmt.setCcy(companyCurrency.getCodeISO());
      instdAmt.setValue(bankOrderLine.getCompanyCurrencyAmount());

      amt = factory.createAmountType4Choice();
      amt.setInstdAmt(instdAmt);

      // Receiver
      cbtr = factory.createPartyIdentification135();
      cbtr.setNm(receiverBankDetails.getOwnerName());

      // IBAN
      iban = factory.createAccountIdentification4Choice();
      iban.setIBAN(receiverBankDetails.getIban());

      cbtrAcct = factory.createCashAccount38();
      cbtrAcct.setId(iban);

      // BIC
      finInstnId = factory.createFinancialInstitutionIdentification18();

      fillBic(finInstnId, receiverBankDetails.getBank());

      cbtrAgt = factory.createBranchAndFinancialInstitutionIdentification6();
      cbtrAgt.setFinInstnId(finInstnId);

      rmtInf = factory.createRemittanceInformation16();

      String ustrd = "";
      if (!Strings.isNullOrEmpty(bankOrderLine.getReceiverReference())) {
        ustrd += bankOrderLine.getReceiverReference();
      }
      if (!Strings.isNullOrEmpty(bankOrderLine.getReceiverLabel())) {
        if (!Strings.isNullOrEmpty(ustrd)) {
          ustrd += " - ";
        }
        ustrd += bankOrderLine.getReceiverLabel();
      }

      if (!Strings.isNullOrEmpty(ustrd)) {
        rmtInf.getUstrd().add(ustrd.substring(0, Math.min(140, ustrd.length() - 1)));
      }

      //			StructuredRemittanceInformation7 strd = factory.createStructuredRemittanceInformation7();
      //
      //			CreditorReferenceInformation2 cdtrRefInf = factory.createCreditorReferenceInformation2();
      //			cdtrRefInf.setRef(bankOrderLine.getReceiverReference());
      //
      //			strd.setCdtrRefInf(cdtrRefInf);
      //
      //			rmtInf.getStrd().add(strd);

      // Transaction
      cdtTrfTxInf = factory.createCreditTransferTransaction34();
      cdtTrfTxInf.setPmtId(pmtId);
      cdtTrfTxInf.setAmt(amt);
      cdtTrfTxInf.setCdtr(cbtr);
      cdtTrfTxInf.setCdtrAcct(cbtrAcct);
      cdtTrfTxInf.setCdtrAgt(cbtrAgt);
      cdtTrfTxInf.setRmtInf(rmtInf);

      pmtInf.getCdtTrfTxInf().add(cdtTrfTxInf);
    }

    // Header
    GroupHeader85 grpHdr = factory.createGroupHeader85();

    /** Référence du message qui n'est pas utilisée comme référence fonctionnelle. */
    grpHdr.setMsgId(bankOrderSeq);

    /**
     * CreationDateTime Definition : Date and Time at which a (group of) payment instruction(s) was
     * created by the instructing party. XML Tag : <CreDtTm> Occurrences : [1..1] Format :
     * YYYY-MM-DDThh:mm:ss
     */
    grpHdr.setCreDtTm(
        datatypeFactory.newXMLGregorianCalendar(
            generationDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))));
    grpHdr.setNbOfTxs(Integer.toString(nbOfLines));
    grpHdr.setCtrlSum(arithmeticTotal);
    grpHdr.setInitgPty(dbtr);

    // Parent
    CustomerCreditTransferInitiationV09 customerCreditTransferInitiationV09 =
        factory.createCustomerCreditTransferInitiationV09();
    customerCreditTransferInitiationV09.setGrpHdr(grpHdr);
    customerCreditTransferInitiationV09.getPmtInf().add(pmtInf);

    // Document
    Document xml = factory.createDocument();
    xml.setCstmrCdtTrfInitn(customerCreditTransferInitiationV09);

    fileToCreate = factory.createDocument(xml);

    return super.generateFile();
  }

  /**
   * Method to fill the BIC information. If the BIC is not provided or in Iban only mode, we put
   * NOTPROVIDED value. In this case, the bank ignore the BIC and use the Iban only.
   *
   * @param finInstnId The financial instituation identification tag of the generated file.
   * @param bank The bank from which the BIC is get.
   */
  protected void fillBic(FinancialInstitutionIdentification18 finInstnId, Bank bank) {

    if (bankOrderFileFormat.getIbanOnly()
        || bank == null
        || Strings.isNullOrEmpty(bank.getCode())) {
      GenericFinancialIdentification1 genFinId = new GenericFinancialIdentification1();
      genFinId.setId(BIC_NOT_PROVIDED);
      finInstnId.setOthr(genFinId);
    } else {
      finInstnId.setBICFI(bank.getCode());
    }
  }
}
