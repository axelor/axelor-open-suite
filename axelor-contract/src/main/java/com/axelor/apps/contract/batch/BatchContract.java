package com.axelor.apps.contract.batch;

import com.axelor.apps.base.service.batch.BatchStrategy;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.ContractBatchAction;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.common.base.Preconditions;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class BatchContract extends BatchStrategy {
  private static final Map<ContractBatchAction, BatchContractState> STATES =
      new EnumMap<>(ContractBatchAction.class);

  static {
    STATES.put(ContractBatchAction.INVOICING, Beans.get(BatchContractStateInvoicing.class));
    STATES.put(ContractBatchAction.TERMINATE, Beans.get(BatchContractStateTerminate.class));
  }

  @Override
  protected void process() {
    try {
      BatchContractState state = STATES.get(batch.getContractBatch().getActionEnum());
      Preconditions.checkNotNull(
          state,
          String.format(
              I18n.get("Action %s has no Batch implementation."),
              batch.getContractBatch().getActionEnum().getValue()));

      Query<Contract> query = state.prepare();
      List<Contract> contracts;

      while (!(contracts = query.fetch(FETCH_LIMIT)).isEmpty()) {
        findBatch();
        for (Contract contract : contracts) {
          try {
            state.process(contract);
            incrementDone();
          } catch (Exception e) {
            TraceBackService.trace(e);
            incrementAnomaly();
          }
        }
        JPA.clear();
      }
    } catch (Exception e) {
      TraceBackService.trace(e);
      LOG.error(e.getMessage());
    }
  }

  @Override
  protected void stop() {
    super.stop();
    addComment(
        String.format(
            "%d contract(s) treated and %d anomaly(ies) reported !",
            batch.getDone(), batch.getAnomaly()));
  }
}
