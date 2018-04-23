package com.axelor.apps.contract.web;

import com.axelor.apps.contract.db.ConsumptionLine;
import com.axelor.apps.contract.service.ConsumptionLineService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class ConsumptionLineController {

    public void changeProduct(ActionRequest request, ActionResponse response) {
        ConsumptionLine line = request.getContext()
                .asType(ConsumptionLine.class);
        try {
            Beans.get(ConsumptionLineService.class)
                    .fill(line, line.getProduct());
            response.setValues(line);
        } catch (Exception e) {
            TraceBackService.trace(response, e);
        }
    }

}
