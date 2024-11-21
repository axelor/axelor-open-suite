/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.AccountingBatch;
import com.axelor.apps.account.db.AccountingReport;
import com.axelor.apps.account.db.ClosureAssistant;
import com.axelor.apps.account.db.ClosureAssistantLine;
import com.axelor.apps.account.db.repo.AccountingBatchRepository;
import com.axelor.apps.account.db.repo.ClosureAssistantLineRepository;
import com.axelor.apps.account.service.batch.AccountingBatchService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Year;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.utils.db.Wizard;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.persistence.TypedQuery;

public class ClosureAssistantLineServiceImpl implements ClosureAssistantLineService {

  protected ClosureAssistantService closureAssistantService;
  protected ClosureAssistantLineRepository closureAssistantLineRepository;
  protected AppBaseService appBaseService;
  protected AccountingBatchService accountingBatchService;

  @Inject
  public ClosureAssistantLineServiceImpl(
      ClosureAssistantService closureAssistantService,
      ClosureAssistantLineRepository closureAssistantLineRepository,
      AppBaseService appBaseService,
      AccountingBatchService accountingBatchService) {
    this.closureAssistantLineRepository = closureAssistantLineRepository;
    this.closureAssistantService = closureAssistantService;
    this.appBaseService = appBaseService;
    this.accountingBatchService = accountingBatchService;
  }

  @Override
  public List<ClosureAssistantLine> initClosureAssistantLines(ClosureAssistant closureAssistant)
      throws AxelorException {
    List<ClosureAssistantLine> closureAssistantLineList = new ArrayList<>();
    for (int i = 1; i < 8; i++) {
      ClosureAssistantLine closureAssistantLine = new ClosureAssistantLine(i, null, i, false);

      if (i != 1) {
        closureAssistantLine.setIsPreviousLineValidated(false);
      } else {
        closureAssistantLine.setIsPreviousLineValidated(true);
      }

      closureAssistantLineList.add(closureAssistantLine);
    }

    return closureAssistantLineList;
  }

  @Override
  public void cancelClosureAssistantLine(ClosureAssistantLine closureAssistantLine)
      throws AxelorException {
    this.validateOrCancelClosureAssistantLine(closureAssistantLine, false);
  }

  @Override
  public void validateClosureAssistantLine(ClosureAssistantLine closureAssistantLine)
      throws AxelorException {
    this.validateOrCancelClosureAssistantLine(closureAssistantLine, true);
  }

