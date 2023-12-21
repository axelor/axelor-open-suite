package com.axelor.apps.quality.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.quality.db.ControlEntryPlanLine;

public interface ControlEntryPlanLineService {

    /**
     * Will evaluate the formula of controlEntryPlanLine
     * @param controlEntryPlanLine
     */
    void conformityEval(ControlEntryPlanLine controlEntryPlanLine) throws AxelorException;

    String getFormula(ControlEntryPlanLine controlEntryPlanLine) throws AxelorException;
}
