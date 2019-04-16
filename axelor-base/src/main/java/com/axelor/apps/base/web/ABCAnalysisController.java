package com.axelor.apps.base.web;

import com.axelor.apps.base.db.ABCAnalysis;
import com.axelor.apps.base.db.repo.ABCAnalysisRepository;
import com.axelor.apps.base.service.ABCAnalysisServiceImpl;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class ABCAnalysisController {

    @SuppressWarnings("unchecked")
    public void runAnalysis(ActionRequest request, ActionResponse response) {
        ABCAnalysis abcAnalysis = request.getContext().asType(ABCAnalysis.class);
        try {
            Class<? extends ABCAnalysisServiceImpl> clazz = (Class<? extends  ABCAnalysisServiceImpl>) Class.forName(abcAnalysis.getType());
            Beans.get(clazz).runAnalysis(Beans.get(ABCAnalysisRepository.class).find(abcAnalysis.getId()));
            response.setReload(true);
        } catch (ClassNotFoundException e) {
            response.setError(e.getMessage());
        }
    }
}
