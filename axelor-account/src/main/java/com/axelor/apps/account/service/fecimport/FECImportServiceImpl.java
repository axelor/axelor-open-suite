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
package com.axelor.apps.account.service.fecimport;

import com.axelor.apps.account.db.FECImport;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.ReconcileGroup;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.db.repo.ReconcileGroupRepository;
import com.axelor.apps.account.db.repo.ReconcileRepository;
import com.axelor.apps.account.service.ReconcileGroupService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.db.JPA;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.apache.commons.collections.CollectionUtils;

public class FECImportServiceImpl implements FECImportService {

  protected CompanyRepository companyRepository;
  protected MetaFiles metaFiles;
  protected ReconcileGroupService reconcileGroupService;
  protected ReconcileRepository reconcileRepo;
  protected ReconcileGroupRepository reconcileGroupRepo;

  @Inject
  public FECImportServiceImpl(
      CompanyRepository companyRepository,
      MetaFiles metaFiles,
      ReconcileGroupService reconcileGroupService,
      ReconcileGroupRepository reconcileGroupRepo,
      ReconcileRepository reconcileRepo) {
    this.companyRepository = companyRepository;
    this.metaFiles = metaFiles;
    this.reconcileGroupService = reconcileGroupService;
    this.reconcileRepo = reconcileRepo;
    this.reconcileGroupRepo = reconcileGroupRepo;
  }

  @Override
  public Company getCompany(MetaFile dataMetaFile) {
    Company company = null;
    if (dataMetaFile != null && dataMetaFile.getFileName() != null) {
      String fileName = dataMetaFile.getFileName();
      int separatorIndex = fileName.indexOf('F');
      if (separatorIndex > 0) {
        String registrationCode = fileName.substring(0, separatorIndex);
        company =
            companyRepository
                .all()
                .filter("self.partner.registrationCode = ?", registrationCode)
                .fetchOne();
      }
    }

    if (company != null) {
      return company;
    } else if (Optional.ofNullable(AuthUtils.getUser()).map(User::getActiveCompany).orElse(null)
        != null) {
      return Optional.ofNullable(AuthUtils.getUser()).map(User::getActiveCompany).orElse(null);
    } else {
      return companyRepository.all().fetchOne();
    }
  }

  @Override
  public void letterImportedReconcileGroup(FECImport fecImport) throws AxelorException {

    List<Integer> status =
        Arrays.asList(MoveRepository.STATUS_ACCOUNTED, MoveRepository.STATUS_DAYBOOK);
    List<ReconcileGroup> reconcileGroups =
        reconcileGroupRepo
            .all()
            .filter(
                "self IN (SELECT reconcileGroup FROM MoveLine moveLine WHERE moveLine.move.statusSelect IN :status AND moveLine.move.fecImport = :fecImport)")
            .bind("status", status)
            .bind("fecImport", fecImport)
            .fetch();
    for (ReconcileGroup reconcileGroup : reconcileGroups) {
      letterReconcileGroup(reconcileGroup);
    }
  }

  @Transactional
  protected void letterReconcileGroup(ReconcileGroup reconcileGroup) {
    try {
      reconcileGroupService.letter(reconcileGroup);
      reconcileGroup = reconcileGroupRepo.find(reconcileGroup.getId());
      List<Reconcile> reconcileList =
          reconcileRepo
              .all()
              .filter(
                  "self.reconcileGroup.id = :reconcileGroupId AND self.statusSelect = :confirmed")
              .bind("reconcileGroupId", reconcileGroup.getId())
              .bind("confirmed", ReconcileRepository.STATUS_CONFIRMED)
              .fetch();

      if (CollectionUtils.isNotEmpty(reconcileList)
          && reconcileGroupService.isBalanced(reconcileList)) {
        reconcileGroup.setStatusSelect(ReconcileGroupRepository.STATUS_FINAL);
      } else {
        LocalDateTime todayDateTime =
            Beans.get(AppBaseService.class)
                .getTodayDateTime(reconcileGroup.getCompany())
                .toLocalDateTime();
        if (CollectionUtils.isEmpty(reconcileList)) {
          reconcileGroup.setStatusSelect(ReconcileGroupRepository.STATUS_UNLETTERED);
          reconcileGroup.setUnletteringDateTime(todayDateTime);
        } else {
          reconcileGroup.setStatusSelect(ReconcileGroupRepository.STATUS_TEMPORARY);
          reconcileGroup.setLetteringDateTime(todayDateTime);
        }
      }
      reconcileGroupRepo.save(reconcileGroup);
    } catch (Exception e) {
      TraceBackService.trace(e);
    } finally {
      JPA.clear();
    }
  }
}
