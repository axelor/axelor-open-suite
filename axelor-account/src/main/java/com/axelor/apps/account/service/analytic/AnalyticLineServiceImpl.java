/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
import com.axelor.apps.account.db.AnalyticAccount;
import com.axelor.apps.account.db.AnalyticAxis;
import com.axelor.apps.account.db.AnalyticAxisByCompany;
import com.axelor.apps.account.db.AnalyticJournal;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.AnalyticAccountRepository;
import com.axelor.apps.account.db.repo.AnalyticLine;
import com.axelor.apps.account.service.AccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.moveline.MoveLineComputeAnalyticService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.utils.service.ListToolService;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.collections.CollectionUtils;

public class AnalyticLineServiceImpl implements AnalyticLineService {

  private static final int RETURN_SCALE = 2;
  protected AccountConfigService accountConfigService;
  protected AppBaseService appBaseService;
  protected AnalyticToolService analyticToolService;
  protected AnalyticAccountRepository analyticAccountRepository;
  protected AccountService accountService;
  protected ListToolService listToolService;
  protected MoveLineComputeAnalyticService moveLineComputeAnalyticService;

  @Inject
  public AnalyticLineServiceImpl(
      AccountConfigService accountConfigService,
      AppBaseService appBaseService,
      AnalyticToolService analyticToolService,
      AnalyticAccountRepository analyticAccountRepository,
      AccountService accountService,
      ListToolService listToolService,
      MoveLineComputeAnalyticService moveLineComputeAnalyticService) {
    this.accountConfigService = accountConfigService;
    this.appBaseService = appBaseService;
    this.analyticToolService = analyticToolService;
    this.analyticAccountRepository = analyticAccountRepository;
    this.accountService = accountService;
    this.listToolService = listToolService;
    this.moveLineComputeAnalyticService = moveLineComputeAnalyticService;
  }

  @Override
  public AnalyticJournal getAnalyticJournal(AnalyticLine analyticLine) throws AxelorException {
    if (analyticLine.getAccount() != null && analyticLine.getAccount().getCompany() != null) {
      return accountConfigService
          .getAccountConfig(analyticLine.getAccount().getCompany())
          .getAnalyticJournal();
    }
    return null;
  }

  @Override
  public LocalDate getDate(AnalyticLine analyticLine) {
    if (analyticLine instanceof MoveLine) {
      MoveLine line = (MoveLine) analyticLine;
      if (line.getDate() != null) {
        return line.getDate();
      }
    }
    if (analyticLine.getAccount() != null && analyticLine.getAccount().getCompany() != null) {
      return appBaseService.getTodayDate(analyticLine.getAccount().getCompany());
    }
    return appBaseService.getTodayDate(null);
  }

  @Override
  public BigDecimal getAnalyticAmountFromParent(
      AnalyticLine parent, AnalyticMoveLine analyticMoveLine) {

    if (parent.getLineAmount().signum() > 0) {
      return analyticMoveLine
          .getPercentage()
          .multiply(parent.getLineAmount())
          .divide(new BigDecimal(100), RETURN_SCALE, RoundingMode.HALF_UP);
    }
    return BigDecimal.ZERO;
  }

  @Override
  public List<Long> getAxisDomains(AnalyticLine line, Company company, int position)
      throws AxelorException {
    List<Long> analyticAccountListByAxis = new ArrayList<>();

    AnalyticAxis analyticAxis = new AnalyticAxis();

    if (analyticToolService.isPositionUnderAnalyticAxisSelect(company, position)) {

      for (AnalyticAxisByCompany axis :
          accountConfigService.getAccountConfig(company).getAnalyticAxisByCompanyList()) {
        if (axis.getSequence() + 1 == position) {
          analyticAxis = axis.getAnalyticAxis();
        }
      }
      analyticAccountListByAxis = getAnalyticAccountsByAxis(line, analyticAxis);
    }
    return analyticAccountListByAxis;
  }

