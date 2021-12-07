package com.axelor.apps.bankpayment.service.bankorder.file.transfer;

import java.time.format.DateTimeFormatter;

import com.axelor.apps.bankpayment.db.BankOrder;
import com.axelor.apps.bankpayment.db.BankOrderLine;
import com.axelor.apps.bankpayment.exception.IExceptionMessage;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;

public class BankOrderFileAFB160DCOService extends BankOrderFileAFB160Service {

  public BankOrderFileAFB160DCOService(BankOrder bankOrder) throws AxelorException {
    super(bankOrder);
  }

  @Override
  protected String getB1Area() {
    return null;
  }

  @Override
  protected String getSenderEArea() {
    return null;
  }

  @Override
  protected String getC11Area() {
    return null;
  }

  @Override
  protected String getB3Area() {
    return null;
  }

  @Override
  protected String createSenderRecord() throws AxelorException {
    StringBuilder senderRecordBuilder = new StringBuilder();

    try {
      // Area A : Register code
      senderRecordBuilder.append(
          cfonbToolService.createZone(
              "A", "03", cfonbToolService.STATUS_MANDATORY, cfonbToolService.FORMAT_NUMERIC, 2));
      // Area B-1 : Operation code
      senderRecordBuilder.append(
          cfonbToolService.createZone(
              "B1", "60", cfonbToolService.STATUS_MANDATORY, cfonbToolService.FORMAT_NUMERIC, 2));
      // Area B-2 : Numbering record
      senderRecordBuilder.append(
          cfonbToolService.createZone(
              "B2",
              "00000001",
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_NUMERIC,
              8));
      // Area B-3 : Sender number
      senderRecordBuilder.append(
          cfonbToolService.createZone(
              "B3", "0", cfonbToolService.STATUS_OPTIONAL, cfonbToolService.FORMAT_NUMERIC, 6));
      // Area C-1: Convention type
      senderRecordBuilder.append(
          cfonbToolService.createZone(
              "C1",
              "0",
              cfonbToolService.STATUS_OPTIONAL,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              6));
      // Area C-2: Deposit date JJMMAA
      senderRecordBuilder.append(
          cfonbToolService.createZone(
              "C2",
              getSenderC2Value(),
              cfonbToolService.STATUS_OPTIONAL,
              cfonbToolService.FORMAT_NUMERIC,
              6));
      // Area C-3: Raison social du cedant
      senderRecordBuilder.append(
          cfonbToolService.createZone(
              "C3",
              senderCompany.getName(),
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              24));
      // Area D-1: Domiciliation bancaire du cedant
      senderRecordBuilder.append(
          cfonbToolService.createZone(
              "D1",
              getSenderD1Value(),
              cfonbToolService.STATUS_OPTIONAL,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              24));
      // Area D-2-1: Entry code
      senderRecordBuilder.append(
          cfonbToolService.createZone(
              "D2-1",
              getSenderD21Value(),
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_NUMERIC,
              1));
      // Area D-2-2: Dailly code
      senderRecordBuilder.append(
          cfonbToolService.createZone(
              "D2-2",
              getSenderD22Value(),
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_NUMERIC,
              1));
      // Area D-2-3: Currency code
      senderRecordBuilder.append(
          cfonbToolService.createZone(
              "D2-3",
              "E",
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              1));
      // Area D-3: Code etablissement bancaire du cédant
      senderRecordBuilder.append(
          cfonbToolService.createZone(
              "D3",
              "30004",
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_NUMERIC,
              5));
      // Area D-4: Code guichet du cédant
      senderRecordBuilder.append(
          cfonbToolService.createZone(
              "D4",
              senderBankDetails.getSortCode(),
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_NUMERIC,
              5));
      // Area D-5: Numéro de compte du cédant
      senderRecordBuilder.append(
          cfonbToolService.createZone(
              "D5",
              senderBankDetails.getAccountNbr(),
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_NUMERIC,
              11));
      // Area E: reserved
      senderRecordBuilder.append(
          cfonbToolService.createZone(
              "E",
              "",
              cfonbToolService.STATUS_NOT_USED,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              16));
      // Area F-1: Value's date
      senderRecordBuilder.append(
          cfonbToolService.createZone(
              "F1",
              getSenderF1Value(),
              cfonbToolService.STATUS_OPTIONAL,
              cfonbToolService.FORMAT_NUMERIC,
              6));
      // Area F-2: Reserved
      senderRecordBuilder.append(
          cfonbToolService.createZone(
              "F2",
              "",
              cfonbToolService.STATUS_NOT_USED,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              10));
      // Area F-3: SIREN du cédant
      senderRecordBuilder.append(
          cfonbToolService.createZone(
              "F3",
              this.partnerService.getSIRENNumber(senderCompany.getPartner()),
              cfonbToolService.STATUS_OPTIONAL,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              15));
      // Area G: deposit reference
      senderRecordBuilder.append(
          cfonbToolService.createZone(
              "G",
              bankOrderSeq,
              cfonbToolService.STATUS_OPTIONAL,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              11));

      String senderRecord = senderRecordBuilder.toString();
      cfonbToolService.toUpperCase(senderRecord);
      cfonbToolService.testLength(senderRecord, NB_CHAR_PER_LINE);

      return senderRecord;
    } catch (Exception e) {
      throw new AxelorException(
          e,
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(IExceptionMessage.BANK_ORDER_WRONG_SENDER_RECORD) + ": " + e.getMessage(),
          bankOrderSeq);
    }
  }

