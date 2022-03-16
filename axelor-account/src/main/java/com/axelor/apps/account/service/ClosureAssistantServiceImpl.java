package com.axelor.apps.account.service;

import com.axelor.apps.account.db.ClosureAssistant;
import com.axelor.apps.account.db.ClosureAssistantLine;
import com.axelor.apps.account.db.repo.ClosureAssistantRepository;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Year;
import com.axelor.apps.base.db.repo.YearRepository;
import com.axelor.auth.AuthUtils;
import com.axelor.common.ObjectUtils;
import com.axelor.db.JPA;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import javax.persistence.TypedQuery;

public class ClosureAssistantServiceImpl implements ClosureAssistantService {

  protected ClosureAssistantRepository closureAssistantRepository;

  @Inject
  public ClosureAssistantServiceImpl(ClosureAssistantRepository closureAssistantRepository) {
    this.closureAssistantRepository = closureAssistantRepository;
  }

  @Override
  @Transactional
  public ClosureAssistant updateClosureAssistantProgress(ClosureAssistant closureAssistant) {
    closureAssistant = closureAssistantRepository.find(closureAssistant.getId());
    if (!ObjectUtils.isEmpty(closureAssistant.getClosureAssistantLineList())) {
      BigDecimal countValidatedLines =
          BigDecimal.valueOf(
              closureAssistant.getClosureAssistantLineList().stream()
                  .filter(ClosureAssistantLine::getIsValidated)
                  .count());
      BigDecimal nbClosureAssistantLines =
          BigDecimal.valueOf(closureAssistant.getClosureAssistantLineList().size());
      closureAssistant.setProgress(
          (countValidatedLines.multiply(
              BigDecimal.valueOf(100).divide(nbClosureAssistantLines, 2, RoundingMode.HALF_UP))));
    }
    return closureAssistant;
  }

  @Override
  public ClosureAssistant updateFiscalYear(ClosureAssistant closureAssistant) {
    TypedQuery<Year> yearQuery =
        JPA.em()
            .createQuery(
                "SELECT self FROM Year self  "
                    + "WHERE self.company = :company AND self.typeSelect = :typeSelect AND self.statusSelect = :statusSelect "
                    + "ORDER BY self.fromDate DESC",
                Year.class);

    yearQuery.setParameter("company", closureAssistant.getCompany());
    yearQuery.setParameter("typeSelect", YearRepository.STATUS_OPENED);
    yearQuery.setParameter("statusSelect", YearRepository.TYPE_FISCAL);

    List<Year> yearList = yearQuery.getResultList();

    if (!ObjectUtils.isEmpty(yearList)) {
      closureAssistant.setFiscalYear(yearList.get(0));
    }
    return closureAssistant;
  }

  @Override
  public ClosureAssistant updateCompany(ClosureAssistant closureAssistant) {
    Company company = null;

    if (AuthUtils.getUser() != null) {
      if (AuthUtils.getUser().getActiveCompany() != null) {
        company = AuthUtils.getUser().getActiveCompany();
      } else if (!ObjectUtils.isEmpty(AuthUtils.getUser().getCompanySet())) {
        company = (Company) AuthUtils.getUser().getCompanySet().toArray()[0];
      }
    }

    closureAssistant.setCompany(company);
    return closureAssistant;
  }

  @Override
  public boolean checkNoExistingClosureAssistantForSameYear(ClosureAssistant closureAssistant) {
    TypedQuery<ClosureAssistant> closureAssistantQuery =
        JPA.em()
            .createQuery(
                "SELECT self FROM ClosureAssistant self  " + "WHERE self.fiscalYear = :year",
                ClosureAssistant.class);

    closureAssistantQuery.setParameter("year", closureAssistant.getFiscalYear());

    List<ClosureAssistant> ClosureAssistantList = closureAssistantQuery.getResultList();

    return !ObjectUtils.isEmpty(ClosureAssistantList);
  }
}
