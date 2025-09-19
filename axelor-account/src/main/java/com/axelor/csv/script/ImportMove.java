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
package com.axelor.csv.script;

import com.axelor.apps.account.db.FECImport;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.FECImportRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.analytic.ImportAnalyticInMoveService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.fecimport.ImportMoveFecService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.common.StringUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

public class ImportMove {

  @Inject private MoveRepository moveRepository;
  @Inject private MoveValidateService moveValidateService;
  @Inject private AppAccountService appAccountService;
  @Inject private ImportMoveFecService importMoveFecService;
  @Inject private FECImportRepository fecImportRepository;
  @Inject private ImportAnalyticInMoveService importAnalyticInMoveService;

  private String lastImportDate;

  public Object importFECMove(Object bean, Map<String, Object> values) throws AxelorException {
    assert bean instanceof MoveLine;
    MoveLine moveLine = (MoveLine) bean;
    FECImport fecImport = null;
    try {
      if (values.get("FECImport") != null) {
        fecImport = fecImportRepository.find(((FECImport) values.get("FECImport")).getId());
      }
      moveLine.setCounter(1);

      if (values.get("EcritureNum") == null) {
        return null;
      }
      Company company = getCompany(values, fecImport);

      String csvReference = values.get("EcritureNum").toString();
      if (lastImportDate == null) {
        lastImportDate =
            appAccountService
                .getTodayDateTime(company)
                .format(DateTimeFormatter.ofPattern("yyyyMMddHH:mm:ss"));
      }
      String importReference = String.format("#%s-%s", csvReference, lastImportDate);

      if (values.get("EcritureDate") != null) {
        moveLine.setDate(parseDate(values.get("EcritureDate").toString()));
      }

      Move move =
          importMoveFecService.createOrGetMove(
              values, company, fecImport, moveLine.getDate(), importReference);
      if (move == null) {
        return moveLine;
      }

      moveLine =
          importMoveFecService.fillMoveLineInformation(
              moveLine, values, move, fecImport, importReference);

      importAnalyticInMoveService.fillAnalyticOnMoveLine(moveLine, move, values, csvReference);

    } catch (AxelorException e) {
      TraceBackService.trace(e);
      throw e;
    } catch (Exception e) {
      TraceBackService.trace(e);
      throw new AxelorException(
          fecImport, TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, e.getMessage());
    }
    return moveLine;
  }

  protected Company getCompany(Map<String, Object> values, FECImport fecImport)
      throws AxelorException {
    Company company = null;
    if (fecImport != null) {
      company = fecImport.getCompany();
    } else {
      final Path path = (Path) values.get("__path__");
      String fileName =
          Optional.ofNullable(path).map(Path::getFileName).map(Path::toString).orElse("");
      String registrationCode = fileName.substring(0, fileName.indexOf('F'));

      company =
          Beans.get(CompanyRepository.class)
              .all()
              .filter("self.partner.registrationCode = ?", registrationCode)
              .fetchOne();
    }

    if (company != null) {
      return company;
    }

    Company activeCompany =
        Optional.ofNullable(AuthUtils.getUser()).map(User::getActiveCompany).orElse(null);
    if (activeCompany != null) {
      company = activeCompany;
    } else {
      company = Beans.get(CompanyRepository.class).all().fetchOne();
    }

    if (company == null) {
      throw new AxelorException(
          fecImport,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.IMPORT_FEC_COMPANY_NOT_FOUND));
    }
    return company;
  }

  @Transactional
  public Object validateMove(Object bean, Map<String, Object> values) {
    assert bean instanceof Move;
    Move move = (Move) bean;
    try {
      if (move.getStatusSelect() == MoveRepository.STATUS_DAYBOOK
          || move.getStatusSelect() == MoveRepository.STATUS_ACCOUNTED) {
        moveValidateService.accounting(move);
      }
    } catch (Exception e) {
      TraceBackService.trace(e);
      move.setStatusSelect(MoveRepository.STATUS_NEW);
    }
    moveRepository.save(move);
    return move;
  }

  protected LocalDate parseDate(String date) throws Exception {
    if (!StringUtils.isEmpty(date)) {
      try {
        return LocalDate.parse(date, DateTimeFormatter.BASIC_ISO_DATE);
      } catch (Exception e) {
        TraceBackService.trace(e);
        throw e;
      }
    }
    return null;
  }
}
