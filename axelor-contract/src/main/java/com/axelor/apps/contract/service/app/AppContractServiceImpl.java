package com.axelor.apps.contract.service.app;

import com.axelor.apps.base.service.app.AppBaseServiceImpl;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.repo.MetaFileRepository;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.axelor.meta.db.repo.MetaModuleRepository;
import com.axelor.studio.app.service.AppVersionService;
import com.axelor.studio.db.AppContract;
import com.axelor.studio.db.repo.AppContractRepository;
import com.axelor.studio.db.repo.AppRepository;
import com.axelor.studio.service.AppSettingsStudioService;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class AppContractServiceImpl extends AppBaseServiceImpl implements AppContractService {

  protected AppContractRepository appContractRepository;

  @Inject
  public AppContractServiceImpl(
      AppRepository appRepo,
      MetaFiles metaFiles,
      AppVersionService appVersionService,
      MetaModelRepository metaModelRepo,
      AppSettingsStudioService appSettingsService,
      MetaModuleRepository metaModuleRepo,
      MetaFileRepository metaFileRepo,
      AppContractRepository appContractRepository) {
    super(
        appRepo,
        metaFiles,
        appVersionService,
        metaModelRepo,
        appSettingsService,
        metaModuleRepo,
        metaFileRepo);
    this.appContractRepository = appContractRepository;
  }

  @Override
  public AppContract getAppContract() {
    return appContractRepository.all().fetchOne();
  }
}
