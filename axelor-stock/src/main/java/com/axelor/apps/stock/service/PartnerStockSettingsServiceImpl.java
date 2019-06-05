/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
package com.axelor.apps.stock.service;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.stock.db.PartnerStockSettings;
import com.axelor.apps.stock.db.StockConfig;
import com.axelor.apps.stock.db.repo.PartnerStockSettingsRepository;
import com.axelor.apps.stock.service.config.StockConfigService;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.persist.Transactional;
import java.util.List;

public class PartnerStockSettingsServiceImpl implements PartnerStockSettingsService {

  @Override
  public PartnerStockSettings getOrCreateMailSettings(Partner partner, Company company)
      throws AxelorException {
    List<PartnerStockSettings> mailSettingsList = partner.getPartnerStockSettingsList();
    if (mailSettingsList == null || mailSettingsList.isEmpty()) {
      return createMailSettings(partner, company);
    }
    return mailSettingsList
        .stream()
        .filter(partnerStockSettings -> company.equals(partnerStockSettings.getCompany()))
        .findAny()
        .orElse(createMailSettings(partner, company));
  }

  @Override
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public PartnerStockSettings createMailSettings(Partner partner, Company company)
      throws AxelorException {
    PartnerStockSettings mailSettings = new PartnerStockSettings();
    mailSettings.setCompany(company);
    StockConfig stockConfig = Beans.get(StockConfigService.class).getStockConfig(company);
    mailSettings.setStockMoveAutomaticMail(stockConfig.getStockMoveAutomaticMail());
    mailSettings.setStockMoveMessageTemplate(stockConfig.getStockMoveMessageTemplate());
    partner.addPartnerStockSettingsListItem(mailSettings);
    return Beans.get(PartnerStockSettingsRepository.class).save(mailSettings);
  }
}
