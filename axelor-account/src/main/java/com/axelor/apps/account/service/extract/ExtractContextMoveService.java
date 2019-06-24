package com.axelor.apps.account.service.extract;

import com.axelor.exception.AxelorException;
import com.axelor.rpc.Context;
import java.util.HashMap;

public interface ExtractContextMoveService {

  public HashMap<String, Object> getMapFromMoveWizardGenerateReverseForm(Context context)
      throws AxelorException;
}
