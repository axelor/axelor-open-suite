/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.purchase.service.purchase.request;

import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.utils.helpers.StringHtmlListBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PurchaseRequestToPoGenerationResult {

  private final List<PurchaseOrder> purchaseOrders;
  private final List<String> warningMessages;

  public PurchaseRequestToPoGenerationResult(
      List<PurchaseOrder> purchaseOrders, List<String> warningMessages) {
    List<PurchaseOrder> safePurchaseOrders =
        purchaseOrders == null ? Collections.emptyList() : purchaseOrders;
    List<String> safeWarningMessages =
        warningMessages == null ? Collections.emptyList() : warningMessages;

    this.purchaseOrders = Collections.unmodifiableList(new ArrayList<>(safePurchaseOrders));
    this.warningMessages = Collections.unmodifiableList(new ArrayList<>(safeWarningMessages));
  }

  public List<PurchaseOrder> getPurchaseOrders() {
    return purchaseOrders;
  }

  public List<String> getWarningMessages() {
    return warningMessages;
  }

  public boolean hasWarnings() {
    return !warningMessages.isEmpty();
  }

  public boolean hasPurchaseOrders() {
    return !purchaseOrders.isEmpty();
  }

  public String getWarningMessage() {
    return StringHtmlListBuilder.formatMessage(warningMessages);
  }
}
