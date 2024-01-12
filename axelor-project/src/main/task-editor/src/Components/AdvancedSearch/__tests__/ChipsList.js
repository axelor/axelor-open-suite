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
import React from 'react';
import { mount } from 'enzyme';
import { List, Chip, IconButton } from '@material-ui/core';

import ChipsList from '../ChipsList';
import withRoot from '../../../withRoot';

const customeFilter = [
  {
    createdBy: { code: 'admin', name: 'Administrator', id: 1, $version: 0 },
    createdOn: '2019-07-03T07:07:26.137Z',
    filterCustom: {
      operator: 'and',
      criteria: [
        {
          fieldName: 'orderAmount',
          operator: '>=',
          value: '10000',
          $new: true,
        },
      ],
    },
    filterView: 'filter-sales',
    filters: '',
    id: 1,
    name: 'amount_greater_than_10k',
    selected: false,
    shared: false,
    title: 'amount greater than 10k',
    updatedBy: null,
    updatedOn: null,
    user: { code: 'admin', name: 'Administrator', id: 1, $version: 0 },
    version: 0,
  },
  {
    createdBy: { code: 'admin', name: 'Administrator', id: 1, $version: 0 },
    createdOn: '2019-07-03T11:54:34.238Z',
    filterCustom: {
      operator: 'and',
      criteria: [
        {
          fieldName: 'confirmDate',
          operator: 'between',
          value: '2019-06-30T18:30:00.000Z',
          value2: '2019-07-10T18:29:59.999Z',
        },
      ],
    },
    filterView: 'filter-sales',
    filters: '',
    id: 2,
    name: 'future_date',
    selected: false,
    shared: true,
    title: 'future date',
    updatedBy: { code: 'admin', name: 'Administrator', id: 1, $version: 0 },
    updatedOn: '2019-07-04T06:48:03.148Z',
    user: { code: 'admin', name: 'Administrator', id: 1, $version: 0 },
    version: 1,
  },
  {
    createdBy: { code: 'admin', name: 'Administrator', id: 1, $version: 0 },
    createdOn: '2019-07-03T11:54:34.238Z',
    filterCustom: {
      operator: 'and',
      criteria: [
        {
          fieldName: 'confirmDate',
          operator: 'between',
          value: '2019-06-30T18:30:00.000Z',
          value2: '2019-07-10T18:29:59.999Z',
        },
      ],
    },
    filterView: 'filter-sales',
    filters: '',
    id: 3,
    name: 'future_date',
    selected: false,
    shared: true,
    title: 'future date',
    updatedBy: { code: 'admin', name: 'Administrator', id: 1, $version: 0 },
    updatedOn: '2019-07-04T06:48:03.148Z',
    user: { code: 'admin', name: 'Administrator', id: 1, $version: 0 },
    version: 1,
  },
];

describe('Advance Search', () => {
  let wrapper, ChipsListComponent, onDelete;
  beforeEach(() => {
    onDelete = jest.fn();
    ChipsListComponent = withRoot(props => <ChipsList {...props} />);

    wrapper = mount(<ChipsListComponent value={customeFilter.map(f => f.id)} onDelete={onDelete} />);
  });

  it('should render ChipList', () => {
    expect(wrapper.find(List).length).toBe(1);
    expect(wrapper.find(Chip).length).toBe(1);
    expect(wrapper.find(IconButton).length).toBe(2);
  });

  it('shpuld render chip', () => {
    expect(wrapper.find(Chip).length).toBe(1);
    expect(wrapper.find(Chip).props().label).toBe('Filters 3');

    wrapper = mount(<ChipsListComponent value={[customeFilter[0].id]} filters={customeFilter} />);
    expect(wrapper.find(Chip).length).toBe(1);
    expect(wrapper.find(Chip).props().label).toBe(customeFilter[0].title);

    wrapper = mount(<ChipsListComponent value={[null]} filters={customeFilter} />);
    expect(wrapper.find(Chip).length).toBe(1);
    expect(wrapper.find(Chip).props().label).toBe('Custom');

    wrapper = mount(<ChipsListComponent value={[]} filters={customeFilter} />);
    expect(wrapper.find(Chip).length).toBe(1);
    expect(wrapper.find(Chip).props().label).toBe('');
  });

  it('should call onDelte', () => {
    wrapper.find(Chip).props().onDelete();
    expect(onDelete).toHaveBeenCalled();
  });
});
