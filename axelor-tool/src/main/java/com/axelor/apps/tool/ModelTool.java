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

import java.util.Collection;

import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.google.common.base.Preconditions;

public final class ModelTool {

    private ModelTool() {
    }

    /**
     * Apply consumer to each record found from collection of IDs.
     * 
     * @param ids
     *            collection of IDs.
     * @param consumer
     *            to apply on each record.
     * @return the number of errors that occurred.
     */
    public static <T extends Model> int apply(Class<? extends Model> modelClass, Collection<? extends Number> ids,
            ThrowConsumer<T> consumer) {

        Preconditions.checkNotNull(ids, I18n.get("The collection of IDs cannot be null."));
        Preconditions.checkNotNull(consumer, I18n.get("The consumer cannot be null."));

        int errorCount = 0;

        for (Number id : ids) {
            try {
                if (id != null) {
                    Model model = JPA.find(modelClass, id.longValue());
                    if (model != null) {
                        consumer.accept((T) model);
                        continue;
                    }
                }

                throw new AxelorException(modelClass, IException.NO_VALUE, I18n.get("Cannot find record #%s"),
                        String.valueOf(id));
            } catch (Exception e) {
                ++errorCount;
                TraceBackService.trace(e);
            } finally {
                JPA.clear();
            }
        }

        return errorCount;
    }

}
