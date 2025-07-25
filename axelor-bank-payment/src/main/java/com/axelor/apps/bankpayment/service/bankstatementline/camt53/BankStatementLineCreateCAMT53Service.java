package com.axelor.apps.bankpayment.service.bankstatementline.camt53;

import com.axelor.apps.account.db.InterbankCode;
import com.axelor.apps.account.db.InterbankCodeLine;
import com.axelor.apps.account.db.repo.InterbankCodeLineRepository;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.db.BankStatementLine;
import com.axelor.apps.bankpayment.db.repo.BankStatementRepository;
import com.axelor.apps.bankpayment.exception.BankPaymentExceptionMessage;
import com.axelor.apps.bankpayment.service.bankstatement.BankStatementImportService;
import com.axelor.apps.bankpayment.service.bankstatementline.BankStatementLineCreateAbstractService;
import com.axelor.apps.bankpayment.service.bankstatementline.afb120.StructuredContentLine;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.AccountIdentification4Choice;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.AccountStatement2;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.ActiveOrHistoricCurrencyAndAmount;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.BalanceType12;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.BalanceType12Code;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.BalanceType5Choice;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.BankToCustomerStatementV02;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.BankTransactionCodeStructure4;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.BatchInformation2;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.CashAccount16;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.CashAccount20;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.CashBalance3;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.CreditDebitCode;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.DateAndDateTimeChoice;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.DateTimePeriodDetails;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.Document;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.EntryDetails1;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.EntryTransaction2;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.GenericAccountIdentification1;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.OrganisationIdentification4;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.Party6Choice;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.PartyIdentification32;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.ProprietaryBankTransactionCodeStructure1;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.RemittanceInformation5;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.ReportEntry2;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.ReturnReason5Choice;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.ReturnReasonInformation10;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.TransactionParty2;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.TransactionReferences2;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.repo.BankDetailsRepository;
import com.axelor.apps.base.db.repo.CurrencyRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.common.ObjectUtils;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.persistence.Query;
import javax.xml.datatype.XMLGregorianCalendar;

public class BankStatementLineCreateCAMT53Service {

    private int differentCurrencyOccurrence = 0;
    private int sequence = 0;
    protected BankStatement bankStatement;

    protected CAMT53ToolService camt53ToolService;
    protected BankStatementRepository bankStatementRepository;

    @Inject
    public BankStatementLineCreateCAMT53Service(CAMT53ToolService camt53ToolService,
                                                BankStatementRepository bankStatementRepository){
        this.camt53ToolService = camt53ToolService;
        this.bankStatementRepository = bankStatementRepository;
    }

    public void processCAMT53(BankStatement bankStatement) throws AxelorException {
        this.bankStatement = bankStatement;
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(Document.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            Document document = (Document) jaxbUnmarshaller.unmarshal(MetaFiles.getPath(bankStatement.getBankStatementFile()).toFile());
            if (document == null) {
                throw new AxelorException(
                        TraceBackRepository.CATEGORY_NO_VALUE,
                        I18n.get(BankPaymentExceptionMessage.BANK_STATEMENT_XML_FILE_READ_ERROR));
            }
            BankToCustomerStatementV02 bkToCstmrStmt = document.getBkToCstmrStmt();
            if (bkToCstmrStmt == null) {
                throw new AxelorException(
                        TraceBackRepository.CATEGORY_NO_VALUE,
                        I18n.get(
                                BankPaymentExceptionMessage.BANK_STATEMENT_XML_FILE_NO_BANK_STATEMENT_FOUND_ERROR));
            }

            // May be multiple bank statements.
            List<AccountStatement2> stmtList = bkToCstmrStmt.getStmt();
            if (ObjectUtils.isEmpty(stmtList)) {
                throw new AxelorException(
                        TraceBackRepository.CATEGORY_NO_VALUE,
                        I18n.get(
                                BankPaymentExceptionMessage.BANK_STATEMENT_XML_FILE_NO_BANK_STATEMENT_FOUND_ERROR));
            }

            // initial the counter
            sequence = 0;
            differentCurrencyOccurrence = 0;

            camt53ToolService.computeBankStatementDates(bankStatement, stmtList);

            for (int i = 0; i < stmtList.size(); i++) {
                // handle each AccountStatement2 object in list
                AccountStatement2 curBankStatement = stmtList.get(i);

                fillBankStatement(curBankStatement, bankStatement);
            }
        } catch (jakarta.xml.bind.JAXBException e) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
                    I18n.get(BankPaymentExceptionMessage.BANK_STATEMENT_XML_FILE_UNMARSHAL_ERROR));
        }
    }

    /**
     * This method takes the input AccountStatement2 object and generates related bankStatementLines
     * under the current BankStatement Object.
     *
     * @param stmt the input AccountStatement2 object from the file
     * @throws AxelorException
     */
    @Transactional
    protected void fillBankStatement(AccountStatement2 stmt, BankStatement bankStatement) throws AxelorException {
        CashAccount20 acct = stmt.getAcct();
        BankDetails bankDetails = null;
        String currencyCodeFromStmt = null;
        if (acct != null) {
            String ibanOrOthers = getIBANOrOtherAccountIdentification(acct);
            // find bankDetails
            bankDetails = findBankDetailsByIBAN(ibanOrOthers);

            if (bankDetails == null) {
                throw new AxelorException(
                        TraceBackRepository.CATEGORY_NO_VALUE,
                        I18n.get(BankPaymentExceptionMessage.BANK_STATEMENT_BANK_DETAILS_NOT_EXIST_ERROR));
            }

            currencyCodeFromStmt = acct.getCcy();
            if (currencyCodeFromStmt == null) {
                currencyCodeFromStmt =
                        Optional.of(bankDetails)
                                .map(BankDetails::getCompany)
                                .map(Company::getCurrency)
                                .map(Currency::getCode)
                                .orElse(null);
            }
        }

        List<CashBalance3> balList = stmt.getBal();
        if (balList != null && !balList.isEmpty()) {
            for (CashBalance3 balanceEntry : balList) {
                sequence =
                        createBalanceLine(
                                bankDetails,
                                balanceEntry,
                                sequence,
                                BALANCE_TYPE_INITIAL_BALANCE,
                                currencyCodeFromStmt);
                if (sequence % 10 == 0) {
                    JPA.clear();
                    findBankStatement();
                }
            }
        }

        List<ReportEntry2> ntryList = stmt.getNtry();
        if (ntryList != null && !ntryList.isEmpty()) {
            for (ReportEntry2 ntry : ntryList) {
                sequence = createEntryLine(bankDetails, ntry, sequence, currencyCodeFromStmt);
                if (sequence % 10 == 0) {
                    JPA.clear();
                    findBankStatement();
                }
            }
        }

        if (balList != null && !balList.isEmpty()) {
            for (CashBalance3 balanceEntry : balList) {
                sequence =
                        createBalanceLine(
                                bankDetails,
                                balanceEntry,
                                sequence,
                                BALANCE_TYPE_FINAL_BALANCE,
                                currencyCodeFromStmt);
                if (sequence % 10 == 0) {
                    JPA.clear();
                    findBankStatement();
                }
            }
        }
    }

    protected BankStatement findBankStatement() {
        bankStatement =
                JPA.em().contains(bankStatement)
                        ? bankStatement
                        : bankStatementRepository.find(bankStatement.getId());
        return bankStatement;
    }
}
