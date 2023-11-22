package com.axelor.apps.bankpayment.service.bankstatement.line.afb120;

import com.axelor.apps.account.db.InterbankCodeLine;
import com.axelor.apps.account.db.repo.InterbankCodeLineRepository;
import com.axelor.apps.account.db.repo.InterbankCodeRepository;
import com.axelor.apps.bankpayment.db.repo.BankStatementLineAFB120Repository;
import com.axelor.apps.bankpayment.service.cfonb.CfonbToolService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.repo.BankDetailsRepository;
import com.axelor.apps.base.db.repo.CurrencyRepository;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class BankStatementLineMapperAFB120ServiceImpl
    implements BankStatementLineMapperAFB120Service {

  protected CfonbToolService cfonbToolService;
  protected CurrencyRepository currencyRepository;
  protected InterbankCodeLineRepository interbankCodeLineRepository;
  protected BankDetailsRepository bankDetailsRepository;

  protected static final String PREVIOUS_BALANCE_OPERATION_CODE = "01";
  protected static final String MOVEMENT_OPERATION_CODE = "04";
  protected static final String COMPLEMENT_MOVEMENT_OPERATION_CODE = "05";
  protected static final String NEW_BALANCE_OPERATION_CODE = "07";
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("ddMMyy");

  @Inject
  public BankStatementLineMapperAFB120ServiceImpl(
      CfonbToolService cfonbToolService,
      CurrencyRepository currencyRepository,
      InterbankCodeLineRepository interbankCodeLineRepository,
      BankDetailsRepository bankDetailsRepository) {
    this.cfonbToolService = cfonbToolService;
    this.currencyRepository = currencyRepository;
    this.interbankCodeLineRepository = interbankCodeLineRepository;
    this.bankDetailsRepository = bankDetailsRepository;
  }

  @Override
  public void writeStructuredContent(String lineData, List<Map<String, Object>> structuredContent)
      throws AxelorException {
    // Code enregistrement
    String operationCode =
        cfonbToolService.readZone(
            "Record code",
            lineData,
            cfonbToolService.STATUS_MANDATORY,
            cfonbToolService.FORMAT_NUMERIC,
            1,
            2);

    switch (operationCode) {
      case PREVIOUS_BALANCE_OPERATION_CODE:
        structuredContent.add(readPreviousBalanceRecord(lineData));
        break;
      case MOVEMENT_OPERATION_CODE:
        structuredContent.add(readMovementRecord(lineData));
        break;
      case COMPLEMENT_MOVEMENT_OPERATION_CODE:
        writeAdditionalInformation(lineData, structuredContent);
        break;
      case NEW_BALANCE_OPERATION_CODE:
        structuredContent.add(readNewBalanceRecord(lineData));
        break;
      default:
        break;
    }
  }

  protected void writeAdditionalInformation(
      String lineData, List<Map<String, Object>> structuredContent) throws AxelorException {
    Map<String, Object> movementLine = structuredContent.get(structuredContent.size() - 1);
    String additionalInformation = "";
    if (movementLine.containsKey("additionalInformation")) {
      additionalInformation = (String) movementLine.get("additionalInformation") + "\n";
    }
    additionalInformation +=
        (String) readAdditionalMovementRecord(lineData).get("additionalInformation");

    movementLine.put("additionalInformation", additionalInformation);
  }

  protected Map<String, Object> readPreviousBalanceRecord(String lineContent)
      throws AxelorException {

    Map<String, Object> structuredLineContent = Maps.newHashMap();

    structuredLineContent.put(
        "lineType", BankStatementLineAFB120Repository.LINE_TYPE_INITIAL_BALANCE);

    // Zone 1-B : Code banque
    String bankCode =
        cfonbToolService.readZone(
            "1-B : bank code",
            lineContent,
            cfonbToolService.STATUS_MANDATORY,
            cfonbToolService.FORMAT_NUMERIC,
            3,
            5);

    // Zone 1-D : Code guichet
    String sortCode =
        cfonbToolService.readZone(
            "1-D : sort code",
            lineContent,
            cfonbToolService.STATUS_MANDATORY,
            cfonbToolService.FORMAT_NUMERIC,
            12,
            5);

    // Zone 1-E : Code devise ISO
    String currencyCode =
        cfonbToolService.readZone(
            "1-E : currency code",
            lineContent,
            cfonbToolService.STATUS_MANDATORY,
            cfonbToolService.FORMAT_ALPHA,
            17,
            3);
    structuredLineContent.put("currency", getCurrency(currencyCode));

    // Zone 1-F : Nombre de décimales du montant de l'ancien solde
    int decimalDigitNumber =
        Integer.parseInt(
            cfonbToolService.readZone(
                "1-F : decimal number",
                lineContent,
                cfonbToolService.STATUS_MANDATORY,
                cfonbToolService.FORMAT_NUMERIC,
                20,
                1));

    // Zone 1-H : Numéro de compte
    String accountNumber =
        cfonbToolService.readZone(
            "1-H : account number",
            lineContent,
            cfonbToolService.STATUS_MANDATORY,
            cfonbToolService.FORMAT_ALPHA_NUMERIC,
            22,
            11);

    structuredLineContent.put("bankDetails", getBankDetails(accountNumber, bankCode, sortCode));

    // Zone 1-J : Date de l'ancien solde (JJMMAA)
    String date =
        cfonbToolService.readZone(
            "1-J : date",
            lineContent,
            cfonbToolService.STATUS_MANDATORY,
            cfonbToolService.FORMAT_NUMERIC,
            35,
            6);
    structuredLineContent.put("operationDate", getDate(date));

    // Zone 1-L : Montant de l'ancien solde
    String amountStr =
        cfonbToolService.readZone(
            "1-L : amount",
            lineContent,
            cfonbToolService.STATUS_MANDATORY,
            cfonbToolService.FORMAT_ALPHA_NUMERIC,
            91,
            14);

    BigDecimal amount = getAmount(amountStr, decimalDigitNumber);

    if (amount.signum() == 1) {
      structuredLineContent.put("debit", BigDecimal.ZERO);
      structuredLineContent.put("credit", amount.abs());
    } else {
      structuredLineContent.put("credit", BigDecimal.ZERO);
      structuredLineContent.put("debit", amount.abs());
    }

    return structuredLineContent;
  }

  protected Map<String, Object> readMovementRecord(String lineContent) throws AxelorException {

    Map<String, Object> structuredLineContent = Maps.newHashMap();

    structuredLineContent.put("lineType", BankStatementLineAFB120Repository.LINE_TYPE_MOVEMENT);

    // Zone 2-B : Code banque
    String bankCode =
        cfonbToolService.readZone(
            "2-B : bank code",
            lineContent,
            cfonbToolService.STATUS_MANDATORY,
            cfonbToolService.FORMAT_NUMERIC,
            3,
            5);

    // Zone 2-D : Code guichet
    String sortCode =
        cfonbToolService.readZone(
            "2-D : sort code",
            lineContent,
            cfonbToolService.STATUS_MANDATORY,
            cfonbToolService.FORMAT_NUMERIC,
            12,
            5);

    // Zone 2-E : Code devise ISO
    String currencyCode =
        cfonbToolService.readZone(
            "2-E : currency code",
            lineContent,
            cfonbToolService.STATUS_MANDATORY,
            cfonbToolService.FORMAT_ALPHA,
            17,
            3);
    structuredLineContent.put("currency", getCurrency(currencyCode));

    // Zone 2-F : Nombre de décimales du montant du mouvement
    int decimalDigitNumber =
        Integer.parseInt(
            cfonbToolService.readZone(
                "2-F : decimal number",
                lineContent,
                cfonbToolService.STATUS_MANDATORY,
                cfonbToolService.FORMAT_NUMERIC,
                20,
                1));

    // Zone 2-H : Numéro de compte
    String accountNumber =
        cfonbToolService.readZone(
            "2-H : account number",
            lineContent,
            cfonbToolService.STATUS_MANDATORY,
            cfonbToolService.FORMAT_ALPHA_NUMERIC,
            22,
            11);

    structuredLineContent.put("bankDetails", getBankDetails(accountNumber, bankCode, sortCode));

    // Zone 2-I : Code opération interbancaire
    String operationInterbankCode =
        cfonbToolService.readZone(
            "2-I : interbank operation code",
            lineContent,
            cfonbToolService.STATUS_MANDATORY,
            cfonbToolService.FORMAT_ALPHA_NUMERIC,
            33,
            2);
    structuredLineContent.put(
        "operationInterbankCodeLine", getInterbankCodeLine(operationInterbankCode));

    // Zone 2-J : Date de comptabilisation de l'opération (JJMMAA)
    String movementDate =
        cfonbToolService.readZone(
            "2-J : operation date",
            lineContent,
            cfonbToolService.STATUS_MANDATORY,
            cfonbToolService.FORMAT_NUMERIC,
            35,
            6);
    structuredLineContent.put("operationDate", getDate(movementDate));

    // Zone 2-K : Code motif de rejet
    String rejectInterbankCodeLine =
        cfonbToolService.readZone(
            "2-K : interbank reject code",
            lineContent,
            cfonbToolService.STATUS_DEPENDENT,
            cfonbToolService.FORMAT_NUMERIC,
            41,
            2);
    structuredLineContent.put(
        "rejectInterbankCodeLine", getInterbankCodeLine(rejectInterbankCodeLine));

    // Zone 2-L : Date de valeur (JJMMAA)
    String valueDate =
        cfonbToolService.readZone(
            "2-L : value date",
            lineContent,
            cfonbToolService.STATUS_MANDATORY,
            cfonbToolService.FORMAT_NUMERIC,
            43,
            6);
    structuredLineContent.put("valueDate", getDate(valueDate));

    // Zone 2-M : Libellé
    structuredLineContent.put(
        "description",
        cfonbToolService.readZone(
            "2-M : label",
            lineContent,
            cfonbToolService.STATUS_MANDATORY,
            cfonbToolService.FORMAT_ALPHA_NUMERIC,
            49,
            31));

    // Zone 2-O : Numéro d'écriture
    structuredLineContent.put(
        "origin",
        cfonbToolService.readZone(
            "2-O : move number",
            lineContent,
            cfonbToolService.STATUS_MANDATORY,
            cfonbToolService.FORMAT_ALPHA_NUMERIC,
            82,
            7));

    // Zone 2-P : Indice d'exonération de commission de mouvement de compte
    structuredLineContent.put(
        "commissionExemptionIndexSelect",
        cfonbToolService.readZone(
            "2-P : turnover commission exemption index",
            lineContent,
            cfonbToolService.STATUS_OPTIONAL,
            cfonbToolService.FORMAT_ALPHA_NUMERIC,
            89,
            1));

    // Zone 2-Q : Indice d'indisponibilité
    structuredLineContent.put(
        "unavailabilityIndexSelect",
        cfonbToolService.readZone(
            "2-Q : unavailability index",
            lineContent,
            cfonbToolService.STATUS_OPTIONAL,
            cfonbToolService.FORMAT_ALPHA_NUMERIC,
            90,
            1));

    // Zone 2-R : Montant du mouvement
    String amountStr =
        cfonbToolService.readZone(
            "2-R : amount",
            lineContent,
            cfonbToolService.STATUS_MANDATORY,
            cfonbToolService.FORMAT_ALPHA_NUMERIC,
            91,
            14);
    BigDecimal amount = getAmount(amountStr, decimalDigitNumber);

    if (amount.signum() == 1) {
      structuredLineContent.put("debit", BigDecimal.ZERO);
      structuredLineContent.put("credit", amount.abs());
    } else {
      structuredLineContent.put("credit", BigDecimal.ZERO);
      structuredLineContent.put("debit", amount.abs());
    }

    // Zone 2-S : Zone référence
    structuredLineContent.put(
        "reference",
        cfonbToolService.readZone(
            "2-S : reference zone",
            lineContent,
            cfonbToolService.STATUS_OPTIONAL,
            cfonbToolService.FORMAT_ALPHA_NUMERIC,
            105,
            16));

    return structuredLineContent;
  }

  protected Map<String, Object> readNewBalanceRecord(String lineContent) throws AxelorException {

    Map<String, Object> structuredLineContent = Maps.newHashMap();

    structuredLineContent.put(
        "lineType", BankStatementLineAFB120Repository.LINE_TYPE_FINAL_BALANCE);

    // Zone 1-B : Code banque
    String bankCode =
        cfonbToolService.readZone(
            "3-B : bank code",
            lineContent,
            cfonbToolService.STATUS_MANDATORY,
            cfonbToolService.FORMAT_NUMERIC,
            3,
            5);

    // Zone 1-D : Code guichet
    String sortCode =
        cfonbToolService.readZone(
            "3-D : sort code",
            lineContent,
            cfonbToolService.STATUS_MANDATORY,
            cfonbToolService.FORMAT_NUMERIC,
            12,
            5);

    // Zone 1-E : Code devise ISO
    String currencyCode =
        cfonbToolService.readZone(
            "3-E : currency code",
            lineContent,
            cfonbToolService.STATUS_MANDATORY,
            cfonbToolService.FORMAT_ALPHA,
            17,
            3);
    structuredLineContent.put("currency", getCurrency(currencyCode));

    // Zone 1-F : Nombre de décimales du montant du nouveau solde
    int nbDecimalDigit =
        Integer.parseInt(
            cfonbToolService.readZone(
                "3-F : decimal number",
                lineContent,
                cfonbToolService.STATUS_MANDATORY,
                cfonbToolService.FORMAT_NUMERIC,
                20,
                1));

    // Zone 1-H : Numéro de compte
    String accountNumber =
        cfonbToolService.readZone(
            "3-H : account number",
            lineContent,
            cfonbToolService.STATUS_MANDATORY,
            cfonbToolService.FORMAT_ALPHA_NUMERIC,
            22,
            11);

    structuredLineContent.put("bankDetails", getBankDetails(accountNumber, bankCode, sortCode));

    // Zone 1-J : Date du nouveau solde (JJMMAA)
    String date =
        cfonbToolService.readZone(
            "3-J : date",
            lineContent,
            cfonbToolService.STATUS_MANDATORY,
            cfonbToolService.FORMAT_NUMERIC,
            35,
            6);
    structuredLineContent.put("operationDate", getDate(date));

    // Zone 1-L : Montant du nouveau solde
    String amountStr =
        cfonbToolService.readZone(
            "3-L : amount",
            lineContent,
            cfonbToolService.STATUS_MANDATORY,
            cfonbToolService.FORMAT_ALPHA_NUMERIC,
            91,
            14);

    BigDecimal amount = getAmount(amountStr, nbDecimalDigit);

    if (amount.signum() == 1) {
      structuredLineContent.put("debit", BigDecimal.ZERO);
      structuredLineContent.put("credit", amount.abs());
    } else {
      structuredLineContent.put("credit", BigDecimal.ZERO);
      structuredLineContent.put("debit", amount.abs());
    }

    return structuredLineContent;
  }

  protected Map<String, Object> readAdditionalMovementRecord(String lineContent)
      throws AxelorException {

    Map<String, Object> structuredLineContent = Maps.newHashMap();

    // Zone 2b-B : Code banque
    String bankCode =
        cfonbToolService.readZone(
            "2b-B : bank code",
            lineContent,
            cfonbToolService.STATUS_MANDATORY,
            cfonbToolService.FORMAT_NUMERIC,
            3,
            5);

    // Zone 2b-D : Code guichet
    String sortCode =
        cfonbToolService.readZone(
            "2b-D : sort code",
            lineContent,
            cfonbToolService.STATUS_MANDATORY,
            cfonbToolService.FORMAT_NUMERIC,
            12,
            5);

    // Zone 2b-E : Code devise ISO
    String currencyCode =
        cfonbToolService.readZone(
            "2b-E : currency code",
            lineContent,
            cfonbToolService.STATUS_MANDATORY,
            cfonbToolService.FORMAT_ALPHA,
            17,
            3);
    structuredLineContent.put("currency", getCurrency(currencyCode));

    // Zone 2b-F : Nombre de décimales du montant du mouvement
    cfonbToolService.readZone(
        "2b-F : decimal number",
        lineContent,
        cfonbToolService.STATUS_MANDATORY,
        cfonbToolService.FORMAT_NUMERIC,
        20,
        1);

    // Zone 2b-H : Numéro de compte
    String accountNumber =
        cfonbToolService.readZone(
            "2b-H : account number",
            lineContent,
            cfonbToolService.STATUS_MANDATORY,
            cfonbToolService.FORMAT_ALPHA_NUMERIC,
            22,
            11);

    structuredLineContent.put("bankDetails", getBankDetails(accountNumber, bankCode, sortCode));

    // Zone 2b-I : Code opération interbancaire
    String operationInterbankCode =
        cfonbToolService.readZone(
            "2b-I : interbank operation code",
            lineContent,
            cfonbToolService.STATUS_MANDATORY,
            cfonbToolService.FORMAT_ALPHA_NUMERIC,
            33,
            2);
    structuredLineContent.put(
        "operationInterbankCodeLine", getInterbankCodeLine(operationInterbankCode));

    // Zone 2b-J : Date de comptabilisation de l'opération (JJMMAA)
    String date =
        cfonbToolService.readZone(
            "2b-J : operation date",
            lineContent,
            cfonbToolService.STATUS_MANDATORY,
            cfonbToolService.FORMAT_NUMERIC,
            35,
            6);
    structuredLineContent.put("operationDate", getDate(date));

    // Zone 2b-L : Qualifiant de la zone "Informations complémentaires"
    String additionalInformationType =
        cfonbToolService.readZone(
            "2b-L : qualifying of additional information zone",
            lineContent,
            cfonbToolService.STATUS_MANDATORY,
            cfonbToolService.FORMAT_ALPHA_NUMERIC,
            46,
            3);

    switch (additionalInformationType) {
      case "LIB":
        // Zone 2b-M : Informations complémentaires
        structuredLineContent.put(
            "additionalInformation",
            cfonbToolService.readZone(
                "2b-M : additional information",
                lineContent,
                cfonbToolService.STATUS_MANDATORY,
                cfonbToolService.FORMAT_ALPHA_NUMERIC,
                49,
                70));
        break;
      case "MMO":
        // Zone 2b-M : Informations complémentaires
        // 2b-M-1 : Code devise ISO (norme ISO4217 (NF K 10 020)) du montant d'origine
        String origineCurrencyCode =
            cfonbToolService.readZone(
                "2b-M-1 : currency code",
                lineContent,
                cfonbToolService.STATUS_MANDATORY,
                cfonbToolService.FORMAT_ALPHA_NUMERIC,
                49,
                3);
        // 2b-M-2 : nombre de décimales du montant d'origine
        int decimalDigitNumber =
            Integer.parseInt(
                cfonbToolService.readZone(
                    "2b-M-2 : decimal digit number",
                    lineContent,
                    cfonbToolService.STATUS_MANDATORY,
                    cfonbToolService.FORMAT_ALPHA_NUMERIC,
                    52,
                    1));
        // 2b-M-3 : Montant d'origine (non signé)
        String amountInCurrency =
            cfonbToolService.readZone(
                "2b-M-3 : original amount",
                lineContent,
                cfonbToolService.STATUS_MANDATORY,
                cfonbToolService.FORMAT_ALPHA_NUMERIC,
                53,
                14);
        String integerPartOfAmount =
            amountInCurrency.substring(0, amountInCurrency.length() - decimalDigitNumber);
        String decimalPartOfAmount =
            amountInCurrency.substring(amountInCurrency.length() - decimalDigitNumber);
        String correctAmount = integerPartOfAmount + "." + decimalPartOfAmount;
        structuredLineContent.put(
            "additionalInformation", correctAmount + " " + origineCurrencyCode);
        break;
      default:
        // Zone 2b-M : Informations complémentaires
        structuredLineContent.put(
            "additionalInformation",
            cfonbToolService.readZone(
                "2b-M : additional information",
                lineContent,
                cfonbToolService.STATUS_MANDATORY,
                cfonbToolService.FORMAT_ALPHA_NUMERIC,
                49,
                70));
        break;
    }

    return structuredLineContent;
  }

  protected Currency getCurrency(String isoCode) {

    return currencyRepository.findByCode(isoCode);
  }

  protected BankDetails getBankDetails(String accountNumber, String bankCode, String sortCode) {

    return bankDetailsRepository
        .all()
        .filter(
            "self.accountNbr = ?1 and self.bankCode = ?2 and self.sortCode = ?3 and self.company is not null and active is true",
            accountNumber,
            bankCode,
            sortCode)
        .fetchOne();
  }

  protected LocalDate getDate(String date) {

    return LocalDate.parse(date, DATE_FORMATTER);
  }

  /**
   * Le montant est cadré à droite, complété à gauche par des zéros ; le montant étant signé, le
   * signe est superposé au dernier caractère à droite. La valeur hexadécimale dans cette position
   * est : - pour un montant créditeur "C0" à "C9" pour + 0 à + 9 } fichiers en EBCDIC - pour un
   * montant débiteur "D0" à "D9" pour - 0 à - 9 } - pour un montant créditeur "7B" et "41" à "49"
   * pour + 0 à + 9 } fichiers en ASCII - pour un montant débiteur "7D" et "4A" à "52" pour - 0 à -
   * 9 }
   *
   * @param amount
   * @return the correct amount
   */
  protected BigDecimal getAmount(String amount, int decimalDigitNumber) {

    String signHex = amount.substring(amount.length() - 1);
    String lastDigit = "";
    String sign = "";

    switch (signHex) {
      case "{":
        lastDigit = "0";
        sign = "+";
        break;
      case "A":
        lastDigit = "1";
        sign = "+";
        break;
      case "B":
        lastDigit = "2";
        sign = "+";
        break;
      case "C":
        lastDigit = "3";
        sign = "+";
        break;
      case "D":
        lastDigit = "4";
        sign = "+";
        break;
      case "E":
        lastDigit = "5";
        sign = "+";
        break;
      case "F":
        lastDigit = "6";
        sign = "+";
        break;
      case "G":
        lastDigit = "7";
        sign = "+";
        break;
      case "H":
        lastDigit = "8";
        sign = "+";
        break;
      case "I":
        lastDigit = "9";
        sign = "+";
        break;
      case "}":
        lastDigit = "0";
        sign = "-";
        break;
      case "J":
        lastDigit = "1";
        sign = "-";
        break;
      case "K":
        lastDigit = "2";
        sign = "-";
        break;
      case "L":
        lastDigit = "3";
        sign = "-";
        break;
      case "M":
        lastDigit = "4";
        sign = "-";
        break;
      case "N":
        lastDigit = "5";
        sign = "-";
        break;
      case "O":
        lastDigit = "6";
        sign = "-";
        break;
      case "P":
        lastDigit = "7";
        sign = "-";
        break;
      case "Q":
        lastDigit = "8";
        sign = "-";
        break;
      case "R":
        lastDigit = "9";
        sign = "-";
        break;

      default:
        break;
    }

    String completeAmount = sign + amount.substring(0, amount.length() - 1) + lastDigit;

    String integerPartOfAmount =
        completeAmount.substring(0, completeAmount.length() - decimalDigitNumber);
    String decimalPartOfAmount =
        completeAmount.substring(completeAmount.length() - decimalDigitNumber);
    String correctAmount = integerPartOfAmount + "." + decimalPartOfAmount;

    return new BigDecimal(correctAmount);
  }

  protected InterbankCodeLine getInterbankCodeLine(String code) {
    return interbankCodeLineRepository
        .all()
        .filter("self.code = :code AND self.interbankCode.typeSelect = :type")
        .bind("code", code)
        .bind("type", InterbankCodeRepository.TYPE_OPERATION_CODE)
        .fetchOne();
  }
}
