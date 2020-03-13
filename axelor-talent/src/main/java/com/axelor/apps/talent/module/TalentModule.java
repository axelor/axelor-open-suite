/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.talent.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.base.service.message.MailAccountServiceBaseImpl;
import com.axelor.apps.talent.db.repo.JobApplicationRepository;
import com.axelor.apps.talent.db.repo.JobApplicationTalentRepository;
import com.axelor.apps.talent.db.repo.JobPositionRepository;
import com.axelor.apps.talent.db.repo.JobPositionTalentRepository;
import com.axelor.apps.talent.db.repo.TrainingRegisterRepository;
import com.axelor.apps.talent.db.repo.TrainingRegisterTalentRepository;
import com.axelor.apps.talent.db.repo.TrainingSessionRepository;
import com.axelor.apps.talent.db.repo.TrainingSessionTalentRepository;
import com.axelor.apps.talent.service.AppraisalService;
import com.axelor.apps.talent.service.AppraisalServiceImpl;
import com.axelor.apps.talent.service.JobApplicationService;
import com.axelor.apps.talent.service.JobApplicationServiceImpl;
import com.axelor.apps.talent.service.JobPositionService;
import com.axelor.apps.talent.service.JobPositionServiceImpl;
import com.axelor.apps.talent.service.MailAccountServiceTalentImpl;
import com.axelor.apps.talent.service.TrainingRegisterService;
import com.axelor.apps.talent.service.TrainingRegisterServiceImpl;
import com.axelor.apps.talent.service.TrainingSessionService;
import com.axelor.apps.talent.service.TrainingSessionServiceImpl;

public class TalentModule extends AxelorModule {

  @Override
  protected void configure() {

    bind(TrainingRegisterRepository.class).to(TrainingRegisterTalentRepository.class);
    bind(TrainingRegisterService.class).to(TrainingRegisterServiceImpl.class);
    bind(TrainingSessionService.class).to(TrainingSessionServiceImpl.class);
    bind(TrainingSessionRepository.class).to(TrainingSessionTalentRepository.class);
    bind(JobPositionRepository.class).to(JobPositionTalentRepository.class);
    bind(JobApplicationService.class).to(JobApplicationServiceImpl.class);
    bind(JobApplicationRepository.class).to(JobApplicationTalentRepository.class);
    bind(MailAccountServiceBaseImpl.class).to(MailAccountServiceTalentImpl.class);
    bind(JobPositionService.class).to(JobPositionServiceImpl.class);
    bind(AppraisalService.class).to(AppraisalServiceImpl.class);
  }
}
