/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
package com.axelor.apps.bankpayment.service.bankorder.file.transfer;

import com.axelor.apps.bankpayment.db.BankOrder;
import com.axelor.apps.bankpayment.db.BankOrderEconomicReason;
import com.axelor.apps.bankpayment.db.BankOrderFileFormatCountry;
import com.axelor.apps.bankpayment.db.BankOrderLine;
import com.axelor.apps.bankpayment.db.repo.BankOrderFileFormatRepository;
import com.axelor.apps.bankpayment.db.repo.BankOrderLineRepository;
import com.axelor.apps.bankpayment.exception.IExceptionMessage;
import com.axelor.apps.bankpayment.service.bankorder.file.BankOrderFileService;
import com.axelor.apps.bankpayment.service.cfonb.CfonbToolService;
import com.axelor.apps.base.db.Bank;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Country;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.repo.BankRepository;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.tool.StringTool;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;

public class BankOrderFileAFB320XCTService extends BankOrderFileService {

  protected String registrationCode;
  protected String senderAddress;
  protected CfonbToolService cfonbToolService;
  protected PartnerService partnerService;
  protected int sequence = 1;
  protected static final int NB_CHAR_PER_LINE = 320;
  protected Currency senderCurrency;
  protected String qualityOfAmount;

  /**
   * "Remises informatisées d'ordres de paiement international au format 320 caractères" révisée
   * (code opération "PI")
   */
  protected static final String OPERATION_CODE_PI = "PI";
  /**
   * "Remises informatisées d'ordres de paiement déplacé au format 320 caractères" (code opération
   * "RF")
   */
  protected static final String OPERATION_CODE_RF = "RF";
  /**
   * "Remises informatisées d'ordres de virement national France au format 320 caractères" (code
   * opération "VF")
   */
  protected static final String OPERATION_CODE_VF = "VF";

