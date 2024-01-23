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
import com.axelor.apps.bankpayment.service.cfonb.CfonbToolService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.PartnerService;
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

public abstract class BankOrderFileAFB160Service extends BankOrderFileService {

  protected CfonbToolService cfonbToolService;
  protected PartnerService partnerService;
  protected final int NB_CHAR_PER_LINE = 160;

  // Virements Ordinaires
  protected final String OPERATION_STANDARD_TRANSFER = "02";
  // Virements à vérifier
  protected final String OPERATION_TO_CHECK_TRANSFER = "29";
  // Virements Particuliers ("Aide Personnalisée au Logement", APL identifié par le n° d'émetteur
  // 900xxx)
  protected final String OPERATION_INDIVIDUAL_TRANSFER = "22";
  // Virements à Echéance "E-3" (à échanger 3 jours ouvrés avant l'échéance)
  protected final String OPERATION_SCHEDULE_E3_TRANSFER = "27";
  // Virements à Echéance "E-2" (à échanger 2 jours ouvrés avant l'échéance)
  protected final String OPERATION_SCHEDULE_E2_TRANSFER = "28";
  // Virements de Trésorerie (VSOT)
  protected final String OPERATION_TREASURY_TRANSFER = "76";

  @Inject
  public BankOrderFileAFB160Service(BankOrder bankOrder) throws AxelorException {

    super(bankOrder);

    this.partnerService = Beans.get(PartnerService.class);
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
  public File generateFile()
      throws JAXBException, IOException, AxelorException, DatatypeConfigurationException {

    List<String> records = Lists.newArrayList();

    records.add(this.createSenderRecord());

    for (BankOrderLine bankOrderLine : bankOrderLineList) {

      records.add(this.createDetailRecord(bankOrderLine));

      if (this.useOptionalFurtherInformationRecord(bankOrderLine)) {
        records.add(this.createOptionalFurtherInformationRecord(bankOrderLine));
      }
    }

    records.add(this.createTotalRecord());

    fileToCreate = records;

    return super.generateFile();
  }

  protected boolean useOptionalFurtherInformationRecord(BankOrderLine bankOrderLine) {

    if (Strings.isNullOrEmpty(bankOrderLine.getPaymentReasonLine1())) {
      return false;
    }

    return true;
  }

  /**
   * B1. Code opération La liste des codes opération possibles est indiquée page 4 au paragraphe
   * "types de virements pouvant être émis par la clientèle". Les codes utilisés doivent faire
   * l'objet d'un accord contractuel avec la banque réceptrice.
   *
   * @return
   */
  abstract String getB1Area();

  /**
   * B3. Numéro d'émetteur Attribué par la banque du donneur d'ordre (ou numéro d'identification
   * pour le Virement particulier code 22). Pour le Virement d'APL il est constitué de la façon
   * suivante : • positions 13 à 15 = 900 pour l'APL • positions 16 à 17 = numéro de département de
   * la Caisse émettrice (alphanumérique) • position 18 = rang de la Caisse dans le département (1 à
   * 9 pour les CAF, 0 pour le régime agricole)
   *
   * @return
   */
  abstract String getB3Area();

  /**
   * C1-1. Code " CCD " (comportement en cas de décalage) Utilisé pour le Virement "E-3" code
   * opération 27 • 0 ou blanc = pas d'instruction • 6 = maintien de l'échéance (transformation du
   * virement en "E-2") • 7 = maintien de l'anticipation Pour les autres Virements cette zone n'est
   * pas utilisée.
   *
   * @return
   */
  abstract String getC11Area();

  /**
   * C1-3. Date Facultative mais recommandée pour les Virements ordinaires, particuliers et de
   * trésorerie : date demandée pour le règlement interbancaire. Obligatoire pour les Virements à
   * échéance : date d'échéance demandée.
   *
   * @return
   */
  protected String getSenderC13Area() {

    int year = this.bankOrderDate.getYear();

    return this.bankOrderDate.format(DateTimeFormatter.ofPattern("ddMM"))
        + (String.valueOf(year).substring(3));
  }

  /**
   * E. Identifiant du donneur d'ordre Dans le cas d'utilisation de cet identifiant cette zone se
   * décompose de la façon suivante : 1 caractère (position 103) signe recognitif = ")" signalant la
   * présence d'un code identifiant et d'un identifiant 1 caractère (position 104) code identifiant
   * : • 1 = SIRET • 2 = AUTRE 14 caractères (positions 105 à 118) identifiant donneur d'ordre
   *
   * @return
   */
  abstract String getSenderEArea();

  /**
   * D1. Domiciliation ¾ Si le compte du bénéficiaire est un compte de résident : Désignation en
   * clair de la banque et du guichet domiciliataires. Informations figurant sur le RIB du
   * bénéficiaire. Cette information est optionnelle, mais lorsque la zone est utilisée elle doit
   * être cadrée à gauche. ¾ Si le compte du bénéficiaire est un compte de non résident : ♦ Pour les
   * Virements de Salaires, Pensions et Prestations Assimilées, la zone D1 se décompose de la façon
   * suivante : D1-1. (20 caractères) positions 55 à 74 pour indiquer la domiciliation D1-2. (4
   * caractères) positions 75 à 78 • 1 caractère : Code nature économique pouvant prendre les
   * valeurs, 5 = salaires transférés par des employeurs du secteur officiel 6 = salaires transférés
   * par des employeurs privés 7 = autres rémunérations du travail 8 = pensions, retraites et
   * prestations sociales • 3 caractères : identification géographique du pays de résidence du
   * bénéficiaire du Virement. Code géonomenclature de la CEE (liste de codes pays fournie par la
   * Banque de France) ♦ Pour la Déclaration à la Balance des Paiements, cette zone D1 se décompose
   * de la façon suivante : D1-1. (9 caractères) positions 55 à 63 numéro SIREN du résident D1-2.
   * (15 caractères) positions 64 à 78 : pour indiquer la domiciliation la zone D2 se décompose en 4
   * sous zones : D2-1. (1 caractère) position 79, code type de déclaration D2-2. (3 caractères)
   * positions 80 à 82, code nature économique D2-3. (3 caractères) positions 83 à 85, code pays
   * D2-4. (1 caractère) position 86, zone réservée
   *
   * @return
   */
  protected String getDetailD1Area(BankOrderLine bankOrderLine) throws AxelorException {

    if (bankOrderLine.getReceiverBankDetails().getBankAddress() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(
              BankPaymentExceptionMessage.BANK_ORDER_RECEIVER_BANK_DETAILS_MISSING_BANK_ADDRESS));
    }
    return bankOrderLine.getReceiverBankDetails().getBankAddress().getAddress();
  }

