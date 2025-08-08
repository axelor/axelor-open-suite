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
package com.axelor.apps.account.service.analytic;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.AnalyticAxis;
import com.axelor.apps.account.db.AnalyticAxisByCompany;
import com.axelor.apps.account.db.AnalyticDistributionTemplate;
import com.axelor.apps.account.db.repo.AnalyticAccountRepository;
import com.axelor.apps.account.db.repo.AnalyticDistributionTemplateRepository;
import com.axelor.apps.account.db.repo.AnalyticLine;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.moveline.MoveLineComputeAnalyticService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.TradingName;
import com.axelor.common.ObjectUtils;
import com.axelor.utils.helpers.StringHelper;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AnalyticAttrsServiceImpl implements AnalyticAttrsService {

  private final int startAxisPosition = 1;
  private final int endAxisPosition = 5;

  protected AccountConfigService accountConfigService;
  protected MoveLineComputeAnalyticService moveLineComputeAnalyticService;
  protected AnalyticLineService analyticLineService;
  protected AnalyticToolService analyticToolService;
  protected AnalyticAccountService analyticAccountService;
  protected AnalyticDistributionTemplateRepository analyticDistributionTemplateRepository;
  protected AnalyticMoveLineService analyticMoveLineService;

  @Inject
  public AnalyticAttrsServiceImpl(
      AccountConfigService accountConfigService,
      MoveLineComputeAnalyticService moveLineComputeAnalyticService,
      AnalyticLineService analyticLineService,
      AnalyticToolService analyticToolService,
      AnalyticAccountService analyticAccountService,
      AnalyticDistributionTemplateRepository analyticDistributionTemplateRepository,
      AnalyticMoveLineService analyticMoveLineService) {
    this.accountConfigService = accountConfigService;
    this.moveLineComputeAnalyticService = moveLineComputeAnalyticService;
    this.analyticLineService = analyticLineService;
    this.analyticToolService = analyticToolService;
    this.analyticAccountService = analyticAccountService;
    this.analyticDistributionTemplateRepository = analyticDistributionTemplateRepository;
    this.analyticMoveLineService = analyticMoveLineService;
  }

  protected void addAttr(
      String field, String attr, Object value, Map<String, Map<String, Object>> attrsMap) {
    if (!attrsMap.containsKey(field)) {
      attrsMap.put(field, new HashMap<>());
    }

    attrsMap.get(field).put(attr, value);
  }

  @Override
  public void addAnalyticAxisAttrs(
      Company company, int massEntryStatusSelect, Map<String, Map<String, Object>> attrsMap)
      throws AxelorException {
    this.addAnalyticAxisAttrs(company, this.getMoveLineFieldName(massEntryStatusSelect), attrsMap);
  }

  @Override
  public void addAnalyticAxisAttrs(
      Company company, String parentField, Map<String, Map<String, Object>> attrsMap)
      throws AxelorException {
    if (analyticToolService.isManageAnalytic(company)) {
      AccountConfig accountConfig = accountConfigService.getAccountConfig(company);
      AnalyticAxis analyticAxis = null;

      for (int i = startAxisPosition; i <= endAxisPosition; i++) {
        String axisFieldName = getAxisFieldName(i, parentField);

        this.addAttr(
            axisFieldName, "hidden", !(i <= accountConfig.getNbrOfAnalyticAxisSelect()), attrsMap);

        for (AnalyticAxisByCompany analyticAxisByCompany :
            accountConfig.getAnalyticAxisByCompanyList()) {
          if (analyticAxisByCompany.getSequence() == i) {
            analyticAxis = analyticAxisByCompany.getAnalyticAxis();
          }
        }

        if (analyticAxis != null) {
          this.addAttr(axisFieldName, "title", analyticAxis.getName(), attrsMap);

          analyticAxis = null;
        }
      }
    } else {
      this.addAttr("analyticDistributionTemplate", "hidden", true, attrsMap);
      this.addAttr("analyticMoveLineList", "hidden", true, attrsMap);

      for (int i = startAxisPosition; i <= endAxisPosition; i++) {
        this.addAttr(getAxisFieldName(i, parentField), "hidden", true, attrsMap);
      }
    }
  }

  protected String getAxisFieldName(int axisPosition, String parentField) {
    String result = String.format("axis%dAnalyticAccount", axisPosition);

    return parentField != null ? parentField + "." + result : result;
  }

  protected String getMoveLineFieldName(int massEntryStatusSelect) {
    return massEntryStatusSelect != MoveRepository.MASS_ENTRY_STATUS_NULL
        ? "moveLineMassEntryList"
        : "moveLineList";
  }

  public void addAnalyticAxisDomains(
      AnalyticLine analyticLine, Company company, Map<String, Map<String, Object>> attrsMap)
      throws AxelorException {
    List<Long> analyticAccountList;

    for (int i = startAxisPosition; i <= endAxisPosition; i++) {
      if (analyticToolService.isPositionUnderAnalyticAxisSelect(company, i)) {
        analyticAccountList = analyticLineService.getAxisDomains(analyticLine, company, i);

        if (ObjectUtils.isEmpty(analyticAccountList)) {
          this.addAttr(
              String.format("axis%dAnalyticAccount", i), "domain", "self.id IN (0)", attrsMap);
        } else if (company != null) {
          String idList =
              analyticAccountList.stream().map(Object::toString).collect(Collectors.joining(","));

          this.addAttr(
              String.format("axis%dAnalyticAccount", i),
              "domain",
              String.format(
                  "%s AND self.id IN (%s) AND self.statusSelect = %d AND (self.company IS NULL OR self.company.id = %d)",
                  analyticAccountService.getIsNotParentAnalyticAccountQuery(),
                  idList,
                  AnalyticAccountRepository.STATUS_ACTIVE,
                  company.getId()),
              attrsMap);
        }
      }
    }
  }

  @Override
  public String getAnalyticDistributionTemplateDomain(
      Partner partner,
      Product product,
      Company company,
      TradingName tradingName,
      Account account,
      boolean isPurchase)
      throws AxelorException {
    if (company == null) {
      return "self.id IN (0)";
    }

    List<AnalyticDistributionTemplate> analyticDistributionTemplateList =
        analyticDistributionTemplateRepository
            .all()
            .filter("self.company.id = ?1 AND self.isSpecific ='false'", company.getId())
            .fetch();
    analyticDistributionTemplateList.add(
        analyticMoveLineService.getAnalyticDistributionTemplate(
            partner, product, company, tradingName, account, isPurchase));

    return "self.id IN (" + StringHelper.getIdListString(analyticDistributionTemplateList) + ")";
  }
}
