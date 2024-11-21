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
import NavSelect from '../nav-select';

const options = [
  { value: 'new', title: 'New' },
  { value: 'waiting', title: 'Waiting' },
  { value: 'validate', title: 'Validate' },
  { value: 'complete', title: 'Complete' },
  { value: 'cancel', title: 'Cancel' },
];

const store = new Store({
  value: 'validate',
});

function NavSelectWrapper(props) {
  return (
    <State store={store}>
      {state => <NavSelect {...props} active={state.value} onSelect={value => store.set({ value })} />}
    </State>
  );
}

storiesOf('Form | NavSelect', module)
  .add('Default', () => {
    const Wrapper = withRoot(() => <NavSelectWrapper data={options} />);
    return <Wrapper />;
  })
  .add('Read only', () => {
    const Wrapper = withRoot(() => <NavSelectWrapper data={options} readOnly={true} />);
    return <Wrapper />;
  });