  @Override
  public List<Long> getAnalyticAccountsByAxis(AnalyticLine line, AnalyticAxis analyticAxis) {
    List<Long> analyticAccountListByAxis = new ArrayList<>();
    List<Long> analyticAccountListByRules = new ArrayList<>();

    for (AnalyticAccount analyticAccount :
        analyticAccountRepository.findByAnalyticAxis(analyticAxis).fetch()) {
      analyticAccountListByAxis.add(analyticAccount.getId());
    }
    if (line.getAccount() != null) {
      List<Long> analyticAccountIdList = accountService.getAnalyticAccountsIds(line.getAccount());
      if (CollectionUtils.isNotEmpty(analyticAccountIdList)) {
        for (Long analyticAccountId : analyticAccountIdList) {
          analyticAccountListByRules.add(analyticAccountId);
        }
        if (!CollectionUtils.isEmpty(analyticAccountListByRules)) {
          analyticAccountListByAxis =
              listToolService.intersection(analyticAccountListByAxis, analyticAccountListByRules);
        }
      }
    }
    return analyticAccountListByAxis;
  }

  @Override
  public boolean isAxisRequired(AnalyticLine line, Company company, int position)
      throws AxelorException {
    if (company != null
        && moveLineComputeAnalyticService.checkManageAnalytic(company)
        && line != null
        && line.getAccount() != null
        && line.getAccount().getCompany() != null) {
      Account account = line.getAccount();
      Integer nbrAxis =
          accountConfigService.getAccountConfig(account.getCompany()).getNbrOfAnalyticAxisSelect();
      return account.getAnalyticDistributionAuthorized()
          && account.getAnalyticDistributionRequiredOnMoveLines()
          && line.getAnalyticDistributionTemplate() == null
          && position <= nbrAxis;
    }
    return false;
  }

  @Override
  public AnalyticLine checkAnalyticLineForAxis(AnalyticLine line) {
    if (analyticToolService.isAnalyticAxisFilled(
        line.getAxis1AnalyticAccount(), line.getAnalyticMoveLineList())) {
      line.setAxis1AnalyticAccount(null);
    }
    if (analyticToolService.isAnalyticAxisFilled(
        line.getAxis2AnalyticAccount(), line.getAnalyticMoveLineList())) {
      line.setAxis2AnalyticAccount(null);
    }
    if (analyticToolService.isAnalyticAxisFilled(
        line.getAxis3AnalyticAccount(), line.getAnalyticMoveLineList())) {
      line.setAxis3AnalyticAccount(null);
    }
    if (analyticToolService.isAnalyticAxisFilled(
        line.getAxis4AnalyticAccount(), line.getAnalyticMoveLineList())) {
      line.setAxis4AnalyticAccount(null);
    }
    if (analyticToolService.isAnalyticAxisFilled(
        line.getAxis5AnalyticAccount(), line.getAnalyticMoveLineList())) {
      line.setAxis5AnalyticAccount(null);
    }
    return line;
  }

  @Override
  public AnalyticLine printAnalyticAccount(AnalyticLine analyticLine, Company company)
      throws AxelorException {
    if (CollectionUtils.isEmpty(analyticLine.getAnalyticMoveLineList()) || company == null) {
      this.resetAxisAnalyticAccount(analyticLine);
      return analyticLine;
    }

    for (AnalyticAxisByCompany analyticAxisByCompany :
        accountConfigService.getAccountConfig(company).getAnalyticAxisByCompanyList()) {
      if (checkAnalyticAxisPercentage(analyticLine, analyticAxisByCompany.getAnalyticAxis())) {
        AnalyticMoveLine analyticMoveLine =
            getAnalyticMoveLines(analyticLine, analyticAxisByCompany.getAnalyticAxis()).get(0);
        this.setAxisAccount(analyticLine, analyticAxisByCompany, analyticMoveLine);
      } else {
        this.setAxisAccount(analyticLine, analyticAxisByCompany, null);
      }
    }

    return analyticLine;
  }

