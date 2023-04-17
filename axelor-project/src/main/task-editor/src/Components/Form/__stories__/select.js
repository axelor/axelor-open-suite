import React from 'react';
import { storiesOf } from '@storybook/react';
import { State, Store } from '@sambego/storybook-state';
import withRoot from '../../../withRoot';
import Select from '../selection';

const store = new Store({
  value: 'black',
  value1: 'black,white',
});

const options = [
  { title: 'Black', value: 'black' },
  { title: 'White', value: 'white' },
  { title: 'Red', value: 'red' },
  { title: 'Yellow', value: 'yellow' },
  { title: 'Purple', value: 'purple' },
];

function SelectWrapper(props) {
  const { isMulti } = props;
  return (
    <State store={store}>
      {state => (
        <Select
          {...props}
          value={isMulti ? state.value1 : state.value}
          onChange={value => store.set(isMulti ? { value1: value } : { value })}
        />
      )}
    </State>
  );
}
storiesOf('Form | Select', module)
  .add('Default', () => {
    const Wrapper = withRoot(() => (
      <SelectWrapper
        name="color"
        title="Color"
        placeholder="Color"
        options={options}
        optionLabelKey="title"
        optionValueKey="value"
      />
    ));
    return <Wrapper />;
  })
  .add('MultiSelect', () => {
    const Wrapper = withRoot(() => (
      <SelectWrapper
        name="color"
        title="Color"
        placeholder="Color"
        options={options}
        isMulti={true}
        isSearchable={true}
        optionLabelKey="title"
        optionValueKey="value"
        closeMenuOnSelect={false}
      />
    ));
    return <Wrapper />;
  })
  .add('Read Only', () => {
    const Wrapper = withRoot(() => (
      <SelectWrapper
        name="color"
        title="Color"
        placeholder="Color"
        options={options}
        readOnly={true}
        optionLabelKey="title"
        optionValueKey="value"
      />
    ));
    return <Wrapper />;
  });
