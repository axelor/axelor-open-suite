package com.axelor.apps.account.service.move.attributes;

import com.axelor.apps.account.db.Move;
import com.axelor.exception.AxelorException;
import java.util.Map;

public interface MoveAttrsService {

  /**
   * This method generates a map of move fields that should be hide or not for the current move
   *
   * <p>The map form is as follow: (fieldName, (attribute("hidden" in our case), attributeValue))
   *
   * @param move
   * @return generated map
   */
  Map<String, Map<String, Object>> getHiddenAttributeValues(Move move);

  /**
   * This method generates a map of move fields for the selection attribute for the current move.
   *
   * <p>The map form is as follow: (fieldName, (attribute("selection-in" in our case),
   * attributeValue))
   *
   * @param move
   * @return generated map
   */
  Map<String, Map<String, Object>> getFunctionalOriginSelectDomain(Move move);

  boolean isHiddenMoveLineListViewer(Move move);

  Map<String, Map<String, Object>> getMoveLineAnalyticAttrs(Move move) throws AxelorException;

  boolean isHiddenDueDate(Move move);
}
