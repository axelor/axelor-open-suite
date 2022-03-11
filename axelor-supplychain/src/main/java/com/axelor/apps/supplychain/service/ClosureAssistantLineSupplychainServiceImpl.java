package com.axelor.apps.supplychain.service;

import com.axelor.apps.account.db.ClosureAssistant;
import com.axelor.apps.account.db.ClosureAssistantLine;
import com.axelor.apps.account.db.repo.ClosureAssistantLineRepository;
import com.axelor.apps.account.service.ClosureAssistantLineServiceImpl;
import com.axelor.apps.account.service.ClosureAssistantService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class ClosureAssistantLineSupplychainServiceImpl extends ClosureAssistantLineServiceImpl {

  @Inject
  public ClosureAssistantLineSupplychainServiceImpl(
      ClosureAssistantService closureAssistantService,
      ClosureAssistantLineRepository closureAssistantLineRepository,
      AppBaseService appBaseService) {
    super(closureAssistantService, closureAssistantLineRepository, appBaseService);
  }

  @Override
  public List<ClosureAssistantLine> initClosureAssistantLines(ClosureAssistant closureAssistant)
      throws AxelorException {
    List<ClosureAssistantLine> closureAssistantLineList = new ArrayList<ClosureAssistantLine>();
    for (int i = 1; i < 8; i++) {
      ClosureAssistantLine closureAssistantLine = new ClosureAssistantLine(i, null, i, false);

      if (i != 1) {
        closureAssistantLine.setIsPreviousLineValidated(false);
      } else {
        closureAssistantLine.setIsPreviousLineValidated(true);
      }

      closureAssistantLineList.add(closureAssistantLine);
    }

    return closureAssistantLineList;
  }
}
