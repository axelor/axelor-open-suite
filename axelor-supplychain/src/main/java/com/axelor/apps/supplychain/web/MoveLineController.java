package com.axelor.apps.supplychain.web;

import com.axelor.apps.base.db.Batch;
import com.axelor.apps.supplychain.exception.IExceptionMessage;
import com.axelor.apps.supplychain.service.MoveLineServiceSupplychain;
import com.axelor.common.ObjectUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class MoveLineController {
  @SuppressWarnings("unchecked")
  public void validateCutOffBatch(ActionRequest request, ActionResponse response) {
    try {
      Context context = request.getContext();

      if (!context.containsKey("_ids")) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_NO_VALUE, IExceptionMessage.CUT_OFF_BATCH_NO_LINE);
      }

      List<Long> ids =
          (List)
              (((List) context.get("_ids"))
                  .stream()
                      .filter(ObjectUtils::notEmpty)
                      .map(input -> Long.parseLong(input.toString()))
                      .collect(Collectors.toList()));
      Long id = (long) (int) context.get("_batchId");

      if (CollectionUtils.isEmpty(ids)) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_NO_VALUE, IExceptionMessage.CUT_OFF_BATCH_NO_LINE);
      } else {
        Batch batch = Beans.get(MoveLineServiceSupplychain.class).validateCutOffBatch(ids, id);
        response.setFlash(batch.getComments());
      }

      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
