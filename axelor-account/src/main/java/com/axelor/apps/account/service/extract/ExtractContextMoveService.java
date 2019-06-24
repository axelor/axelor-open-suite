package com.axelor.apps.account.service.extract;

import com.axelor.exception.AxelorException;
import com.axelor.rpc.Context;
import java.util.Map;

public interface ExtractContextMoveService {

  public Map<String, Object> getMapFromMoveWizardGenerateReverseForm(Context context)
      throws AxelorException;
}
