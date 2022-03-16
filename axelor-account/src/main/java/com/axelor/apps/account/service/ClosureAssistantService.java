package com.axelor.apps.account.service;

import com.axelor.apps.account.db.ClosureAssistant;

public interface ClosureAssistantService {

  public ClosureAssistant updateClosureAssistantProgress(ClosureAssistant closureAssistant);

  public ClosureAssistant updateFiscalYear(ClosureAssistant closureAssistant);

  public ClosureAssistant updateCompany(ClosureAssistant closureAssistant);

  public boolean checkNoExistingClosureAssistantForSameYear(ClosureAssistant closureAssistant);

  public boolean setStatusWithLines(ClosureAssistant closureAssistant);
}
