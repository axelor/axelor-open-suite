package com.axelor.apps.base.service;

import com.axelor.apps.base.db.PrintTemplate;
import com.axelor.exception.AxelorException;
import java.io.IOException;
import java.util.Map;

public interface PrintTemplateService {

  Map<String, Object> generatePrint(
      Long objectId, String model, String simpleModel, PrintTemplate printTemplate)
      throws AxelorException, IOException, ClassNotFoundException;
}
