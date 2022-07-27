package com.axelor.apps.account.service.moveline;

import com.axelor.apps.account.db.MoveLine;

public interface MoveLineCompletionService {

  /**
   * Complete account and partner related fields of move line
   *
   * @param moveLine
   */
  void freezeAccountAndPartnerFields(MoveLine moveLine);

  /**
   * Complete the analytic move line
   *
   * @param moveLine
   */
  void completeAnalyticMoveLine(MoveLine moveLine);
}
