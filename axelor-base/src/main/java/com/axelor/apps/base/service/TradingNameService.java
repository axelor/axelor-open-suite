package com.axelor.apps.base.service;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.PrintingSettings;
import com.axelor.apps.base.db.TradingName;

import java.util.List;

public interface TradingNameService {
    List<PrintingSettings> getPrintingSettingsList(TradingName tradingName, Company company);
}
