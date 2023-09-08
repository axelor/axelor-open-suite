package com.axelor.apps.base.service.api;

import com.axelor.apps.base.AxelorException;
import com.axelor.db.Model;

public interface ResponseComputeService {
  String compute(Model model) throws AxelorException;
}
