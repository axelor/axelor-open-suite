package com.axelor.apps.account.web;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.service.batch.BatchControlMovesConsistency;
import com.axelor.apps.base.db.Batch;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class BatchAccountController {

  public void showMoveError(ActionRequest request, ActionResponse response) {
    try {
      Long batchId = request.getContext().asType(Batch.class).getId();

      List<Long> idList = Beans.get(BatchControlMovesConsistency.class).getAllMovesId(batchId);
      if (!CollectionUtils.isEmpty(idList)) {
        response.setView(
            ActionView.define("Moves")
                .model(Move.class.getName())
                .add("grid", "move-grid")
                .add("form", "move-form")
                .domain(
                    "self.id in ("
                        + idList.stream().map(id -> id.toString()).collect(Collectors.joining(","))
                        + ")")
                .map());
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
