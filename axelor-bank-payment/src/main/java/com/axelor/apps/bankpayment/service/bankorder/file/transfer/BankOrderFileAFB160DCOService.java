/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
import com.axelor.apps.bankpayment.exception.BankPaymentExceptionMessage;
import com.axelor.apps.bankpayment.service.bankorder.file.BankOrderFileService;
import com.axelor.apps.bankpayment.service.bankorder.file.BankOrderFileZoneName;
import com.axelor.apps.bankpayment.service.cfonb.CfonbToolService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;

public class BankOrderFileAFB160DCOService extends BankOrderFileService {

  protected CfonbToolService cfonbToolService;
  protected PartnerService partnerService;
  protected final int NB_CHAR_PER_LINE = 160;

  private int numberingRecord;

  @Inject
  public BankOrderFileAFB160DCOService(BankOrder bankOrder) throws AxelorException {
    super(bankOrder);
    this.partnerService = Beans.get(PartnerService.class);
    this.cfonbToolService = Beans.get(CfonbToolService.class);
    fileExtension = FILE_EXTENSION_TXT;
    this.numberingRecord = 1;
  }

  @Override
  public File generateFile()
      throws JAXBException, IOException, AxelorException, DatatypeConfigurationException {

    List<String> records = Lists.newArrayList();

    records.add(this.createSenderRecord());

    for (BankOrderLine bankOrderLine : bankOrderLineList) {

      records.add(this.createMainDetailRecord(bankOrderLine));
      records.add(this.createEndorsedDetailRecord(bankOrderLine));
      records.add(this.createAdditionalDetailRecord(bankOrderLine));
    }

    records.add(this.createTotalRecord());

    fileToCreate = records;

    return super.generateFile();
  }