  protected String getSenderF1Value() {
	  return this.generationDateTime.format(DateTimeFormatter.ofPattern("ddMMyy"));
  }

  protected String getSenderD22Value() {
    return this.bankOrderFileFormat.getDailyCodeSelect().toString();
  }

  protected String getSenderD21Value() {
	 return this.bankOrderFileFormat.getEntryCodeSelect().toString();
  }

  protected String getSenderD1Value() {
    if (senderCompany.getDefaultBankDetails() != null && senderCompany.getDefaultBankDetails().getBankAddress() != null) {
    	return senderCompany.getDefaultBankDetails().getBankAddress().getAddress();
    }
    return "";
  }

  protected String getSenderC2Value() {
	    return this.bankOrderDate.format(DateTimeFormatter.ofPattern("ddMMyy"));
  }

  @Override
  protected String createDetailRecord(BankOrderLine bankOrderLine) throws AxelorException {
    StringBuilder detailRecordBuilder = new StringBuilder();

    try {
    	BankDetails receiverBankDetails = bankOrderLine.getReceiverBankDetails();
    	
      // Area A : Register code
      detailRecordBuilder.append(
          cfonbToolService.createZone(
              "A", "06", cfonbToolService.STATUS_MANDATORY, cfonbToolService.FORMAT_NUMERIC, 2));
      // Area B-1 : Operation code
      detailRecordBuilder.append(
          cfonbToolService.createZone(
              "B1", "60", cfonbToolService.STATUS_MANDATORY, cfonbToolService.FORMAT_NUMERIC, 2));
      // Area B-2 : Numbering record
      detailRecordBuilder.append(
          cfonbToolService.createZone(
              "B2",
              "00000002",
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_NUMERIC,
              8));
      // Area B-3 : Reserved
      detailRecordBuilder.append(
          cfonbToolService.createZone(
              "B3",
              "",
              cfonbToolService.STATUS_NOT_USED,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              6));
      // Area C-1: Reserved
      detailRecordBuilder.append(
          cfonbToolService.createZone(
              "C1-1",
              "",
              cfonbToolService.STATUS_NOT_USED,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              2));
      // Area C-1-2: Reference tiré
      detailRecordBuilder.append(
          cfonbToolService.createZone(
              "C1-2",
              bankOrderLine.getSequence(),
              cfonbToolService.STATUS_OPTIONAL,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              10));
      // Area C-2: Nom du tiré
      detailRecordBuilder.append(
          cfonbToolService.createZone(
              "C2",
              bankOrderLine.getReceiverCompany().getName(),
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              24));
      // Area D-1: Domiciliation bancaire du tiré
      detailRecordBuilder.append(
          cfonbToolService.createZone(
              "D1",
              getDetailD1Value(bankOrderLine),
              cfonbToolService.STATUS_OPTIONAL,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              24));
      // Area D-2-1: ACceptation
      detailRecordBuilder.append(
          cfonbToolService.createZone(
              "D2-1",
              getDetailD21Value(),
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_NUMERIC,
              1));
      // Area D-2-2: Reserved
      detailRecordBuilder.append(
          cfonbToolService.createZone(
              "D2-2",
              "",
              cfonbToolService.STATUS_NOT_USED,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              2));
      // Area D-3: Code etablissement bancaire du domiciliataire
      detailRecordBuilder.append(
          cfonbToolService.createZone(
              "D3",
              receiverBankDetails.getBankCode(),
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_NUMERIC,
              5));
      // Area D-4: Code guichet domiciliataire
      detailRecordBuilder.append(
          cfonbToolService.createZone(
              "D4",
              receiverBankDetails.getSortCode(),
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_NUMERIC,
              5));
      // Area D-5: Numéro de compte du tiré
      detailRecordBuilder.append(
          cfonbToolService.createZone(
              "D5",
              receiverBankDetails.getAccountNbr(),
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              11));
      // Area E1: Amount
      detailRecordBuilder.append(
          cfonbToolService.createZone(
              "E1",
              bankOrderLine.getBankOrderAmount(),
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_NUMERIC,
              12));
      // Area E-2: Reserved
      detailRecordBuilder.append(
          cfonbToolService.createZone(
              "E2",
              "",
              cfonbToolService.STATUS_NOT_USED,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              4));
      // Area F-1: Date d'échéance
      detailRecordBuilder.append(
          cfonbToolService.createZone(
              "F1",
              getDetailF1Value(bankOrderLine),
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_NUMERIC,
              6));
      // Area F-2-1: Date de création
      detailRecordBuilder.append(
          cfonbToolService.createZone(
              "F2-1",
              getDetailF21Value(bankOrderLine),
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_NUMERIC,
              6));
      // Area F-2-2: Reserved
      detailRecordBuilder.append(
          cfonbToolService.createZone(
              "F2-2",
              "",
              cfonbToolService.STATUS_NOT_USED,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              4));
      // Area F-3-1: Type
      detailRecordBuilder.append(
          cfonbToolService.createZone(
              "F3-1",
              "",
              cfonbToolService.STATUS_DEPENDENT,
              cfonbToolService.FORMAT_NUMERIC,
              1));
      // Area F-3-2: Nature
      detailRecordBuilder.append(
          cfonbToolService.createZone(
              "F3-2",
              "",
              cfonbToolService.STATUS_DEPENDENT,
              cfonbToolService.FORMAT_NUMERIC,
              3));
      // Area F-3-3: Country
      detailRecordBuilder.append(
          cfonbToolService.createZone(
              "F3-3",
              "",
              cfonbToolService.STATUS_DEPENDENT,
              cfonbToolService.FORMAT_ALPHA,
              3));
      // Area F-3-4: SIREN
      detailRecordBuilder.append(
          cfonbToolService.createZone(
              "F3-4",
              this.partnerService.getSIRENNumber(bankOrderLine.getPartner()),
              cfonbToolService.STATUS_DEPENDENT,
              cfonbToolService.FORMAT_NUMERIC,
              9));
      // Area G: Reference tireur
      detailRecordBuilder.append(
          cfonbToolService.createZone(
              "G",
              "",
              cfonbToolService.STATUS_OPTIONAL,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              10));

      String detailRecord = detailRecordBuilder.toString();
      cfonbToolService.toUpperCase(detailRecord);
      cfonbToolService.testLength(detailRecord, NB_CHAR_PER_LINE);

      return detailRecord;
    } catch (Exception e) {
      throw new AxelorException(
          e,
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(IExceptionMessage.BANK_ORDER_WRONG_MAIN_DETAIL_RECORD) + ": " + e.getMessage(),
          bankOrderSeq);
    }
  }


