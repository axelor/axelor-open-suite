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
import { State, Store } from '@sambego/storybook-state';
import withRoot from '../../../withRoot';
import Select from '../selection';

const store = new Store({
  value: 'black',
  value1: 'black,white',
});

const options = [
  { title: 'Black', value: 'black' },
  { title: 'White', value: 'white' },
  { title: 'Red', value: 'red' },
  { title: 'Yellow', value: 'yellow' },
  { title: 'Purple', value: 'purple' },
];

function SelectWrapper(props) {
  const { isMulti } = props;
  return (
    <State store={store}>
      {state => (
        <Select
          {...props}
          value={isMulti ? state.value1 : state.value}
          onChange={value => store.set(isMulti ? { value1: value } : { value })}
        />
      )}
    </State>
  );
}
storiesOf('Form | Select', module)
  .add('Default', () => {
    const Wrapper = withRoot(() => (
      <SelectWrapper
        name="color"
        title="Color"
        placeholder="Color"
        options={options}
        optionLabelKey="title"
        optionValueKey="value"
      />
    ));
    return <Wrapper />;
  })
  .add('MultiSelect', () => {
    const Wrapper = withRoot(() => (
      <SelectWrapper
        name="color"
        title="Color"
        placeholder="Color"
        options={options}
        isMulti={true}
        isSearchable={true}
        optionLabelKey="title"
        optionValueKey="value"
        closeMenuOnSelect={false}
      />
    ));
    return <Wrapper />;
  })
  .add('Read Only', () => {
    const Wrapper = withRoot(() => (
      <SelectWrapper
        name="color"
        title="Color"
        placeholder="Color"
        options={options}
        readOnly={true}
        optionLabelKey="title"
        optionValueKey="value"
      />
    ));
    return <Wrapper />;
  });