  protected String createSenderRecord() throws AxelorException {
    StringBuilder senderRecordBuilder = new StringBuilder();

    try {
      // Area A : Register code
      senderRecordBuilder.append(
          cfonbToolService.createZone(
              I18n.get(BankOrderFileZoneName.BOF_AFB_160_DCO_SENDER_RECORD_ZONE_A),
              "03",
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_NUMERIC,
              2));
      // Area B-1 : Operation code
      senderRecordBuilder.append(
          cfonbToolService.createZone(
              I18n.get(BankOrderFileZoneName.BOF_AFB_160_DCO_SENDER_RECORD_ZONE_B1),
              "60",
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_NUMERIC,
              2));
      // Area B-2 : Numbering record
      senderRecordBuilder.append(
          cfonbToolService.createZone(
              I18n.get(BankOrderFileZoneName.BOF_AFB_160_DCO_SENDER_RECORD_ZONE_B2),
              numberingRecord++,
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_NUMERIC,
              8));
      // Area B-3 : Sender number
      senderRecordBuilder.append(
          cfonbToolService.createZone(
              I18n.get(BankOrderFileZoneName.BOF_AFB_160_DCO_SENDER_RECORD_ZONE_B3),
              "0",
              cfonbToolService.STATUS_OPTIONAL,
              cfonbToolService.FORMAT_NUMERIC,
              6));
      // Area C-1: Convention type
      senderRecordBuilder.append(
          cfonbToolService.createZone(
              I18n.get(BankOrderFileZoneName.BOF_AFB_160_DCO_SENDER_RECORD_ZONE_C1),
              "0",
              cfonbToolService.STATUS_OPTIONAL,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              6));
      // Area C-2: Deposit date JJMMAA
      senderRecordBuilder.append(
          cfonbToolService.createZone(
              I18n.get(BankOrderFileZoneName.BOF_AFB_160_DCO_SENDER_RECORD_ZONE_C2),
              getSenderC2Value(),
              cfonbToolService.STATUS_OPTIONAL,
              cfonbToolService.FORMAT_NUMERIC,
              6));
      // Area C-3: Raison social du cedant
      senderRecordBuilder.append(
          cfonbToolService.createZone(
              I18n.get(BankOrderFileZoneName.BOF_AFB_160_DCO_SENDER_RECORD_ZONE_C3),
              senderCompany.getName(),
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              24));
      // Area D-1: Domiciliation bancaire du cedant
      senderRecordBuilder.append(
          cfonbToolService.createZone(
              I18n.get(BankOrderFileZoneName.BOF_AFB_160_DCO_SENDER_RECORD_ZONE_D1),
              getSenderD1Value(),
              cfonbToolService.STATUS_OPTIONAL,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              24));
      // Area D-2-1: Entry code
      senderRecordBuilder.append(
          cfonbToolService.createZone(
              I18n.get(BankOrderFileZoneName.BOF_AFB_160_DCO_SENDER_RECORD_ZONE_D21),
              getSenderD21Value(),
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_NUMERIC,
              1));
      // Area D-2-2: Dailly code
      senderRecordBuilder.append(
          cfonbToolService.createZone(
              I18n.get(BankOrderFileZoneName.BOF_AFB_160_DCO_SENDER_RECORD_ZONE_D22),
              getSenderD22Value(),
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_NUMERIC,
              1));
      // Area D-2-3: Currency code
      senderRecordBuilder.append(
          cfonbToolService.createZone(
              I18n.get(BankOrderFileZoneName.BOF_AFB_160_DCO_SENDER_RECORD_ZONE_D23),
              "E",
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              1));
      // Area D-3: Code etablissement bancaire du cédant
      senderRecordBuilder.append(
          cfonbToolService.createZone(
              I18n.get(BankOrderFileZoneName.BOF_AFB_160_DCO_SENDER_RECORD_ZONE_D3),
              "30004",
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_NUMERIC,
              5));
      // Area D-4: Code guichet du cédant
      senderRecordBuilder.append(
          cfonbToolService.createZone(
              I18n.get(BankOrderFileZoneName.BOF_AFB_160_DCO_SENDER_RECORD_ZONE_D4),
              senderBankDetails.getSortCode(),
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_NUMERIC,
              5));
      // Area D-5: Numéro de compte du cédant
      senderRecordBuilder.append(
          cfonbToolService.createZone(
              I18n.get(BankOrderFileZoneName.BOF_AFB_160_DCO_SENDER_RECORD_ZONE_D5),
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
              I18n.get(BankOrderFileZoneName.BOF_AFB_160_DCO_SENDER_RECORD_ZONE_F1),
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
              I18n.get(BankOrderFileZoneName.BOF_AFB_160_DCO_SENDER_RECORD_ZONE_F3),
              this.partnerService.getSIRENNumber(senderCompany.getPartner()),
              cfonbToolService.STATUS_OPTIONAL,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              15));
      // Area G: deposit reference
      senderRecordBuilder.append(
          cfonbToolService.createZone(
              I18n.get(BankOrderFileZoneName.BOF_AFB_160_DCO_SENDER_RECORD_ZONE_G),
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
          I18n.get(BankPaymentExceptionMessage.BANK_ORDER_WRONG_SENDER_RECORD)
              + ": "
              + e.getMessage(),
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
    if (senderBankDetails.getBankAddress() != null) {
      return senderBankDetails.getBankAddress().getAddress();
    }
    return "";
  }

  protected String getSenderC2Value() {
    return this.bankOrderDate.format(DateTimeFormatter.ofPattern("ddMMyy"));
  }

  protected String createMainDetailRecord(BankOrderLine bankOrderLine) throws AxelorException {

    StringBuilder mainDetailRecordBuilder = new StringBuilder();
    try {
      BankDetails receiverBankDetails = bankOrderLine.getReceiverBankDetails();

      // Area A : Register code
      mainDetailRecordBuilder.append(
          cfonbToolService.createZone(
              I18n.get(BankOrderFileZoneName.BOF_AFB_160_DCO_MAIN_DETAIL_RECORD_ZONE_A),
              "06",
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_NUMERIC,
              2));
      // Area B-1 : Operation code
      mainDetailRecordBuilder.append(
          cfonbToolService.createZone(
              I18n.get(BankOrderFileZoneName.BOF_AFB_160_DCO_MAIN_DETAIL_RECORD_ZONE_B1),
              "60",
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_NUMERIC,
              2));
      // Area B-2 : Numbering record
      mainDetailRecordBuilder.append(
          cfonbToolService.createZone(
              I18n.get(BankOrderFileZoneName.BOF_AFB_160_DCO_MAIN_DETAIL_RECORD_ZONE_B2),
              numberingRecord++,
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_NUMERIC,
              8));
      // Area B-3 : Reserved
      mainDetailRecordBuilder.append(
          cfonbToolService.createZone(
              "B3",
              "",
              cfonbToolService.STATUS_NOT_USED,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              6));
      // Area C-1: Reserved
      mainDetailRecordBuilder.append(
          cfonbToolService.createZone(
              "C1-1",
              "",
              cfonbToolService.STATUS_NOT_USED,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              2));
      // Area C-1-2: Reference tiré
      mainDetailRecordBuilder.append(
          cfonbToolService.createZone(
              I18n.get(BankOrderFileZoneName.BOF_AFB_160_DCO_MAIN_DETAIL_RECORD_ZONE_C12),
              bankOrderLine.getSequence(),
              cfonbToolService.STATUS_OPTIONAL,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              10));
      // Area C-2: Nom du tiré
      mainDetailRecordBuilder.append(
          cfonbToolService.createZone(
              I18n.get(BankOrderFileZoneName.BOF_AFB_160_DCO_MAIN_DETAIL_RECORD_ZONE_C2),
              bankOrderLine.getPartner().getName(),
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              24));
      // Area D-1: Domiciliation bancaire du tiré
      mainDetailRecordBuilder.append(
          cfonbToolService.createZone(
              I18n.get(BankOrderFileZoneName.BOF_AFB_160_DCO_MAIN_DETAIL_RECORD_ZONE_D1),
              getDetailD1Value(bankOrderLine),
              cfonbToolService.STATUS_OPTIONAL,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              24));
      // Area D-2-1: ACceptation
      mainDetailRecordBuilder.append(
          cfonbToolService.createZone(
              I18n.get(BankOrderFileZoneName.BOF_AFB_160_DCO_MAIN_DETAIL_RECORD_ZONE_D21),
              getDetailD21Value(),
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_NUMERIC,
              1));
      // Area D-2-2: Reserved
      mainDetailRecordBuilder.append(
          cfonbToolService.createZone(
              "D2-2",
              "",
              cfonbToolService.STATUS_NOT_USED,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              2));
      // Area D-3: Code etablissement bancaire du domiciliataire
      mainDetailRecordBuilder.append(
          cfonbToolService.createZone(
              I18n.get(BankOrderFileZoneName.BOF_AFB_160_DCO_MAIN_DETAIL_RECORD_ZONE_D3),
              receiverBankDetails.getBankCode(),
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_NUMERIC,
              5));
      // Area D-4: Code guichet domiciliataire
      mainDetailRecordBuilder.append(
          cfonbToolService.createZone(
              I18n.get(BankOrderFileZoneName.BOF_AFB_160_DCO_MAIN_DETAIL_RECORD_ZONE_D4),
              receiverBankDetails.getSortCode(),
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_NUMERIC,
              5));
      // Area D-5: Numéro de compte du tiré
      mainDetailRecordBuilder.append(
          cfonbToolService.createZone(
              I18n.get(BankOrderFileZoneName.BOF_AFB_160_DCO_MAIN_DETAIL_RECORD_ZONE_D5),
              receiverBankDetails.getAccountNbr(),
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              11));
      // Area E1: Amount
      mainDetailRecordBuilder.append(
          cfonbToolService.createZone(
              I18n.get(BankOrderFileZoneName.BOF_AFB_160_DCO_MAIN_DETAIL_RECORD_ZONE_E1),
              bankOrderLine.getBankOrderAmount(),
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_NUMERIC,
              12));
      // Area E-2: Reserved
      mainDetailRecordBuilder.append(
          cfonbToolService.createZone(
              "E2",
              "",
              cfonbToolService.STATUS_NOT_USED,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              4));
      // Area F-1: Date d'échéance
      mainDetailRecordBuilder.append(
          cfonbToolService.createZone(
              I18n.get(BankOrderFileZoneName.BOF_AFB_160_DCO_MAIN_DETAIL_RECORD_ZONE_F1),
              getDetailF1Value(bankOrderLine),
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_NUMERIC,
              6));
      // Area F-2-1: Date de création
      mainDetailRecordBuilder.append(
          cfonbToolService.createZone(
              I18n.get(BankOrderFileZoneName.BOF_AFB_160_DCO_MAIN_DETAIL_RECORD_ZONE_F21),
              getDetailF21Value(bankOrderLine),
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_NUMERIC,
              6));
      // Area F-2-2: Reserved
      mainDetailRecordBuilder.append(
          cfonbToolService.createZone(
              "F2-2",
              "",
              cfonbToolService.STATUS_NOT_USED,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              4));
      // Area F-3-1: Type
      mainDetailRecordBuilder.append(
          cfonbToolService.createZone(
              "F3-1", "", cfonbToolService.STATUS_DEPENDENT, cfonbToolService.FORMAT_NUMERIC, 1));
      // Area F-3-2: Nature
      mainDetailRecordBuilder.append(
          cfonbToolService.createZone(
              "F3-2", "", cfonbToolService.STATUS_DEPENDENT, cfonbToolService.FORMAT_NUMERIC, 3));
      // Area F-3-3: Country
      mainDetailRecordBuilder.append(
          cfonbToolService.createZone(
              "F3-3", "", cfonbToolService.STATUS_DEPENDENT, cfonbToolService.FORMAT_ALPHA, 3));
      // Area F-3-4: SIREN
      mainDetailRecordBuilder.append(
          cfonbToolService.createZone(
              I18n.get(BankOrderFileZoneName.BOF_AFB_160_DCO_MAIN_DETAIL_RECORD_ZONE_F34),
              this.partnerService.getSIRENNumber(
                  bankOrderLine.getReceiverBankDetails().getPartner()),
              cfonbToolService.STATUS_DEPENDENT,
              cfonbToolService.FORMAT_NUMERIC,
              9));
      // Area G: Reference tireur
      mainDetailRecordBuilder.append(
          cfonbToolService.createZone(
              I18n.get(BankOrderFileZoneName.BOF_AFB_160_DCO_MAIN_DETAIL_RECORD_ZONE_G),
              this.bankOrderSeq,
              cfonbToolService.STATUS_OPTIONAL,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              10));

      String detailRecord = mainDetailRecordBuilder.toString();
      cfonbToolService.toUpperCase(detailRecord);
      cfonbToolService.testLength(detailRecord, NB_CHAR_PER_LINE);

      return detailRecord;
    } catch (Exception e) {
      throw new AxelorException(
          e,
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(BankPaymentExceptionMessage.BANK_ORDER_WRONG_MAIN_DETAIL_RECORD)
              + ": "
              + e.getMessage(),
          bankOrderLine.getSequence());
    }
  }

  protected String getDetailF21Value(BankOrderLine bankOrderLine) {
    return bankOrderLine.getCreatedOn().format(DateTimeFormatter.ofPattern("ddMMyy"));
  }

  protected String getDetailF1Value(BankOrderLine bankOrderLine) {
    return this.bankOrderDate.format(DateTimeFormatter.ofPattern("ddMMyy"));
  }

  protected String getDetailD21Value() {
    return this.bankOrderFileFormat.getAcceptPerLineCodeSelect().toString();
  }

  protected String getDetailD1Value(BankOrderLine bankOrderLine) {
    if (bankOrderLine.getReceiverBankDetails() != null
        && bankOrderLine.getReceiverBankDetails().getBankAddress() != null) {
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
              I18n.get(BankOrderFileZoneName.BOF_AFB_160_DCO_ENDORSED_DETAIL_RECORD_ZONE_A),
              "07",
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_NUMERIC,
              2));
      // Area B-1 : Operation code
      endorsedDetailRecordBuilder.append(
          cfonbToolService.createZone(
              I18n.get(BankOrderFileZoneName.BOF_AFB_160_DCO_ENDORSED_DETAIL_RECORD_ZONE_B1),
              "60",
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_NUMERIC,
              2));
      // Area B-2 : Numbering record
      endorsedDetailRecordBuilder.append(
          cfonbToolService.createZone(
              I18n.get(BankOrderFileZoneName.BOF_AFB_160_DCO_ENDORSED_DETAIL_RECORD_ZONE_B2),
              numberingRecord++,
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
              I18n.get(BankOrderFileZoneName.BOF_AFB_160_DCO_ENDORSED_DETAIL_RECORD_ZONE_C2),
              senderCompany.getName(),
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
              I18n.get(BankOrderFileZoneName.BOF_AFB_160_DCO_ENDORSED_DETAIL_RECORD_ZONE_E),
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
              I18n.get(BankOrderFileZoneName.BOF_AFB_160_DCO_ENDORSED_DETAIL_RECORD_ZONE_F3),
              this.partnerService.getSIRENNumber(senderCompany.getPartner()),
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
          I18n.get(BankPaymentExceptionMessage.BANK_ORDER_WRONG_ENDORSED_DETAIL_RECORD)
              + ": "
              + e.getMessage(),
          bankOrderLine.getSequence());
    }
  }

  protected String createTotalRecord() throws AxelorException {
    StringBuilder totalRecordBuilder = new StringBuilder();
    try {
      // Area A : Register code
      totalRecordBuilder.append(
          cfonbToolService.createZone(
              I18n.get(BankOrderFileZoneName.BOF_AFB_160_DCO_TOTAL_DETAIL_RECORD_ZONE_A),
              "08",
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_NUMERIC,
              2));
      // Area B-1 : Operation code
      totalRecordBuilder.append(
          cfonbToolService.createZone(
              I18n.get(BankOrderFileZoneName.BOF_AFB_160_DCO_TOTAL_DETAIL_RECORD_ZONE_B1),
              "60",
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_NUMERIC,
              2));
      // Area B-2 : Numbering record
      totalRecordBuilder.append(
          cfonbToolService.createZone(
              I18n.get(BankOrderFileZoneName.BOF_AFB_160_DCO_TOTAL_DETAIL_RECORD_ZONE_B2),
              numberingRecord++,
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
              I18n.get(BankOrderFileZoneName.BOF_AFB_160_DCO_TOTAL_DETAIL_RECORD_ZONE_E1),
              this.arithmeticTotal,
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_NUMERIC,
              12));
      // Area E-2: Mandatory area
      totalRecordBuilder.append(
          cfonbToolService.createZone(
              I18n.get(BankOrderFileZoneName.BOF_AFB_160_DCO_TOTAL_DETAIL_RECORD_ZONE_E2),
              "0000",
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_NUMERIC,
              4));
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
              cfonbToolService.STATUS_NOT_USED,
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
          I18n.get(BankPaymentExceptionMessage.BANK_ORDER_WRONG_TOTAL_RECORD)
              + ": "
              + e.getMessage(),
          bankOrderSeq);
    }
  }

  protected String createAdditionalDetailRecord(BankOrderLine bankOrderLine)
      throws AxelorException {
    StringBuilder additionalDetailRecordBuilder = new StringBuilder();

    try {
      Address address = null;
      Partner partner = bankOrderLine.getPartner();
      if (partner.getMainAddress() != null) {
        address = partner.getMainAddress();
      } else {
        address = partnerService.getDefaultAddress(partner);
      }
      // Area A : Register code
      additionalDetailRecordBuilder.append(
          cfonbToolService.createZone(
              I18n.get(BankOrderFileZoneName.BOF_AFB_160_DCO_ADDITIONAL_DETAIL_RECORD_ZONE_A),
              "16",
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_NUMERIC,
              2));
      // Area B-1 : Operation code
      additionalDetailRecordBuilder.append(
          cfonbToolService.createZone(
              I18n.get(BankOrderFileZoneName.BOF_AFB_160_DCO_ADDITIONAL_DETAIL_RECORD_ZONE_B1),
              "60",
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_NUMERIC,
              2));
      // Area B-2 : Numbering record
      additionalDetailRecordBuilder.append(
          cfonbToolService.createZone(
              I18n.get(BankOrderFileZoneName.BOF_AFB_160_DCO_ADDITIONAL_DETAIL_RECORD_ZONE_B2),
              numberingRecord++,
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
      // Area C-1 Adresse du tiré : N° et nom de voie
      additionalDetailRecordBuilder.append(
          cfonbToolService.createZone(
              I18n.get(BankOrderFileZoneName.BOF_AFB_160_DCO_ADDITIONAL_DETAIL_RECORD_ZONE_C1),
              getAdditionalC1Value(address, partner),
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              32));
      // Area C-2 Adresse du tiré : Localité
      additionalDetailRecordBuilder.append(
          cfonbToolService.createZone(
              I18n.get(BankOrderFileZoneName.BOF_AFB_160_DCO_ADDITIONAL_DETAIL_RECORD_ZONE_C2),
              getAdditionalC2Value(address, partner),
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              32));
      // Area C-3 Adresse du tiré : Code postale et bureau distributeur
      additionalDetailRecordBuilder.append(
          cfonbToolService.createZone(
              I18n.get(BankOrderFileZoneName.BOF_AFB_160_DCO_ADDITIONAL_DETAIL_RECORD_ZONE_C3),
              getAdditionalC3Value(address, partner),
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
          I18n.get(BankPaymentExceptionMessage.BANK_ORDER_WRONG_ADDITIONAL_DETAIL_RECORD)
              + ": "
              + e.getMessage(),
          bankOrderLine.getSequence());
    }
  }

  protected String getAdditionalC3Value(Address address, Partner partner) throws AxelorException {
    if (address == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(
              BankPaymentExceptionMessage.BANK_ORDER_RECEIVER_BANK_DETAILS_MISSING_PARTNER_ZIP),
          partner.getFullName());
    }
    return address.getZip();
  }

  protected String getAdditionalC2Value(Address address, Partner partner) throws AxelorException {
    if (address == null || address.getCity() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(
              BankPaymentExceptionMessage.BANK_ORDER_RECEIVER_BANK_DETAILS_MISSING_PARTNER_CITY),
          partner.getFullName());
    }
    return address.getCity().getName();
  }

  protected String getAdditionalC1Value(Address address, Partner partner) throws AxelorException {
    if (address == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(
              BankPaymentExceptionMessage.BANK_ORDER_RECEIVER_BANK_DETAILS_MISSING_PARTNER_ADDRESS),
          partner.getFullName());
    }
    return address.getAddressL4();
  }
}
