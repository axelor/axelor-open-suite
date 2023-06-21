package com.axelor.apps.talent.service;

import com.axelor.apps.base.service.app.AppBaseServiceImpl;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.axelor.studio.app.service.AppVersionService;
import com.axelor.studio.db.AppRecruitment;
import com.axelor.studio.db.repo.AppRecruitmentRepository;
import com.axelor.studio.db.repo.AppRepository;
import com.axelor.studio.service.AppSettingsStudioService;
import com.google.inject.Inject;

public class AppTalentServiceImpl extends AppBaseServiceImpl implements AppTalentService {
  protected AppRecruitmentRepository appRecruitmentRepository;

  @Inject
  public AppTalentServiceImpl(
      AppRepository appRepo,
      MetaFiles metaFiles,
      AppVersionService appVersionService,
      MetaModelRepository metaModelRepo,
      AppSettingsStudioService appSettingsStudioService,
      AppRecruitmentRepository appRecruitmentRepository) {
    super(appRepo, metaFiles, appVersionService, metaModelRepo, appSettingsStudioService);
    this.appRecruitmentRepository = appRecruitmentRepository;
  }

  @Override
  public AppRecruitment getAppRecruitment() {
    return appRecruitmentRepository.all().fetchOne();
  }
}
