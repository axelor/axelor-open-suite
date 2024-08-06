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

import DateTimePicker from '../datetime-picker';
import withRoot from '../../../../withRoot';

const store = new Store({
  value: new Date(),
});

const DateWrapper = withRoot(function DateWrapper(props) {
  return (
    <State store={store}>
      {state => <DateTimePicker {...props} value={state.value} onChange={value => store.set({ value })} />}
    </State>
  );
});

function TimeWrapper(props) {
  return DateWrapper({ ...props, type: 'time' });
}

function DateTimeWrapper(props) {
  return DateWrapper({ ...props, type: 'datetime' });
}

storiesOf('Form |Date', module)
  .add('Default', () => <DateWrapper name="dob" title="DOB" />)
  .add('Read Only', () => <DateWrapper name="dob" title="DOB" readOnly={true} />);

storiesOf('Form |Time', module)
  .add('Default', () => {
    const Wrapper = withRoot(() => <TimeWrapper name="dob" title="DOB" />);
    return <Wrapper />;
  })
  .add('Read Only', () => {
    const Wrapper = withRoot(() => <TimeWrapper name="dob" title="DOB" readOnly={true} />);
    return <Wrapper />;
  });

storiesOf('Form |DateTime', module)
  .add('Default', () => <DateTimeWrapper name="dob" title="DOB" />)
  .add('Read Only', () => <DateTimeWrapper name="dob" title="DOB" readOnly={true} />);
