package com.axelor.apps.account.service.analytic;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.AnalyticAxis;
import com.axelor.apps.account.db.AnalyticAxisByCompany;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.moveline.MoveLineComputeAnalyticService;
import com.axelor.apps.base.AxelorException;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class AnalyticAttrsServiceImpl implements AnalyticAttrsService {

  private final int startAxisPosition = 1;
  private final int endAxisPosition = 5;

  protected AccountConfigService accountConfigService;
  protected MoveLineComputeAnalyticService moveLineComputeAnalyticService;

  @Inject
  public AnalyticAttrsServiceImpl(
      AccountConfigService accountConfigService,
      MoveLineComputeAnalyticService moveLineComputeAnalyticService) {
    this.accountConfigService = accountConfigService;
    this.moveLineComputeAnalyticService = moveLineComputeAnalyticService;
  }

  protected void addAttr(
      String field, String attr, Object value, Map<String, Map<String, Object>> attrsMap) {
    if (!attrsMap.containsKey(field)) {
      attrsMap.put(field, new HashMap<>());
    }

    attrsMap.get(field).put(attr, value);
  }

  @Override
  public void addAnalyticAxisAttrs(Move move, Map<String, Map<String, Object>> attrsMap)
      throws AxelorException {
    if (move != null && move.getCompany() != null) {
      AccountConfig accountConfig = accountConfigService.getAccountConfig(move.getCompany());

      if (moveLineComputeAnalyticService.checkManageAnalytic(move.getCompany())) {
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
                String.format("axis%dAnalyticAccount", i),
                "title",
                analyticAxis.getName(),
                attrsMap);

            analyticAxis = null;
          }
        }
      } else {
        this.addAttr("analyticDistributionTemplate", "hidden", true, attrsMap);
        this.addAttr("analyticMoveLineList", "hidden", true, attrsMap);

        for (int i = startAxisPosition; i <= endAxisPosition; i++) {
          this.addAttr(
              "axis".concat(Integer.toString(i)).concat("AnalyticAccount"),
              "hidden",
              true,
              attrsMap);
        }
      }
    }
  }
}