  protected String getDetailD2Area(BankOrderLine bankOrderLine) {

    return "";
  }

  /**
   * E. Montant Exprimé en "centimes" (00 s'il y a lieu) cadré à droite, non signé, complété
   * éventuellement à gauche par des zéros. Le montant est nul (à zéro) pour les virements à
   * vérifier
   *
   * @param bankOrderLine
   * @return
   */
  protected BigDecimal getDetailEAreaAmount(BankOrderLine bankOrderLine) {

    if (getB1Area().equals(OPERATION_TO_CHECK_TRANSFER)) {
      return BigDecimal.ZERO;
    }
    return bankOrderLine.getBankOrderAmount();
  }

  /**
   * F. Libellé Zone à la disposition du donneur d'ordre pour l'indication d'un libellé au
   * bénéficiaire dans le cadre d'un accord bilatéral entre le donneur d'ordre et sa banque. A
   * partir du premier novembre 2004 la banque du donneur d'ordre acheminera sans altération au
   * moins les 30 premiers caractères de cette zone jusqu'à la banque du bénéficiaire. Cette zone
   * peut être structurée de la façon suivante, afin de permettre au bénéficiaire du virement de
   * l'identifier, en tant que "REFERENCE DE L'OPERATION". Elle se décompose de la façon suivante :
   * F1. (1 caractère) position 119 = ")" F2. (12 caractères) positions 120 à 131 - Référence
   * commerciale F3. (18 caractères) positions 132 à 149 - A la disposition du donneur d'ordre
   *
   * <p>Pour les Virements Particuliers d'APL, cette zone se décompose de la façon suivante : • 13
   * caractères : Référence numéro de dossier (idem certificat de prêt) • 1 caractère : Rang de
   * l'échéance du prêt (*) • 1 caractère : Mois de l'échéance valeurs = A = janvier B = février C =
   * mars ......... • 1 caractère : Mois d'APL payé à l'établissement prêteur (*) Valeur Sans compte
   * de trésorerie Avec compte de trésorerie "1" échéance du 5 échéance du 7 au 11 "2" échéance du
   * 10 échéance du 12 au 26 "3" échéance du 25 échéance du 27 au 6
   *
   * <p>(en cas de rappel ces deux dernières positions comportent les lettres "RA")
   *
   * <p>• 15 caractères : Numéro de l'allocataire
   *
   * @return
   */
  protected String getDetailFArea(BankOrderLine bankOrderLine) {

    return bankOrderLine.getReceiverLabel();
  }

