/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.FECImport;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.AccountRepository;
import com.axelor.apps.account.db.repo.FECImportRepository;
import com.axelor.apps.account.db.repo.JournalRepository;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Period;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.db.repo.CurrencyRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.db.repo.YearRepository;
import com.axelor.apps.base.service.PeriodService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.common.StringUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class ImportMove {

  @Inject private MoveRepository moveRepository;
  @Inject private MoveLineRepository moveLineRepo;
  @Inject private MoveValidateService moveValidateService;
  @Inject private AppAccountService appAccountService;
  @Inject private PeriodService periodService;
  @Inject private FECImportRepository fecImportRepository;

  private String lastImportDate;

  @Transactional(rollbackOn = {Exception.class})
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
      Company company = null;
      if (fecImport != null) {
        company = fecImport.getCompany();
      } else {
        company = getCompany(values);
      }

      String csvReference = values.get("EcritureNum").toString();
      if (lastImportDate == null) {
        lastImportDate =
            appAccountService
                .getTodayDateTime(company)
                .format(DateTimeFormatter.ofPattern("yyyyMMddHH:mm:ss"));
      }
      String importReference = String.format("#%s@%s", csvReference, lastImportDate);

      MoveLine mvLine =
          moveLineRepo
              .all()
              .filter("self.name LIKE '" + importReference + "@%'")
              .order("-counter")
              .fetchOne();
      if (mvLine != null) {
        int counter = mvLine.getCounter() + 1;
        moveLine.setCounter(counter);
      }

      if (values.get("EcritureDate") != null) {
        moveLine.setDate(parseDate(values.get("EcritureDate").toString()));
      }

      Period period =
          periodService.getPeriod(moveLine.getDate(), company, YearRepository.TYPE_FISCAL);

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
        move.setCompanyCurrency(move.getCompany().getCurrency());

        if (values.get("EcritureDate") != null) {
          move.setDate(parseDate(values.get("EcritureDate").toString()));
        }
        if (period == null) {
          throw new AxelorException(
              fecImport,
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(AccountExceptionMessage.IMPORT_FEC_PERIOD_NOT_FOUND),
              moveLine.getDate(),
              company);
        }
        move.setPeriod(period);

        setMoveCurrency(move, values.get("Idevise"));

        Journal journal = null;
        if (values.get("JournalCode") != null) {
          journal =
              Beans.get(JournalRepository.class)
                  .all()
                  .filter(
                      "self.code = ?1 AND self.company.id = ?2",
                      values.get("JournalCode").toString(),
                      move.getCompany().getId())
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

        if (fecImport != null && fecImport.getImportFECType().getFunctionalOriginSelect() > 0) {
          move.setFunctionalOriginSelect(fecImport.getImportFECType().getFunctionalOriginSelect());
        } else if (journal != null) {
          String authorizedFunctionalOriginSelect = journal.getAuthorizedFunctionalOriginSelect();

          if (StringUtils.notEmpty(authorizedFunctionalOriginSelect)
              && authorizedFunctionalOriginSelect.split(",").length == 1) {
            move.setFunctionalOriginSelect(Integer.parseInt(authorizedFunctionalOriginSelect));
          }
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
        if (account == null) {
          throw new AxelorException(
              fecImport,
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(AccountExceptionMessage.IMPORT_FEC_ACCOUNT_NOT_FOUND),
              values.get("CompteNum"));
        }
        moveLine.setAccount(account);
      }

      if (moveLine.getReconcileGroup() != null) {
        moveLine.getReconcileGroup().setCompany(company);
      }

      move.addMoveLineListItem(moveLine);

      setMovePartner(move, moveLine);
      moveLine.setMove(move);
      computeImportedCurrencyAmount(
          moveLine,
          values.get("Idevise"),
          values.get("Montantdevise"),
          values.get("EcritureNum") != null ? values.get("EcritureNum").toString() : null,
          fecImport);
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

  protected void setMoveCurrency(Move move, Object importedCurrency) {
    Currency companyCurrency = move.getCompany().getCurrency();

    if (isCompanyCurrency(importedCurrency, companyCurrency)) {
      move.setCurrency(companyCurrency);
      if (companyCurrency != null) {
        move.setCurrencyCode(companyCurrency.getCodeISO());
      }
    } else if (importedCurrency != null) {
      String importedCurrencyValue = importedCurrency.toString().trim();
      move.setCurrency(Beans.get(CurrencyRepository.class).findByCode(importedCurrencyValue));
      move.setCurrencyCode(importedCurrencyValue);
    }
  }

  protected void computeImportedCurrencyAmount(
      MoveLine moveLine,
      Object importedCurrency,
      Object importedCurrencyAmount,
      String entryNumber,
      FECImport fecImport)
      throws AxelorException {
    if (moveLine == null) {
      return;
    }

    if (isZero(moveLine.getDebit()) && isZero(moveLine.getCredit())) {
      moveLine.setCurrencyAmount(BigDecimal.ZERO);
      return;
    }

    BigDecimal currencyAmount =
        getCurrencyAmount(
            moveLine, importedCurrency, importedCurrencyAmount, entryNumber, fecImport);
    moveLine.setCurrencyAmount(applyCurrencyAmountSign(moveLine, currencyAmount));
  }

  protected BigDecimal getCurrencyAmount(
      MoveLine moveLine,
      Object importedCurrency,
      Object importedCurrencyAmount,
      String entryNumber,
      FECImport fecImport)
      throws AxelorException {
    BigDecimal currencyAmount = parseCurrencyAmount(importedCurrencyAmount);
    if (!isZero(currencyAmount)) {
      return currencyAmount.abs();
    }

    if (isCompanyCurrency(importedCurrency, getCompanyCurrency(moveLine))) {
      return getSourceAmount(moveLine).abs();
    }

    throw new AxelorException(
        fecImport,
        TraceBackRepository.CATEGORY_MISSING_FIELD,
        I18n.get(AccountExceptionMessage.IMPORT_FEC_FOREIGN_CURRENCY_AMOUNT_REQUIRED),
        entryNumber);
  }

  protected BigDecimal parseCurrencyAmount(Object importedCurrencyAmount) {
    if (importedCurrencyAmount == null || StringUtils.isBlank(importedCurrencyAmount.toString())) {
      return null;
    }

    String currencyAmount = importedCurrencyAmount.toString().trim();
    return new BigDecimal(currencyAmount.replace(',', '.'));
  }

  protected Currency getCompanyCurrency(MoveLine moveLine) {
    return Optional.ofNullable(moveLine)
        .map(MoveLine::getMove)
        .map(Move::getCompany)
        .map(Company::getCurrency)
        .orElse(null);
  }

  protected boolean isCompanyCurrency(Object importedCurrency, Currency companyCurrency) {
    if (companyCurrency == null) {
      return false;
    }

    if (importedCurrency == null) {
      return true;
    }

    String currencyCode = importedCurrency.toString().trim();
    if (StringUtils.isEmpty(currencyCode) || "0".equals(currencyCode)) {
      return true;
    }

    return currencyCode.equalsIgnoreCase(companyCurrency.getCode())
        || currencyCode.equalsIgnoreCase(companyCurrency.getCodeISO())
        || currencyCode.equalsIgnoreCase(companyCurrency.getName());
  }

  protected BigDecimal applyCurrencyAmountSign(MoveLine moveLine, BigDecimal absoluteAmount) {
    BigDecimal amount = absoluteAmount.abs();

    if (!isZero(moveLine.getDebit())) {
      return moveLine.getDebit().signum() > 0 ? amount : amount.negate();
    }

    return moveLine.getCredit().signum() < 0 ? amount : amount.negate();
  }

  protected BigDecimal getSourceAmount(MoveLine moveLine) {
    return !isZero(moveLine.getDebit()) ? moveLine.getDebit() : moveLine.getCredit();
  }

  protected boolean isZero(BigDecimal amount) {
    return amount == null || amount.signum() == 0;
  }

  protected Company getCompany(Map<String, Object> values) {
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
