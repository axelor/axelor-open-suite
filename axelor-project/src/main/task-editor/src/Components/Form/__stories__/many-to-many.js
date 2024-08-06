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
import { storiesOf } from '@storybook/react';
import { action } from '@storybook/addon-actions';
import { State, Store } from '@sambego/storybook-state';

import withRoot from '../../../withRoot';
import ManyToMany from '../many-to-many';

const companies = [
  { id: 1, code: 'myCompany', name: 'My Company' },
  { id: 2, code: 'axelorIndia', name: 'Axelor India' },
  { id: 3, code: 'axelorSAS', name: 'Axelor SAS' },
];

const store = new Store({
  value: [companies[0]],
});

function fetchCompany({ search }) {
  return Promise.resolve(companies.filter(i => i.name.toLowerCase().includes(search.toLowerCase())));
}

function ManyToManyWrapper(props) {
  return (
    <State store={store}>
      {state => <ManyToMany {...props} value={state.value} onChange={value => store.set({ value })} />}
    </State>
  );
}

storiesOf('Form | ManyToMany', module)
  .add('Default', () => {
    const Wrapper = withRoot(() => (
      <ManyToManyWrapper
        name="company"
        title="Company"
        onChange={action('onChange')}
        fetchAPI={fetchCompany}
        optionValueKey="id"
        optionLabelKey="name"
        isMulti={true}
        isSearchable={true}
      />
    ));
    return <Wrapper />;
  })
  .add('Read Only', () => {
    const Wrapper = withRoot(() => (
      <ManyToManyWrapper
        name="company"
        title="Company"
        onChange={action('onChange')}
        fetchAPI={fetchCompany}
        optionValueKey="id"
        optionLabelKey="name"
        isMulti={true}
        isSearchable={true}
        readOnly={true}
      />
    ));
    return <Wrapper />;
  });
