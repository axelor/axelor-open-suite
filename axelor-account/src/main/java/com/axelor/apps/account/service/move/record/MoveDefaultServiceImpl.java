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
  public void setDefaultValues(Move move) {
    this.setCompany(move);
    this.setDate(move);
    this.setDefaultCurrency(move);

    move.setTechnicalOriginSelect(MoveRepository.TECHNICAL_ORIGIN_ENTRY);
    move.setTradingName(userService.getTradingName());
  }

  protected void setDate(Move move) {
    if (move.getDate() == null) {
      move.setDate(appBaseService.getTodayDate(move.getCompany()));
    }
  }

  protected void setCompany(Move move) {
    Company activeCompany = userService.getUserActiveCompany();

    if (activeCompany != null) {
      move.setCompany(activeCompany);
    } else if (companyRepository.all().count() == 1) {
      move.setCompany(companyRepository.all().fetchOne());
    }
  }

  @Override
  public void setDefaultCurrency(Move move) {
    Company company = move.getCompany();

    if (move.getPartner() == null && company != null && company.getCurrency() != null) {
      move.setCompanyCurrency(company.getCurrency());
      move.setCurrency(company.getCurrency());
      move.setCurrencyCode(company.getCurrency().getCodeISO());
      move.setCompanyCurrencyCode(company.getCurrency().getCodeISO());
    }
  }

  @Override
  public void setDefaultCurrencyOnChange(Move move) {
    if (move.getCurrency() != null) {
      move.setCurrencyCode(move.getCurrency().getCodeISO());
    }
  }
}
