package com.axelor.apps.base.listener;

import com.axelor.app.AppSettings;
import com.axelor.apps.base.db.App;
import com.axelor.apps.base.db.repo.AppRepository;
import com.axelor.apps.base.service.app.AppService;
import com.axelor.common.StringUtils;
import com.axelor.event.Observes;
import com.axelor.events.StartupEvent;
import com.axelor.exception.service.TraceBackService;
import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoper;
import com.google.inject.servlet.ServletScopes;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseServerStartListener {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  protected AppService appService;
  protected AppRepository appRepository;
  protected static String DEFAULT_LOCALE = "en";

  @Inject
  public BaseServerStartListener(AppService appService, AppRepository appRepository) {
    this.appService = appService;
    this.appRepository = appRepository;
  }

  public void installAppsOnStartup(@Observes StartupEvent event) {

    final RequestScoper scope = ServletScopes.scopeRequest(Collections.emptyMap());

    try (RequestScoper.CloseableScope ignored = scope.open()) {

      AppSettings appSettings = AppSettings.get();

      String apps = appSettings.get("aos.apps.install-apps");
      if (StringUtils.isBlank(apps)) {
        return;
      }

      List<App> appList = new ArrayList<>();

      if (apps.equalsIgnoreCase("all")) {
        appList = appRepository.all().filter("self.active IS NULL OR self.active = false").fetch();
      } else {
        String[] appCodes = apps.split(",");
        for (String code : appCodes) {
          App app = appRepository.findByCode(code.trim());
          if (app != null) {
            appList.add(app);
          }
        }
      }

      if (appList.size() == 0) {
        return;
      }

      appService.bulkInstall(
          appList,
          appSettings.getBoolean("data.import.demo-data", false),
          appSettings.get("application.locale", DEFAULT_LOCALE));

    } catch (Exception e) {
      TraceBackService.trace(e);
      log.debug(e.getMessage());
    }
  }
}
