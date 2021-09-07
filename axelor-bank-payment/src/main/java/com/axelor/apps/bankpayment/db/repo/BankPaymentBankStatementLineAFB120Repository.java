package com.axelor.apps.bankpayment.db.repo;

import com.axelor.apps.bankpayment.db.BankStatementLineAFB120;
import com.axelor.apps.base.db.BankDetails;
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
    if (!soonest) order = "-" + order;
    return all()
        .filter(
            "operationDate >= :fromDate and operationDate <= :toDate and lineTypeSelect = :lineType and bankDetails = :bankDetails")
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
            "operationDate >= :fromDate and operationDate <= :toDate and lineTypeSelect = :lineType and bankDetails = :bankDetails")
        .bind("fromDate", fromDate)
        .bind("toDate", toDate)
        .bind("lineType", lineType)
        .bind("bankDetails", bankDetails)
        .order("operationDate")
        .fetch();
  }
}