  protected String getDetailF21Value(BankOrderLine bankOrderLine) {
    return bankOrderLine.getCreatedOn().format(DateTimeFormatter.ofPattern("ddMMyy"));
  }

  protected String getDetailF1Value(BankOrderLine bankOrderLine) {
	  return bankOrderLine.getBankOrderDate().format(DateTimeFormatter.ofPattern("ddMMyy"));
  }

  protected String getDetailD21Value() {
    return this.bankOrderFileFormat.getAcceptPerLineCodeSelect().toString();
  }

  protected String getDetailD1Value(BankOrderLine bankOrderLine) {
    if (bankOrderLine.getReceiverBankDetails() != null &&  bankOrderLine.getReceiverBankDetails().getBankAddress() != null) {
    	return bankOrderLine.getReceiverBankDetails().getBankAddress().getAddress();
    }
    return "";
  }


  protected String createEndorsedDetailRecord(BankOrderLine bankOrderLine) throws AxelorException {
    StringBuilder endorsedDetailRecordBuilder = new StringBuilder();
    try {
      // Area A : Register code
      endorsedDetailRecordBuilder.append(
          cfonbToolService.createZone(
              "A", "07", cfonbToolService.STATUS_MANDATORY, cfonbToolService.FORMAT_NUMERIC, 2));
      // Area B-1 : Operation code
      endorsedDetailRecordBuilder.append(
          cfonbToolService.createZone(
              "B1", "60", cfonbToolService.STATUS_MANDATORY, cfonbToolService.FORMAT_NUMERIC, 2));
      // Area B-2 : Numbering record
      endorsedDetailRecordBuilder.append(
          cfonbToolService.createZone(
              "B2",
              "00000003",
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_NUMERIC,
              8));
      // Area B-3 : Reserved
      endorsedDetailRecordBuilder.append(
          cfonbToolService.createZone(
              "B3",
              "",
              cfonbToolService.STATUS_NOT_USED,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              6));
      // Area C-1: Reserved
      endorsedDetailRecordBuilder.append(
          cfonbToolService.createZone(
              "C1",
              "",
              cfonbToolService.STATUS_NOT_USED,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              12));
      // Area C-2: Nom du tireur
      endorsedDetailRecordBuilder.append(
          cfonbToolService.createZone(
              "C2",
              bankOrderLine.getReceiverCompany().getName(),
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              24));
      // Area D-1: Reserved
      endorsedDetailRecordBuilder.append(
          cfonbToolService.createZone(
              "D1",
              "",
              cfonbToolService.STATUS_NOT_USED,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              24));
      // Area D-2-1: Reserved
      endorsedDetailRecordBuilder.append(
          cfonbToolService.createZone(
              "D2-1",
              "",
              cfonbToolService.STATUS_NOT_USED,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              1));
      // Area D-2-2: Reserved
      endorsedDetailRecordBuilder.append(
          cfonbToolService.createZone(
              "D2-2",
              "",
              cfonbToolService.STATUS_NOT_USED,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              2));
      // Area D-3: Reserved
      endorsedDetailRecordBuilder.append(
          cfonbToolService.createZone(
              "D3",
              "",
              cfonbToolService.STATUS_NOT_USED,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              5));
      // Area D-4: Reserved
      endorsedDetailRecordBuilder.append(
          cfonbToolService.createZone(
              "D4",
              "",
              cfonbToolService.STATUS_NOT_USED,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              5));
      // Area D-5: Reserved
      endorsedDetailRecordBuilder.append(
          cfonbToolService.createZone(
              "D5",
              "",
              cfonbToolService.STATUS_NOT_USED,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              11));
      // Area E: Mandatory area
      endorsedDetailRecordBuilder.append(
          cfonbToolService.createZone(
              "E",
              "0000000000000000",
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_NUMERIC,
              16));
      // Area F-1: Reserved
      endorsedDetailRecordBuilder.append(
          cfonbToolService.createZone(
              "F1",
              "",
              cfonbToolService.STATUS_NOT_USED,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              6));
      // Area F-2: Reserved
      endorsedDetailRecordBuilder.append(
          cfonbToolService.createZone(
              "F2",
              "",
              cfonbToolService.STATUS_NOT_USED,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              10));
      // Area F-3: SIREN du tireur
      endorsedDetailRecordBuilder.append(
          cfonbToolService.createZone(
              "F3",
              this.partnerService.getSIRENNumber(bankOrderLine.getPartner()),
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              15));
      // Area G: Reserved
      endorsedDetailRecordBuilder.append(
          cfonbToolService.createZone(
              "G",
              "",
              cfonbToolService.STATUS_NOT_USED,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              11));

      String endorsedDetailRecord = endorsedDetailRecordBuilder.toString();
      cfonbToolService.toUpperCase(endorsedDetailRecord);
      cfonbToolService.testLength(endorsedDetailRecord, NB_CHAR_PER_LINE);

      return endorsedDetailRecord;
    } catch (Exception e) {
      throw new AxelorException(
          e,
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(IExceptionMessage.BANK_ORDER_WRONG_MAIN_DETAIL_RECORD) + ": " + e.getMessage(),
          bankOrderSeq);
    }
  }

