package com.axelor.apps.account.service.extract;

import java.util.Map;
import com.axelor.exception.AxelorException;
import com.axelor.rpc.Context;

public interface ExtractContextMoveService {

  public Map<String, Object> getMapFromMoveWizardGenerateReverseForm(Context context)
      throws AxelorException;
}
