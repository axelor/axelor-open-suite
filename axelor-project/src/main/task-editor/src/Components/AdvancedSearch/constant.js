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
export const mapOperator = {
  '=': 'eq',
  '!=': 'ne',
  '>': 'gt',
  '>=': 'ge',
  '<': 'lt',
  '<=': 'le',
  in: 'in',
  notIn: 'notIn',
  isNull: 'isNull',
  notNull: 'notNull',
  like: 'like',
  notLike: 'notLike',
  is: 'is',
  between: 'between',
  notBetween: 'notBetween',
};

export let operators_by_type = {
  enum: ['=', '!=', 'isNull', 'notNull'],
  text: ['like', 'notLike', 'isNull', 'notNull'],
  string: ['=', '!=', 'like', 'notLike', 'isNull', 'notNull'],
  integer: ['=', '!=', '>=', '<=', '>', '<', 'between', 'notBetween', 'isNull', 'notNull'],
  boolean: ['isTrue', 'isFalse'],
};

export const operators = [
  { name: '=', title: 'equals' },
  { name: '!=', title: 'not equal' },
  { name: '>', title: 'greater than' },
  { name: '>=', title: 'greater or equal' },
  { name: '<', title: 'less than' },
  { name: '<=', title: 'less or equal' },
  { name: 'in', title: 'in' },
  { name: 'between', title: 'between' },
  { name: 'notBetween', title: 'not Between' },
  { name: 'notIn', title: 'not in' },
  { name: 'isNull', title: 'is null' },
  { name: 'notNull', title: 'is not null' },
  { name: 'like', title: 'contains' },
  { name: 'notLike', title: "doesn't contain" },
  { name: 'isTrue', title: 'true' },
  { name: 'isFalse', title: 'false' },
];
