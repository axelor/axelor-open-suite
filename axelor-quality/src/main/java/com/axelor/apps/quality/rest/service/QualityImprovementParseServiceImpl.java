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
package com.axelor.apps.quality.rest.service;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.quality.db.QIDefault;
import com.axelor.apps.quality.db.QIDetection;
import com.axelor.apps.quality.db.QIIdentification;
import com.axelor.apps.quality.db.QIResolution;
import com.axelor.apps.quality.db.QIResolutionDefault;
import com.axelor.apps.quality.db.QualityImprovement;
import com.axelor.apps.quality.rest.dto.QIIdentificationRequest;
import com.axelor.apps.quality.rest.dto.QIResolutionDefaultRequest;
import com.axelor.apps.quality.rest.dto.QIResolutionRequest;
import com.axelor.apps.quality.rest.dto.QualityImprovementRequest;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import java.util.List;
import java.util.stream.Collectors;

public class QualityImprovementParseServiceImpl implements QualityImprovementParseService {

  @Override
  public QualityImprovement getQualityImprovementFromRequestBody(
      QualityImprovementRequest requestBody) {

    QualityImprovement qualityImprovement = new QualityImprovement();
    qualityImprovement.setType(requestBody.getType());
    qualityImprovement.setGravityTypeSelect(requestBody.getGravityType());
    qualityImprovement.setQiDetection(requestBody.fetchQIDetection());
    qualityImprovement.setAnalysisMethod(requestBody.fetchAnalysisMethod());

    return qualityImprovement;
  }

  @Override
  public QIIdentification getQiIdentificationFromRequestBody(
      QIIdentificationRequest qiIdentificationRequest, QIDetection qiDetection) {

    QIIdentification qiIdentification = new QIIdentification();

    // get customer information
    SaleOrderLine customerSaleOrderLine = qiIdentificationRequest.fetchCustomerSaleOrderLine();

    // fill from bottom element, check sol first
    if (customerSaleOrderLine != null) {
      qiIdentification.setCustomerSaleOrderLine(customerSaleOrderLine);
      SaleOrder saleOrder = customerSaleOrderLine.getSaleOrder();
      qiIdentification.setCustomerSaleOrder(saleOrder);
      qiIdentification.setCustomerPartner(saleOrder.getClientPartner());
    } else {
      // check SaleOrder
      SaleOrder customerSaleOrder = qiIdentificationRequest.fetchCustomerSaleOrder();
      if (customerSaleOrder != null) {
        qiIdentification.setCustomerSaleOrder(customerSaleOrder);
        qiIdentification.setCustomerPartner(customerSaleOrder.getClientPartner());
      } else {
        // check Partner
        Partner customerPartner = qiIdentificationRequest.fetchCustomerPartner();
        if (customerPartner != null) {
          qiIdentification.setCustomerPartner(customerPartner);
        }
      }
    }

    // get supplier information
    PurchaseOrderLine supplierPurchaseOrderLine =
        qiIdentificationRequest.fetchSupplierPurchaseOrderLine();

    // fill from bottom element, check pol first
    if (supplierPurchaseOrderLine != null) {
      qiIdentification.setSupplierPurchaseOrderLine(supplierPurchaseOrderLine);
      PurchaseOrder purchaseOrder = supplierPurchaseOrderLine.getPurchaseOrder();
      qiIdentification.setSupplierPurchaseOrder(purchaseOrder);
      qiIdentification.setSupplierPartner(purchaseOrder.getSupplierPartner());
    } else {
      // check order
      PurchaseOrder supplierPurchaseOrder = qiIdentificationRequest.fetchSupplierPurchaseOrder();
      if (supplierPurchaseOrder != null) {
        qiIdentification.setSupplierPurchaseOrder(supplierPurchaseOrder);
        qiIdentification.setSupplierPartner(supplierPurchaseOrder.getSupplierPartner());
      } else {
        // check partner
        Partner supplierPartner = qiIdentificationRequest.fetchSupplierPartner();
        if (supplierPartner != null) {
          qiIdentification.setSupplierPartner(supplierPartner);
        }
      }
    }

    qiIdentification.setManufOrder(qiIdentificationRequest.fetchManufOrder());
    qiIdentification.setOperationOrder(qiIdentificationRequest.fetchOperationOrder());
    qiIdentification.setProduct(qiIdentificationRequest.fetchProduct());
    qiIdentification.setNonConformingQuantity(qiIdentificationRequest.getNonConformingQuantity());

    return qiIdentification;
  }

  @Override
  public QIResolution getQiResolutionFromRequestBody(QIResolutionRequest qiResolutionPostRequest) {

    QIResolution qiResolution = new QIResolution();

    List<QIResolutionDefaultRequest> defects = qiResolutionPostRequest.getDefects();

    if (defects == null) {
      return qiResolution;
    }

    for (QIResolutionDefaultRequest qiResolutionDefaultRequest : defects) {
      QIResolutionDefault qiResolutionDefault =
          getQiResolutionDefaultFromRequestBody(qiResolutionDefaultRequest);
      qiResolution.addQiResolutionDefaultsListItem(qiResolutionDefault);
    }

    return qiResolution;
  }

  protected QIResolutionDefault getQiResolutionDefaultFromRequestBody(
      QIResolutionDefaultRequest qiResolutionDefaultRequest) {

    QIResolutionDefault qiResolutionDefault = new QIResolutionDefault();
    QIDefault qiDefault = qiResolutionDefaultRequest.fetchQiDefault();
    qiResolutionDefault.setQiDefault(qiDefault);
    qiResolutionDefault.setId(qiResolutionDefaultRequest.getId());
    qiResolutionDefault.setQuantity(qiResolutionDefaultRequest.getQuantity());
    qiResolutionDefault.setDescription(qiResolutionDefaultRequest.getDescription());
    qiResolutionDefault.setName(qiDefault.getName());

    return qiResolutionDefault;
  }

  @Override
  public int filterQIResolutionDefaultValues(QIResolution qiResolution, int qiType) {
    if (qiResolution == null || qiResolution.getQiResolutionDefaultsList() == null) {
      return 0;
    }
    List<QIResolutionDefault> qiResolutionDefaultsList = qiResolution.getQiResolutionDefaultsList();

    List<QIResolutionDefault> toRemove =
        qiResolutionDefaultsList.stream()
            .filter(
                defaultValue ->
                    (qiType == 2 && !defaultValue.getQiDefault().getIsSystemDefault())
                        || (qiType == 1 && !defaultValue.getQiDefault().getIsProductDefault()))
            .collect(Collectors.toList());

    qiResolutionDefaultsList.removeAll(toRemove);

    return toRemove.size();
  }
}
