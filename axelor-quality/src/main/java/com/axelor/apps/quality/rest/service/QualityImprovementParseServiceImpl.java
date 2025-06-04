package com.axelor.apps.quality.rest.service;

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
import com.axelor.apps.sale.db.SaleOrderLine;

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
    qiIdentification.setCustomerPartner(qiIdentificationRequest.fetchCustomerPartner());

    SaleOrderLine customerSaleOrderLine = qiIdentificationRequest.fetchCustomerSaleOrderLine();
    if (customerSaleOrderLine != null) {
      qiIdentification.setCustomerSaleOrder(customerSaleOrderLine.getSaleOrder());
      qiIdentification.setCustomerSaleOrderLine(customerSaleOrderLine);
    }
    PurchaseOrderLine supplierPurchaseOrderLine =
        qiIdentificationRequest.fetchSupplierPurchaseOrderLine();
    qiIdentification.setSupplierPartner(qiIdentificationRequest.fetchSupplierPartner());
    if (supplierPurchaseOrderLine != null) {
      qiIdentification.setSupplierPurchaseOrder(supplierPurchaseOrderLine.getPurchaseOrder());
      qiIdentification.setSupplierPurchaseOrderLine(supplierPurchaseOrderLine);
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

    for (QIResolutionDefaultRequest qiResolutionDefaultRequest :
        qiResolutionPostRequest.getDefects()) {
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
}
