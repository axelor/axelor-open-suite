import React, { useState } from 'react';
import { storiesOf } from '@storybook/react';

import withRoot from '../../../withRoot';
import ChipsList from '../ChipsList';

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

function ChipsListWrapper(props) {
  const [value, setValue] = useState(props.value || '');
  return <ChipsList {...props} value={value} onChange={v => setValue(v)} />;
}

storiesOf('Advanced Search | Chips', module).add('Default', () => {
  const Wrapper = withRoot(() => <ChipsListWrapper value={customeFilter} />);
  return <Wrapper />;
});
