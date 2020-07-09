package com.axelor.apps.account.listeners;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.move.MoveService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import javax.persistence.PostPersist;
import javax.persistence.PostUpdate;

public class MoveListener {

  @PostUpdate
  @PostPersist
  private void updateInDayBookMode(Move move) {
    move = Beans.get(MoveRepository.class).find(move.getId());
    try {
      if (move.getStatusSelect() == MoveRepository.STATUS_DAYBOOK) {
        Beans.get(MoveService.class).getMoveValidateService().updateInDayBookMode(move);
      }
    } catch (Exception e) {
      TraceBackService.trace(e);
    }
  }
}
