/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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

import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.JournalRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.db.repo.CurrencyRepository;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.db.repo.YearRepository;
import com.axelor.apps.base.service.PeriodService;
import com.axelor.auth.AuthUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import javax.transaction.Transactional;

public class ImportMove {

  @Inject private MoveRepository moveRepository;

  @Transactional
  public Object importFECMove(Object bean, Map<String, Object> values) throws AxelorException {
    assert bean instanceof MoveLine;
    MoveLine moveLine = (MoveLine) bean;
    try {
      String moveLineName = values.get("moveLine_name").toString();
      String moveReference = moveLineName.substring(0, moveLineName.length() - 2);

      Move move = moveRepository.all().filter("self.reference = ?", moveReference).fetchOne();
      if (move == null) {
        move = new Move();
        move.setReference(moveReference);
        move.setStatusSelect(MoveRepository.STATUS_VALIDATED);
        move.setCompany(getCompany(values));
        move.setCompanyCurrency(move.getCompany().getCurrency());

        if (values.get("move_currency") != null) {
          move.setCurrency(
              Beans.get(CurrencyRepository.class)
                  .findByCode(values.get("move_currency").toString()));
          move.setCurrencyCode(values.get("move_currency").toString());
        }

        Journal journal =
            Beans.get(JournalRepository.class)
                .all()
                .filter("self.code = ?", values.get("journalCode").toString())
                .fetchOne();
        move.setJournal(journal);

        move.setDate(
            LocalDate.parse(
                values.get("moveLine_date").toString(), DateTimeFormatter.BASIC_ISO_DATE));

        move.setValidationDate(LocalDate.parse(values.get("validationDate").toString()));

        move.setPeriod(
            Beans.get(PeriodService.class)
                .getPeriod(move.getDate(), move.getCompany(), YearRepository.TYPE_FISCAL));

        if (values.get("partnerSeq") != null) {
          Partner partner =
              Beans.get(PartnerRepository.class)
                  .all()
                  .filter("self.partnerSeq = ?", values.get("partnerSeq").toString())
                  .fetchOne();
          move.setPartner(partner);
        }
        moveRepository.save(move);
      }
      moveLine.setMove(move);

    } catch (Exception e) {
      TraceBackService.trace(e);
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
    } else if (AuthUtils.getUser().getActiveCompany() != null) {
      return AuthUtils.getUser().getActiveCompany();
    } else {
      return Beans.get(CompanyRepository.class).all().fetchOne();
    }
  }
}
