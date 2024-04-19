package com.axelor.apps.account.service.reconcilegroup;

import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.ReconcileGroup;
import com.axelor.apps.base.AxelorException;
import java.util.List;

public interface ReconcileGroupProposalService {

  void validateProposal(ReconcileGroup reconcileGroup) throws AxelorException;

  void createProposal(List<MoveLine> moveLineList);

  void cancelProposal(ReconcileGroup reconcileGroup);
}
