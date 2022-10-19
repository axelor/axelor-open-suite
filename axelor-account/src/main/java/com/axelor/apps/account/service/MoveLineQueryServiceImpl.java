package com.axelor.apps.account.service;

import com.axelor.apps.account.db.MoveLineQuery;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.repo.MoveLineQueryRepository;
import com.axelor.apps.account.db.repo.ReconcileRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.common.ObjectUtils;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;

public class MoveLineQueryServiceImpl implements MoveLineQueryService {

  protected MoveLineQueryRepository moveLineQueryRepository;
  protected AppBaseService appBaseService;
  protected ReconcileRepository reconcileRepository;
  protected ReconcileService reconcileService;

  @Inject
  public MoveLineQueryServiceImpl(
      MoveLineQueryRepository moveLineQueryRepository,
      AppBaseService appBaseService,
      ReconcileRepository reconcileRepository,
      ReconcileService reconcileService) {
    this.moveLineQueryRepository = moveLineQueryRepository;
    this.appBaseService = appBaseService;
    this.reconcileRepository = reconcileRepository;
    this.reconcileService = reconcileService;
  }

  @Override
  public String getMoveLineQuery(MoveLineQuery moveLineQuery) {
    moveLineQuery = moveLineQueryRepository.find(moveLineQuery.getId());

    String query = "";

    query += "self.move.company.id = " + moveLineQuery.getCompany().getId();

    if (appBaseService.getAppBase().getEnableTradingNamesManagement()
        && ObjectUtils.notEmpty(moveLineQuery.getTradingName())) {
      query += " AND self.move.tradingName.id = " + moveLineQuery.getTradingName().getId();
    }

    query += String.format(" AND self.date >= '%s'", moveLineQuery.getFromDate().toString());
    query += String.format(" AND self.date <= '%s'", moveLineQuery.getToDate().toString());

    query += " AND self.account.id = " + moveLineQuery.getAccount().getId();

    query += " AND self.partner.id = " + moveLineQuery.getPartner().getId();

    if (moveLineQuery.getProcessSelect() == MoveLineQueryRepository.PROCESS_RECONCILE) {
      query += "AND self.amountRemaining != 0 ";
    } else if (moveLineQuery.getProcessSelect() == MoveLineQueryRepository.PROCESS_UNRECONCILE) {
      query += "AND self.amountRemaining != debit + credit ";
    }

    return query;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void ureconcileMoveLinesWithCacheManagement(List<Reconcile> reconcileList)
      throws AxelorException {
    for (Reconcile reconcile : reconcileList) {
      reconcile = reconcileRepository.find(reconcile.getId());
      reconcileService.unreconcile(reconcile);
      JPA.clear();
    }
  }
}
