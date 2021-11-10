package com.axelor.apps.account.service.fecimport;

import com.axelor.apps.account.db.FECImport;
import com.axelor.apps.base.db.ImportConfiguration;
import com.axelor.apps.base.db.ImportHistory;

public interface FECImportService {

	ImportHistory runImport(FECImport fecImport, ImportConfiguration importConfig);
	
	
}
