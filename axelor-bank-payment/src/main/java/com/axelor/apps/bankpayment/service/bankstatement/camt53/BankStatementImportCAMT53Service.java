package com.axelor.apps.bankpayment.service.bankstatement.camt53;

import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.db.BankStatementLineCAMT53;
import com.axelor.apps.bankpayment.db.repo.BankStatementLineCAMT53Repository;
import com.axelor.apps.bankpayment.db.repo.BankStatementRepository;
import com.axelor.apps.bankpayment.service.CurrencyScaleServiceBankPayment;
import com.axelor.apps.bankpayment.service.bankstatement.BankStatementImportAbstractService;
import com.axelor.apps.bankpayment.service.bankstatementline.BankStatementLineDeleteService;
import com.axelor.apps.bankpayment.service.bankstatementline.BankStatementLineFetchService;
import com.axelor.apps.bankpayment.service.bankstatementline.camt53.BankStatementLineCreateCAMT53Service;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.AccountStatement2;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.BankToCustomerStatementV02;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.CashAccount20;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.CashBalance3;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.DateTimePeriodDetails;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.Document;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.FinancialInstitutionIdentification7;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.GroupHeader42;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.ReportEntry2;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Bank;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.repo.BankDetailsRepository;
import com.axelor.apps.base.db.repo.BankRepository;
import com.axelor.apps.base.db.repo.CurrencyRepository;
import com.axelor.db.JPA;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import javax.persistence.Query;
import javax.xml.datatype.XMLGregorianCalendar;

public class BankStatementImportCAMT53Service extends BankStatementImportAbstractService {
  protected BankStatementLineDeleteService bankStatementLineDeleteService;
  protected BankStatementLineFetchService bankStatementLineFetchService;

  protected CurrencyScaleServiceBankPayment currencyScaleServiceBankPayment;

  protected BankStatementLineCreateCAMT53Service bankStatementLineCreateCAMT53Service;

  protected BankRepository bankRepository;

  protected BankDetailsRepository bankDetailsRepository;

  protected CurrencyRepository currencyRepository;

  @Inject
  public BankStatementImportCAMT53Service(
      BankStatementRepository bankStatementRepository,
      BankStatementLineDeleteService bankStatementLineDeleteService,
      BankStatementLineFetchService bankStatementLineFetchService,
      BankStatementLineCreateCAMT53Service bankStatementLineCreateCAMT53Service,
      CurrencyScaleServiceBankPayment currencyScaleServiceBankPayment,
      BankRepository bankRepository,
      BankDetailsRepository bankDetailsRepository,
      CurrencyRepository currencyRepository) {
    super(bankStatementRepository);
    this.bankStatementLineDeleteService = bankStatementLineDeleteService;
    this.bankStatementLineFetchService = bankStatementLineFetchService;
    this.currencyScaleServiceBankPayment = currencyScaleServiceBankPayment;
    this.bankStatementLineCreateCAMT53Service = bankStatementLineCreateCAMT53Service;
    this.bankRepository = bankRepository;
    this.bankDetailsRepository = bankDetailsRepository;
    this.currencyRepository = currencyRepository;
  }

  @Override
  public void runImport(BankStatement bankStatement) throws AxelorException, IOException {
    importXMLFile(bankStatement);
  }

  public void importXMLFile(BankStatement bankStatement) throws IOException {
    //    bankStatementLineCreateCAMT53Service.process(bankStatement);
    File xmlFile = MetaFiles.getPath(bankStatement.getBankStatementFile()).toFile();
    try {

      JAXBContext jaxbContext = JAXBContext.newInstance(Document.class);

      Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
      Document unmarshal = (Document) jaxbUnmarshaller.unmarshal(xmlFile);
      BankToCustomerStatementV02 bkToCstmrStmt = unmarshal.getBkToCstmrStmt();
      GroupHeader42 grpHdr = bkToCstmrStmt.getGrpHdr();
      List<AccountStatement2> stmtList = bkToCstmrStmt.getStmt();
      AccountStatement2 stmt = stmtList.get(0);
      DateTimePeriodDetails frToDt = stmt.getFrToDt();
      // Bank Statement From Date
      XMLGregorianCalendar frDtTm = frToDt.getFrDtTm();
      // Bank Statement To Date
      XMLGregorianCalendar toDtTm = frToDt.getToDtTm();

      // create bank details
      // bank and iban are required.
      CashAccount20 acct = stmt.getAcct();
      // 1.create bank first.
      // check IBAN or other, by default bankDetailsTypeSelect is 0 (other);
      int bankDetailsTypeSelect = 0;
      String ibanOrOthers = acct.getId().getIBAN();
      if (ibanOrOthers != null) {
        bankDetailsTypeSelect = 1;
      } else {
        ibanOrOthers = acct.getId().getOthr().getId();
      }
      String bic = acct.getSvcr().getFinInstnId().getBIC();
      Bank bank = findBankByBic(bic);
      if (bank == null) {
        bank = createBank(bic, bankDetailsTypeSelect, acct.getSvcr().getFinInstnId());
      }
      // 2.create bankDetails
      BankDetails bankDetails = findBankDetails(ibanOrOthers);
      //      if (bankDetails == null) {
      //        // show an error message
      //      }

      // handle balance lines
      List<CashBalance3> balList = stmt.getBal();

      for (CashBalance3 balanceEntry : balList) {
        createBalanceLine(bankStatement, bankDetails, balanceEntry);
      }

      List<ReportEntry2> ntryList = stmt.getNtry();
      System.out.println(frDtTm);
      System.out.println(toDtTm);

    } catch (jakarta.xml.bind.JAXBException e) {
      e.printStackTrace();
    }
  }

