/*
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
package com.axelor.apps.tool;

import java.lang.invoke.MethodHandles;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.google.common.base.Preconditions;
import com.google.inject.persist.Transactional;

public final class ModelTool {
	
	private static final Logger LOG = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	/**
	 * Find models by id from id list and apply given function on each message found.
	 * @param ids list of message ids.
	 * @param consumer to apply on each message.
	 * @return the number of errors append.
	 */
	@Transactional
	public static int apply(Class<? extends Model> modelClass, List<Integer> ids, ThrowConsumer<Model> consumer) {
		Preconditions.checkNotNull(ids, I18n.get("The given ids list cannot be null."));
		Preconditions.checkNotNull(consumer, I18n.get("The given function cannot be null."));
		int error = 0;
		for (Integer id: ids) {
		    try {
				Model model = JPA.find(modelClass, Long.valueOf(id));
				if (model == null) { error++; continue; }
				consumer.accept(model);
			} catch (Exception e) {
				TraceBackService.trace(e);
				error++;
			} finally {
		    	JPA.clear();
			}
		}
		return error;
	}

}
