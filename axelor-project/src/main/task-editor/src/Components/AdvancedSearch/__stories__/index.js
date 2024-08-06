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
import React, { useState } from 'react';
import { storiesOf } from '@storybook/react';

import withRoot from '../../../withRoot';
import Search from '../index';
import moment from 'moment';

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

const domains = [
  { id: 100, domain: 'self.confirmed = true', title: 'Confirmed' },
  {
    id: 101,
    domain: 'self.confirmed = false OR self.confirmed is null',
    title: 'Not Confirmed',
  },
  {
    id: 102,
    domain: 'self.totalAmount >= 1000',
    title: 'High Value (>= 1000)',
  },
];

const filters = [
  {
    createdBy: { code: 'admin', name: 'Administrator', id: 1, $version: 0 },
    createdOn: '2019-07-03T07:07:26.137Z',
    filterCustom:
      '{"operator":"or","criteria":[{"fieldName":"orderAmount","operator":">=","value":"10000","$new":true}]}',
    filterView: 'filter-sales',
    filters: '',
    id: 1,
    name: 'amount_greater_than_10k',
    selected: false,
    shared: false,
    title: 'amount greater than 10k',
    updatedBy: null,
    updatedOn: null,
  },
  {
    createdBy: { code: 'admin', name: 'Administrator', id: 1, $version: 0 },
    createdOn: '2019-07-08T14:03:13.748Z',
    filterCustom:
      '{"operator":"and","criteria":[{"fieldName":"confirmDate","operator":"between","value":"2019-06-30T18:30:00.000Z","value2":"2019-07-10T18:29:59.999Z","$new":true}]}',
    filterView: 'filter-sales',
    filters: '',
    id: 4,
    name: 'future_date',
    selected: false,
    shared: false,
    title: 'future date',
    updatedBy: null,
    updatedOn: null,
  },
];

function SearchWrapper(props) {
  const [customFilter, setCustomFilter] = useState(filters);
  const [value, setValue] = useState({ selected: [] });

  function onSave(filter) {
    const { id, filterCustom, filterView, filters, name, isShare, title } = filter;
    const ind = customFilter.findIndex(x => x.id === id);
    if (ind > -1) {
      customFilter[ind] = filter;
      setCustomFilter([...customFilter]);
      return { ...customFilter[ind] };
    } else {
      const { operator, criteria } = JSON.parse(filterCustom);
      const record = {
        createdBy: { code: 'admin', name: 'Administrator', id: 1, $version: 0 },
        createdOn: moment().format(),
        filterCustom: JSON.stringify({
          operator: operator,
          criteria: criteria.map(c => {
            return {
              fieldName: c.fieldName,
              operator: c.operator,
              value: c.value,
            };
          }),
        }),
        filterView: filterView,
        filters: filters,
        id: Boolean(id) ? id : Math.random(),
        name: name,
        selected: false,
        shared: isShare,
        title: title,
        updatedBy: null,
        updatedOn: null,
        user: { code: 'admin', name: 'Administrator', id: 1, $version: 0 },
        version: 0,
      };
      customFilter.push(record);
      setCustomFilter([...customFilter]);
      return record;
    }
  }

  function onDelete(filter) {
    const ind = customFilter.findIndex(x => x.id === filter.id);
    if (ind > -1) {
      customFilter.splice(ind, 1);
      setCustomFilter([...customFilter]);
    }
  }

  return (
    <Search
      {...props}
      value={value}
      filters={customFilter}
      onSave={onSave}
      onDelete={onDelete}
      handleChange={v => {
        setValue(v);
      }}
    />
  );
}

storiesOf('Advanced Search | Search', module).add('Default', () => {
  const Wrapper = withRoot(() => <SearchWrapper name="search" fields={options} domain={domains} />);
  return <Wrapper />;
});
