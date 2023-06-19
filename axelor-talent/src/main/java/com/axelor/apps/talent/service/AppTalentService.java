package com.axelor.apps.talent.service;

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.studio.db.AppRecruitment;

public interface AppTalentService extends AppBaseService {
  AppRecruitment getAppRecruitment();
}
