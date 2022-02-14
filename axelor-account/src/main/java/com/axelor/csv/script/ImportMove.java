/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.csv.script;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.AccountRepository;
import com.axelor.apps.account.db.repo.JournalRepository;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.db.repo.CurrencyRepository;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.db.repo.YearRepository;
import com.axelor.apps.base.service.PeriodService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

public class ImportMove {

  @Inject private MoveRepository moveRepository;
  @Inject private MoveLineRepository moveLineRepo;
  @Inject private MoveValidateService moveValidateService;

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public Object importFECMove(Object bean, Map<String, Object> values) throws AxelorException {
    assert bean instanceof MoveLine;
    MoveLine moveLine = (MoveLine) bean;
    try {
      moveLine.setCounter(1);

      if (values.get("EcritureNum") == null) {
        return null;
      }
      String moveReference = values.get("EcritureNum").toString();

      MoveLine mvLine =
          moveLineRepo
              .all()
              .filter("self.name LIKE '" + moveReference + "-%'")
              .order("-counter")
              .fetchOne();
      if (mvLine != null) {
        int counter = mvLine.getCounter() + 1;
        moveLine.setCounter(counter);
      }

      if (values.get("EcritureDate") != null) {
        LocalDate moveLineDate =
            LocalDate.parse(
                values.get("EcritureDate").toString(), DateTimeFormatter.BASIC_ISO_DATE);
        moveLine.setDate(moveLineDate);
      }

      Move move = moveRepository.all().filter("self.reference = ?", moveReference).fetchOne();
      if (move == null) {
        move = new Move();
        move.setReference(moveReference);

        if (values.get("ValidDate") != null) {
          move.setStatusSelect(MoveRepository.STATUS_VALIDATED);
          move.setValidationDate(
              LocalDate.parse(
                  values.get("ValidDate").toString(), DateTimeFormatter.BASIC_ISO_DATE));
        } else {
          move.setStatusSelect(MoveRepository.STATUS_ACCOUNTED);
        }

        move.setCompany(getCompany(values));
        move.setCompanyCurrency(move.getCompany().getCurrency());

        move.setDate(
            LocalDate.parse(
                values.get("EcritureDate").toString(), DateTimeFormatter.BASIC_ISO_DATE));

        move.setPeriod(
            Beans.get(PeriodService.class)
                .getPeriod(move.getDate(), move.getCompany(), YearRepository.TYPE_FISCAL));

        if (values.get("Idevise") != null) {
          move.setCurrency(
              Beans.get(CurrencyRepository.class).findByCode(values.get("Idevise").toString()));
          move.setCurrencyCode(values.get("Idevise").toString());
        }

        if (values.get("JournalCode") != null) {
          Journal journal =
              Beans.get(JournalRepository.class)
                  .all()
                  .filter(
                      "self.code = ?1 AND self.company.id = ?2",
                      values.get("JournalCode").toString(),
                      move.getCompany().getId())
                  .fetchOne();
          move.setJournal(journal);
        }

        if (values.get("CompAuxNum") != null) {
          Partner partner =
              Beans.get(PartnerRepository.class)
                  .all()
                  .filter("self.partnerSeq = ?", values.get("CompAuxNum").toString())
                  .fetchOne();
          move.setPartner(partner);
        }
        moveRepository.save(move);
      }
      if (values.get("CompteNum") != null) {
        Account account =
            Beans.get(AccountRepository.class)
                .all()
                .filter(
                    "self.code = ?1 AND self.company.id = ?2",
                    values.get("CompteNum").toString(),
                    move.getCompany().getId())
                .fetchOne();
        moveLine.setAccount(account);
      }
      moveLine.setMove(move);
    } catch (Exception e) {
      TraceBackService.trace(e);
      throw new AxelorException(TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, e.getMessage());
    }
    return moveLine;
  }

  private Company getCompany(Map<String, Object> values) {
    final Path path = (Path) values.get("__path__");
    String fileName = path.getFileName().toString();
    String registrationCode = fileName.substring(0, fileName.indexOf('F'));

    Company company =
        Beans.get(CompanyRepository.class)
            .all()
            .filter("self.partner.registrationCode = ?", registrationCode)
            .fetchOne();

    if (company != null) {
      return company;
    } else if (Optional.ofNullable(AuthUtils.getUser()).map(User::getActiveCompany).orElse(null)
        != null) {
      return Optional.ofNullable(AuthUtils.getUser()).map(User::getActiveCompany).orElse(null);
    } else {
      return Beans.get(CompanyRepository.class).all().fetchOne();
    }
  }

  @Transactional(rollbackOn = Exception.class)
  public Object validateMove(Object bean, Map<String, Object> values) throws AxelorException {
    assert bean instanceof Move;
    Move move = (Move) bean;
    try {
      if (move.getStatusSelect() == MoveRepository.STATUS_ACCOUNTED
          || move.getStatusSelect() == MoveRepository.STATUS_VALIDATED) {
        moveValidateService.validate(move);
      }
    } catch (Exception e) {
      TraceBackService.trace(e);
      move.setStatusSelect(MoveRepository.STATUS_NEW);
    }
    moveRepository.save(move);
    return move;
  }
}
