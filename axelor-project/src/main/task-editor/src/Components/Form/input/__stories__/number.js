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
