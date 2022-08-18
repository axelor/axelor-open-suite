package com.axelor.apps.account.service.move;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.service.moveline.MoveLineCompletionService;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MoveCompletionServiceImpl implements MoveCompletionService {
  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  protected MoveLineCompletionService moveLineCompletionService;
  protected MoveSequenceService moveSequenceService;

  @Inject
  public MoveCompletionServiceImpl(
      MoveLineCompletionService moveLineCompletionService,
      MoveSequenceService moveSequenceService) {
    this.moveLineCompletionService = moveLineCompletionService;
    this.moveSequenceService = moveSequenceService;
  }

  @Override
  public void completeMove(Move move) throws AxelorException {
    log.debug("Completing move {}", move);
    Objects.requireNonNull(move);

    if (move.getCurrency() != null) {
      move.setCurrencyCode(move.getCurrency().getCodeISO());
    }

    moveSequenceService.setDraftSequence(move);
  }

  @Override
  public void freezeAccountAndPartnerFieldsOnMoveLines(Move move) {
    for (MoveLine moveLine : move.getMoveLineList()) {
      moveLineCompletionService.freezeAccountAndPartnerFields(moveLine);
    }
  }
}
