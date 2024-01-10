package com.axelor.apps.base.service.subline;

import com.axelor.apps.base.AxelorException;
import com.axelor.db.Model;
import java.math.BigDecimal;

public interface SubLineService {

  <T extends Model, U extends Model> T updateSubLinesQty(BigDecimal oldQty, T line, U parent)
      throws AxelorException;

  <T extends Model, U extends Model> T updateSubLinesPrice(BigDecimal oldPrice, T line, U parent)
      throws AxelorException;

  <T extends Model> boolean isChildCounted(T line);

  <T extends Model> T updateIsNotCountable(T line);
}
