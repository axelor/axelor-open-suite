package com.axelor.apps.account.service.moveline.massentry;

import com.axelor.apps.account.db.MoveLineMassEntry;
import com.axelor.apps.base.db.Partner;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MoveLineMassEntryToolServiceImpl implements MoveLineMassEntryToolService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Override
  public void setPaymentModeOnMoveLineMassEntry(
      MoveLineMassEntry line, Integer technicalTypeSelect) {
    switch (technicalTypeSelect) {
      case 1:
        line.setMovePaymentMode(line.getPartner().getOutPaymentMode());
        break;
      case 2:
        line.setMovePaymentMode(line.getPartner().getInPaymentMode());
        break;
      default:
        line.setMovePaymentMode(null);
        break;
    }
  }

  @Override
  public void setPartnerChanges(MoveLineMassEntry moveLine, MoveLineMassEntry newMoveLine) {
    if (newMoveLine == null) {
      moveLine.setPartner(null);
      moveLine.setPartnerId(null);
      moveLine.setPartnerSeq(null);
      moveLine.setPartnerFullName(null);
      moveLine.setMovePartnerBankDetails(null);
      moveLine.setVatSystemSelect(null);
      moveLine.setTaxLine(null);
      moveLine.setAnalyticDistributionTemplate(null);
      moveLine.setCurrencyCode(null);
    } else {
      Partner newPartner = newMoveLine.getPartner();
      moveLine.setPartner(newPartner);
      moveLine.setPartnerId(newPartner.getId());
      moveLine.setPartnerSeq(newPartner.getPartnerSeq());
      moveLine.setPartnerFullName(newPartner.getFullName());
      moveLine.setMovePartnerBankDetails(newMoveLine.getMovePartnerBankDetails());
      moveLine.setVatSystemSelect(newMoveLine.getVatSystemSelect());
      moveLine.setTaxLine(newMoveLine.getTaxLine());
      moveLine.setAnalyticDistributionTemplate(newMoveLine.getAnalyticDistributionTemplate());
      moveLine.setCurrencyCode(newMoveLine.getCurrencyCode());
    }
  }
}
