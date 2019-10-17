package com.axelor.apps.tool.service;

import com.axelor.exception.AxelorException;
import java.util.Map;

public interface ArchivingToolService {

  public Map<String, String> getObjectLinkTo(Object object, Long id) throws AxelorException;
}
