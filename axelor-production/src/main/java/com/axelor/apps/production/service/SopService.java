package com.axelor.apps.production.service;

import com.axelor.apps.production.db.Sop;
import com.axelor.exception.AxelorException;

public interface SopService {
  public void generateSOPLines(Sop sop) throws AxelorException;
}
