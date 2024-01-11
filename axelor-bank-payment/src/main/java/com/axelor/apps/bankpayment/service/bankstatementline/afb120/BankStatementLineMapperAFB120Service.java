package com.axelor.apps.bankpayment.service.bankstatementline.afb120;

import com.axelor.apps.base.AxelorException;
import java.util.List;

public interface BankStatementLineMapperAFB120Service {
  void writeStructuredContent(String lineData, List<StructuredContentLine> structuredContent)
      throws AxelorException;
}
