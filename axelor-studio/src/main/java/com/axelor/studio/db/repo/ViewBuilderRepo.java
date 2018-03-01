/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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

import javax.validation.ValidationException;

import com.axelor.i18n.I18n;
import com.axelor.meta.db.MetaView;
import com.axelor.meta.db.repo.MetaActionRepository;
import com.axelor.meta.db.repo.MetaViewRepository;
import com.axelor.studio.db.ViewBuilder;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ViewBuilderRepo extends ViewBuilderRepository {

	@Inject
	MetaViewRepository metaViewRepo;

	@Inject
	MetaActionRepository metaActionRepo;

	@Override
	public ViewBuilder save(ViewBuilder viewBuilder) throws ValidationException {

		if (viewBuilder.getName().contains(" ")) {
			throw new ValidationException(
					I18n.get("Name must not contains space"));
		}

		return super.save(viewBuilder);
	}

	@Override
	@Transactional
	public void remove(ViewBuilder viewBuilder) {

		MetaView metaView = viewBuilder.getMetaViewGenerated();
		if (metaView != null) {
			metaView.setRemoveView(true);
			metaViewRepo.save(metaView);
		}

		// String onSave = viewBuilder.getOnSave();
		//
		// if(onSave != null){
		// List<MetaAction> metaActions = metaActionRepo.all()
		// .filter("self.name in (?1)", Arrays.asList(onSave.split(",")))
		// .fetch();
		// for(MetaAction action : metaActions){
		// action.setRemoveAction(true);
		// metaActionRepo.save(action);
		// }
		// }

		super.remove(viewBuilder);
	}

}
