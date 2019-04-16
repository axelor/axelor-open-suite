package com.axelor.apps.base.service;

import com.axelor.apps.base.db.ABCAnalysis;
import com.axelor.apps.base.db.ABCAnalysisLine;
import com.axelor.apps.base.db.Product;

public interface ABCAnalysisService {
    void runAnalysis(ABCAnalysis abcAnalysis);
}
