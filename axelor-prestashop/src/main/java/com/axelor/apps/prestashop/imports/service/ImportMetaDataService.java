package com.axelor.apps.prestashop.imports.service;

import com.axelor.apps.base.db.Language;
import com.axelor.apps.prestashop.service.library.PSWebServiceClient;
import com.axelor.apps.prestashop.service.library.PrestaShopWebserviceException;

public interface ImportMetaDataService {
	void importLanguages(PSWebServiceClient ws) throws PrestaShopWebserviceException;
	void importOrderStatuses(Language prestashopLanguage, PSWebServiceClient ws) throws PrestaShopWebserviceException;
}
