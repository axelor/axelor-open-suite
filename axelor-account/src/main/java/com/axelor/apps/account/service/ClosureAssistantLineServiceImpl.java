package com.axelor.apps.account.service;

import com.axelor.apps.account.db.ClosureAssistant;
import com.axelor.apps.account.db.ClosureAssistantLine;
import com.axelor.exception.AxelorException;
import java.util.ArrayList;
import java.util.List;

public class ClosureAssistantLineServiceImpl implements ClosureAssistantLineService {

  @Override
  public List<ClosureAssistantLine> initClosureAssistantLines(ClosureAssistant closureAssistant)
      throws AxelorException {
    List<ClosureAssistantLine> closureAssistantLineList = new ArrayList<ClosureAssistantLine>();
    for (int i = 1; i < 8; i++) {
      closureAssistantLineList.add(new ClosureAssistantLine(i, closureAssistant, i, false));
    }
    return closureAssistantLineList;
  }
}
