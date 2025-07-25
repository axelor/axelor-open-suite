package com.axelor.apps.bankpayment.service.bankstatementline.camt53;

import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.db.repo.BankStatementRepository;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.AccountStatement2;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.DateTimePeriodDetails;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import javax.xml.datatype.XMLGregorianCalendar;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class CAMT53ToolServiceImpl implements CAMT53ToolService{

    protected BankStatementRepository bankStatementRepository;

    @Inject
    public CAMT53ToolServiceImpl(BankStatementRepository bankStatementRepository){
        this.bankStatementRepository = bankStatementRepository;
    }

    @Override
    @Transactional(rollbackOn = {Exception.class})
    public void computeBankStatementDates(BankStatement bankStatement, List<AccountStatement2> stmtList){
        bankStatement.setFromDate(computeLocalDate(Optional.ofNullable(stmtList.get(0)).map(AccountStatement2::getFrToDt).map(DateTimePeriodDetails::getFrDtTm).orElse(null)));
        bankStatement.setToDate(computeLocalDate(Optional.ofNullable(stmtList.get(stmtList.size()-1)).map(AccountStatement2::getFrToDt).map(DateTimePeriodDetails::getToDtTm).orElse(null)));

        bankStatementRepository.save(bankStatement);
    }

    protected LocalDate computeLocalDate(XMLGregorianCalendar date){
        if (date == null){
            return null;
        }

        return LocalDate.of(date.getYear(), date.getMonth(), date.getDay());
    }
}
