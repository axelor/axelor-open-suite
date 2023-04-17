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
