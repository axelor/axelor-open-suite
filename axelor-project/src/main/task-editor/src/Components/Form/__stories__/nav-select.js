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