  protected BankDetails findBankDetails(String ibanOrOthers) {
    return bankDetailsRepository.all().filter("self.iban = ?1", ibanOrOthers).fetchOne();
  }

  @Transactional
  protected void createBalanceLine(
      BankStatement bankStatement, BankDetails bankDetails, CashBalance3 balanceEntry) {
    BankStatementLineCAMT53 bankStatementLineCAMT53 = new BankStatementLineCAMT53();
    int lineTypeSelect = 0;
    String balanceType = balanceEntry.getTp().getCdOrPrtry().getCd().value();
    if ("OPBD".equals(balanceType)) {
      lineTypeSelect = 1;
    } else if ("CLBD".equals(balanceType)) {
      lineTypeSelect = 3;
    }
    XMLGregorianCalendar date = balanceEntry.getDt().getDt();
    LocalDate operationDate = LocalDate.of(date.getYear(), date.getMonth(), date.getDay());
    String creditOrDebit = balanceEntry.getCdtDbtInd().value();
    // set currency
    String currencyCode = balanceEntry.getAmt().getCcy();
    Query query =
        JPA.em()
            .createQuery(
                "select self.id " + "from Currency as self " + "where self.codeISO = :currencyCode")
            .setParameter("currencyCode", currencyCode);
    List resultList = query.getResultList();
    if (!resultList.isEmpty()) {
      long currencyId = (long) resultList.get(0);
      Currency currency = Beans.get(CurrencyRepository.class).find(currencyId);
      bankStatementLineCAMT53.setCurrency(currency);
    }
    // set credit or debit
    if ("CRDT".equals(creditOrDebit)) {
      BigDecimal credit = balanceEntry.getAmt().getValue();
      bankStatementLineCAMT53.setCredit(credit);
    } else if ("DBIT".equals(creditOrDebit)) {
      BigDecimal debit = balanceEntry.getAmt().getValue();
      bankStatementLineCAMT53.setDebit(debit);
    }

    bankStatementLineCAMT53.setBankStatement(bankStatement);
    bankStatementLineCAMT53.setBankDetails(bankDetails);
    bankStatementLineCAMT53.setOperationDate(operationDate);
    bankStatementLineCAMT53.setLineTypeSelect(lineTypeSelect);
    Beans.get(BankStatementLineCAMT53Repository.class).save(bankStatementLineCAMT53);
    System.out.println("test");
  }

  /*
  TODO: Don't create the bank manually, find the bankDetails, then the bank is determined.
   */
  @Transactional
  protected Bank createBank(
      String bic, int bankDetailsTypeSelect, FinancialInstitutionIdentification7 finInstnId) {
    Bank bank = new Bank();
    bank.setCode(bic);
    bank.setBankName(finInstnId.getNm());
    bank.setBankDetailsTypeSelect(bankDetailsTypeSelect);
    bankRepository.save(bank);
    return bank;
  }

  protected Bank findBankByBic(String bic) {
    Query query =
        JPA.em()
            .createQuery("select self.id " + " from Bank as self " + " where self.code = :bic ")
            .setParameter("bic", bic);
    List resultList = query.getResultList();
    if (!resultList.isEmpty()) {
      long firstResult = (long) resultList.get(0);
      return bankRepository.find(firstResult);
    } else {
      return null;
    }
  }

  @Override
  protected void checkImport(BankStatement bankStatement) throws AxelorException, IOException {}

  @Override
  protected void updateBankDetailsBalance(BankStatement bankStatement) {}
}
