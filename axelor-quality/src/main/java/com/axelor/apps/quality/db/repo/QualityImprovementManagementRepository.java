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
package com.axelor.apps.quality.db.repo;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.quality.db.QIAnalysis;
import com.axelor.apps.quality.db.QIIdentification;
import com.axelor.apps.quality.db.QIResolution;
import com.axelor.apps.quality.db.QualityImprovement;
import com.axelor.apps.quality.exception.QualityExceptionMessage;
import com.axelor.apps.supplychain.service.SupplierScoreService;
import com.axelor.auth.AuthUtils;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.google.common.base.Strings;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import java.util.List;

public class QualityImprovementManagementRepository extends QualityImprovementRepository {

  protected SequenceService sequenceService;
  protected AppBaseService appBaseService;
  protected SupplierScoreService supplierScoreService;

  @Inject
  public QualityImprovementManagementRepository(
      SequenceService sequenceService,
      AppBaseService appBaseService,
      SupplierScoreService supplierScoreService) {
    this.sequenceService = sequenceService;
    this.appBaseService = appBaseService;
    this.supplierScoreService = supplierScoreService;
  }

  @Override
  public QualityImprovement save(QualityImprovement qualityImprovement) {
    try {
      Company company = qualityImprovement.getCompany();
      if (Strings.isNullOrEmpty(qualityImprovement.getSequence())) {
        String sequence =
            sequenceService.getSequenceNumber(
                SequenceRepository.QUALITY_IMPROVEMENT,
                company,
                QualityImprovement.class,
                "sequence",
                qualityImprovement);

        if (sequence == null) {
          throw new AxelorException(
              company,
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(QualityExceptionMessage.QUALITY_IMPROVEMENT_SEQUENCE_ERROR),
              company.getName());
        } else {
          qualityImprovement.setSequence(sequence);
        }
      }
      getOrCreateQIIdentification(qualityImprovement);
      getOrCreateQIResolution(qualityImprovement);
      getOrCreateQIAnalysis(qualityImprovement);

      Long previousSupplierPartnerId = findPersistedSupplierPartnerId(qualityImprovement);
      qualityImprovement = super.save(qualityImprovement);
      updateSupplierScore(qualityImprovement, previousSupplierPartnerId);
      return qualityImprovement;

    } catch (AxelorException e) {
      TraceBackService.traceExceptionFromSaveMethod(e);
      throw new PersistenceException(e.getMessage(), e);
    }
  }

  /**
   * Open quality improvements feed the supplier score, so every save recomputes it, for the
   * previous supplier too when the quality improvement changed hands.
   */
  protected void updateSupplierScore(
      QualityImprovement qualityImprovement, Long previousSupplierPartnerId) {
    QIIdentification qiIdentification = qualityImprovement.getQiIdentification();
    Partner supplierPartner =
        qiIdentification != null ? qiIdentification.getSupplierPartner() : null;
    if (supplierPartner != null) {
      supplierScoreService.computeAndSave(supplierPartner);
    }
    if (previousSupplierPartnerId != null
        && (supplierPartner == null
            || !previousSupplierPartnerId.equals(supplierPartner.getId()))) {
      supplierScoreService.computeAndSave(JPA.find(Partner.class, previousSupplierPartnerId));
    }
  }

  /**
   * The edited quality improvement is already flushed on the current entity manager, which would
   * return the new supplier; a separate entity manager reads the committed state instead.
   */
  protected Long findPersistedSupplierPartnerId(QualityImprovement qualityImprovement) {
    if (qualityImprovement.getId() == null) {
      return null;
    }
    try (EntityManager readEm = JPA.em().getEntityManagerFactory().createEntityManager()) {
      List<Long> supplierPartnerIds =
          readEm
              .createQuery(
                  "SELECT qid.supplierPartner.id FROM QualityImprovement qi "
                      + "JOIN qi.qiIdentification qid WHERE qi.id = :id",
                  Long.class)
              .setParameter("id", qualityImprovement.getId())
              .getResultList();
      return supplierPartnerIds.isEmpty() ? null : supplierPartnerIds.get(0);
    }
  }

  protected QIIdentification getOrCreateQIIdentification(QualityImprovement qualityImprovement) {
    QIIdentification qiIdentification = qualityImprovement.getQiIdentification();
    if (qiIdentification == null) {
      qiIdentification = new QIIdentification();
      qiIdentification.setQi(qualityImprovement);
    }
    return qiIdentification;
  }

  protected QIResolution getOrCreateQIResolution(QualityImprovement qualityImprovement) {
    QIResolution qiResolution = qualityImprovement.getQiResolution();
    if (qiResolution == null) {
      qiResolution = new QIResolution();
      qiResolution.setQi(qualityImprovement);
    }
    return qiResolution;
  }

  protected QIAnalysis getOrCreateQIAnalysis(QualityImprovement qualityImprovement) {
    QIAnalysis qiAnalysis = qualityImprovement.getQiAnalysis();
    if (qiAnalysis == null) {
      qiAnalysis = new QIAnalysis();
      qiAnalysis.setQi(qualityImprovement);
      qiAnalysis.setPlanOwner(AuthUtils.getUser());
    }
    return qiAnalysis;
  }
}
