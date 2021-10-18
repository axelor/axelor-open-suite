package com.axelor.apps.bankpayment.db.repo;

import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.db.BankStatementLineAFB120;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.db.Query;
import java.time.LocalDate;
import java.util.List;

public class BankPaymentBankStatementLineAFB120Repository
    extends BankStatementLineAFB120Repository {
  public BankStatementLineAFB120 findLineBetweenDate(
      LocalDate fromDate,
      LocalDate toDate,
      int lineType,
      boolean soonest,
      BankDetails bankDetails) {
    String order = "operationDate";
    if (!soonest) {
      order = "-" + order;
    }
    return all()
        .filter(
            "operationDate >= :fromDate"
                + " AND operationDate <= :toDate"
                + " AND lineTypeSelect = :lineType"
                + " AND bankDetails = :bankDetails")
        .bind("fromDate", fromDate)
        .bind("toDate", toDate)
        .bind("lineType", lineType)
        .bind("bankDetails", bankDetails)
        .order(order)
        .fetchOne();
  }

  public List<BankStatementLineAFB120> findLinesBetweenDate(
      LocalDate fromDate, LocalDate toDate, int lineType, BankDetails bankDetails) {
    return all()
        .filter(
            "operationDate >= :fromDate"
                + " AND operationDate <= :toDate"
                + " AND lineTypeSelect = :lineType"
                + " AND bankDetails = :bankDetails")
        .bind("fromDate", fromDate)
        .bind("toDate", toDate)
        .bind("lineType", lineType)
        .bind("bankDetails", bankDetails)
        .order("operationDate")
        .fetch();
  }

  public Query<BankStatementLineAFB120> findByBankStatementBankDetailsAndLineType(
      BankStatement bankStatement, BankDetails bankDetails, int lineType) {
    return all()
        .filter(
            "self.bankStatement = :bankStatement"
                + " AND self.bankDetails = :bankDetails"
                + " AND self.lineTypeSelect = :lineTypeSelect")
        .bind("bankStatement", bankStatement)
        .bind("bankDetails", bankDetails)
        .bind("lineTypeSelect", lineType);
  }

  public Query<BankStatementLineAFB120> findByBankStatementAndLineType(
      BankStatement bankStatement, int lineType) {
    return all()
        .filter("self.bankStatement = :bankStatement AND self.lineTypeSelect = :lineTypeSelect")
        .bind("bankStatement", bankStatement)
        .bind("lineTypeSelect", lineType);
  }

  public Query<BankStatementLineAFB120> findByBankDetailsLineTypeExcludeBankStatement(
      BankStatement bankStatement, BankDetails bankDetails, int lineType) {
    return all()
        .filter(
            "self.bankStatement != :bankStatement"
                + " AND self.bankDetails = :bankDetails"
                + " AND self.lineTypeSelect = :lineTypeSelect")
        .bind("bankStatement", bankStatement)
        .bind("bankDetails", bankDetails)
        .bind("lineTypeSelect", lineType);
  }
}