  @Inject
  public BankOrderFileAFB320XCTService(BankOrder bankOrder) throws AxelorException {

    super(bankOrder);

    this.partnerService = Beans.get(PartnerService.class);
    this.registrationCode =
        this.partnerService.getSIRENNumber(senderCompany.getPartner()); // Add it on company
    this.senderAddress = this.getSenderAddress();
    this.cfonbToolService = Beans.get(CfonbToolService.class);
    fileExtension = FILE_EXTENSION_TXT;
    this.senderCurrency = senderBankDetails.getCurrency();
    this.qualityOfAmount = bankOrderFileFormat.getQualifyingOfAmountSelect();
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
  public File generateFile()
      throws JAXBException, IOException, AxelorException, DatatypeConfigurationException {

    List<String> records = Lists.newArrayList();

    records.add(this.createSenderRecord());

    for (BankOrderLine bankOrderLine : bankOrderLineList) {

      records.add(this.createDetailRecord(bankOrderLine));

      if (bankOrderLine.getPaymentModeSelect()
          == BankOrderLineRepository.PAYMENT_MODE_TRANSFER_OR_OTHER) {
        records.add(this.createDependentReceiverBankRecord(bankOrderLine));
      }
      if (this.useOptionnalFurtherInformationRecord(bankOrderLine)) {
        records.add(this.createOptionnalFurtherInformationRecord(bankOrderLine));
      }
    }

    records.add(this.createTotalRecord());

    fileToCreate = records;

    return super.generateFile();
  }

  protected boolean useOptionnalFurtherInformationRecord(BankOrderLine bankOrderLine) {

    if (Strings.isNullOrEmpty(bankOrderLine.getPaymentReasonLine1())
        && Strings.isNullOrEmpty(bankOrderLine.getPaymentReasonLine2())
        && Strings.isNullOrEmpty(bankOrderLine.getPaymentReasonLine3())
        && Strings.isNullOrEmpty(bankOrderLine.getPaymentReasonLine4())) {
      return false;
    }

    return true;
  }

  /**
   * Method to create a sender record for international transfer AFB320
   *
   * @param company
   * @param ZonedDateTime
   * @return
   * @throws AxelorException
   */
  protected String createSenderRecord() throws AxelorException {

    try {

      Bank senderBank = senderBankDetails.getBank();

      // Zone 1 : Code enregistrement
      String senderRecord =
          cfonbToolService.createZone(
              "1", "03", cfonbToolService.STATUS_MANDATORY, cfonbToolService.FORMAT_NUMERIC, 2);

      // Zone 2 : Code opération
      senderRecord +=
          cfonbToolService.createZone(
              "2",
              OPERATION_CODE_PI,
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              2);

      // Zone 3 : Numéro séquentiel
      senderRecord +=
          cfonbToolService.createZone(
              "3",
              sequence++,
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_NUMERIC,
              6);

      // Zone 4 : Date de création
      senderRecord +=
          cfonbToolService.createZone(
              I18n.get("4 - Generation date"),
              this.generationDateTime.format(DateTimeFormatter.ofPattern("yyyyMMdd")),
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_NUMERIC,
              8);

      // Zone 5 : Raison sociale émetteur
      senderRecord +=
          cfonbToolService.createZone(
              I18n.get("5 - Sender company"),
              senderCompany.getName(),
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              35);

      // Zone 6 : Adresse de l'émetteur
      senderRecord +=
          cfonbToolService.createZone(
              I18n.get("6 - Sender address"),
              senderAddress,
              cfonbToolService.STATUS_OPTIONAL,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              3 * 35);

      // Zone 7 : N° SIRET de l'émetteur
      senderRecord +=
          cfonbToolService.createZone(
              I18n.get("7 - Registration code"),
              registrationCode,
              cfonbToolService.STATUS_DEPENDENT,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              14);

      // Zone 8 : Référence remise
      senderRecord +=
          cfonbToolService.createZone(
              I18n.get("8 - Sequence"),
              bankOrderSeq,
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              16);

      // Zone 9 : Code BIC banque d'exécution
      senderRecord +=
          cfonbToolService.createZone(
              "9", "", cfonbToolService.STATUS_OPTIONAL, cfonbToolService.FORMAT_ALPHA_NUMERIC, 11);

      // Zone 10 : Type identifiant du compte à débiter à la banque d'éxécution ("1" : IBAN, "2" :
      // Identifiant national, "0" : Autre )
      senderRecord +=
          cfonbToolService.createZone(
              I18n.get("10 - Sender bank details type"),
              senderBank.getBankDetailsTypeSelect(),
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_NUMERIC,
              1);

      // Zone 11 : Identifiant du compte à débiter à la banque d'exécution
      senderRecord +=
          cfonbToolService.createZone(
              I18n.get("11 - Sender bank details IBAN"),
              getIban(senderBankDetails),
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              34);

      // Zone 12 : Code devise du compte à débiter à la banque d'éxécution
      senderRecord +=
          cfonbToolService.createZone(
              I18n.get("12 - Bank order currency"),
              senderCurrency.getCode(),
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              3);

      // Zone 13 : Identification du contrat/client
      senderRecord +=
          cfonbToolService.createZone(
              "13",
              "",
              cfonbToolService.STATUS_OPTIONAL,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              16);

      // Zone 14 : Type identifiant du compte émetteur ("1" : IBAN, "2" : Identifiant national, "0"
      // : Autre )
      senderRecord +=
          cfonbToolService.createZone(
              I18n.get("14 - Sender bank details type"),
              senderBank.getBankDetailsTypeSelect(),
              cfonbToolService.STATUS_DEPENDENT,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              1);

      // Zone 15 : Identifiant du compte émetteur
      senderRecord +=
          cfonbToolService.createZone(
              I18n.get("15 - Sender bank details IBAN"),
              getIban(senderBankDetails),
              cfonbToolService.STATUS_DEPENDENT,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              34);

      // Zone 16 : Code devise du compte émetteur (Norme ISO)
      senderRecord +=
          cfonbToolService.createZone(
              I18n.get("16 - Bank order currency"),
              senderCurrency.getCode(),
              cfonbToolService.STATUS_DEPENDENT,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              3);

      // Zone 17-1 : Zone non utilisée
      senderRecord +=
          cfonbToolService.createZone(
              "17-1",
              "",
              cfonbToolService.STATUS_NOT_USED,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              4);

      // Zone 17-2 : Zone non utilisée
      senderRecord +=
          cfonbToolService.createZone(
              "17-2",
              "",
              cfonbToolService.STATUS_NOT_USED,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              1);

      // Zone 17-3 : Qualifiant de la date ("203" (date d'exécution demandée) valeur par défaut,
      // "227" soumis à accord contractuel avec la banque)
      senderRecord +=
          cfonbToolService.createZone(
              I18n.get("17-3 - File format qualifying date"),
              bankOrderFileFormat.getQualifyingOfDate(),
              cfonbToolService.STATUS_OPTIONAL,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              3);

      // Zone 17-4 : Zone réservée
      senderRecord +=
          cfonbToolService.createZone(
              "17-4",
              "",
              cfonbToolService.STATUS_NOT_USED,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              8);

      // Zone 18 : Indice type de débit de la remise
      senderRecord +=
          cfonbToolService.createZone(
              I18n.get("18 - Order debit type index"),
              bankOrderFileFormat.getOrderDebitTypeSelect(),
              cfonbToolService.STATUS_DEPENDENT,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              1);

      // Zone 19 : Indice type de remises :
      // "1" : mono date et mono devise : La date et la devise sont prises dans l'enregistrement
      // "En-tête".
      // "2" : mono date et multi devises : La date est prise dans l'enregistrement "En-tête" et la
      // "devise" dans les enregistrements "Détail de l’opération".
      // "3" : multi dates et mono devise : la date est prise dans les enregistrements "Détail de
      // l’opération"et la devise dans l'enregistrement "En-tête".
      // "4" : multi dates et multi devises : la date et la devise sont prises dans les
      // enregistrements "Détail de l’opération".
      // NB : La valeur par défaut est "1". La possibilité d'utiliser les autres valeurs doit être
      // vérifiée auprès de la banque d'acheminement.
      senderRecord +=
          cfonbToolService.createZone(
              I18n.get("19 - Order index type"),
              this.getOrderIndexType(isMultiDates, isMultiCurrencies),
              cfonbToolService.STATUS_OPTIONAL,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              1);

      // Zone 20 : Date :
      // Cette donnée est obligatoire pour les remises mono-date (zone 19 de l'"Entête" = "1" ou
      // "2"), pour les autres remises, elle ne doit pas être renseignée.
      if (!isMultiDates) {
        senderRecord +=
            cfonbToolService.createZone(
                I18n.get("20 - Bank order date"),
                bankOrderDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")),
                cfonbToolService.STATUS_MANDATORY,
                cfonbToolService.FORMAT_NUMERIC,
                8);
      } else {
        senderRecord +=
            cfonbToolService.createZone(
                "20", "", cfonbToolService.STATUS_NOT_USED, cfonbToolService.FORMAT_NUMERIC, 8);
      }

      // Zone 21 : Code devise des ordres de paiements
      // Norme ISO :
      // Cette donnée est obligatoire pour les remises mono-devise (zone 19 de l'"Entête" = "1" ou
      // "3"), pour les autres remises, elle ne doit pas être renseignée.
      if (!isMultiCurrencies) {
        senderRecord +=
            cfonbToolService.createZone(
                I18n.get("21 - Currency"),
                bankOrderCurrency.getCode(),
                cfonbToolService.STATUS_MANDATORY,
                cfonbToolService.FORMAT_ALPHA_NUMERIC,
                3);
      } else {
        senderRecord +=
            cfonbToolService.createZone(
                "21",
                "",
                cfonbToolService.STATUS_NOT_USED,
                cfonbToolService.FORMAT_ALPHA_NUMERIC,
                3);
      }

      cfonbToolService.toUpperCase(senderRecord);

      cfonbToolService.testLength(senderRecord, NB_CHAR_PER_LINE);

      return senderRecord;

    } catch (Exception e) {
      throw new AxelorException(
          e,
          senderBankDetails,
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(IExceptionMessage.BANK_ORDER_WRONG_SENDER_RECORD) + ": " + e.getMessage(),
          bankOrderSeq);
    }
  }

  /**
   * 3.1.2 Identification des comptes Les zones "compte à débiter" et "compte du bénéficiaire"
   * doivent respecter les règles suivantes : lorsque la zone "type identifiant de compte" est
   * renseignée, elle prend les valeurs : "1" si le compte est identifié par un IBAN lequel doit
   * être cadré à gauche, "2" si le compte est identifié par un identifiant national, lequel doit
   * alors être précédé de quatre blancs, "0" dans les autres cas ; l'identifiant doit alors
   * également être précédé de quatre blancs.
   *
   * @param bankDetails
   * @return
   * @throws AxelorException
   */
  public String getIban(BankDetails bankDetails) throws AxelorException {

    String iban = bankDetails.getIban();

    if (Strings.isNullOrEmpty(iban)) {
      throw new AxelorException(
          bankDetails,
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(IExceptionMessage.BANK_ORDER_BANK_DETAILS_EMPTY_IBAN),
          bankDetails.getOwnerName(),
          bankOrderSeq);
    }

    switch (bankDetails.getBank().getBankDetailsTypeSelect()) {
      case BankRepository.BANK_IDENTIFIER_TYPE_IBAN:
        return StringTool.fillStringRight(bankDetails.getIban(), ' ', 34);

      case BankRepository.BANK_IDENTIFIER_TYPE_NATIONAL:
        return StringTool.fillStringRight(
            StringTool.fillString(' ', 4) + bankDetails.getIban(), ' ', 34);

      case BankRepository.BANK_IDENTIFIER_TYPE_OTHER:
        return StringTool.fillStringRight(
            StringTool.fillString(' ', 4) + bankDetails.getIban(), ' ', 34);

      default:
        return StringTool.fillStringRight(bankDetails.getIban(), ' ', 34);
    }
  }

  /**
   * Indice type de remises : "1" : mono date et mono devise : La date et la devise sont prises dans
   * l'enregistrement "En-tête". "2" : mono date et multi devises : La date est prise dans
   * l'enregistrement "En-tête" et la "devise" dans les enregistrements "Détail de l’opération". "3"
   * : multi dates et mono devise : la date est prise dans les enregistrements "Détail de
   * l’opération"et la devise dans l'enregistrement "En-tête". "4" : multi dates et multi devises :
   * la date et la devise sont prises dans les enregistrements "Détail de l’opération". NB : La
   * valeur par défaut est "1". La possibilité d'utiliser les autres valeurs doit être vérifiée
   * auprès de la banque d'acheminement.
   *
   * @param isMultiDates
   * @param isMultiCurrencies
   * @return
   */
  protected int getOrderIndexType(boolean isMultiDates, boolean isMultiCurrencies) {

    int orderIndexType = 1;

    if (isMultiDates) {
      orderIndexType += 2;
    }
    if (isMultiCurrencies) {
      orderIndexType += 1;
    }

    return orderIndexType;
  }

  /**
   * Method to create a recipient record for international transfer AFB320
   *
   * @param company
   * @param ZonedDateTime
   * @return
   * @throws AxelorException
   */
  protected String createDetailRecord(BankOrderLine bankOrderLine) throws AxelorException {

    try {
      BankDetails receiverBankDetails = bankOrderLine.getReceiverBankDetails();

      // Zone 1 : Code enregistrement
      String detailRecord =
          cfonbToolService.createZone(
              "1", "04", cfonbToolService.STATUS_MANDATORY, cfonbToolService.FORMAT_NUMERIC, 2);

      // Zone 2 : Code opération
      detailRecord +=
          cfonbToolService.createZone(
              "2",
              OPERATION_CODE_PI,
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              2);

      // Zone 3 : Numéro séquentiel
      detailRecord +=
          cfonbToolService.createZone(
              "3",
              sequence++,
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_NUMERIC,
              6);

      // Zone 4 : Type identifiant du compte du bénéficiaire ("1" : IBAN, "2" : Identifiant
      // national, "0" : Autre )
      if (receiverBankDetails.getBank() == null) {
        throw new AxelorException(
            I18n.get(IExceptionMessage.BANK_ORDER_RECEIVER_BANK_DETAILS_MISSING_BANK),
            TraceBackRepository.CATEGORY_MISSING_FIELD);
      }
      detailRecord +=
          cfonbToolService.createZone(
              I18n.get("4 - Bank details type"),
              receiverBankDetails.getBank().getBankDetailsTypeSelect(),
              cfonbToolService.STATUS_DEPENDENT,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              1);

      // Zone 5 : Identifiant du compte du bénéficiaire
      detailRecord +=
          cfonbToolService.createZone(
              I18n.get("5 - Bank details IBAN"),
              getIban(receiverBankDetails),
              cfonbToolService.STATUS_DEPENDENT,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              34);

      // Zone 6 : Nom du bénéficiaire
      detailRecord +=
          cfonbToolService.createZone(
              I18n.get("6 - Bank details owner name"),
              receiverBankDetails.getOwnerName(),
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              35);

      // Zone 7 : Adresse du bénéficiaire (Obligatoire si mode de règlement par chèque (zone 18 =
      // "1" ou "2")
      // Si le nom du bénéficiaire contient plus de 35 caractères, utiliser le début de la première
      // zone pour le compléter et le reste de cette zone pour indiquer le début de l'adresse)

      detailRecord +=
          cfonbToolService.createZone(
              "7",
              getReceiverAddress(bankOrderLine),
              cfonbToolService.STATUS_DEPENDENT,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              3 * 35);

      // Zone 8 : Identification nationale du bénéficiaire (Cette zone n'est pas utilisée)
      detailRecord +=
          cfonbToolService.createZone(
              "8", "", cfonbToolService.STATUS_OPTIONAL, cfonbToolService.FORMAT_ALPHA_NUMERIC, 17);

      // Zone 9 : Code pays du bénéficiaire (Norme ISO)
      Country receiverCountry = bankOrderLine.getReceiverCountry();
      String countryCode = "";
      if (receiverCountry != null) {
        countryCode = receiverCountry.getAlpha2Code();
      }
      detailRecord +=
          cfonbToolService.createZone(
              I18n.get("9 - Country code"),
              countryCode,
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              2);

      // Zone 10 : Référence de l'opération
      detailRecord +=
          cfonbToolService.createZone(
              I18n.get("10 - Sequence"),
              bankOrderLine.getSequence(),
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              16);

      // Zone 11 : Qualifiant du montant de l'ordre :
      // "T" : Montant exprimé dans la devise du transfert
      // "D" : Montant équivalent exprimé dans la devise du compte à débiter. Cette valeur ne doit
      // être utilisée que lorsque la devise du compte à débiter est différente de celle du
      // transfert.
      String qualifyngOfAmountStr;
      if (senderCurrency.equals(bankOrderCurrency)) {
        qualifyngOfAmountStr = BankOrderFileFormatRepository.QUALIFYING_AMOUNT_TRANSFER_CURRENCY;
      } else {
        qualifyngOfAmountStr = qualityOfAmount;
        if (Strings.isNullOrEmpty(qualifyngOfAmountStr)) {
          qualifyngOfAmountStr = BankOrderFileFormatRepository.QUALIFYING_AMOUNT_TRANSFER_CURRENCY;
        }
      }
      detailRecord +=
          cfonbToolService.createZone(
              "11",
              qualifyngOfAmountStr,
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              1);

      // Zone 12 : Zone réservée
      detailRecord +=
          cfonbToolService.createZone(
              "12", "", cfonbToolService.STATUS_NOT_USED, cfonbToolService.FORMAT_ALPHA_NUMERIC, 4);

      // Zone 13 : Montant de l'ordre (Le montant comporte le nombre de décimales indiqué dans la
      // zone "Nombre de décimales" du même enregistrement)
      BigDecimal orderAmount;
      if (qualifyngOfAmountStr.equals(
          BankOrderFileFormatRepository.QUALIFYING_AMOUNT_SENDER_BANK_DETAILS_CURRENCY)) {
        orderAmount = bankOrderLine.getCompanyCurrencyAmount();
      } else {
        orderAmount = bankOrderLine.getBankOrderAmount();
      }
      detailRecord +=
          cfonbToolService.createZone(
              "13",
              orderAmount,
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_NUMERIC,
              14);

      // Zone 14 : Nombre de décimales
      detailRecord +=
          cfonbToolService.createZone(
              "14",
              "2",
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_NUMERIC,
              1); // TODO

      // Zone 15 : Zone réservée
      detailRecord +=
          cfonbToolService.createZone(
              "15", "", cfonbToolService.STATUS_NOT_USED, cfonbToolService.FORMAT_ALPHA_NUMERIC, 1);

      // Zone 16 : Code motif économique (3 caract. numériques ou valeur "NNN" )
      BankOrderEconomicReason bankOrderEconomicReason = bankOrderLine.getBankOrderEconomicReason();
      String bankOrderEconomicReasonCode = "NNN";
      if (bankOrderEconomicReason != null) {
        bankOrderEconomicReasonCode = bankOrderEconomicReason.getCode();
      }
      detailRecord +=
          cfonbToolService.createZone(
              I18n.get("16 - Economic reason code"),
              bankOrderEconomicReasonCode,
              cfonbToolService.STATUS_OPTIONAL,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              3);

      // Zone 17 : Zone réservée
      detailRecord +=
          cfonbToolService.createZone(
              "17", "", cfonbToolService.STATUS_NOT_USED, cfonbToolService.FORMAT_ALPHA_NUMERIC, 2);

      // Zone 18 : Mode de règlement ("0" = Virement ou autre sauf chèque, "1" ou "2" = par chèque)
      detailRecord +=
          cfonbToolService.createZone(
              I18n.get("18 - Payment mode select"),
              bankOrderLine.getPaymentModeSelect(),
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              1);

      // Zone 19 : Code imputation des frais ("13" = Bénéficiaire (BEN), "14" = Emetteur et
      // Bénéficiaire (SHA), "15" = Emetteur (OUR))
      detailRecord +=
          cfonbToolService.createZone(
              I18n.get("19 - Fees imputation mode"),
              bankOrderLine.getFeesImputationModeSelect(),
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_NUMERIC,
              2);

      // Zone 23 : Zone réservée
      detailRecord +=
          cfonbToolService.createZone(
              "23",
              "",
              cfonbToolService.STATUS_NOT_USED,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              57);

      // Zone 24-1 : Qualifiant de la date ("203" (date d'exécution demandée))
      detailRecord +=
          cfonbToolService.createZone(
              I18n.get("24-1 - Qualifying of date"),
              bankOrderFileFormat.getQualifyingOfDate(),
              cfonbToolService.STATUS_OPTIONAL,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              3);

      // Zone 24-2 : Date ( Cette donnée est obligatoire pour les remises multi dates (zone 19 de
      // l'"Entête" = "3" ou "4"), pour les autres remises, elle ne doit pas être renseignée.)
      if (isMultiDates) {
        String bankOrderDate = "";
        if (bankOrderLine.getBankOrderDate() != null) {
          bankOrderDate =
              bankOrderLine.getBankOrderDate().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        }
        detailRecord +=
            cfonbToolService.createZone(
                I18n.get("24-2 - Date"),
                bankOrderDate,
                cfonbToolService.STATUS_MANDATORY,
                cfonbToolService.FORMAT_NUMERIC,
                8);
      } else {
        detailRecord +=
            cfonbToolService.createZone(
                I18n.get("24-2 - Date"),
                "",
                cfonbToolService.STATUS_NOT_USED,
                cfonbToolService.FORMAT_NUMERIC,
                8);
      }

      // Zone 25 : Code devise du transfert (Cette donnée est obligatoire pour les remises multi
      // devise (zone 19 de l'"Entête" = "2" ou "4"), pour les autres remises, elle ne doit pas être
      // renseignée.
      if (isMultiCurrencies) {
        String bankOrderCurrencyCode = "";
        if (bankOrderLine.getBankOrderCurrency() != null) {
          bankOrderCurrencyCode = bankOrderLine.getBankOrderCurrency().getCode();
        }
        detailRecord +=
            cfonbToolService.createZone(
                I18n.get("25 - Currency"),
                bankOrderCurrencyCode,
                cfonbToolService.STATUS_MANDATORY,
                cfonbToolService.FORMAT_ALPHA_NUMERIC,
                3);

      } else {
        detailRecord +=
            cfonbToolService.createZone(
                I18n.get("25 - Currency"),
                "",
                cfonbToolService.STATUS_NOT_USED,
                cfonbToolService.FORMAT_ALPHA_NUMERIC,
                3);
      }

      cfonbToolService.toUpperCase(detailRecord);

      cfonbToolService.testLength(detailRecord, NB_CHAR_PER_LINE);

      return detailRecord;

    } catch (Exception e) {
      throw new AxelorException(
          e,
          bankOrderLine,
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(IExceptionMessage.BANK_ORDER_WRONG_MAIN_DETAIL_RECORD) + ": " + e.getMessage(),
          bankOrderLine.getSequence());
    }
  }

  protected String getReceiverAddress(BankOrderLine bankOrderLine) throws AxelorException {

    String receiverAddress = bankOrderLine.getReceiverAddressStr();

    if (Strings.isNullOrEmpty(receiverAddress)) {
      if (receiverAddressRequired(bankOrderLine.getReceiverCountry())) {

        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            String.format(
                I18n.get(IExceptionMessage.BANK_ORDER_LINE_NO_RECEIVER_ADDRESS),
                bankOrderLine.getPartner().getFullName()));
      } else {
        return "";
      }
    }

    return receiverAddress;
  }

