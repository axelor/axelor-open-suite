package com.axelor.apps.account.service;

import com.axelor.apps.account.db.ClosureAssistant;
import com.axelor.exception.AxelorException;

public interface ClosureAssistantService {

  public ClosureAssistant updateClosureAssistantProgress(ClosureAssistant closureAssistant)
      throws AxelorException;

  public ClosureAssistant updateFicalYear(ClosureAssistant closureAssistant) throws AxelorException;

  public ClosureAssistant updateCompany(ClosureAssistant closureAssistant) throws AxelorException;

  public boolean checkNoExistingClosureAssistantForSameYear(ClosureAssistant closureAssistant)
      throws AxelorException;
}
