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
package com.axelor.apps.stock.rest.dto;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.RequestPostStructure;
import java.time.LocalDate;
import javax.validation.constraints.Min;

public class LogisticalFormPostRequest extends RequestPostStructure {

  @Min(0)
  private Long carrierPartnerId;

  @Min(0)
  private Long deliverToCustomerPartnerId;

  @Min(0)
  private Long stockLocationId;

  private LocalDate collectionDate;

  private String internalDeliveryComment;

  private String externalDeliveryComment;

  public Long getCarrierPartnerId() {
    return carrierPartnerId;
  }

  public void setCarrierPartnerId(Long carrierPartnerId) {
    this.carrierPartnerId = carrierPartnerId;
  }

  public Long getDeliverToCustomerPartnerId() {
    return deliverToCustomerPartnerId;
  }

  public void setDeliverToCustomerPartnerId(Long deliverToCustomerPartnerId) {
    this.deliverToCustomerPartnerId = deliverToCustomerPartnerId;
  }

  public Long getStockLocationId() {
    return stockLocationId;
  }

  public void setStockLocationId(Long stockLocationId) {
    this.stockLocationId = stockLocationId;
  }

  public LocalDate getCollectionDate() {
    return collectionDate;
  }

  public void setCollectionDate(LocalDate collectionDate) {
    this.collectionDate = collectionDate;
  }

  public String getInternalDeliveryComment() {
    return internalDeliveryComment;
  }

  public void setInternalDeliveryComment(String internalDeliveryComment) {
    this.internalDeliveryComment = internalDeliveryComment;
  }

  public String getExternalDeliveryComment() {
    return externalDeliveryComment;
  }

  public void setExternalDeliveryComment(String externalDeliveryComment) {
    this.externalDeliveryComment = externalDeliveryComment;
  }

  public Partner fetchCarrierPartner() {
    if (carrierPartnerId == null || carrierPartnerId == 0L) {
      return null;
    }
    return ObjectFinder.find(Partner.class, carrierPartnerId, ObjectFinder.NO_VERSION);
  }

  public Partner fetchDeliverToCustomerPartner() {
    if (deliverToCustomerPartnerId == null || deliverToCustomerPartnerId == 0L) {
      return null;
    }
    return ObjectFinder.find(Partner.class, deliverToCustomerPartnerId, ObjectFinder.NO_VERSION);
  }

  public StockLocation fetchStockLocation() {
    if (stockLocationId == null || stockLocationId == 0L) {
      return null;
    }
    return ObjectFinder.find(StockLocation.class, stockLocationId, ObjectFinder.NO_VERSION);
  }
}
