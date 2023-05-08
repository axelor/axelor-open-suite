package com.axelor.apps.bankpayment.service.moveline;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.move.MoveLoadDefaultConfigService;
import com.axelor.apps.account.service.moveline.MoveLineComputeAnalyticService;
import com.axelor.apps.account.service.moveline.MoveLineDefaultServiceImpl;
import com.google.inject.Inject;
import org.apache.commons.collections.CollectionUtils;

public class MoveLineDefaultServiceBankPaymentImpl extends MoveLineDefaultServiceImpl {

  @Inject
  public MoveLineDefaultServiceBankPaymentImpl(
      AppAccountService appAccountService,
      MoveLoadDefaultConfigService moveLoadDefaultConfigService,
      MoveLineComputeAnalyticService moveLineComputeAnalyticService) {
    super(appAccountService, moveLoadDefaultConfigService, moveLineComputeAnalyticService);
  }

  @Override
  public void setFieldsFromFirstMoveLine(MoveLine moveLine, Move move) {
    if (move == null || CollectionUtils.isEmpty(move.getMoveLineList())) {
      return;
    }
    super.setFieldsFromFirstMoveLine(moveLine, move);
    moveLine.setInterbankCodeLine(move.getMoveLineList().get(0).getInterbankCodeLine());
  }
}
