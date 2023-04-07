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
    resultMap.put("getInfoFromFirstMoveLineOk", move.getGetInfoFromFirstMoveLineOk());
    resultMap.put("date", move.getDate());
    resultMap.put("technicalOriginSelect", move.getTechnicalOriginSelect());
    resultMap.put("tradingName", move.getTradingName());

    return resultMap;
  }

  protected void setDefaultValues(Move move) {
    Company activeCompany = userService.getUserActiveCompany();

    setCompany(move, activeCompany);
    move.setGetInfoFromFirstMoveLineOk(true);
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

    if (company != null && company.getCurrency() != null) {
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
