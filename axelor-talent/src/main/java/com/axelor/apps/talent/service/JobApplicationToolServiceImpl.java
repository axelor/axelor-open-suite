package com.axelor.apps.talent.service;

import com.axelor.apps.talent.db.JobApplication;

public class JobApplicationToolServiceImpl implements JobApplicationToolService {

  @Override
  public String computeFullName(JobApplication jobApplication) {

    String fullName = null;

    if (jobApplication.getFirstName() != null) {
      fullName = jobApplication.getFirstName();
    }
    if (fullName == null) {
      fullName = jobApplication.getLastName();
    } else {
      fullName += " " + jobApplication.getLastName();
    }

    return fullName;
  }
}
