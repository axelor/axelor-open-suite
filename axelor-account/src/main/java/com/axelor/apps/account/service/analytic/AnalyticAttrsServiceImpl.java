package com.axelor.apps.account.service.analytic;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.AnalyticAxis;
import com.axelor.apps.account.db.AnalyticAxisByCompany;
import com.axelor.apps.account.db.repo.AnalyticAccountRepository;
import com.axelor.apps.account.db.repo.AnalyticLine;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.moveline.MoveLineComputeAnalyticService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.common.ObjectUtils;
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

  @Inject
  public AnalyticAttrsServiceImpl(
      AccountConfigService accountConfigService,
      MoveLineComputeAnalyticService moveLineComputeAnalyticService,
      AnalyticLineService analyticLineService,
      AnalyticToolService analyticToolService,
      AnalyticAccountService analyticAccountService) {
    this.accountConfigService = accountConfigService;
    this.moveLineComputeAnalyticService = moveLineComputeAnalyticService;
    this.analyticLineService = analyticLineService;
    this.analyticToolService = analyticToolService;
    this.analyticAccountService = analyticAccountService;
  }

  protected void addAttr(
      String field, String attr, Object value, Map<String, Map<String, Object>> attrsMap) {
    if (!attrsMap.containsKey(field)) {
      attrsMap.put(field, new HashMap<>());
    }

    attrsMap.get(field).put(attr, value);
  }

  @Override
  public void addAnalyticAxisAttrs(Company company, Map<String, Map<String, Object>> attrsMap)
      throws AxelorException {
    if (analyticToolService.isManageAnalytic(company)) {
      AccountConfig accountConfig = accountConfigService.getAccountConfig(company);
      AnalyticAxis analyticAxis = null;

      for (int i = startAxisPosition; i <= endAxisPosition; i++) {
        this.addAttr(
            String.format("axis%dAnalyticAccount", i),
            "hidden",
            !(i <= accountConfig.getNbrOfAnalyticAxisSelect()),
            attrsMap);

        for (AnalyticAxisByCompany analyticAxisByCompany :
            accountConfig.getAnalyticAxisByCompanyList()) {
          if (analyticAxisByCompany.getSequence() + 1 == i) {
            analyticAxis = analyticAxisByCompany.getAnalyticAxis();
          }
        }

        if (analyticAxis != null) {
          this.addAttr(
              String.format("axis%dAnalyticAccount", i), "title", analyticAxis.getName(), attrsMap);

          analyticAxis = null;
        }
      }
    } else {
      this.addAttr("analyticDistributionTemplate", "hidden", true, attrsMap);
      this.addAttr("analyticMoveLineList", "hidden", true, attrsMap);

      for (int i = startAxisPosition; i <= endAxisPosition; i++) {
        this.addAttr(
            "axis".concat(Integer.toString(i)).concat("AnalyticAccount"), "hidden", true, attrsMap);
      }
    }
  }

  public void addAxisDomains(
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
                  "self.id IN (%s) AND self.statusSelect = %d AND (self.company IS NULL OR self.company.id = %d)",
                  idList, AnalyticAccountRepository.STATUS_ACTIVE, company.getId()),
              attrsMap);
        }
      }
    }
  }
}
