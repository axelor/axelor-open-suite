package com.axelor.apps.account.service.fecimport;

import com.axelor.apps.account.db.FECImport;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.google.inject.persist.Transactional;
import java.time.LocalDate;
import java.util.Map;

public interface ImportMoveFecService {
  @Transactional(rollbackOn = {Exception.class})
  Move createOrGetMove(
      Map<String, Object> values,
      Company company,
      FECImport fecImport,
      LocalDate moveLineDate,
      String importReference)
      throws Exception;

  MoveLine fillMoveLineInformation(
      MoveLine moveLine,
      Map<String, Object> values,
      Move move,
      FECImport fecImport,
      String importReference)
      throws AxelorException;
}
