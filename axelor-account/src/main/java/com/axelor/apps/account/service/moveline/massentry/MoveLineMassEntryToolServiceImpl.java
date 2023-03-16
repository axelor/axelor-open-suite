package com.axelor.apps.account.service.moveline.massentry;

import com.axelor.apps.account.db.MoveLineMassEntry;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MoveLineMassEntryToolServiceImpl implements MoveLineMassEntryToolService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Override
  public void setPaymentModeOnMoveLineMassEntry(
      MoveLineMassEntry moveLineMassEntry, Integer technicalTypeSelect) {
    switch (technicalTypeSelect) {
      case 1:
        moveLineMassEntry.setMovePaymentMode(moveLineMassEntry.getPartner().getOutPaymentMode());
        break;
      case 2:
        moveLineMassEntry.setMovePaymentMode(moveLineMassEntry.getPartner().getInPaymentMode());
        break;
      default:
        moveLineMassEntry.setMovePaymentMode(null);
        break;
    }
  }
}
