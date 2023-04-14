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
