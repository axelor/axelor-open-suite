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
import { act } from 'react-dom/test-utils';
import { mount } from 'enzyme';
import {
  IconButton,
  Popper,
  Paper,
  Chip,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogContentText,
  DialogActions,
  Button,
} from '@material-ui/core';

import AdvanceSearch, { AdvanceFilterInput, FilterList, FilterListItem } from '../index';
import FilterEditor from '../filterEditor';
import ChipsList from '../ChipsList';
import { SimpleButton, ButtonLink } from '../common';
import withRoot from '../../../withRoot';

const options = [
  {
    name: 'confirmDate',
    type: 'DATE',
    title: 'Confirm date',
  },
  {
    defaultValue: '0',
    precision: 20,
    name: 'totalAmount',
    scale: 2,
    type: 'DECIMAL',
    title: 'Total amount',
  },
  {
    defaultValue: '0',
    precision: 20,
    name: 'orderAmount',
    scale: 4,
    type: 'INTEGER',
    title: 'Amount',
  },
  {
    name: 'notes',
    type: 'TEXT',
    title: 'Notes',
  },
  {
    sequence: true,
    readonly: true,
    name: 'name',
    type: 'STRING',
    title: 'Name',
  },
  {
    defaultValue: '0',
    precision: 20,
    name: 'taxAmount',
    scale: 4,
    type: 'DECIMAL',
    title: 'Tax amount',
  },
  {
    defaultValue: false,
    name: 'confirmed',
    type: 'BOOLEAN',
    title: 'Confirmed',
  },
  {
    name: 'orderDate',
    type: 'DATE',
    title: 'Order date',
    required: true,
  },
  {
    targetSearch: [],
    name: 'orderItems',
    type: 'ONE_TO_MANY',
    title: 'Items',
    mappedBy: 'order',
    target: 'com.axelor.sale.db.OrderLine',
  },
  {
    targetName: 'name',
    targetSearch: ['name', 'code'],
    name: 'circles',
    type: 'MANY_TO_MANY',
    title: 'Circles',
    target: 'com.axelor.contact.db.Circle',
  },
  {
    name: 'status',
    enumType: 'com.axelor.sale.db.OrderStatus',
    selectionList: [
      {
        value: 'DRAFT',
        data: {
          value: 'draft',
        },
        title: 'Draft',
      },
      {
        value: 'OPEN',
        data: {
          value: 'open',
        },
        title: 'Open',
      },
      {
        value: 'CLOSED',
        data: {
          value: 'closed',
        },
        title: 'Closed',
      },
      {
        value: 'CANCELED',
        data: {
          value: 'canceled',
        },
        title: 'Canceled',
      },
    ],
    type: 'ENUM',
    title: 'Status',
  },
  {
    targetName: 'fullName',
    targetSearch: ['fullName', 'firstName', 'lastName'],
    name: 'customer',
    type: 'MANY_TO_ONE',
    title: 'Customer',
    required: true,
    target: 'com.axelor.contact.db.Contact',
  },
];

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

const filters = [
  { domain: 'self.confirmed = true', title: 'Confirmed' },
  {
    domain: 'self.confirmed = false OR self.confirmed is null',
    title: 'Not Confirmed',
  },
  { domain: 'self.totalAmount >= 1000', title: 'High Value (>= 1000)' },
];

global.document.createRange = () => ({
  setStart: () => {},
  setEnd: () => {},
  commonAncestorContainer: {
    nodeName: 'BODY',
    ownerDocument: document,
  },
});

