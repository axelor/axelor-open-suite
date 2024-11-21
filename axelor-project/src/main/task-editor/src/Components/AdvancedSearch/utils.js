/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
import Service from '../../Services/index';

export function searchRecordsByField(field, searchValue, options = {}) {
  let columns = [{ name: field.targetName }];
  if (field.targetSearch) {
    columns = field.targetSearch.map(name => ({ name }));
  }

  return Service.search(field.target, {
    ...options,
    limit: 10,
    offset: 0,
    fields: columns.map(x => x.name),
    data: searchValue
      ? columns.reduce((data, field) => ({ ...data, [field.name]: searchValue }), {
          criteria: undefined,
          operator: undefined,
        })
      : {},
  });
}
