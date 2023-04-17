import React from 'react';
import { storiesOf } from '@storybook/react';
import { action } from '@storybook/addon-actions';
import { State, Store } from '@sambego/storybook-state';
import withRoot from '../../../withRoot';
import ManyToOne from '../many-to-one';

const companies = [
  { id: 1, code: 'myCompany', name: 'My Company' },
  { id: 2, code: 'axelorIndia', name: 'Axelor India' },
  { id: 3, code: 'axelorSAS', name: 'Axelor SAS' },
];

const store = new Store({
  value: companies[0],
});

function fetchCompany({ search }) {
  return Promise.resolve(companies.filter(i => i.name.toLowerCase().includes(search.toLowerCase())));
}

function ManyToOneWrapper(props) {
  return (
    <State store={store}>
      {state => <ManyToOne {...props} value={state.value} onChange={value => store.set({ value })} />}
    </State>
  );
}

storiesOf('Form | ManyToOne', module)
  .add('Default', () => {
    const Wrapper = withRoot(() => (
      <ManyToOneWrapper
        name="company"
        title="Company"
        placeholder="Company"
        onChange={action('onChange')}
        fetchAPI={fetchCompany}
        optionValueKey="id"
        optionLabelKey="name"
        isSearchable={true}
      />
    ));
    return <Wrapper />;
  })
  .add('Read Only', () => {
    const Wrapper = withRoot(() => (
      <ManyToOneWrapper
        name="company"
        title="Company"
        value={companies[1]}
        onChange={action('onChange')}
        fetchAPI={fetchCompany}
        optionValueKey="id"
        optionLabelKey="name"
        readOnly={true}
      />
    ));
    return <Wrapper />;
  });
