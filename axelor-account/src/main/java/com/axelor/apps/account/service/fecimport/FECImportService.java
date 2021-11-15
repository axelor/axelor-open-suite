package com.axelor.apps.account.service.fecimport;

import com.axelor.apps.account.db.FECImport;
import com.axelor.apps.account.db.Move;
import java.util.List;

public interface FECImportService {

  void completeImport(FECImport fecImport, List<Move> moves);
}
