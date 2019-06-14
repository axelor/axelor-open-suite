package com.axelor.apps.base.service;

import com.axelor.apps.base.db.ABCAnalysis;
import com.axelor.apps.base.db.ABCAnalysisClass;
import com.axelor.exception.AxelorException;
import java.util.List;

public interface ABCAnalysisService {
    void reset(ABCAnalysis abcAnalysis);
    void runAnalysis(ABCAnalysis abcAnalysis) throws AxelorException;
    List<ABCAnalysisClass> initABCClasses();
    void setSequence(ABCAnalysis abcAnalysis);
    String printReport(ABCAnalysis abcAnalysis) throws AxelorException;
    void checkClasses(ABCAnalysis abcAnalysis) throws AxelorException;
}