  protected boolean receiverAddressRequired(Country country) {

    if (bankOrderFileFormat.getBankOrderFileFormatCountryList() == null || country == null) {
      return false;
    }

    for (BankOrderFileFormatCountry bankOrderFileFormatCountry :
        bankOrderFileFormat.getBankOrderFileFormatCountryList()) {

      if (bankOrderFileFormatCountry.getCountry().equals(country)
          && bankOrderFileFormatCountry.getReceiverAddressRequired()) {
        return true;
      }
    }

    return false;
  }

  /**
   * Method to create a dependent receiver bank record for international transfer AFB320
   *
   * @param company
   * @param ZonedDateTime
   * @return
   * @throws AxelorException
   */
  protected String createDependentReceiverBankRecord(BankOrderLine bankOrderLine)
      throws AxelorException {

    try {

      BankDetails receiverBankDetails = bankOrderLine.getReceiverBankDetails();
      Bank bank = receiverBankDetails.getBank();

      // Zone 1 : Code enregistrement
      String totalRecord =
          cfonbToolService.createZone(
              "1", "05", cfonbToolService.STATUS_MANDATORY, cfonbToolService.FORMAT_NUMERIC, 2);

      // Zone 2 : Code opération
      totalRecord +=
          cfonbToolService.createZone(
              "2",
              OPERATION_CODE_PI,
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              2);

      // Zone 3 : Numéro séquentiel
      totalRecord +=
          cfonbToolService.createZone(
              "3",
              sequence++,
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_NUMERIC,
              6);

      // Zone 4 : Nom de la banque du bénéficiaire (A ne renseigner que si le code BIC de la banque
      // du bénéficiaire est absent.
      // Si cette zone est renseignée ainsi que le code BIC, elle est ignorée par la banque sauf en
      // cas d'anomalie sur le code BIC.)
      totalRecord +=
          cfonbToolService.createZone(
              I18n.get("4 - Bank name"),
              bank.getBankName(),
              cfonbToolService.STATUS_DEPENDENT,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              35);

      // Zone 5 : Localisation de l'agence (Si le nom de la banque contient plus de 35 caractères,
      // utiliser le début de la première zone
      // pour le compléter et le reste de cette zone pour indiquer le début de l'adresse)
      String bankAddress = "";
      if (bank.getBankName() != null && bank.getBankName().length() > 35) {
        bankAddress = bank.getBankName().substring(35) + " ";
      }
      if (receiverBankDetails.getBankAddress() != null) {
        bankAddress += receiverBankDetails.getBankAddress().getAddress();
      }
      totalRecord +=
          cfonbToolService.createZone(
              I18n.get("5 - Bank address"),
              bankAddress,
              cfonbToolService.STATUS_DEPENDENT,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              3 * 35);

      // Zone 6 : Code BIC de la banque du bénéficiaire (Si ce code est renseigné, c'est lui qui est
      // utilisé pour identifier la banque du bénéficiaire.
      // C'est cette option qui est préconisée pour identifier la banque du bénéficiaire. )
      totalRecord +=
          cfonbToolService.createZone(
              I18n.get("6 - Bank code"),
              bank.getCode(),
              cfonbToolService.STATUS_OPTIONAL,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              11);

      // Zone 7 : Code pays de la banque du bénéficiaire (Norme ISO.)
      String countryCode = "";
      if (bank.getCountry() != null) {
        countryCode = bank.getCountry().getAlpha2Code();
      }
      totalRecord +=
          cfonbToolService.createZone(
              I18n.get("7 - Country code"),
              countryCode,
              cfonbToolService.STATUS_OPTIONAL,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              2);

      // Zone 8 : Zone réservée
      totalRecord +=
          cfonbToolService.createZone(
              "8",
              "",
              cfonbToolService.STATUS_NOT_USED,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              157);

      cfonbToolService.toUpperCase(totalRecord);

      cfonbToolService.testLength(totalRecord, NB_CHAR_PER_LINE);

      return totalRecord;

    } catch (Exception e) {
      throw new AxelorException(
          e.getCause(),
          bankOrderLine,
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(IExceptionMessage.BANK_ORDER_WRONG_BENEFICIARY_BANK_DETAIL_RECORD)
              + ": "
              + e.getMessage(),
          bankOrderLine.getSequence());
    }
  }

