/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
    } else {
      List<TradingNamePrintingSettings> tradingNamePrintingSettingsList =
          JPA.all(TradingNamePrintingSettings.class)
              .filter("self.company.id = :company AND self.tradingName.id = :tradingName")
              .bind("company", company.getId())
              .bind("tradingName", tradingName.getId())
              .fetch();
      printingSettingsList =
          tradingNamePrintingSettingsList
              .stream()
              .map(TradingNamePrintingSettings::getPrintingSettings)
              .collect(Collectors.toList());
    }
    return printingSettingsList;
  }

  @Override
  public PrintingSettings getDefaultPrintingSettings(TradingName tradingName, Company company) {
    List<PrintingSettings> printingSettingsList = getPrintingSettingsList(tradingName, company);
    return printingSettingsList.isEmpty() ? null : printingSettingsList.get(0);
  }
}
