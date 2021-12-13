package com.axelor.apps.account.service.moveline;

import com.axelor.apps.account.db.MoveLine;
import java.util.List;
import java.util.Map;

public interface MoveLineConsolidateService {
  public MoveLine findConsolidateMoveLine(
      Map<List<Object>, MoveLine> map, MoveLine moveLine, List<Object> keys);

  public List<MoveLine> consolidateMoveLines(List<MoveLine> moveLines);
}