  /**
   * Method to create an optional further information record for international transfer AFB320
   *
   * @param company
   * @param ZonedDateTime
   * @return
   * @throws AxelorException
   */
  protected String createOptionnalFurtherInformationRecord(BankOrderLine bankOrderLine)
      throws AxelorException {

    try {

      // Zone 1 : Code enregistrement
      String totalRecord =
          cfonbToolService.createZone(
              "1", "07", cfonbToolService.STATUS_MANDATORY, cfonbToolService.FORMAT_NUMERIC, 2);

      // Zone 2 : Code opération
      totalRecord +=
          cfonbToolService.createZone(
              "2",
              OPERATION_CODE_PI,
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              2);

      // Zone 3 : Numéro séquentiel
      totalRecord +=
          cfonbToolService.createZone(
              "3",
              sequence++,
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_NUMERIC,
              6);

      // Zone 4 : Motif du règlement :
      // Ces 4 zones de 35 caractères sont à la disposition du donneur d'ordre et à destination du
      // bénéficiaire. Pour faciliter l'identification des références transmises
      // dans ces zones, le donneur d'ordre peut utiliser les mots clé suivant : /INV/, /IPI/,
      // /RFB/, /ROC/
      totalRecord +=
          cfonbToolService.createZone(
              I18n.get("4-1 - Payment Reason 1"),
              bankOrderLine.getPaymentReasonLine1(),
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              35);
      totalRecord +=
          cfonbToolService.createZone(
              I18n.get("4-2 - Payment Reason 2"),
              bankOrderLine.getPaymentReasonLine2(),
              cfonbToolService.STATUS_OPTIONAL,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              35);
      totalRecord +=
          cfonbToolService.createZone(
              I18n.get("4-3 - Payment Reason 3"),
              bankOrderLine.getPaymentReasonLine3(),
              cfonbToolService.STATUS_OPTIONAL,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              35);
      totalRecord +=
          cfonbToolService.createZone(
              I18n.get("4-4 - Payment Reason 4"),
              bankOrderLine.getPaymentReasonLine4(),
              cfonbToolService.STATUS_OPTIONAL,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              35);

      // Zone 5 : Zone non utilisée
      totalRecord +=
          cfonbToolService.createZone(
              "5", "", cfonbToolService.STATUS_NOT_USED, cfonbToolService.FORMAT_ALPHA_NUMERIC, 1);

      // Zone 6 : Zone non utilisée
      totalRecord +=
          cfonbToolService.createZone(
              "6", "", cfonbToolService.STATUS_NOT_USED, cfonbToolService.FORMAT_ALPHA_NUMERIC, 16);

      // Zone 7 : Zone non utilisée
      totalRecord +=
          cfonbToolService.createZone(
              "7", "", cfonbToolService.STATUS_NOT_USED, cfonbToolService.FORMAT_ALPHA_NUMERIC, 8);

      // Zone 8 : Zone non utilisée
      totalRecord +=
          cfonbToolService.createZone(
              "8", "", cfonbToolService.STATUS_NOT_USED, cfonbToolService.FORMAT_ALPHA_NUMERIC, 12);

      // Zone 9 : Instructions particulières
      // Ces instructions particulières sont destinées à la banque d'exécution et éventuellement à
      // la banque du bénéficiaire.
      // Elles sont soumises à accord contractuel. Lorsqu'elles sont utilisées elles doivent
      // respecter les règles d'utilisations ci-dessous (2)
      totalRecord +=
          cfonbToolService.createZone(
              I18n.get("9-1 - Special instructions 1"),
              bankOrderLine.getSpecialInstructionsLine1(),
              cfonbToolService.STATUS_OPTIONAL,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              35);
      totalRecord +=
          cfonbToolService.createZone(
              I18n.get("9-2 - Special instructions 2"),
              bankOrderLine.getSpecialInstructionsLine2(),
              cfonbToolService.STATUS_OPTIONAL,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              35);
      totalRecord +=
          cfonbToolService.createZone(
              I18n.get("9-3 - Special instructions 3"),
              bankOrderLine.getSpecialInstructionsLine3(),
              cfonbToolService.STATUS_OPTIONAL,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              35);

      // Zone 10 : Zone réservée
      totalRecord +=
          cfonbToolService.createZone(
              "8", "", cfonbToolService.STATUS_NOT_USED, cfonbToolService.FORMAT_ALPHA_NUMERIC, 28);

      cfonbToolService.toUpperCase(totalRecord);

      cfonbToolService.testLength(totalRecord, NB_CHAR_PER_LINE);

      return totalRecord;

    } catch (Exception e) {
      throw new AxelorException(
          e,
          bankOrderLine,
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(IExceptionMessage.BANK_ORDER_WRONG_FURTHER_INFORMATION_DETAIL_RECORD)
              + ": "
              + e.getMessage(),
          bankOrderLine.getSequence());
    }
  }

