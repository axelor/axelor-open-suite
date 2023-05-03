package com.axelor.apps.account.service.moveline;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.exception.AxelorException;
import java.util.Map;

public interface MoveLineAttrsService {
  void addAnalyticAxisAttrs(Move move, Map<String, Map<String, Object>> attrsMap)
      throws AxelorException;

  void addDescriptionRequired(Move move, Map<String, Map<String, Object>> attrsMap)
      throws AxelorException;

  void addAnalyticAccountRequired(
      MoveLine moveLine, Move move, Map<String, Map<String, Object>> attrsMap)
      throws AxelorException;

  void addAnalyticDistributionTypeSelect(Move move, Map<String, Map<String, Object>> attrsMap)
      throws AxelorException;

  void addInvoiceTermListPercentageWarningText(
      MoveLine moveLine, Map<String, Map<String, Object>> attrsMap);

  void addReadonly(Move move, Map<String, Map<String, Object>> attrsMap);

  void addShowTaxAmount(MoveLine moveLine, Map<String, Map<String, Object>> attrsMap);

  void addShowAnalyticDistributionPanel(
      Move move, MoveLine moveLine, Map<String, Map<String, Object>> attrsMap)
      throws AxelorException;

  void addValidatePeriod(Move move, Map<String, Map<String, Object>> attrsMap)
      throws AxelorException;

  void addPartnerReadonly(MoveLine moveLine, Move move, Map<String, Map<String, Object>> attrsMap);

  void addAccountDomain(Move move, Map<String, Map<String, Object>> attrsMap);

  void addPartnerDomain(Move move, Map<String, Map<String, Object>> attrsMap);

  void addAnalyticDistributionTemplateDomain(Move move, Map<String, Map<String, Object>> attrsMap);

  void addPartnerRequired(Move move, Map<String, Map<String, Object>> attrsMap);
}
