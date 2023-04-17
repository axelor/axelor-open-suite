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
