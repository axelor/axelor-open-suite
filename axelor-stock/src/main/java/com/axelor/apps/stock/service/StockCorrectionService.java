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
package com.axelor.apps.stock.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.stock.db.StockCorrection;
import com.axelor.apps.stock.db.StockCorrectionReason;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.apps.stock.db.TrackingNumber;
import java.math.BigDecimal;
import java.util.Map;

public interface StockCorrectionService {

  public Map<String, Object> fillDefaultValues(StockLocationLine stockLocationLine);

  public Map<String, Object> fillDeafultQtys(StockCorrection stockCorrection);

  public void getDefaultQtys(
      StockLocationLine stockLocationLine, Map<String, Object> stockCorrectionQtys);

  public boolean validate(StockCorrection stockCorrection) throws AxelorException;

  StockCorrection generateStockCorrection(
      StockLocation stockLocation,
      Product product,
      TrackingNumber trackingNumber,
      BigDecimal realQty,
      StockCorrectionReason reason)
      throws Exception;

  void updateCorrectionQtys(StockCorrection stockCorrection, BigDecimal realQty);

  void updateReason(StockCorrection stockCorrection, StockCorrectionReason reason);
}
