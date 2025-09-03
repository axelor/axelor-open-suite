/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service.fixedasset.attributes;

import com.axelor.apps.account.db.AnalyticDistributionTemplate;
import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.db.repo.FixedAssetRepository;
import com.axelor.apps.account.service.analytic.AnalyticAttrsService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FixedAssetAttrsServiceImpl implements FixedAssetAttrsService {

  protected CurrencyScaleService currencyScaleService;

  @Inject
  public FixedAssetAttrsServiceImpl(CurrencyScaleService currencyScaleService) {
    this.currencyScaleService = currencyScaleService;
  }

  protected void addAttr(
      String field, String attr, Object value, Map<String, Map<String, Object>> attrsMap) {
    if (!attrsMap.containsKey(field)) {
      attrsMap.put(field, new HashMap<>());
    }

    attrsMap.get(field).put(attr, value);
  }

  @Override
  public void addDisposalAmountTitle(
      Integer disposalTypeSelect, Map<String, Map<String, Object>> attrsMap) {
    String title = "";

    if (Objects.equals(FixedAssetRepository.DISPOSABLE_TYPE_SELECT_SCRAPPING, disposalTypeSelect)) {
      title = I18n.get("Amount excluding taxes");
    } else if (Objects.equals(
        FixedAssetRepository.DISPOSABLE_TYPE_SELECT_CESSION, disposalTypeSelect)) {
      title = I18n.get("Sales price excluding taxes");
    }

    this.addAttr("disposalAmount", "title", title, attrsMap);
  }

  @Override
  public void addDisposalAmountReadonly(
      Integer disposalTypeSelect, Map<String, Map<String, Object>> attrsMap) {
    this.addAttr(
        "disposalAmount",
        "readonly",
        !Objects.equals(FixedAssetRepository.DISPOSABLE_TYPE_SELECT_CESSION, disposalTypeSelect),
        attrsMap);
  }

  @Override
  public void addDisposalAmountScale(
      FixedAsset fixedAsset, Map<String, Map<String, Object>> attrsMap) {
    this.addAttr(
        "disposalAmount", "scale", currencyScaleService.getCompanyScale(fixedAsset), attrsMap);
  }

  @Override
  public void addSplitTypeSelectValue(BigDecimal qty, Map<String, Map<String, Object>> attrsMap) {
    this.addAttr(
        "splitTypeSelect",
        "value",
        qty.compareTo(BigDecimal.ONE) == 0
            ? FixedAssetRepository.SPLIT_TYPE_AMOUNT
            : FixedAssetRepository.SPLIT_TYPE_QUANTITY,
        attrsMap);
  }

  @Override
  public void addSplitTypeSelectReadonly(
      BigDecimal qty, Map<String, Map<String, Object>> attrsMap) {
    this.addAttr("splitTypeSelect", "readonly", qty.compareTo(BigDecimal.ONE) == 0, attrsMap);
  }

  @Override
  public void addGrossValueScale(Company company, Map<String, Map<String, Object>> attrsMap) {
    this.addAttr(
        "grossValue", "scale", currencyScaleService.getCompanyCurrencyScale(company), attrsMap);
  }

  @Override
  public String addCurrentAnalyticDistributionTemplateInDomain(FixedAsset fixedAsset)
      throws AxelorException {
    String domain =
        Beans.get(AnalyticAttrsService.class)
            .getAnalyticDistributionTemplateDomain(
                fixedAsset.getPartner(),
                null,
                fixedAsset.getCompany(),
                null,
                fixedAsset.getPurchaseAccount(),
                false);

    AnalyticDistributionTemplate analyticDistributionTemplate =
        fixedAsset.getAnalyticDistributionTemplate();

    if (analyticDistributionTemplate != null) {
      domain += " OR self.id IN (" + analyticDistributionTemplate.getId() + ")";
    }

    return domain;
  }
}
