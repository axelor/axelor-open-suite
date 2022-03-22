package com.axelor.apps.account.service;

import com.axelor.apps.account.db.ClosureAssistant;
import com.axelor.apps.account.db.ClosureAssistantLine;
import com.axelor.exception.AxelorException;
import java.util.List;
import java.util.Map;

public interface ClosureAssistantLineService {

  List<ClosureAssistantLine> initClosureAssistantLines(ClosureAssistant closureAssistant)
      throws AxelorException;

  void cancelClosureAssistantLine(ClosureAssistantLine closureAssistantLine) throws AxelorException;

  void validateClosureAssistantLine(ClosureAssistantLine closureAssistantLine)
      throws AxelorException;

  Map<String, Object> getViewToOpen(ClosureAssistantLine closureAssistantLine)
      throws AxelorException;
}
