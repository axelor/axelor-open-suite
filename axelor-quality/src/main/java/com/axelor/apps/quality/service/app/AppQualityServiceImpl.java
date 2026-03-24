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
package com.axelor.apps.quality.service.app;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.service.app.AppBaseServiceImpl;
import com.axelor.apps.quality.db.QualityConfig;
import com.axelor.apps.quality.db.repo.QualityConfigRepository;
import com.axelor.studio.app.service.AppService;
import com.axelor.studio.db.AppQuality;
import com.axelor.studio.db.repo.AppQualityRepository;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;
import java.util.List;

public class AppQualityServiceImpl extends AppBaseServiceImpl implements AppQualityService {

  private AppQualityRepository appQualityRepo;
  private CompanyRepository companyRepository;
  private QualityConfigRepository qualityConfigRepository;

  @Inject
  public AppQualityServiceImpl(
      AppService appService,
      AppQualityRepository appQualityRepo,
      CompanyRepository companyRepository,
      QualityConfigRepository qualityConfigRepository) {
    super(appService);
    this.appQualityRepo = appQualityRepo;
    this.companyRepository = companyRepository;
    this.qualityConfigRepository = qualityConfigRepository;
  }

  @Override
  public AppQuality getAppQuality() {
    return appQualityRepo.all().cacheable().autoFlush(false).fetchOne();
  }

  @Override
  @Transactional
  public void generateQualityConfigurations() {
    List<Company> companies = companyRepository.all().filter("self.qualityConfig is null").fetch();

    for (Company company : companies) {
      QualityConfig qualityConfig = new QualityConfig();
      qualityConfig.setCompany(company);
      qualityConfigRepository.save(qualityConfig);
    }
  }
}
