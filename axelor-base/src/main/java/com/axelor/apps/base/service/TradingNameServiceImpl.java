package com.axelor.apps.base.service;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.PrintingSettings;
import com.axelor.apps.base.db.TradingName;
import com.axelor.apps.base.db.TradingNamePrintingSettings;
import com.axelor.db.JPA;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TradingNameServiceImpl implements TradingNameService {
    @Override
    public List<PrintingSettings> getPrintingSettingsList(TradingName tradingName, Company company) {
        List<PrintingSettings> printingSettingsList = new ArrayList<>();

        if (company == null) {
            return printingSettingsList;
        }
        if (tradingName == null || company.getId() == null || tradingName.getId() == null) {
            if (company.getPrintingSettings() != null) {
                printingSettingsList.add(company.getPrintingSettings());
            }
        }
        else {
            List<TradingNamePrintingSettings> tradingNamePrintingSettingsList = JPA.all(TradingNamePrintingSettings.class)
                    .filter("self.company.id = :company AND self.tradingName.id = :tradingName")
                    .bind("company", company.getId())
                    .bind("tradingName", tradingName.getId())
                    .fetch();
            printingSettingsList = tradingNamePrintingSettingsList.stream().map(TradingNamePrintingSettings::getPrintingSettings).collect(Collectors.toList());
        }
        return printingSettingsList;
    }
}