  protected boolean checkAnalyticAxisPercentage(
      AnalyticLine analyticLine, AnalyticAxis analyticAxis) {
    return getAnalyticMoveLines(analyticLine, analyticAxis).size() == 1
        && getAnalyticMoveLineOnAxis(analyticLine, analyticAxis).count() == 1;
  }

  protected List<AnalyticMoveLine> getAnalyticMoveLines(
      AnalyticLine analyticLine, AnalyticAxis analyticAxis) {
    return getAnalyticMoveLineOnAxis(analyticLine, analyticAxis)
        .filter(it -> it.getPercentage().compareTo(new BigDecimal(100)) == 0)
        .collect(Collectors.toList());
  }

  protected Stream<AnalyticMoveLine> getAnalyticMoveLineOnAxis(
      AnalyticLine analyticLine, AnalyticAxis analyticAxis) {
    return analyticLine.getAnalyticMoveLineList().stream()
        .filter(it -> it.getAnalyticAxis().equals(analyticAxis));
  }

  protected void setAxisAccount(
      AnalyticLine analyticLine,
      AnalyticAxisByCompany analyticAxisByCompany,
      AnalyticMoveLine analyticMoveLine) {
    AnalyticAccount analyticAccount = null;
    if (analyticMoveLine != null) {
      analyticAccount = analyticMoveLine.getAnalyticAccount();
    }

    switch (analyticAxisByCompany.getSequence()) {
      case 0:
        analyticLine.setAxis1AnalyticAccount(analyticAccount);
        break;
      case 1:
        analyticLine.setAxis2AnalyticAccount(analyticAccount);
        break;
      case 2:
        analyticLine.setAxis3AnalyticAccount(analyticAccount);
        break;
      case 3:
        analyticLine.setAxis4AnalyticAccount(analyticAccount);
        break;
      case 4:
        analyticLine.setAxis5AnalyticAccount(analyticAccount);
        break;
      default:
        break;
    }
  }

  @Override
  public Map<String, Map<String, Object>> getAnalyticAxisAttrsMap(
      Company company, int startAxisPosition, int endAxisPosition) throws AxelorException {
    Map<String, Map<String, Object>> attrsMap = new HashMap<>();

    AccountConfig accountConfig = accountConfigService.getAccountConfig(company);

    if (moveLineComputeAnalyticService.checkManageAnalytic(company)) {
      AnalyticAxis analyticAxis = null;

      for (int i = startAxisPosition; i <= endAxisPosition; i++) {
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

        this.addAttr(
            String.format("axis%dAnalyticAccount", i),
            "hidden",
            !(i <= accountConfig.getNbrOfAnalyticAxisSelect()),
            attrsMap);
      }
    } else {
      this.addAttr("analyticDistributionTemplate", "hidden", true, attrsMap);
      this.addAttr("analyticMoveLineList", "hidden", true, attrsMap);

      for (int i = startAxisPosition; i <= endAxisPosition; i++) {
        this.addAttr(String.format("axis%dAnalyticAccount", i), "hidden", true, attrsMap);
      }
    }

    return attrsMap;
  }

  protected void addAttr(
      String field, String attr, Object value, Map<String, Map<String, Object>> attrsMap) {
    if (!attrsMap.containsKey(field)) {
      attrsMap.put(field, new HashMap<>());
    }

    attrsMap.get(field).put(attr, value);
  }

  protected void resetAxisAnalyticAccount(AnalyticLine analyticLine) {
    analyticLine.setAxis1AnalyticAccount(null);
    analyticLine.setAxis2AnalyticAccount(null);
    analyticLine.setAxis3AnalyticAccount(null);
    analyticLine.setAxis4AnalyticAccount(null);
    analyticLine.setAxis5AnalyticAccount(null);
  }
}
