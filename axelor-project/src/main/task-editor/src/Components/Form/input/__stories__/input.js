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
import Input from '../input';

const store = new Store({
  value: '',
});

function InputWrapper(props) {
  return (
    <State store={store}>
      {state => <Input {...props} value={state.value} onChange={value => store.set({ value })} />}
    </State>
  );
}

storiesOf('Form |TextField', module)
  .add('Default', () => <InputWrapper name="name" title="Name" />)
  .add('Multiline', () => <InputWrapper name="name" title="Name" multiline={true} />)
  .add('Inline', () => <InputWrapper name="name" title="Name" inline={true} />)
  .add('Read Only', () => <InputWrapper name="name" title="Name" readOnly={true} />);
