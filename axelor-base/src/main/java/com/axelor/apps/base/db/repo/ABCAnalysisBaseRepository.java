package com.axelor.apps.base.db.repo;

import com.axelor.apps.base.db.ABCAnalysis;

public class ABCAnalysisBaseRepository extends  ABCAnalysisRepository{

    @Override
    public ABCAnalysis copy(ABCAnalysis entity, boolean deep) {
        ABCAnalysis abcAnalysis = super.copy(entity, true);
        abcAnalysis.setStatusSelect(STATUS_DRAFT);
        abcAnalysis.setAbcAnalysisSeq(null);
        return abcAnalysis;
    }
}