  /**
   * Ces 4 zones de 35 caractères sont à la disposition du donneur d'ordre et à destination du
   * bénéficiaire. Pour faciliter l'identification des références transmises dans ces zones, le
   * donneur d'ordre peut utiliser les mots clé suivant : /INV/, /IPI/, /RFB/, /ROC/
   *
   * <p>Mot clé | Signification | Suivi de :
   * ---------------------------------------------------------------------------------------------------------------------------
   * INV | Facture (Invoice) | Date | | Référence | | Détail éventuel de la facture | | (ces trois
   * zones séparées par des blancs) IPI | International Payment Instruction (pied de facture
   * normalisé) | Référence de 20 caractères maximum RFB | Référence pour le bénéficiaire |
   * Référence de 20 caractères maximum ROC | Référence du donneur d'ordre | Référence dans la
   * limite de la longueur disponible
   *
   * <p>Les mots clé sont placés entre deux "/". Si un mot clé n'est pas placé en début d'une des
   * zones de 35 caractères il doit être précédé par un double "/" ("//"). Exemple 1 : /INV/20040423
   * 1234567 36 BOITES DE GATEAUX /RFB/AKC2847312
   *
   * <p>Exemple 2 : /INV/20040423 1234567 36 BOITES DE GATEAUX//RFB/AKC2847312
   *
   * @param bankOrderLine
   * @return
   */
  protected String computePaymentReason(BankOrderLine bankOrderLine) {

    String paymentReason = "";

    if (!Strings.isNullOrEmpty(bankOrderLine.getReceiverReference())) {
      paymentReason += bankOrderLine.getReceiverReference();
    }
    if (!Strings.isNullOrEmpty(bankOrderLine.getReceiverLabel())) {
      if (!Strings.isNullOrEmpty(paymentReason)) {
        paymentReason += "/";
      }
      paymentReason += bankOrderLine.getReceiverLabel();
    }
    return paymentReason;
  }

