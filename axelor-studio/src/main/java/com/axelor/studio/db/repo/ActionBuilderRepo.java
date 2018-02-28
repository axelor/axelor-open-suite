/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2017 Axelor (<http://axelor.com>).
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
package com.axelor.studio.db.repo;

import com.axelor.meta.db.MetaAction;
import com.axelor.meta.db.repo.MetaActionRepository;
import com.axelor.studio.db.ActionBuilder;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ActionBuilderRepo extends ActionBuilderRepository {

	@Inject
	private MetaActionRepository metaActionRepo;

	@Transactional
	@Override
	public void remove(ActionBuilder actionBuilder) {

		super.remove(actionBuilder);

		MetaAction metaAction = metaActionRepo.findByName(actionBuilder
				.getName());
		if (metaAction != null) {
			metaAction.setRemoveAction(true);
			metaActionRepo.save(metaAction);
		}

		metaAction = metaActionRepo.findByName(actionBuilder.getName()
				+ "-assign");
		if (metaAction != null) {
			metaAction.setRemoveAction(true);
			metaActionRepo.save(metaAction);
		}
	}

}