  @Override
  protected String createTotalRecord() throws AxelorException {
    StringBuilder totalRecordBuilder = new StringBuilder();
    try {
      // Area A : Register code
      totalRecordBuilder.append(
          cfonbToolService.createZone(
              "A", "08", cfonbToolService.STATUS_MANDATORY, cfonbToolService.FORMAT_NUMERIC, 2));
      // Area B-1 : Operation code
      totalRecordBuilder.append(
          cfonbToolService.createZone(
              "B1", "60", cfonbToolService.STATUS_MANDATORY, cfonbToolService.FORMAT_NUMERIC, 2));
      // Area B-2 : Numbering record
      totalRecordBuilder.append(
          cfonbToolService.createZone(
              "B2",
              "00000005",
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_NUMERIC,
              8));
      // Area B-3 : Reserved
      totalRecordBuilder.append(
          cfonbToolService.createZone(
              "B3",
              "",
              cfonbToolService.STATUS_NOT_USED,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              6));
      // Area C-1: Reserved
      totalRecordBuilder.append(
          cfonbToolService.createZone(
              "C1",
              "",
              cfonbToolService.STATUS_NOT_USED,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              12));
      // Area C-2: Reserved
      totalRecordBuilder.append(
          cfonbToolService.createZone(
              "C2",
              "",
              cfonbToolService.STATUS_NOT_USED,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              24));
      // Area D-1: Reserved
      totalRecordBuilder.append(
          cfonbToolService.createZone(
              "D1",
              "",
              cfonbToolService.STATUS_NOT_USED,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              24));
      // Area D-2-1: Reserved
      totalRecordBuilder.append(
          cfonbToolService.createZone(
              "D2-1",
              "",
              cfonbToolService.STATUS_NOT_USED,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              1));
      // Area D-2-2: Reserved
      totalRecordBuilder.append(
          cfonbToolService.createZone(
              "D2-2",
              "",
              cfonbToolService.STATUS_NOT_USED,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              2));
      // Area D-3: Reserved
      totalRecordBuilder.append(
          cfonbToolService.createZone(
              "D3",
              "",
              cfonbToolService.STATUS_NOT_USED,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              5));
      // Area D-4: Reserved
      totalRecordBuilder.append(
          cfonbToolService.createZone(
              "D4",
              "",
              cfonbToolService.STATUS_NOT_USED,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              5));
      // Area D-5: Reserved
      totalRecordBuilder.append(
          cfonbToolService.createZone(
              "D5",
              "",
              cfonbToolService.STATUS_NOT_USED,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              11));
      // Area E-1: Total amount
      totalRecordBuilder.append(
          cfonbToolService.createZone(
              "E1",
              getTotalE1Value(),
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_NUMERIC,
              12));
      // Area E-2: Mandatory area
      totalRecordBuilder.append(
          cfonbToolService.createZone(
              "E2", "0000", cfonbToolService.STATUS_MANDATORY, cfonbToolService.FORMAT_NUMERIC, 4));
      // Area F-1: Reserved
      totalRecordBuilder.append(
          cfonbToolService.createZone(
              "F1",
              "",
              cfonbToolService.STATUS_NOT_USED,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              6));
      // Area F-2: Reserved
      totalRecordBuilder.append(
          cfonbToolService.createZone(
              "F2",
              "",
              cfonbToolService.STATUS_NOT_USED,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              10));
      // Area F-3: Reserved
      totalRecordBuilder.append(
          cfonbToolService.createZone(
              "F3",
              "",
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              15));
      // Area G-1: Reserved
      totalRecordBuilder.append(
          cfonbToolService.createZone(
              "G1",
              "",
              cfonbToolService.STATUS_NOT_USED,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              5));
      // Area G-2: Reserved
      totalRecordBuilder.append(
          cfonbToolService.createZone(
              "G2",
              "",
              cfonbToolService.STATUS_NOT_USED,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              6));

      String totalRecord = totalRecordBuilder.toString();
      cfonbToolService.toUpperCase(totalRecord);
      cfonbToolService.testLength(totalRecord, NB_CHAR_PER_LINE);

      return totalRecord;
    } catch (Exception e) {
      throw new AxelorException(
          e,
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(IExceptionMessage.BANK_ORDER_WRONG_MAIN_DETAIL_RECORD) + ": " + e.getMessage(),
          bankOrderSeq);
    }
  }

