/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2016 Axelor (<http://axelor.com>).
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
import com.axelor.apps.talent.db.repo.TrainingRegisterRepository;
import com.axelor.apps.talent.db.repo.TrainingRegisterTalentRepository;
import com.axelor.apps.talent.db.repo.TrainingSessionRepository;
import com.axelor.apps.talent.db.repo.TrainingSessionTalentRepository;
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
	}

}
