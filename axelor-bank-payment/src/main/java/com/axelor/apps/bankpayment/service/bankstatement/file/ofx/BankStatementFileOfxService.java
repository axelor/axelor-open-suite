package com.axelor.apps.bankpayment.service.bankstatement.file.ofx;

import com.axelor.apps.account.db.InterbankCodeLine;
import com.axelor.apps.bankpayment.db.BankStatementLine;
import com.axelor.apps.bankpayment.db.repo.BankStatementLineRepository;
import com.axelor.apps.bankpayment.service.bankstatement.BankStatementLineService;
import com.axelor.apps.bankpayment.service.bankstatement.BankStatementService;
import com.axelor.apps.bankpayment.service.bankstatement.file.BankStatementFileService;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.repo.BankDetailsRepository;
import com.axelor.apps.base.db.repo.CurrencyRepository;
import com.axelor.apps.tool.file.FileTool;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.ExceptionOriginRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class BankStatementFileOfxService extends BankStatementFileService {
  protected CurrencyRepository currencyRepository;
  protected BankDetailsRepository bankDetailsRepository;
  protected BankStatementLineService bankStatementLineService;
  protected BankStatementLineRepository bankStatementLineRepository;
  protected OfxParserService ofxParserService;

  @Inject
  public BankStatementFileOfxService(BankStatementService bankStatementService) {
    super(bankStatementService);

    this.currencyRepository = Beans.get(CurrencyRepository.class);
    this.bankDetailsRepository = Beans.get(BankDetailsRepository.class);
    this.bankStatementLineService = Beans.get(BankStatementLineService.class);
    this.bankStatementLineRepository = Beans.get(BankStatementLineRepository.class);
    this.ofxParserService = Beans.get(OfxParserService.class);
  }

  @Override
  public void process() throws IOException, AxelorException {
    super.process();

    List<Map<String, Object>> structuredContentFile = readOFXFile();

    int sequence = 0;
    findBankStatement();

    for (Map<String, Object> structuredContentLine : structuredContentFile) {

      try {
        createBankStatementLine(structuredContentLine, sequence++);
      } catch (Exception e) {
        TraceBackService.trace(
            new Exception(String.format("Line %s : %s", sequence, e), e),
            ExceptionOriginRepository.IMPORT);
        findBankStatement();
      } finally {
        if (sequence % 10 == 0) {
          JPA.clear();
          findBankStatement();
        }
      }
    }

    JPA.clear();
  }

  protected List<Map<String, Object>> readOFXFile() throws IOException {
    List<Map<String, Object>> structuredContent = Lists.newArrayList();
    List<String> fileContent = FileTool.reader(file.getPath());
    String content = String.join("", fileContent);
    structuredContent = ofxParserService.parse(content);
    return structuredContent;
  }

  @Transactional
  public BankStatementLine createBankStatementLine(
      Map<String, Object> structuredContentLine, int sequence) {

    String description = (String) structuredContentLine.get("description");

    BankDetails bankDetails = null;
    if (structuredContentLine.containsKey("bankDetails")
        && structuredContentLine.get("bankDetails") != null) {
      bankDetails =
          bankDetailsRepository.find(
              ((BankDetails) structuredContentLine.get("bankDetails")).getId());
    }

    Currency currency = null;
    if (structuredContentLine.containsKey("currency")
        && structuredContentLine.get("currency") != null) {
      currency =
          currencyRepository.find(((Currency) structuredContentLine.get("currency")).getId());
    }

    InterbankCodeLine operationInterbankCodeLine = null;
    InterbankCodeLine rejectInterbankCodeLine = null;
    BankStatementLine bankStatementLine =
        bankStatementLineService.createBankStatementLine(
            findBankStatement(),
            sequence,
            bankDetails,
            (BigDecimal) structuredContentLine.get("debit"),
            (BigDecimal) structuredContentLine.get("credit"),
            currency,
            description,
            (LocalDate) structuredContentLine.get("operationDate"),
            (LocalDate) structuredContentLine.get("valueDate"),
            operationInterbankCodeLine,
            rejectInterbankCodeLine,
            (String) structuredContentLine.get("origin"),
            (String) structuredContentLine.get("reference"));
    return bankStatementLineRepository.save(bankStatementLine);
  }
}
