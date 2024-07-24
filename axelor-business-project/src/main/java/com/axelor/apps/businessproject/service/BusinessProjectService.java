package com.axelor.apps.businessproject.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.project.db.Project;
import com.google.inject.persist.Transactional;

public interface BusinessProjectService {

  void setAsBusinessProject(Project project, Company company, Partner clientPartner)
      throws AxelorException;

  Project computePartnerData(Project project, Partner partner) throws AxelorException;
}