  @Override
  public Map<String, Object> getViewToOpen(ClosureAssistantLine closureAssistantLine)
      throws AxelorException {
    if (Optional.ofNullable(AuthUtils.getUser()).map(User::getActiveCompany).orElse(null) == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(BaseExceptionMessage.PRODUCT_NO_ACTIVE_COMPANY));
    }
    AccountingBatch accountingBatch = new AccountingBatch();
    switch (closureAssistantLine.getActionSelect()) {
      case ClosureAssistantLineRepository.ACTION_CUT_OF_GENERATION:
        accountingBatch =
            accountingBatchService.createNewAccountingBatch(
                AccountingBatchRepository.ACTION_ACCOUNTING_CUT_OFF,
                AuthUtils.getUser().getActiveCompany());
        if (accountingBatch != null && accountingBatch.getId() != null) {
          return this.getAccountingBatchView(accountingBatch.getId());
        }
        break;
      case ClosureAssistantLineRepository.ACTION_FIXED_ASSET_REALIZATION:
        accountingBatch =
            accountingBatchService.createNewAccountingBatch(
                AccountingBatchRepository.ACTION_REALIZE_FIXED_ASSET_LINES,
                AuthUtils.getUser().getActiveCompany());
        if (accountingBatch != null && accountingBatch.getId() != null) {
          return this.getAccountingBatchView(accountingBatch.getId());
        }
        break;
      case ClosureAssistantLineRepository.ACTION_MOVE_CONSISTENCY_CHECK:
        accountingBatch =
            accountingBatchService.createNewAccountingBatch(
                AccountingBatchRepository.ACTION_MOVES_CONSISTENCY_CONTROL,
                AuthUtils.getUser().getActiveCompany());
        if (accountingBatch != null && accountingBatch.getId() != null) {
          return this.getAccountingBatchView(accountingBatch.getId());
        }
        break;
      case ClosureAssistantLineRepository.ACTION_ACCOUNTING_REPORTS:
        return ActionView.define(I18n.get("Accounting report"))
            .model(AccountingReport.class.getName())
            .add("form", "accounting-report-form")
            .map();
      case ClosureAssistantLineRepository.ACTION_CALCULATE_THE_OUTRUN_OF_THE_YEAR:
        return ActionView.define(I18n.get("Outrun result"))
            .model(Wizard.class.getName())
            .add("form", "closure-assistant-line-outrun-wizard")
            .context("_year", closureAssistantLine.getClosureAssistant().getFiscalYear().getId())
            .map();
      case ClosureAssistantLineRepository.ACTION_CLOSURE_AND_OPENING_OF_FISCAL_YEAR_BATCH:
        accountingBatch =
            accountingBatchService.createNewAccountingBatch(
                AccountingBatchRepository.ACTION_CLOSE_OR_OPEN_THE_ANNUAL_ACCOUNTS,
                AuthUtils.getUser().getActiveCompany());
        if (accountingBatch != null && accountingBatch.getId() != null) {
          return this.getAccountingBatchView(accountingBatch.getId());
        }
        break;
      case ClosureAssistantLineRepository.ACTION_FISCAL_YEAR_CLOSURE:
        return ActionView.define(I18n.get("Fiscal year"))
            .model(Year.class.getName())
            .add("form", "year-account-form")
            .context(
                "_showRecord", closureAssistantLine.getClosureAssistant().getFiscalYear().getId())
            .map();
      default:
        return null;
    }
    return null;
  }

  protected Map<String, Object> getAccountingBatchView(long id) {
    return ActionView.define(I18n.get("Accounting Batch"))
        .model(AccountingBatch.class.getName())
        .add("form", "accounting-batch-form")
        .context("_showRecord", id)
        .map();
  }

  @Transactional
  protected ClosureAssistantLine validateOrCancelClosureAssistantLine(
      ClosureAssistantLine closureAssistantLine, boolean isValidated) {
    closureAssistantLine.setIsValidated(isValidated);
    if (isValidated) {
      closureAssistantLine.setValidatedByUser(AuthUtils.getUser());
      closureAssistantLine.setValidatedOnDateTime(
          appBaseService
              .getTodayDateTime(closureAssistantLine.getClosureAssistant().getCompany())
              .toLocalDateTime());
    } else {
      closureAssistantLine.setValidatedByUser(null);
      closureAssistantLine.setValidatedOnDateTime(null);
    }
    setIsPreviousLineValidatedForPreviousAndNextLine(closureAssistantLine, isValidated);
    closureAssistantLineRepository.save(closureAssistantLine);
    closureAssistantService.updateClosureAssistantProgress(
        closureAssistantLine.getClosureAssistant());
    return closureAssistantLine;
  }

  @Transactional
  protected void setIsPreviousLineValidatedForPreviousAndNextLine(
      ClosureAssistantLine closureAssistantLine, boolean isValidated) {
    ClosureAssistantLine previousClosureAssistantLine = null;
    ClosureAssistantLine nextClosureAssistantLine = null;

    TypedQuery<ClosureAssistantLine> closureAssistantLineQuery =
        JPA.em()
            .createQuery(
                "SELECT self FROM ClosureAssistantLine self  "
                    + "WHERE self.closureAssistant = :closureAssistant AND self.sequence = :sequence ",
                ClosureAssistantLine.class);

    closureAssistantLineQuery.setParameter(
        "closureAssistant", closureAssistantLine.getClosureAssistant());
    closureAssistantLineQuery.setParameter("sequence", closureAssistantLine.getSequence() - 1);

    List<ClosureAssistantLine> previousClosureAssistantLineList =
        closureAssistantLineQuery.getResultList();

    if (!ObjectUtils.isEmpty(previousClosureAssistantLineList)) {
      previousClosureAssistantLine = previousClosureAssistantLineList.get(0);
    }

    closureAssistantLineQuery.setParameter("sequence", closureAssistantLine.getSequence() + 1);

    List<ClosureAssistantLine> nextClosureAssistantLineList =
        closureAssistantLineQuery.getResultList();
    if (!ObjectUtils.isEmpty(nextClosureAssistantLineList)) {
      nextClosureAssistantLine = nextClosureAssistantLineList.get(0);
    }

    if (previousClosureAssistantLine != null) {
      previousClosureAssistantLine.setIsPreviousLineValidated(!isValidated);
      previousClosureAssistantLine.setIsNextLineValidated(isValidated);
      closureAssistantLineRepository.save(previousClosureAssistantLine);
    }
    if (nextClosureAssistantLine != null) {
      nextClosureAssistantLine.setIsPreviousLineValidated(isValidated);
      closureAssistantLineRepository.save(nextClosureAssistantLine);
    }
  }
}
