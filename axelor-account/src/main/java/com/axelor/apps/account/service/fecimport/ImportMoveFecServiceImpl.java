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
package com.axelor.apps.account.service.fecimport;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.FECImport;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.ReconcileGroup;
import com.axelor.apps.account.db.repo.AccountRepository;
import com.axelor.apps.account.db.repo.JournalRepository;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.moveline.MoveLineToolService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Period;
import com.axelor.apps.base.db.repo.CurrencyRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.db.repo.YearRepository;
import com.axelor.apps.base.service.PeriodService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.common.StringUtils;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class ImportMoveFecServiceImpl implements ImportMoveFecService {

  protected PeriodService periodService;
  protected MoveLineToolService moveLineToolService;
  protected MoveRepository moveRepository;
  protected MoveLineRepository moveLineRepository;
  protected CurrencyRepository currencyRepository;
  protected JournalRepository journalRepository;
  protected AccountRepository accountRepository;

  @Inject
  public ImportMoveFecServiceImpl(
      PeriodService periodService,
      MoveLineToolService moveLineToolService,
      MoveRepository moveRepository,
      MoveLineRepository moveLineRepository,
      CurrencyRepository currencyRepository,
      JournalRepository journalRepository,
      AccountRepository accountRepository) {
    this.periodService = periodService;
    this.moveLineToolService = moveLineToolService;
    this.moveRepository = moveRepository;
    this.moveLineRepository = moveLineRepository;
    this.currencyRepository = currencyRepository;
    this.journalRepository = journalRepository;
    this.accountRepository = accountRepository;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Move createOrGetMove(
      Map<String, Object> values,
      Company company,
      FECImport fecImport,
      LocalDate moveLineDate,
      String importReference)
      throws Exception {
    if (company == null) {
      return null;
    }

    Move move = moveRepository.all().filter("self.reference = ?", importReference).fetchOne();
    if (move == null) {
      move = new Move();
      move.setFecImport(fecImport);
      move.setReference(importReference);
      if (values.get("PieceRef") != null) {
        move.setOrigin(values.get("PieceRef").toString());
      }

      if (values.get("ValidDate") != null) {
        move.setAccountingDate(parseDate(values.get("ValidDate").toString()));
      }
      move.setStatusSelect(MoveRepository.STATUS_NEW);
      move.setCompany(company);

      move.setCompanyCurrency(company.getCurrency());

      if (values.get("EcritureDate") != null) {
        move.setDate(parseDate(values.get("EcritureDate").toString()));
      }

      Period period = periodService.getPeriod(moveLineDate, company, YearRepository.TYPE_FISCAL);

      if (period == null) {
        throw new AxelorException(
            fecImport,
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(AccountExceptionMessage.IMPORT_FEC_PERIOD_NOT_FOUND),
            moveLineDate,
            company);
      }
      move.setPeriod(period);

      if (values.get("Idevise") != null) {
        move.setCurrency(currencyRepository.findByCode(values.get("Idevise").toString()));
        move.setCurrencyCode(values.get("Idevise").toString());
      }

      Journal journal = null;
      if (values.get("JournalCode") != null) {
        journal =
            journalRepository
                .all()
                .filter(
                    "self.code = ?1 AND self.company.id = ?2",
                    values.get("JournalCode").toString(),
                    company.getId())
                .fetchOne();
        if (journal == null) {
          throw new AxelorException(
              fecImport,
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(AccountExceptionMessage.IMPORT_FEC_JOURNAL_NOT_FOUND),
              values.get("JournalCode"));
        }
        move.setJournal(journal);
      }

      if (values.get("PieceDate") != null) {
        move.setOriginDate(parseDate(values.get("PieceDate").toString()));
      }
      move.setTechnicalOriginSelect(MoveRepository.TECHNICAL_ORIGIN_IMPORT);

      fillFunctionalOriginSelect(fecImport, move, journal);

      moveRepository.save(move);
    }

    return move;
  }

  @Override
  public MoveLine fillMoveLineInformation(
      MoveLine moveLine,
      Map<String, Object> values,
      Move move,
      FECImport fecImport,
      String importReference)
      throws AxelorException {
    if (move == null || move.getCompany() == null || moveLine == null) {
      return moveLine;
    }

    MoveLine mvLine =
        moveLineRepository
            .all()
            .filter("self.name LIKE '" + importReference + "@%'")
            .order("-counter")
            .fetchOne();

    if (mvLine != null) {
      int counter = mvLine.getCounter() + 1;
      moveLine.setCounter(counter);
    }

    if (values.get("CompteNum") != null) {
      Account account =
          accountRepository
              .all()
              .filter(
                  "self.code = ?1 AND self.company.id = ?2",
                  values.get("CompteNum").toString(),
                  move.getCompany().getId())
              .fetchOne();
      if (account == null) {
        throw new AxelorException(
            fecImport,
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(AccountExceptionMessage.IMPORT_FEC_ACCOUNT_NOT_FOUND),
            values.get("CompteNum"));
      }
      moveLine.setAccount(account);
    }

    ReconcileGroup reconcileGroup = moveLine.getReconcileGroup();
    if (reconcileGroup != null) {
      reconcileGroup.setCompany(move.getCompany());
    }

    move.addMoveLineListItem(moveLine);

    setMovePartner(move, moveLine);

    if (values.get("Montantdevise") == null || values.get("Montantdevise").equals("")) {
      moveLine.setMove(move);
      moveLineToolService.setCurrencyAmount(moveLine);
    } else {
      String currencyAmountStr = values.get("Montantdevise").toString().replace(',', '.');
      BigDecimal currencyAmount = (new BigDecimal(currencyAmountStr)).abs();

      if (moveLine.getDebit().signum() > 0) {
        moveLine.setCurrencyAmount(currencyAmount);
      } else {
        moveLine.setCurrencyAmount(currencyAmount.negate());
      }
    }

    return moveLine;
  }

  protected void fillFunctionalOriginSelect(FECImport fecImport, Move move, Journal journal) {
    if (fecImport != null
        && fecImport.getImportFECType() != null
        && fecImport.getImportFECType().getFunctionalOriginSelect()
            >= MoveRepository.FUNCTIONAL_ORIGIN_OPENING) {
      move.setFunctionalOriginSelect(fecImport.getImportFECType().getFunctionalOriginSelect());
    } else if (journal != null) {
      String authorizedFunctionalOriginSelect = journal.getAuthorizedFunctionalOriginSelect();

      if (StringUtils.notEmpty(authorizedFunctionalOriginSelect)
          && authorizedFunctionalOriginSelect.split(",").length == 1) {
        move.setFunctionalOriginSelect(Integer.parseInt(authorizedFunctionalOriginSelect));
      }
    }
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

  protected void setMovePartner(Move move, MoveLine moveLine) {
    List<Partner> partnerList =
        move.getMoveLineList().stream()
            .map(MoveLine::getPartner)
            .filter(Objects::nonNull)
            .distinct()
            .collect(Collectors.toList());
    if (CollectionUtils.isNotEmpty(partnerList)) {
      if (partnerList.size() == 1) {
        move.setPartner(partnerList.stream().findFirst().orElse(null));
      }

      if (partnerList.size() > 1) {
        move.setPartner(null);
      }
    }
  }
}
