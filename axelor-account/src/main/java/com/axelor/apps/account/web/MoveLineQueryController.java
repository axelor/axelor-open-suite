package com.axelor.apps.account.web;

import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.MoveLineQuery;
import com.axelor.apps.account.db.MoveLineQueryLine;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.repo.MoveLineQueryRepository;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.db.repo.ReconcileRepository;
import com.axelor.apps.account.service.MoveLineQueryService;
import com.axelor.apps.account.service.move.MoveLineControlService;
import com.axelor.apps.account.service.moveline.MoveLineService;
import com.axelor.common.ObjectUtils;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MoveLineQueryController {

  public void filterMoveLines(ActionRequest request, ActionResponse response) {
    try {
      MoveLineQuery moveLineQuery = request.getContext().asType(MoveLineQuery.class);
      String query = Beans.get(MoveLineQueryService.class).getMoveLineQuery(moveLineQuery);
      List<MoveLine> moveLineList = Beans.get(MoveLineRepository.class).all().filter(query).fetch();
      if (!ObjectUtils.isEmpty(moveLineList)) {
        List<MoveLineQueryLine> moveLineQueryLineList = new ArrayList<MoveLineQueryLine>();
        for (MoveLine moveLine : moveLineList) {
          moveLineQueryLineList.add(new MoveLineQueryLine(moveLine, moveLineQuery));
        }
        response.setValue("moveLineQueryLineList", moveLineQueryLineList);
      } else {
        response.setValue("moveLineQueryLineList", null);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void computeSelectedTotal(ActionRequest request, ActionResponse response) {
    try {
      MoveLineQuery moveLineQuery = request.getContext().asType(MoveLineQuery.class);
      BigDecimal selectedCreditTotal = BigDecimal.ZERO;
      BigDecimal selectedDebitTotal = BigDecimal.ZERO;
      BigDecimal selectedBalanceTotal = BigDecimal.ZERO;

      for (MoveLineQueryLine moveLineQueryLine : moveLineQuery.getMoveLineQueryLineList()) {
        if (moveLineQueryLine.getIsSelected()) {
          MoveLine moveLine = moveLineQueryLine.getMoveLine();
          selectedCreditTotal = selectedCreditTotal.add(moveLine.getCredit());
          selectedDebitTotal = selectedDebitTotal.add(moveLine.getDebit());
        }
      }
      selectedBalanceTotal = selectedDebitTotal.subtract(selectedCreditTotal);

      response.setValue("$selectedDebit", selectedDebitTotal);
      response.setValue("$selectedCredit", selectedCreditTotal);
      response.setValue("$selectedBalance", selectedBalanceTotal);

      String balanceTitle;
      if (selectedBalanceTotal.signum() > 0) {
        balanceTitle = I18n.get("Debit balance");
      } else if (selectedBalanceTotal.signum() < 0) {
        balanceTitle = I18n.get("Credit balance");
      } else {
        balanceTitle = I18n.get("Balance");
      }
      response.setAttr("$selectedBalance", "title", balanceTitle);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void massReconcile(ActionRequest request, ActionResponse response) {
    try {
      MoveLineQuery moveLineQuery = request.getContext().asType(MoveLineQuery.class);
      moveLineQuery = Beans.get(MoveLineQueryRepository.class).find(moveLineQuery.getId());
      List<MoveLine> moveLineList = new ArrayList<>();
      if (!ObjectUtils.isEmpty(moveLineQuery.getMoveLineQueryLineList())) {
        List<MoveLine> moveLineSelectedList =
            moveLineQuery.getMoveLineQueryLineList().stream()
                .filter(l -> l.getIsSelected())
                .map(l -> l.getMoveLine())
                .collect(Collectors.toList());
        for (MoveLine moveLine : moveLineSelectedList) {
          if (Beans.get(MoveLineControlService.class).canReconcile(moveLine)) {
            moveLineList.add(moveLine);
          }
        }
      }
      if (!moveLineList.isEmpty()) {
        Beans.get(MoveLineService.class).reconcileMoveLinesWithCacheManagement(moveLineList);
        response.setReload(true);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void massUnreconcile(ActionRequest request, ActionResponse response) {
    try {
      MoveLineQuery moveLineQuery = request.getContext().asType(MoveLineQuery.class);
      moveLineQuery = Beans.get(MoveLineQueryRepository.class).find(moveLineQuery.getId());
      List<Reconcile> reconcileList = new ArrayList<>();
      if (!ObjectUtils.isEmpty(moveLineQuery.getMoveLineQueryLineList())) {
        List<MoveLine> moveLineSelectedList =
            moveLineQuery.getMoveLineQueryLineList().stream()
                .filter(l -> l.getIsSelected())
                .map(l -> l.getMoveLine())
                .collect(Collectors.toList());
        for (MoveLine moveLine : moveLineSelectedList) {
          for (Reconcile reconcile : moveLine.getDebitReconcileList()) {
            if (reconcile.getStatusSelect().equals(ReconcileRepository.STATUS_CONFIRMED)
                && !reconcileList.contains(reconcile)) {
              reconcileList.add(reconcile);
            }
          }

          for (Reconcile reconcile : moveLine.getCreditReconcileList()) {
            if (reconcile.getStatusSelect().equals(ReconcileRepository.STATUS_CONFIRMED)
                && !reconcileList.contains(reconcile)) {
              reconcileList.add(reconcile);
            }
          }
        }
      }
      if (!reconcileList.isEmpty()) {
        Beans.get(MoveLineQueryService.class).ureconcileMoveLinesWithCacheManagement(reconcileList);
        response.setReload(true);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
