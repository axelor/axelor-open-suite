package com.axelor.apps.base.service;

import com.axelor.apps.base.db.MainActivity;
import com.axelor.apps.base.db.repo.MainActivityRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class MainActivityServiceImpl implements MainActivityService {

  protected MainActivityRepository mainActivityRepository;

  @Inject
  public MainActivityServiceImpl(MainActivityRepository mainActivityRepository) {
    this.mainActivityRepository = mainActivityRepository;
  }

  @Transactional
  public void computeFullName(MainActivity mainActivity) {
    if (mainActivity != null) {
      mainActivity.setFullName(mainActivity.getCode() + " - " + mainActivity.getShortName());
      mainActivityRepository.save(mainActivity);
    }
  }
}