  /**
   * Method to create a sender record for national transfer AFB160
   *
   * @return
   * @throws AxelorException
   */
  protected String createSenderRecord() throws AxelorException {

    try {
      // Zone A : Code enregistrement
      String senderRecord =
          cfonbToolService.createZone(
              "A", "03", cfonbToolService.STATUS_MANDATORY, cfonbToolService.FORMAT_NUMERIC, 2);

      // Zone B1 : Code opération
      senderRecord +=
          cfonbToolService.createZone(
              "B1",
              getB1Area(),
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              2);

      // Zone B2 : Zone réservée
      senderRecord +=
          cfonbToolService.createZone(
              "B2", "", cfonbToolService.STATUS_NOT_USED, cfonbToolService.FORMAT_ALPHA_NUMERIC, 8);

      // Zone B3 : Numéro d'émetteur ou d'identification pour le virement particulier code 22
      // (Virement APL)
      senderRecord +=
          cfonbToolService.createZone(
              "B3",
              getB3Area(),
              cfonbToolService.STATUS_DEPENDENT,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              6);

      // Zone C1-1 : Code CCD (virement à échéance "E-3")
      senderRecord +=
          cfonbToolService.createZone(
              "C1-1",
              getC11Area(),
              cfonbToolService.STATUS_DEPENDENT,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              1);

      // Zone C1-2 : Zone réservée
      senderRecord +=
          cfonbToolService.createZone(
              "C1-2",
              "",
              cfonbToolService.STATUS_NOT_USED,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              6);

      // Zone C1-3 : Date (JJMMA)
      senderRecord +=
          cfonbToolService.createZone(
              "C1-3",
              getSenderC13Area(),
              cfonbToolService.STATUS_DEPENDENT,
              cfonbToolService.FORMAT_NUMERIC,
              5);

      // Zone C2 : Nom/Raison sociale du donneur d'ordre
      senderRecord +=
          cfonbToolService.createZone(
              I18n.get("C2 - Company name"),
              senderCompany.getName(),
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              24);

      // Zone D1-1 : Référence de la remise (à blanc ou a zéro si non utilisée)
      senderRecord +=
          cfonbToolService.createZone(
              I18n.get("D1-1 - Bank order sequence"),
              bankOrderSeq,
              cfonbToolService.STATUS_OPTIONAL,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              7);

      // Zone D1-2 : Zone réservée
      senderRecord +=
          cfonbToolService.createZone(
              "D1-2",
              "",
              cfonbToolService.STATUS_NOT_USED,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              17);

      // Zone D2-1 : Zone réservée
      senderRecord +=
          cfonbToolService.createZone(
              "D2-1",
              "",
              cfonbToolService.STATUS_NOT_USED,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              2);

      // Zone D2-2 : Code monnaie
      senderRecord +=
          cfonbToolService.createZone(
              "D2-2", "E", cfonbToolService.STATUS_MANDATORY, cfonbToolService.FORMAT_ALPHA, 1);

      // Zone D2-3 : Zone réservée
      senderRecord +=
          cfonbToolService.createZone(
              "D2-3",
              "",
              cfonbToolService.STATUS_NOT_USED,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              5);

      // Zone D3 : Code guichet de la banque du donneur d'ordre
      senderRecord +=
          cfonbToolService.createZone(
              I18n.get("D3 - Sort code"),
              senderBankDetails.getSortCode(),
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_NUMERIC,
              5);

      // Zone D4 : Numéro de compte du donneur d'ordre
      senderRecord +=
          cfonbToolService.createZone(
              I18n.get("D4 - Account number"),
              senderBankDetails.getAccountNbr(),
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              11);

      // Zone E : Identifiant du donneur d'ordre
      senderRecord +=
          cfonbToolService.createZone(
              "E",
              getSenderEArea(),
              cfonbToolService.STATUS_OPTIONAL,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              16);

      // Zone F :  Zone réservée
      senderRecord +=
          cfonbToolService.createZone(
              "F", "", cfonbToolService.STATUS_NOT_USED, cfonbToolService.FORMAT_ALPHA_NUMERIC, 31);

      // Zone G1 : Code établissement de la banque du donneur d'ordre
      senderRecord +=
          cfonbToolService.createZone(
              I18n.get("G1 - Bank code"),
              senderBankDetails.getBankCode(),
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_NUMERIC,
              5);

      // Zone G2 : Zone réservée
      senderRecord +=
          cfonbToolService.createZone(
              "G2", "", cfonbToolService.STATUS_NOT_USED, cfonbToolService.FORMAT_ALPHA_NUMERIC, 6);

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

  /**
   * Method to create a recipient record for national transfer AFB160
   *
   * @param bankOrderLine
   * @return
   * @throws AxelorException
   */
  protected String createDetailRecord(BankOrderLine bankOrderLine) throws AxelorException {

    try {
      BankDetails receiverBankDetails = bankOrderLine.getReceiverBankDetails();

      // Zone A : Code enregistrement
      String detailRecord =
          cfonbToolService.createZone(
              "A", "06", cfonbToolService.STATUS_MANDATORY, cfonbToolService.FORMAT_NUMERIC, 2);

      // Zone B1 : Code opération
      detailRecord +=
          cfonbToolService.createZone(
              "B1",
              getB1Area(),
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              2);

      // Zone B2 : Zone réservée
      detailRecord +=
          cfonbToolService.createZone(
              "B2", "", cfonbToolService.STATUS_NOT_USED, cfonbToolService.FORMAT_ALPHA_NUMERIC, 8);

      // Zone B3 : Numéro d'émetteur ou d'identification pour le virement particulier code 22
      // (Virement APL)
      detailRecord +=
          cfonbToolService.createZone(
              "B3",
              getB3Area(),
              cfonbToolService.STATUS_DEPENDENT,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              6);

      // Zone C1 : Référence
      detailRecord +=
          cfonbToolService.createZone(
              I18n.get("C1 - Sequence"),
              bankOrderLine.getSequence(),
              cfonbToolService.STATUS_OPTIONAL,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              12);

      // Zone C2 : Nom/Raison sociale du bénéficiaire
      detailRecord +=
          cfonbToolService.createZone(
              I18n.get("C2 - Receiver company name"),
              bankOrderLine.getReceiverCompany().getName(),
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              24);

      // Zone D1 : Domiciliation
      detailRecord +=
          cfonbToolService.createZone(
              I18n.get("D1 - Bank address"),
              getDetailD1Area(bankOrderLine),
              cfonbToolService.STATUS_DEPENDENT,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              24);

      // Zone D2 : Déclaration à la balance des paiements
      detailRecord +=
          cfonbToolService.createZone(
              "D2",
              getDetailD2Area(bankOrderLine),
              cfonbToolService.STATUS_DEPENDENT,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              8);

      // Zone D3 : Code guichet de la banque qui tient le compte du bénéficiaire
      detailRecord +=
          cfonbToolService.createZone(
              I18n.get("D3 - Sort code"),
              receiverBankDetails.getSortCode(),
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_NUMERIC,
              5);

      // Zone D4 : Numéro de compte du bénéficiaire
      detailRecord +=
          cfonbToolService.createZone(
              I18n.get("D4 - Account number"),
              receiverBankDetails.getAccountNbr(),
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              11);

      // Zone E : Montant du virement
      detailRecord +=
          cfonbToolService.createZone(
              I18n.get("E - Bank order amount"),
              getDetailEAreaAmount(bankOrderLine),
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_NUMERIC,
              16);

      // Zone F :  Libellé
      detailRecord +=
          cfonbToolService.createZone(
              I18n.get("F - Receiver label"),
              getDetailFArea(bankOrderLine),
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              31);

      // Zone G1 : Code établissement de la banque qui tient le compte du bénéficiaire
      detailRecord +=
          cfonbToolService.createZone(
              "G1",
              receiverBankDetails.getBankCode(),
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_NUMERIC,
              5);

      // Zone G2 : Zone réservée
      detailRecord +=
          cfonbToolService.createZone(
              "G2", "", cfonbToolService.STATUS_NOT_USED, cfonbToolService.FORMAT_ALPHA_NUMERIC, 6);

      cfonbToolService.toUpperCase(detailRecord);

      cfonbToolService.testLength(detailRecord, NB_CHAR_PER_LINE);

      return detailRecord;

    } catch (Exception e) {
      throw new AxelorException(
          e,
          bankOrderLine,
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(BankPaymentExceptionMessage.BANK_ORDER_WRONG_MAIN_DETAIL_RECORD)
              + ": "
              + e.getMessage(),
          bankOrderLine.getSequence());
    }
  }

  /**
   * Method to create an optional further information record for national transfer AFB160
   *
   * @param bankOrderLine
   * @return
   * @throws AxelorException
   */
  protected String createOptionalFurtherInformationRecord(BankOrderLine bankOrderLine)
      throws AxelorException {

    try {
      BankDetails receiverBankDetails = bankOrderLine.getReceiverBankDetails();

      // Zone A : Code enregistrement
      String totalRecord =
          cfonbToolService.createZone(
              "A", "07", cfonbToolService.STATUS_MANDATORY, cfonbToolService.FORMAT_NUMERIC, 2);

      // Zone B1 : Code opération
      totalRecord +=
          cfonbToolService.createZone(
              "B1",
              getB1Area(),
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              2);

      // Zone B2 : Zone réservée
      totalRecord +=
          cfonbToolService.createZone(
              "B2", "", cfonbToolService.STATUS_NOT_USED, cfonbToolService.FORMAT_ALPHA_NUMERIC, 8);

      // Zone B3 : Numéro d'émetteur ou d'identification pour le virement particulier code 22
      // (Virement APL)
      totalRecord +=
          cfonbToolService.createZone(
              "B3",
              getB3Area(),
              cfonbToolService.STATUS_DEPENDENT,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              6);

      // Zone C1 : Référence
      totalRecord +=
          cfonbToolService.createZone(
              I18n.get("C1 - Sequence"),
              bankOrderLine.getSequence(),
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              12);

      // Zone C2 : Nom/Raison sociale du bénéficiaire
      totalRecord +=
          cfonbToolService.createZone(
              I18n.get("C2 - Receiver company name"),
              bankOrderLine.getReceiverCompany().getName(),
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              24);

      // Zone D1 : Domiciliation
      if (bankOrderLine.getReceiverBankDetails().getBankAddress() == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_MISSING_FIELD,
            I18n.get(
                BankPaymentExceptionMessage.BANK_ORDER_RECEIVER_BANK_DETAILS_MISSING_BANK_ADDRESS));
      }
      totalRecord +=
          cfonbToolService.createZone(
              I18n.get("D1 - Bank address"),
              receiverBankDetails.getBankAddress().getAddress(),
              cfonbToolService.STATUS_DEPENDENT,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              24);

      // Zone D2 : Zone réservée
      totalRecord +=
          cfonbToolService.createZone(
              "D2", "", cfonbToolService.STATUS_NOT_USED, cfonbToolService.FORMAT_ALPHA_NUMERIC, 8);

      // Zone D3 : Code guichet de la banque qui tient le compte du bénéficiaire
      totalRecord +=
          cfonbToolService.createZone(
              I18n.get("D3 - Sort code"),
              receiverBankDetails.getSortCode(),
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_NUMERIC,
              5);

      // Zone D4 : Numéro de compte du bénéficiaire
      totalRecord +=
          cfonbToolService.createZone(
              I18n.get("D4 - Account number"),
              receiverBankDetails.getAccountNbr(),
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              11);

      // Zone E : Montant du virement
      totalRecord +=
          cfonbToolService.createZone(
              I18n.get("E - Bank order amount"),
              bankOrderLine.getBankOrderAmount(),
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_NUMERIC,
              16);

      // Zone F :  Libellé complémentaire
      totalRecord +=
          cfonbToolService.createZone(
              I18n.get("F - Receiver label"),
              bankOrderLine.getPaymentReasonLine1(),
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              31);

      // Zone G1 : Code établissement de la banque qui tient le compte du bénéficiaire
      totalRecord +=
          cfonbToolService.createZone(
              I18n.get("G1 - Bank establisment code"),
              receiverBankDetails.getBankCode(),
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_NUMERIC,
              5);

      // Zone G2 : Zone réservée
      totalRecord +=
          cfonbToolService.createZone(
              "G2", "", cfonbToolService.STATUS_NOT_USED, cfonbToolService.FORMAT_ALPHA_NUMERIC, 6);

      cfonbToolService.toUpperCase(totalRecord);

      cfonbToolService.testLength(totalRecord, NB_CHAR_PER_LINE);

      return totalRecord;

    } catch (Exception e) {
      throw new AxelorException(
          e,
          bankOrderLine,
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(BankPaymentExceptionMessage.BANK_ORDER_WRONG_FURTHER_INFORMATION_DETAIL_RECORD)
              + ": "
              + e.getMessage(),
          bankOrderLine.getSequence());
    }
  }

  /**
   * Method to create a total record for national transfer AFB160
   *
   * @param company
   * @param dateTime
   * @return
   * @throws AxelorException
   */
  protected String createTotalRecord() throws AxelorException {

    try {
      // Zone A : Code enregistrement
      String totalRecord =
          cfonbToolService.createZone(
              "A", "08", cfonbToolService.STATUS_MANDATORY, cfonbToolService.FORMAT_NUMERIC, 2);

      // Zone B1 : Code opération
      totalRecord +=
          cfonbToolService.createZone(
              "B1",
              getB1Area(),
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_NUMERIC,
              2);

      // Zone B2 : Zone réservée
      totalRecord +=
          cfonbToolService.createZone(
              "B2", "", cfonbToolService.STATUS_NOT_USED, cfonbToolService.FORMAT_ALPHA_NUMERIC, 8);

      // Zone B3 : Numéro d'émetteur ou d'identification pour le virement particulier code 22
      // (Virement APL)
      totalRecord +=
          cfonbToolService.createZone(
              "B3",
              getB3Area(),
              cfonbToolService.STATUS_DEPENDENT,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              6);

      // Zone C1 : Zone réservée
      totalRecord +=
          cfonbToolService.createZone(
              "C1",
              "",
              cfonbToolService.STATUS_NOT_USED,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              12);

      // Zone C2 : Zone réservée
      totalRecord +=
          cfonbToolService.createZone(
              "C2",
              "",
              cfonbToolService.STATUS_NOT_USED,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              24);

      // Zone D1 : Zone réservée
      totalRecord +=
          cfonbToolService.createZone(
              "D1",
              "",
              cfonbToolService.STATUS_NOT_USED,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              24);

      // Zone D2 : Zone réservée
      totalRecord +=
          cfonbToolService.createZone(
              "D2", "", cfonbToolService.STATUS_NOT_USED, cfonbToolService.FORMAT_ALPHA_NUMERIC, 8);

      // Zone D3 : Zone réservée
      totalRecord +=
          cfonbToolService.createZone(
              "D3", "", cfonbToolService.STATUS_NOT_USED, cfonbToolService.FORMAT_ALPHA_NUMERIC, 5);

      // Zone D4 : Zone réservée
      totalRecord +=
          cfonbToolService.createZone(
              "D4",
              "",
              cfonbToolService.STATUS_NOT_USED,
              cfonbToolService.FORMAT_ALPHA_NUMERIC,
              11);

      // Zone E : Montant du virement
      totalRecord +=
          cfonbToolService.createZone(
              "E",
              arithmeticTotal,
              cfonbToolService.STATUS_MANDATORY,
              cfonbToolService.FORMAT_NUMERIC,
              16);

      // Zone F :  Zone réservée
      totalRecord +=
          cfonbToolService.createZone(
              "F", "", cfonbToolService.STATUS_NOT_USED, cfonbToolService.FORMAT_ALPHA_NUMERIC, 31);

      // Zone G1 : Zone réservée
      totalRecord +=
          cfonbToolService.createZone(
              "G1", "", cfonbToolService.STATUS_NOT_USED, cfonbToolService.FORMAT_ALPHA_NUMERIC, 5);

      // Zone G2 : Zone réservée
      totalRecord +=
          cfonbToolService.createZone(
              "G2", "", cfonbToolService.STATUS_NOT_USED, cfonbToolService.FORMAT_ALPHA_NUMERIC, 6);

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
}
