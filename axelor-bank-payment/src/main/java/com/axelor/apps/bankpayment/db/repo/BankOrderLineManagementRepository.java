package com.axelor.apps.bankpayment.db.repo;

import com.axelor.apps.bankpayment.db.BankOrderLine;
import com.axelor.apps.bankpayment.db.BankOrderLineOrigin;
import com.axelor.apps.tool.StringTool;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class BankOrderLineManagementRepository extends BankOrderLineRepository {

  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {
    Long bankOrderLineId = (Long) json.get("id");
    BankOrderLine bankOrderLine = find(bankOrderLineId);
    String pieceReferenceList = "";
    String pieceDateList = "";
    String pieceDueDateList = "";
    boolean bFirst = true;

    for (BankOrderLineOrigin bankOrderLineOrigin : bankOrderLine.getBankOrderLineOriginList()) {
      if (bFirst) {
        pieceReferenceList += changeNullToEmptyString(bankOrderLineOrigin.getRelatedToSelectName());
        pieceDateList += changeDateToString(bankOrderLineOrigin.getRelatedToSelectDate());
        pieceDueDateList += changeDateToString(bankOrderLineOrigin.getRelatedToSelectDueDate());
        bFirst = false;
      } else {
        pieceReferenceList +=
            "," + changeNullToEmptyString(bankOrderLineOrigin.getRelatedToSelectName());
        pieceDateList += "," + changeDateToString(bankOrderLineOrigin.getRelatedToSelectDate());
        pieceDueDateList +=
            "," + changeDateToString(bankOrderLineOrigin.getRelatedToSelectDueDate());
      }
    }

    json.put("$pieceReferenceList", StringTool.cutTooLongString(pieceReferenceList));
    json.put("$pieceDateList", StringTool.cutTooLongString(pieceDateList));
    json.put("$pieceDueDateList", StringTool.cutTooLongString(pieceDueDateList));

    return super.populate(json, context);
  }

  protected String changeNullToEmptyString(Object object) {
    if (object == null) {
      return "";
    } else {
      return object.toString();
    }
  }

  protected String changeDateToString(LocalDate date) {
    if (date == null) {
      return "";
    } else {
      return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
  }
}
