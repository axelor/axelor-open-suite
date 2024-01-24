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
package com.axelor.apps.account.service.batch;

import com.axelor.apps.account.db.AccountingBatch;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.service.move.MoveToolService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.TraceBack;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class BatchControlMovesConsistency extends BatchStrategy {

  protected MoveToolService moveToolService;
  protected MoveValidateService moveValidateService;
  protected TraceBackRepository tracebackRepository;

  @Inject
  public BatchControlMovesConsistency(
      MoveToolService moveToolService,
      MoveValidateService moveValidateService,
      TraceBackRepository tracebackRepository) {
    this.moveToolService = moveToolService;
    this.moveValidateService = moveValidateService;
    this.tracebackRepository = tracebackRepository;
  }

  protected void process() {
    AccountingBatch accountingBatch = batch.getAccountingBatch();
    if (!CollectionUtils.isEmpty(accountingBatch.getYearSet())) {
      List<Move> moveList =
          moveToolService.findDaybookAndAccountingByYear(accountingBatch.getYearSet());
      if (!CollectionUtils.isEmpty(moveList)) {
        for (Move move : moveList) {
          try {
            move = moveRepo.find(move.getId());
            moveValidateService.checkConsistencyPreconditions(move);
            incrementDone();
          } catch (AxelorException e) {
            TraceBackService.trace(
                new AxelorException(move, e.getCategory(), I18n.get(e.getMessage())),
                null,
                batch.getId());
            incrementAnomaly();
          } catch (Exception e) {
            TraceBackService.trace(
                new AxelorException(e, move, TraceBackRepository.CATEGORY_INCONSISTENCY),
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
    List<Long> idList = new ArrayList<>();
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
