package com.axelor.admin.auth;

import com.axelor.apps.admin.service.GlobalTrackingLogService;
import com.axelor.event.Observes;
import com.axelor.events.PostRequest;
import com.axelor.events.RequestEvent;
import com.axelor.inject.Beans;
import com.axelor.meta.db.repo.MetaModelRepository;
import javax.inject.Named;

public class ExportObserver {

  void onExport(@Observes @Named(RequestEvent.EXPORT) PostRequest event) {

    Beans.get(GlobalTrackingLogService.class)
        .createExportLog(
            Beans.get(MetaModelRepository.class)
                .all()
                .filter("self.fullName = ?", event.getRequest().getBeanClass().getName())
                .fetchOne());
  }
}
