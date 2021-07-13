package com.axelor.apps.base.web;

import com.axelor.apps.base.db.MainActivity;
import com.axelor.apps.base.db.repo.MainActivityRepository;
import com.axelor.apps.base.service.MainActivityService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class MainActivityController {

  protected MainActivityRepository mainActivityRepository;
  protected MainActivityService mainActivityService;

  @Inject
  public MainActivityController(
      MainActivityRepository mainActivityRepository, MainActivityService mainActivityService) {
    this.mainActivityRepository = mainActivityRepository;
    this.mainActivityService = mainActivityService;
  }

  public void computeFullName(ActionRequest request, ActionResponse response) {
    try {
      MainActivity mainActivity = request.getContext().asType(MainActivity.class);
      mainActivity = mainActivityRepository.find(mainActivity.getId());
      mainActivityService.computeFullName(mainActivity);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
