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
package com.axelor.apps.account.service.move.record;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.user.UserService;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MoveDefaultServiceImpl implements MoveDefaultService {

  protected UserService userService;
  protected CompanyRepository companyRepository;
  protected AppBaseService appBaseService;

  @Inject
  public MoveDefaultServiceImpl(
      UserService userService, CompanyRepository companyRepository, AppBaseService appBaseService) {
    this.userService = userService;
    this.companyRepository = companyRepository;
    this.appBaseService = appBaseService;
  }

  @Override
  public Map<String, Object> setDefaultMoveValues(Move move) {

    Objects.requireNonNull(move);
    HashMap<String, Object> resultMap = new HashMap<>();

    setDefaultValues(move);

    resultMap.put("company", move.getCompany());
    resultMap.put("date", move.getDate());
    resultMap.put("technicalOriginSelect", move.getTechnicalOriginSelect());
    resultMap.put("tradingName", move.getTradingName());

    return resultMap;
  }

  protected void setDefaultValues(Move move) {
    Company activeCompany = userService.getUserActiveCompany();

    setCompany(move, activeCompany);
    setDate(move);
    move.setTechnicalOriginSelect(MoveRepository.TECHNICAL_ORIGIN_ENTRY);
    move.setTradingName(userService.getTradingName());
    this.setDefaultCurrency(move);
  }

  protected void setDate(Move move) {
    if (move.getDate() == null) {
      move.setDate(appBaseService.getTodayDate(move.getCompany()));
    }
  }

  protected void setCompany(Move move, Company activeCompany) {
    if (activeCompany != null) {
      move.setCompany(activeCompany);
    } else {
      if (onlyOneCompanyExist()) {
        move.setCompany(companyRepository.all().fetchOne());
      }
    }
  }

  protected boolean onlyOneCompanyExist() {
    return companyRepository.all().count() == 1;
  }

  @Override
  public Map<String, Object> setDefaultCurrency(Move move) {

    Objects.requireNonNull(move);
    HashMap<String, Object> resultMap = new HashMap<>();

    Company company = move.getCompany();

    if (move.getPartner() == null && company != null && company.getCurrency() != null) {
      move.setCompanyCurrency(company.getCurrency());
      move.setCurrency(company.getCurrency());
      move.setCurrencyCode(company.getCurrency().getCodeISO());
      move.setCompanyCurrencyCode(company.getCurrency().getCodeISO());
    }

    resultMap.put("companyCurrency", move.getCompanyCurrency());
    resultMap.put("currency", move.getCurrency());
    resultMap.put("currencyCode", move.getCurrencyCode());
    resultMap.put("companyCurrencyCode", move.getCompanyCurrencyCode());

    return resultMap;
  }
}
