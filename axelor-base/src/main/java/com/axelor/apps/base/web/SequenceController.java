package com.axelor.apps.base.web;

import com.axelor.apps.base.db.Sequence;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class SequenceController {
    public void getDefaultTitle(ActionRequest request, ActionResponse response) {
        Sequence sequence = request.getContext().asType(Sequence.class);
        String defautlTitle = Beans.get(SequenceService.class).getDefaultTitle(sequence);
        response.setValue("name", I18n.get(defautlTitle));
    }
}
