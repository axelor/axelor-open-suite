/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
package com.axelor.apps.purchase.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.purchase.db.CallTenderAttrConfig;

public interface CallTenderAttrConfigService {

  String MODEL_SOURCE = "com.axelor.apps.purchase.db.CallTenderAttrConfigSource";
  String MODEL_PRODUCT = "com.axelor.apps.base.db.Product";
  String MODEL_SO_LINE = "com.axelor.apps.sale.db.SaleOrderLine";
  String MODEL_NEED = "com.axelor.apps.purchase.db.CallTenderNeed";
  String MODEL_OFFER = "com.axelor.apps.purchase.db.CallTenderOffer";

  String MODEL_FIELD = "attrs";

  void syncMirrorFields(CallTenderAttrConfig config);

  void deleteFields(CallTenderAttrConfig config);

  String buildDefaultAttrs(CallTenderAttrConfig config, String existingAttrs);

  void regenerateFieldNames(CallTenderAttrConfig config);

  void validateFieldNameUniqueness(CallTenderAttrConfig config) throws AxelorException;
}