  /**
   * Method to create a total record for internationnal transfer AFB320
   *
   * @param company
   * @param ZonedDateTime
   * @return
   * @throws AxelorException
   */
  protected String createTotalRecord() throws AxelorException {

    try {
      // Zone 1 : Code enregistrement
      String totalRecord =
          cfonbToolService.createZone(
              "1", "08", cfonbToolService.STATUS_MANDATORY, cfonbToolService.FORMAT_NUMERIC, 2);

      // Zone 2 : Code opération
      totalRecord +=
          cfonbToolService.createZone(
              "2",
              OPERATION_CODE_PI,
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              2);

      // Zone 3 : Numéro séquentiel
      totalRecord +=
          cfonbToolService.createZone(
              "3", sequence, cfonbToolService.STATUS_MANDATORY, cfonbToolService.FORMAT_NUMERIC, 6);

      // Zone 4 : Date de création
      totalRecord +=
          cfonbToolService.createZone(
              "4",
              this.generationDateTime.format(DateTimeFormatter.ofPattern("yyyyMMdd")),
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_NUMERIC,
              8);

      // Zone 5 : Zone réservée
      totalRecord +=
          cfonbToolService.createZone(
              "5",
              "",
              cfonbToolService.STATUS_NOT_USED,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              4 * 35);

      // Zone 6 : N° SIRET de l'émetteur
      totalRecord +=
          cfonbToolService.createZone(
              I18n.get("6 - Registration code"),
              registrationCode,
              cfonbToolService.STATUS_DEPENDENT,
              cfonbToolService.FORMAT_NUMERIC,
              14);

      // Zone 7 : Référence remise
      totalRecord +=
          cfonbToolService.createZone(
              I18n.get("7 - Sequence"),
              bankOrderSeq,
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              16);

      // Zone 8 : Zone réservée
      totalRecord +=
          cfonbToolService.createZone(
              "8", "", cfonbToolService.STATUS_NOT_USED, cfonbToolService.FORMAT_ALPHA_NUMERIC, 11);

      // Zone 9 : Type identifiant du compte à débiter à la banque d'éxécution ("1" : IBAN, "2" :
      // Identifiant national, "0" : Autre )
      totalRecord +=
          cfonbToolService.createZone(
              I18n.get("9 - Bank details type"),
              senderBankDetails.getBank().getBankDetailsTypeSelect(),
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_NUMERIC,
              1);

      // Zone 10 : Identifiant du compte à débiter à la banque d'éxécution
      totalRecord +=
          cfonbToolService.createZone(
              I18n.get("10 - Bank details IBAN"),
              getIban(senderBankDetails),
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              34);

      // Zone 11 : Code devise du compte à débiter à la banque d'éxécution
      totalRecord +=
          cfonbToolService.createZone(
              I18n.get("11 - Bank details code"),
              senderCurrency.getCode(),
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              3);

      // Zone 12 : Identification du contrat/client
      totalRecord +=
          cfonbToolService.createZone(
              "12",
              "",
              cfonbToolService.STATUS_OPTIONAL,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              16);

      // Zone 13 : TOTAL DE CONTROLE
      totalRecord +=
          cfonbToolService.createZone(
              I18n.get("13 - Total"),
              arithmeticTotal,
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_NUMERIC,
              18);

      // Zone 14 : Zone réservée
      totalRecord +=
          cfonbToolService.createZone(
              "14",
              "",
              cfonbToolService.STATUS_NOT_USED,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              49);

      cfonbToolService.toUpperCase(totalRecord);

      cfonbToolService.testLength(totalRecord, NB_CHAR_PER_LINE);

      return totalRecord;

    } catch (Exception e) {
      throw new AxelorException(
          e,
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(IExceptionMessage.BANK_ORDER_WRONG_TOTAL_RECORD) + ": " + e.getMessage(),
          bankOrderSeq);
    }
  }
}
