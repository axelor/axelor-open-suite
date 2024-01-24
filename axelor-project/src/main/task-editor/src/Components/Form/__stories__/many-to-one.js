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
import ManyToOne from '../many-to-one';

const companies = [
  { id: 1, code: 'myCompany', name: 'My Company' },
  { id: 2, code: 'axelorIndia', name: 'Axelor India' },
  { id: 3, code: 'axelorSAS', name: 'Axelor SAS' },
];

const store = new Store({
  value: companies[0],
});

function fetchCompany({ search }) {
  return Promise.resolve(companies.filter(i => i.name.toLowerCase().includes(search.toLowerCase())));
}

function ManyToOneWrapper(props) {
  return (
    <State store={store}>
      {state => <ManyToOne {...props} value={state.value} onChange={value => store.set({ value })} />}
    </State>
  );
}

storiesOf('Form | ManyToOne', module)
  .add('Default', () => {
    const Wrapper = withRoot(() => (
      <ManyToOneWrapper
        name="company"
        title="Company"
        placeholder="Company"
        onChange={action('onChange')}
        fetchAPI={fetchCompany}
        optionValueKey="id"
        optionLabelKey="name"
        isSearchable={true}
      />
    ));
    return <Wrapper />;
  })
  .add('Read Only', () => {
    const Wrapper = withRoot(() => (
      <ManyToOneWrapper
        name="company"
        title="Company"
        value={companies[1]}
        onChange={action('onChange')}
        fetchAPI={fetchCompany}
        optionValueKey="id"
        optionLabelKey="name"
        readOnly={true}
      />
    ));
    return <Wrapper />;
  });
