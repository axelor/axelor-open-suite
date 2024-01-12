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

import Switch from '../switch';

const store = new Store({
  value: null,
});

function SwitchWrapper(props) {
  return (
    <State store={store}>
      {state => <Switch {...props} value={state.value} onChange={e => store.set({ value: e.target.checked })} />}
    </State>
  );
}

storiesOf('Form | Switch', module)
  .add('Default', () => <SwitchWrapper name="test" title="Test" />)
  .add('Read Only', () => <SwitchWrapper name="test" title="Test" readOnly={true} />);
