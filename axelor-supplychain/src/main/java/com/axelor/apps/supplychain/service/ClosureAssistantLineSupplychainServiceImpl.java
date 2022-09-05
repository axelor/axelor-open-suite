package com.axelor.apps.supplychain.service;

import com.axelor.apps.account.db.ClosureAssistant;
import com.axelor.apps.account.db.ClosureAssistantLine;
import com.axelor.apps.account.db.repo.AccountingBatchRepository;
import com.axelor.apps.account.db.repo.ClosureAssistantLineRepository;
import com.axelor.apps.account.service.ClosureAssistantLineServiceImpl;
import com.axelor.apps.account.service.ClosureAssistantService;
import com.axelor.apps.account.service.batch.AccountingBatchService;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.supplychain.db.SupplychainBatch;
import com.axelor.apps.supplychain.service.batch.SupplychainBatchService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.meta.schema.actions.ActionView;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ClosureAssistantLineSupplychainServiceImpl extends ClosureAssistantLineServiceImpl {

  protected SupplychainBatchService supplychainBatchService;

  @Inject
  public ClosureAssistantLineSupplychainServiceImpl(
      ClosureAssistantService closureAssistantService,
      ClosureAssistantLineRepository closureAssistantLineRepository,
      AppBaseService appBaseService,
      AccountingBatchService accountingBatchService,
      SupplychainBatchService supplychainBatchService) {
    super(
        closureAssistantService,
        closureAssistantLineRepository,
        appBaseService,
        accountingBatchService);
    this.supplychainBatchService = supplychainBatchService;
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
  public Map<String, Object> getViewToOpen(ClosureAssistantLine closureAssistantLine)
      throws AxelorException {
    if (closureAssistantLine.getActionSelect()
        == ClosureAssistantLineRepository.ACTION_CUT_OF_GENERATION) {
      if (Optional.ofNullable(AuthUtils.getUser()).map(User::getActiveCompany).orElse(null)
          == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_MISSING_FIELD,
            I18n.get(BaseExceptionMessage.PRODUCT_NO_ACTIVE_COMPANY));
      }
      SupplychainBatch supplychainBatch =
          supplychainBatchService.createNewSupplychainBatch(
              AccountingBatchRepository.ACTION_ACCOUNTING_CUT_OFF,
              AuthUtils.getUser().getActiveCompany());
      if (supplychainBatch != null && supplychainBatch.getId() != null) {
        return ActionView.define(I18n.get("Supplychain Batch"))
            .model(SupplychainBatch.class.getName())
            .add("form", "supplychain-batch-form")
            .context("_showRecord", supplychainBatch.getId())
            .map();
      }
      return null;
    } else {
      return super.getViewToOpen(closureAssistantLine);
    }
  }
}
