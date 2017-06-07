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
package com.axelor.studio.db.repo;

import javax.validation.ValidationException;

import com.axelor.i18n.I18n;
import com.axelor.meta.db.MetaView;
import com.axelor.meta.db.repo.MetaViewRepository;
import com.axelor.studio.db.ViewBuilder;
import com.axelor.studio.service.builder.ViewBuilderService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ViewBuilderRepo extends ViewBuilderRepository {

	@Inject
	private MetaViewRepository metaViewRepo;
	
	@Inject
	private ViewBuilderService viewBuilderService;

	@Override
	public ViewBuilder save(ViewBuilder viewBuilder) throws ValidationException {

		if (viewBuilder.getName().contains(" ")) {
			throw new ValidationException(
					I18n.get("Name must not contains space"));
		}
		
		viewBuilder = super.save(viewBuilder);
		
		viewBuilderService.build(viewBuilder);

		return super.save(viewBuilder);
	}

	@Override
	@Transactional
	public void remove(ViewBuilder viewBuilder) {

		MetaView metaView = viewBuilder.getMetaViewGenerated();
		if (metaView != null) {
			metaViewRepo.remove(metaView);
		}

		super.remove(viewBuilder);
	}

}
