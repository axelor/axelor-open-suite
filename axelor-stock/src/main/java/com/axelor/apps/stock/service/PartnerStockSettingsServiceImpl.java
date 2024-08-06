/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.stock.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.stock.db.PartnerStockSettings;
import com.axelor.apps.stock.db.StockConfig;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.repo.PartnerStockSettingsRepository;
import com.axelor.apps.stock.service.config.StockConfigService;
import com.axelor.inject.Beans;
import com.google.common.base.Predicates;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import org.apache.commons.collections.CollectionUtils;

public class PartnerStockSettingsServiceImpl implements PartnerStockSettingsService {

  protected PartnerStockSettingsRepository partnerStockSettingsRepository;

  @Inject
  public PartnerStockSettingsServiceImpl(
      PartnerStockSettingsRepository partnerStockSettingsRepository) {
    this.partnerStockSettingsRepository = partnerStockSettingsRepository;
  }

  @Override
  public PartnerStockSettings getOrCreateMailSettings(Partner partner, Company company)
      throws AxelorException {
    List<PartnerStockSettings> mailSettingsList = partner.getPartnerStockSettingsList();
    if (mailSettingsList == null || mailSettingsList.isEmpty()) {
      return createMailSettings(partner, company);
    }
    Optional<PartnerStockSettings> partnerStockSettings =
        mailSettingsList.stream()
            .filter(stockSettings -> company.equals(stockSettings.getCompany()))
            .findAny();
    return partnerStockSettings.isPresent()
        ? partnerStockSettings.get()
        : createMailSettings(partner, company);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public PartnerStockSettings createMailSettings(Partner partner, Company company)
      throws AxelorException {
    PartnerStockSettings mailSettings = new PartnerStockSettings();
    mailSettings.setCompany(company);
    StockConfig stockConfig = Beans.get(StockConfigService.class).getStockConfig(company);
    mailSettings.setPlannedStockMoveAutomaticMail(stockConfig.getPlannedStockMoveAutomaticMail());
    mailSettings.setPlannedStockMoveMessageTemplate(
        stockConfig.getPlannedStockMoveMessageTemplate());
    mailSettings.setRealStockMoveAutomaticMail(stockConfig.getRealStockMoveAutomaticMail());
    mailSettings.setRealStockMoveMessageTemplate(stockConfig.getRealStockMoveMessageTemplate());
    partner.addPartnerStockSettingsListItem(mailSettings);
    return Beans.get(PartnerStockSettingsRepository.class).save(mailSettings);
  }

  @Override
  public StockLocation getDefaultStockLocation(
      Partner partner, Company company, Predicate<? super StockLocation> predicate) {
    if (predicate == null) {
      predicate = Predicates.alwaysTrue();
    }

    List<PartnerStockSettings> partnerStockSettings = getPartnerStockSettings(partner, company);
    if (CollectionUtils.isEmpty(partnerStockSettings)) {
      return null;
    }

    return partnerStockSettings.stream()
        .map(PartnerStockSettings::getDefaultStockLocation)
        .filter(Objects::nonNull)
        .filter(predicate)
        .findAny()
        .orElse(null);
  }

  @Override
  public StockLocation getDefaultExternalStockLocation(
      Partner partner, Company company, Predicate<? super StockLocation> predicate) {
    if (predicate == null) {
      predicate = Predicates.alwaysTrue();
    }

    List<PartnerStockSettings> partnerStockSettings = getPartnerStockSettings(partner, company);
    if (CollectionUtils.isEmpty(partnerStockSettings)) {
      return null;
    }

    return partnerStockSettings.stream()
        .map(PartnerStockSettings::getDefaultExternalStockLocation)
        .filter(Objects::nonNull)
        .filter(predicate)
        .findAny()
        .orElse(null);
  }

  protected List<PartnerStockSettings> getPartnerStockSettings(Partner partner, Company company) {
    if (partner == null || company == null) {
      return null;
    }
    return partnerStockSettingsRepository
        .all()
        .filter("self.partner = ? AND self.company = ?", partner, company)
        .fetch();
  }
}
