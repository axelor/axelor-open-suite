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

import NumberField from '../number';

const store = new Store({
  value: '100',
});

function NumberFieldWrapper(props) {
  return (
    <State store={store}>
      {state => <NumberField {...props} value={state.value} onChange={value => store.set({ value })} />}
    </State>
  );
}

storiesOf('Form | Number', module)
  .add('Default', () => <NumberFieldWrapper name="salary" title="Salary" />)
  .add('Decimal', () => <NumberFieldWrapper name="salary" title="Salary" type="decimal" />)
  .add('Read Only', () => (
    <NumberFieldWrapper
      name="salary"
      title="Salary"
      readOnly={true}
      value="123456000.00"
      type="decimal"
      customeFormat={{
        thousandSeparator: '-',
      }}
    />
  ));
