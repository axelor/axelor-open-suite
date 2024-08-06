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
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.TypedQuery;
import org.apache.commons.collections.CollectionUtils;

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
                "SELECT self FROM ClosureAssistant self  "
                    + "WHERE self.fiscalYear = :year AND self.id != :id",
                ClosureAssistant.class);

    closureAssistantQuery.setParameter("year", closureAssistant.getFiscalYear());
    closureAssistantQuery.setParameter("id", closureAssistant.getId());

    List<ClosureAssistant> ClosureAssistantList = closureAssistantQuery.getResultList();
    return !ObjectUtils.isEmpty(ClosureAssistantList);
  }

  @Override
  @Transactional
  public boolean setStatusWithLines(ClosureAssistant closureAssistant) {
    if (!CollectionUtils.isEmpty(closureAssistant.getClosureAssistantLineList())) {
      List<ClosureAssistantLine> lines =
          closureAssistant.getClosureAssistantLineList().stream()
              .sorted(Comparator.comparing(ClosureAssistantLine::getSequence))
              .collect(Collectors.toList());
      if (!lines.get(0).getIsValidated()
          && closureAssistant.getStatusSelect() != ClosureAssistantRepository.STATUS_NEW) {
        closureAssistant.setStatusSelect(ClosureAssistantRepository.STATUS_NEW);
        closureAssistantRepository.save(closureAssistant);
        return true;
      } else if (lines.get(lines.size() - 1).getIsValidated()
          && closureAssistant.getStatusSelect() != ClosureAssistantRepository.STATUS_TERMINATED) {
        closureAssistant.setStatusSelect(ClosureAssistantRepository.STATUS_TERMINATED);
        closureAssistantRepository.save(closureAssistant);
        return true;
      } else if (closureAssistant.getStatusSelect()
          != ClosureAssistantRepository.STATUS_IN_PROGRESS) {
        closureAssistant.setStatusSelect(ClosureAssistantRepository.STATUS_IN_PROGRESS);
        closureAssistantRepository.save(closureAssistant);
        return true;
      }
    }
    return false;
  }
}