  protected String getTotalE1Value() {
    // TODO Auto-generated method stub
    return null;
  }

  protected String getTotalB2Value() {
    // TODO Auto-generated method stub
    return null;
  }

  protected String createAdditionalDetailRecord() throws AxelorException {
    StringBuilder additionalDetailRecordBuilder = new StringBuilder();

    try {

      // Area A : Register code
      additionalDetailRecordBuilder.append(
          cfonbToolService.createZone(
              "A", "16", cfonbToolService.STATUS_MANDATORY, cfonbToolService.FORMAT_NUMERIC, 2));
      // Area B-1 : Operation code
      additionalDetailRecordBuilder.append(
          cfonbToolService.createZone(
              "B1", "60", cfonbToolService.STATUS_MANDATORY, cfonbToolService.FORMAT_NUMERIC, 2));
      // Area B-2 : Numbering record
      additionalDetailRecordBuilder.append(
          cfonbToolService.createZone(
              "B2",
              "0000002",
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_NUMERIC,
              8));
      // Area B-3 : Reserved
      additionalDetailRecordBuilder.append(
          cfonbToolService.createZone(
              "B3",
              "",
              cfonbToolService.STATUS_NOT_USED,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              6));
      // Area C-1: N° et nom de voie
      additionalDetailRecordBuilder.append(
          cfonbToolService.createZone(
              "C1",
              getAdditionalC1Value(),
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              32));
      // Area C-3: Localité
      additionalDetailRecordBuilder.append(
          cfonbToolService.createZone(
              "C2",
              getAdditionalC2Value(),
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              32));
      // Area C-3: Code postale et bureau distributeur
      additionalDetailRecordBuilder.append(
          cfonbToolService.createZone(
              "C3",
              getAdditionalC3Value(),
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              32));
      // Area B-3 : Reserved
      additionalDetailRecordBuilder.append(
          cfonbToolService.createZone(
              "B3",
              "",
              cfonbToolService.STATUS_NOT_USED,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              46));

      String additionalDetailRecord = additionalDetailRecordBuilder.toString();
      cfonbToolService.toUpperCase(additionalDetailRecord);
      cfonbToolService.testLength(additionalDetailRecord, NB_CHAR_PER_LINE);

      return additionalDetailRecord;
    } catch (Exception e) {
      throw new AxelorException(
          e,
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(IExceptionMessage.BANK_ORDER_WRONG_MAIN_DETAIL_RECORD) + ": " + e.getMessage(),
          bankOrderSeq);
    }
  }

  protected String getAdditionalC3Value() {
    // TODO Auto-generated method stub
    return null;
  }

  protected String getAdditionalC2Value() {
    // TODO Auto-generated method stub
    return null;
  }

  protected String getAdditionalC1Value() {
    // TODO Auto-generated method stub
    return null;
  }

  protected String getAdditionalB2Value() {
    // TODO Auto-generated method stub
    return null;
  }
}
