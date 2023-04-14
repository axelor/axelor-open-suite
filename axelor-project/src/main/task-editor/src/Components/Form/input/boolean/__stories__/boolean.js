import React from 'react';
import { storiesOf } from '@storybook/react';
import { State, Store } from '@sambego/storybook-state';

import Boolean from '../boolean';

const store = new Store({
  value: null,
});

function BooleanWrapper(props) {
  return (
    <State store={store}>
      {state => <Boolean {...props} value={state.value} onChange={e => store.set({ value: e.target.value })} />}
    </State>
  );
}

storiesOf('Form | Boolean', module)
  .add('Default', () => <BooleanWrapper name="test" title="Test" />)
  .add('Read Only', () => <BooleanWrapper name="test" title="Test" readOnly={true} />);