describe('Advance Search filter widget', () => {
  let wrapper, SearchComponent, onSave, onDelete;
  const onOpen = () => {
    act(() => {
      wrapper.find(IconButton).at(0).props().onClick();
    });
  };

  function AdvanceSearchWrapper(props) {
    const [value, setValue] = React.useState({ selected: [] });
    return <AdvanceSearch {...props} value={value} handleChange={e => setValue(e)} />;
  }

  beforeEach(() => {
    onSave = jest.fn();
    onDelete = jest.fn();
    handleChange = jest.fn();
    SearchComponent = withRoot(() => (
      <AdvanceSearchWrapper
        name="search"
        fields={options}
        domain={filters}
        filters={customeFilter}
        onSave={onSave}
        onDelete={onDelete}
      />
    ));

    wrapper = mount(<SearchComponent />);
  });

  it('should render AdvanceFilterInput', () => {
    expect(wrapper.find(AdvanceFilterInput).length).toBe(1);
    onOpen();
    wrapper.update();
    expect(wrapper.find(Popper).length).toBe(1);
    expect(wrapper.find(Paper).length).toBe(1);
    expect(wrapper.find(FilterEditor).length).toBe(1);
    expect(wrapper.find(FilterList).length).toBe(1);
  });

  it('should render FilterListItem', () => {
    onOpen();
    wrapper.update();
    expect(wrapper.find(FilterListItem).length).toBe(filters.length);
  });

  it('should call handleChange', () => {
    onOpen();
    wrapper.update();
    act(() => {
      wrapper.find(FilterList).props().onClick(customeFilter[0], false);
    });
    wrapper.update();
    expect(wrapper.find(FilterList).at(0).props().active).toEqual([customeFilter[0].id]);
  });

  it('should call onSave', () => {
    onOpen();
    wrapper.update();
    act(() => {
      wrapper.find(SimpleButton).filter('.save').props().onClick();
    });
    expect(onSave).toHaveBeenCalled();
  });

  it('should call onRemove', () => {
    onOpen();
    wrapper.update();
    act(() => {
      wrapper.find(SimpleButton).filter('.delete').props().onClick();
    });
    wrapper.update();
    expect(wrapper.find(Dialog).length).toBe(1);
    expect(wrapper.find(DialogTitle).length).toBe(1);
    expect(wrapper.find(DialogContent).length).toBe(1);
    expect(wrapper.find(DialogContentText).length).toBe(1);
    expect(wrapper.find(DialogActions).length).toBe(1);
    act(() => {
      wrapper.find(Button).filter('.confirm-delete').props().onClick();
    });
    expect(onDelete).toHaveBeenCalled();
  });

  it('should call applyFiltersClick', () => {
    onOpen();
    wrapper.update();
    act(() => {
      wrapper.find(FilterListItem).at(0).props().onChange(true, customeFilter[0]);
    });
    act(() => {
      wrapper.find(ButtonLink).filter('.apply-filter').props().onClick();
    });
    wrapper.update();
    expect(wrapper.find(ChipsList).length).toBe(1);
    expect(wrapper.find(Chip).props().label).toBe(customeFilter[0].title);
  });

  it('should call onFilterCheck', () => {
    onOpen();
    wrapper.update();

    act(() => {
      wrapper.find(FilterListItem).at(0).props().onChange(true, customeFilter[0]);
    });
    wrapper.update();
    expect(wrapper.find(FilterListItem).at(0).props().isChecked).toBe(true);

    act(() => {
      wrapper.find(FilterListItem).at(0).props().onChange(false, customeFilter[0]);
    });
    wrapper.update();
    expect(wrapper.find(FilterListItem).at(0).props().isChecked).toBe(false);
  });

  it('should call applyCustomFilters', () => {
    onOpen();
    wrapper.update();
    act(() => {
      wrapper
        .find(FilterEditor)
        .props()
        .applyFiltersClick({
          operator: 'and',
          criteria: [
            {
              fieldName: 'orderAmount',
              operator: '>=',
              value: '10000',
              $new: true,
            },
            {
              fieldName: 'orderAmount',
              operator: '>=',
              value: '10000',
              $new: true,
            },
          ],
        });
    });
    wrapper.update();
    expect(wrapper.find(Chip).props().label).toBe('Custom');
  });
});
