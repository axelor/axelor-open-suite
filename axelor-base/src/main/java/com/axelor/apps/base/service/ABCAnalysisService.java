package com.axelor.apps.base.service;

import com.axelor.apps.base.db.ABCAnalysis;
import com.axelor.apps.base.db.ABCAnalysisClass;
import com.axelor.apps.base.db.ABCAnalysisLine;
import com.axelor.apps.base.db.Product;
import com.axelor.exception.AxelorException;
import java.util.List;

public interface ABCAnalysisService {
    void runAnalysis(ABCAnalysis abcAnalysis) throws AxelorException;
    List<ABCAnalysisClass> initABCClasses();

}
