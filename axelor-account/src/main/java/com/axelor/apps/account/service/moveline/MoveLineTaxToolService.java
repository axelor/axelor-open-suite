package com.axelor.apps.account.service.moveline;

import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.util.TaxConfiguration;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.tuple.Pair;

public interface MoveLineTaxToolService {
  Map<TaxConfiguration, Pair<BigDecimal, BigDecimal>> getTaxMoveLineMapToRevert(
      List<MoveLine> advancePaymentMoveLineList);
}
