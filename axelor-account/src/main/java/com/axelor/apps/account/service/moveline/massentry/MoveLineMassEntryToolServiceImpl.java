package com.axelor.apps.account.service.moveline.massentry;

import com.axelor.apps.account.db.MoveLineMassEntry;
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
}
