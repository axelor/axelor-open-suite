package com.axelor.apps.account.service.batch;

import com.axelor.apps.account.db.AccountingBatch;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.repo.MoveManagementRepository;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.base.db.Year;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.TraceBack;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class BatchControlMovesConsistency extends BatchStrategy {

  protected MoveManagementRepository moveManagementRepo;
  protected MoveValidateService moveValidateService;
  protected TraceBackRepository tracebackRepository;

  @Inject
  public BatchControlMovesConsistency(
      MoveManagementRepository moveManagementRepo,
      MoveValidateService moveValidateService,
      TraceBackRepository tracebackRepository) {
    this.moveManagementRepo = moveManagementRepo;
    this.moveValidateService = moveValidateService;
    this.tracebackRepository = tracebackRepository;
  }

  protected void process() {
    AccountingBatch accountingBatch = batch.getAccountingBatch();
    List<Year> yearList = accountingBatch.getYearSet();
    if (!CollectionUtils.isEmpty(yearList)) {
      List<Move> moveList = moveManagementRepo.findDaybookByYear(yearList);
      if (!CollectionUtils.isEmpty(moveList)) {
        for (Move move : moveList) {
          try {
            move = moveRepo.find(move.getId());
            moveValidateService.checkPreconditions(move);
          } catch (AxelorException e) {
            TraceBackService.trace(
                new AxelorException(move, e.getCategory(), I18n.get(e.getMessage())),
                null,
                batch.getId());
            incrementAnomaly();
          } finally {
            JPA.clear();
          }
        }
      }
    }
  }

  public List<Long> getAllMovesId(Long batchId) {
    List<Long> idList = new ArrayList<Long>();
    List<TraceBack> traceBackList = tracebackRepository.findByBatchId(batchId).fetch();
    if (!CollectionUtils.isEmpty(traceBackList)) {
      for (TraceBack traceBack : traceBackList) {
        if (Move.class.toString().contains(traceBack.getRef()) && traceBack.getRefId() != null) {
          idList.add(traceBack.getRefId());
        }
      }
    }
    return idList;
  }
}
