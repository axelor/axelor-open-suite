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
package com.axelor.apps.quality.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.quality.db.QIDetection;
import com.axelor.apps.quality.db.QIIdentification;
import com.axelor.apps.quality.db.QualityImprovement;
import com.axelor.apps.quality.db.repo.QIDetectionRepository;
import com.axelor.apps.quality.exception.QualityExceptionMessage;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.i18n.I18n;
import com.axelor.meta.MetaStore;
import com.axelor.meta.schema.views.Selection;

public class QualityImprovementCheckValuesServiceImpl
    implements QualityImprovementCheckValuesService {

  @Override
  public void checkQualityImprovementValues(QualityImprovement qualityImprovement)
      throws AxelorException {
    QIDetection qiDetection = qualityImprovement.getQiDetection();
    Integer type = qualityImprovement.getType();
    String detectionOriginValue = getDetectionOrigin(qiDetection);
    QIIdentification qiIdentification = qualityImprovement.getQiIdentification();

    checkFieldsByType(type, qiIdentification.getProduct(), qiIdentification.getManufOrder());

    if (qiDetection.getOrigin() == QIDetectionRepository.ORIGIN_SUPPLIER) {
      checkSupplierOriginFields(
          qiIdentification.getSupplierPartner(),
          qiIdentification.getCustomerSaleOrderLine(),
          detectionOriginValue);
    } else if (qiDetection.getOrigin() == QIDetectionRepository.ORIGIN_CUSTOMER) {
      checkCustomerOriginFields(
          qiIdentification.getCustomerPartner(),
          qiIdentification.getSupplierPartner(),
          qiIdentification.getSupplierPurchaseOrderLine(),
          detectionOriginValue);
    }
  }

  /**
   * get the title of the origin selection value
   *
   * @param detection
   * @return
   */
  protected String getDetectionOrigin(QIDetection detection) {
    String selection = "quality.qi.detection.origin.select";
    Selection.Option selectionItem =
        MetaStore.getSelectionItem(selection, detection.getOrigin().toString());
    return selectionItem.getLocalizedTitle();
  }

  /**
   * check fields consistency for supplier origin
   *
   * @param supplierPartner
   * @param customerSaleOrderLine
   * @param detectionOriginValue
   * @throws AxelorException
   */
  protected void checkSupplierOriginFields(
      Partner supplierPartner, SaleOrderLine customerSaleOrderLine, String detectionOriginValue)
      throws AxelorException {
    // supplierPartner is required
    if (supplierPartner == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(QualityExceptionMessage.API_SUPPLIER_NOT_PROVIDED));
    }
    // customerSaleOrderLine should not be filled
    if (customerSaleOrderLine != null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          String.format(
              I18n.get(QualityExceptionMessage.API_DATA_DOES_NOT_MATCH_DETECTION_ORIGIN),
              detectionOriginValue));
    }
  }

  /**
   * check fields consistency for customer origin
   *
   * @param customerPartner
   * @param supplierPartner
   * @param supplierPurchaseOrderLine
   * @param detectionOriginValue
   * @throws AxelorException
   */
  protected void checkCustomerOriginFields(
      Partner customerPartner,
      Partner supplierPartner,
      PurchaseOrderLine supplierPurchaseOrderLine,
      String detectionOriginValue)
      throws AxelorException {
    // customerPartner is required
    if (customerPartner == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(QualityExceptionMessage.API_CUSTOMER_NOT_PROVIDED));
    }

    // supplierPartner and supplierPurchaseOrderLine should not be filled
    if (supplierPartner != null || supplierPurchaseOrderLine != null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          String.format(
              I18n.get(QualityExceptionMessage.API_DATA_DOES_NOT_MATCH_DETECTION_ORIGIN),
              detectionOriginValue));
    }
  }

  protected void checkFieldsByType(int type, Product product, ManufOrder manufOrder)
      throws AxelorException {
    if (type == 2 && (product != null || manufOrder != null)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(QualityExceptionMessage.API_TYPE_SYSTEM_PRODUCT_MANUF_ORDER_FILLED));
    }
  }
}
