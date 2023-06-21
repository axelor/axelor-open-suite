package com.axelor.apps.account.service.moveline.massentry;

import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.MoveLineMassEntry;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.base.db.Partner;
import java.util.List;
import java.util.Objects;

public class MoveLineMassEntryToolServiceImpl implements MoveLineMassEntryToolService {

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

  @Override
  public void setAnalyticsFields(MoveLine newMoveLine, MoveLine moveLine) {
    newMoveLine.setAnalyticDistributionTemplate(moveLine.getAnalyticDistributionTemplate());
    newMoveLine.setAxis1AnalyticAccount(moveLine.getAxis1AnalyticAccount());
    newMoveLine.setAxis2AnalyticAccount(moveLine.getAxis2AnalyticAccount());
    newMoveLine.setAxis3AnalyticAccount(moveLine.getAxis3AnalyticAccount());
    newMoveLine.setAxis4AnalyticAccount(moveLine.getAxis4AnalyticAccount());
    newMoveLine.setAxis5AnalyticAccount(moveLine.getAxis5AnalyticAccount());
  }

  @Override
  public void setNewMoveStatusSelectMassEntryLines(
      List<MoveLineMassEntry> massEntryLines, Integer newStatusSelect) {
    for (MoveLineMassEntry line : massEntryLines) {
      if (!Objects.equals(MoveRepository.STATUS_ACCOUNTED, line.getMoveStatusSelect())) {
        line.setMoveStatusSelect(newStatusSelect);
      }
    }
  }
}
