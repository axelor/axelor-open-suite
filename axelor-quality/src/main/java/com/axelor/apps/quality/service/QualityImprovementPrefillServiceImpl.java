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
package com.axelor.apps.quality.service;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.quality.db.ControlEntry;
import com.axelor.apps.quality.db.ControlEntrySample;
import com.axelor.apps.quality.db.QIDetection;
import com.axelor.apps.quality.db.QIIdentification;
import com.axelor.apps.quality.db.repo.ControlEntrySampleRepository;
import com.axelor.apps.quality.db.repo.QIDetectionRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.db.repo.TrackingNumberRepository;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public class QualityImprovementPrefillServiceImpl implements QualityImprovementPrefillService {

  protected StockMoveLineRepository stockMoveLineRepository;
  protected ProductRepository productRepository;
  protected TrackingNumberRepository trackingNumberRepository;
  protected QIDetectionRepository qiDetectionRepository;

  @Inject
  public QualityImprovementPrefillServiceImpl(
      StockMoveLineRepository stockMoveLineRepository,
      ProductRepository productRepository,
      TrackingNumberRepository trackingNumberRepository,
      QIDetectionRepository qiDetectionRepository) {
    this.stockMoveLineRepository = stockMoveLineRepository;
    this.productRepository = productRepository;
    this.trackingNumberRepository = trackingNumberRepository;
    this.qiDetectionRepository = qiDetectionRepository;
  }

  @Override
  public int getDetectionOrigin(ControlEntry controlEntry) {
    StockMoveLine stockMoveLine = findRelatedStockMoveLine(controlEntry);
    if (stockMoveLine == null) {
      return QIDetectionRepository.ORIGIN_INTERNAL;
    }
    return getDetectionOrigin(stockMoveLine);
  }

  protected int getDetectionOrigin(StockMoveLine stockMoveLine) {
    if (stockMoveLine.getPurchaseOrderLine() != null) {
      return QIDetectionRepository.ORIGIN_SUPPLIER;
    }
    if (stockMoveLine.getSaleOrderLine() != null) {
      return QIDetectionRepository.ORIGIN_CUSTOMER;
    }
    StockMove stockMove = stockMoveLine.getStockMove();
    if (stockMove == null || stockMove.getPartner() == null) {
      return QIDetectionRepository.ORIGIN_INTERNAL;
    }
    if (isReceipt(stockMove)) {
      return QIDetectionRepository.ORIGIN_SUPPLIER;
    }
    if (isDelivery(stockMove)) {
      return QIDetectionRepository.ORIGIN_CUSTOMER;
    }
    return QIDetectionRepository.ORIGIN_INTERNAL;
  }

  protected boolean isReceipt(StockMove stockMove) {
    int typeSelect = stockMove.getTypeSelect();
    boolean reversion = Boolean.TRUE.equals(stockMove.getIsReversion());
    return reversion
        ? typeSelect == StockMoveRepository.TYPE_OUTGOING
        : typeSelect == StockMoveRepository.TYPE_INCOMING;
  }

  protected boolean isDelivery(StockMove stockMove) {
    int typeSelect = stockMove.getTypeSelect();
    boolean reversion = Boolean.TRUE.equals(stockMove.getIsReversion());
    return reversion
        ? typeSelect == StockMoveRepository.TYPE_INCOMING
        : typeSelect == StockMoveRepository.TYPE_OUTGOING;
  }

  @Override
  public QIDetection getDefaultDetection(ControlEntry controlEntry) {
    return findDetection(getDetectionOrigin(controlEntry));
  }

  protected QIDetection findDetection(int origin) {
    List<QIDetection> qiDetectionList =
        qiDetectionRepository
            .all()
            .filter(
                "self.origin = :origin AND self.isProductOrigin = true"
                    + " AND (self.archived IS NULL OR self.archived = false)")
            .bind("origin", origin)
            .fetch(2);
    return qiDetectionList.size() == 1 ? qiDetectionList.get(0) : null;
  }

  @Override
  public void fillDetectedBy(QIIdentification qiIdentification, ControlEntry controlEntry) {
    if (controlEntry.getInspector() != null) {
      qiIdentification.setDetectedByInternal(controlEntry.getInspector().getPartner());
    }
  }

  @Override
  public void fillFromControlEntry(
      QIIdentification qiIdentification, ControlEntry controlEntry, QIDetection qiDetection) {
    fillDetectedBy(qiIdentification, controlEntry);
    qiIdentification.setNonConformingQuantity(
        BigDecimal.valueOf(countNonCompliantSamples(controlEntry)));
    fillFromRelatedTo(
        qiIdentification,
        controlEntry.getRelatedToSelect(),
        controlEntry.getRelatedToSelectId(),
        qiDetection);
  }

  protected long countNonCompliantSamples(ControlEntry controlEntry) {
    if (controlEntry.getControlEntrySamplesList() == null) {
      return 0;
    }
    return controlEntry.getControlEntrySamplesList().stream()
        .map(ControlEntrySample::getResultSelect)
        .filter(Objects::nonNull)
        .filter(resultSelect -> resultSelect == ControlEntrySampleRepository.RESULT_NOT_COMPLIANT)
        .count();
  }

  protected void fillFromRelatedTo(
      QIIdentification qiIdentification,
      String relatedToSelect,
      Long relatedToSelectId,
      QIDetection qiDetection) {
    if (relatedToSelect == null || relatedToSelectId == null) {
      return;
    }
    if (StockMoveLine.class.getName().equals(relatedToSelect)) {
      StockMoveLine stockMoveLine = stockMoveLineRepository.find(relatedToSelectId);
      if (stockMoveLine != null) {
        fillFromStockMoveLine(qiIdentification, stockMoveLine, qiDetection);
      }
    } else if (Product.class.getName().equals(relatedToSelect)) {
      qiIdentification.setProduct(productRepository.find(relatedToSelectId));
    } else if (TrackingNumber.class.getName().equals(relatedToSelect)) {
      TrackingNumber trackingNumber = trackingNumberRepository.find(relatedToSelectId);
      if (trackingNumber != null) {
        qiIdentification.setProduct(trackingNumber.getProduct());
      }
    }
  }

  @Override
  public void fillFromStockMoveLine(
      QIIdentification qiIdentification, StockMoveLine stockMoveLine, QIDetection qiDetection) {
    StockMove stockMove = stockMoveLine.getStockMove();
    qiIdentification.setProduct(stockMoveLine.getProduct());
    qiIdentification.setQuantity(stockMoveLine.getRealQty());
    qiIdentification.setStockMove(stockMove);
    qiIdentification.setStockMoveLine(stockMoveLine);
    if (qiDetection == null) {
      return;
    }
    if (qiDetection.getOrigin() == QIDetectionRepository.ORIGIN_SUPPLIER) {
      fillSupplier(qiIdentification, stockMoveLine, stockMove);
    } else if (qiDetection.getOrigin() == QIDetectionRepository.ORIGIN_CUSTOMER) {
      fillCustomer(qiIdentification, stockMoveLine, stockMove);
    }
  }

  protected void fillSupplier(
      QIIdentification qiIdentification, StockMoveLine stockMoveLine, StockMove stockMove) {
    PurchaseOrderLine purchaseOrderLine = stockMoveLine.getPurchaseOrderLine();
    if (purchaseOrderLine != null) {
      PurchaseOrder purchaseOrder = purchaseOrderLine.getPurchaseOrder();
      qiIdentification.setSupplierPurchaseOrderLine(purchaseOrderLine);
      qiIdentification.setSupplierPurchaseOrder(purchaseOrder);
      if (purchaseOrder != null) {
        qiIdentification.setSupplierPartner(purchaseOrder.getSupplierPartner());
      }
    }
    if (qiIdentification.getSupplierPartner() == null
        && stockMove != null
        && isReceipt(stockMove)) {
      qiIdentification.setSupplierPartner(stockMove.getPartner());
    }
  }

  protected void fillCustomer(
      QIIdentification qiIdentification, StockMoveLine stockMoveLine, StockMove stockMove) {
    SaleOrderLine saleOrderLine = stockMoveLine.getSaleOrderLine();
    if (saleOrderLine != null) {
      SaleOrder saleOrder = saleOrderLine.getSaleOrder();
      qiIdentification.setCustomerSaleOrderLine(saleOrderLine);
      qiIdentification.setCustomerSaleOrder(saleOrder);
      if (saleOrder != null) {
        qiIdentification.setCustomerPartner(saleOrder.getClientPartner());
      }
    }
    if (qiIdentification.getCustomerPartner() == null
        && stockMove != null
        && isDelivery(stockMove)) {
      qiIdentification.setCustomerPartner(stockMove.getPartner());
    }
  }

  protected StockMoveLine findRelatedStockMoveLine(ControlEntry controlEntry) {
    if (controlEntry.getRelatedToSelectId() == null
        || !StockMoveLine.class.getName().equals(controlEntry.getRelatedToSelect())) {
      return null;
    }
    return stockMoveLineRepository.find(controlEntry.getRelatedToSelectId());
  }
}
