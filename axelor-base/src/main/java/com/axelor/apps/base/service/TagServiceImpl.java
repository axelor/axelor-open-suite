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
package com.axelor.apps.base.service;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Tag;
import com.axelor.apps.base.db.TradingName;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.common.ObjectUtils;
import com.axelor.common.StringUtils;
import com.axelor.meta.MetaStore;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TagServiceImpl implements TagService {

  protected MetaModelRepository metaModelRepository;
  protected AppBaseService appBaseService;

  @Inject
  public TagServiceImpl(MetaModelRepository metaModelRepository, AppBaseService appBaseService) {
    this.metaModelRepository = metaModelRepository;
    this.appBaseService = appBaseService;
  }

  @Override
  public void addMetaModelToTag(Tag tag, String fullName) {
    if (!StringUtils.isEmpty(fullName)) {
      MetaModel metaModel =
          metaModelRepository.all().filter("self.fullName = ?", fullName).fetchOne();

      if (metaModel != null) {
        tag.addConcernedModelSetItem(metaModel);
      }
    }
  }

  protected void setDefaultColor(Tag tag) {
    String primaryColor = MetaStore.getSelectionItem("color.name.selection", "blue").getColor();
    tag.setColor(primaryColor);
  }

  @Override
  public Map<String, Object> getOnNewValuesMap(Tag tag, String fullNameModel, String fieldModel) {
    Map<String, Object> valuesMap = new HashMap<>();

    this.addMetaModelToTag(tag, fullNameModel);
    this.addMetaModelToTag(tag, fieldModel);
    this.setDefaultColor(tag);

    valuesMap.put("concernedModelSet", tag.getConcernedModelSet());
    valuesMap.put("color", tag.getColor());
    return valuesMap;
  }

  @Override
  public String getTagDomain(String fullNameModel, Company company) {
    String domain = this.getConcernedModelTagDomain(fullNameModel);
    Set<Company> companySet = new HashSet<>();
    List<TradingName> tradingNameList = new ArrayList<>();
    if (company != null) {
      companySet.add(company);
      tradingNameList = company.getTradingNameList();
    }

    domain = this.getCompanyTagDomain(domain, companySet, tradingNameList);

    return domain;
  }

  @Override
  public String getTagDomain(
      String fullNameModel, Set<Company> companySet, TradingName tradingName) {
    String domain = this.getConcernedModelTagDomain(fullNameModel);
    List<TradingName> tradingNameList = new ArrayList<>();
    if (tradingName != null) {
      tradingNameList.add(tradingName);
    }

    domain = this.getCompanyTagDomain(domain, companySet, tradingNameList);

    return domain;
  }

  protected String getConcernedModelTagDomain(String fullNameModel) {
    return String.format(
        "(self.concernedModelSet IS EMPTY OR %s member of self.concernedModelSet)",
        metaModelRepository.findByName(fullNameModel).getId());
  }

  protected String getCompanyTagDomain(
      String domain, Set<Company> companySet, List<TradingName> tradingNameList) {
    if (ObjectUtils.notEmpty(companySet)) {
      domain = domain.concat(" AND (self.companySet IS EMPTY");
      for (Company company : companySet) {
        domain = domain.concat(String.format(" OR (%s member of self.companySet", company.getId()));
        domain = this.getTradingNameTagDomain(domain, tradingNameList).concat(")");
      }
      domain = domain.concat(")");
    } else {
      domain = getTradingNameTagDomain(domain, tradingNameList);
    }

    return domain;
  }

  protected String getTradingNameTagDomain(String domain, List<TradingName> tradingNameList) {
    if (appBaseService.getAppBase().getEnableTradingNamesManagement()
        && !tradingNameList.isEmpty()) {
      domain = domain.concat(" AND (self.tradingNameSet IS EMPTY");
      for (TradingName tradingName : tradingNameList) {
        domain =
            domain.concat(
                String.format(" OR %s member of self.tradingNameSet", tradingName.getId()));
      }
      domain = domain.concat(")");
    }
    return domain;
  }
}
