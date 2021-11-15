package com.axelor.apps.account.service.fecimport;

import java.util.List;

import com.axelor.apps.account.db.FECImport;
import com.axelor.apps.account.db.Move;

public interface FECImportService {

	void completeImport(FECImport fecImport, List<Move> moves);

}
