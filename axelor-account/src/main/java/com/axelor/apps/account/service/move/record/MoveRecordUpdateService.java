package com.axelor.apps.account.service.move.record;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.service.move.record.model.MoveContext;
import com.axelor.exception.AxelorException;
import com.axelor.rpc.Context;

public interface MoveRecordUpdateService {

  void updatePartner(Move move);

  MoveContext updateInvoiceTerms(Move move, Context context) throws AxelorException;

  void updateRoundInvoiceTermPercentages(Move move);

  void updateDueDate(Move move, Context context);

  void updateInDayBookMode(Move move) throws AxelorException;
}
