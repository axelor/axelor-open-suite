import React from 'react';
import { storiesOf } from '@storybook/react';
import { action } from '@storybook/addon-actions';
import { State, Store } from '@sambego/storybook-state';

import withRoot from '../../../withRoot';
import ManyToMany from '../many-to-many';

const companies = [
  { id: 1, code: 'myCompany', name: 'My Company' },
  { id: 2, code: 'axelorIndia', name: 'Axelor India' },
  { id: 3, code: 'axelorSAS', name: 'Axelor SAS' },
];

const store = new Store({
  value: [companies[0]],
});

function fetchCompany({ search }) {
  return Promise.resolve(companies.filter(i => i.name.toLowerCase().includes(search.toLowerCase())));
}

function ManyToManyWrapper(props) {
  return (
    <State store={store}>
      {state => <ManyToMany {...props} value={state.value} onChange={value => store.set({ value })} />}
    </State>
  );
}

storiesOf('Form | ManyToMany', module)
  .add('Default', () => {
    const Wrapper = withRoot(() => (
      <ManyToManyWrapper
        name="company"
        title="Company"
        onChange={action('onChange')}
        fetchAPI={fetchCompany}
        optionValueKey="id"
        optionLabelKey="name"
        isMulti={true}
        isSearchable={true}
      />
    ));
    return <Wrapper />;
  })
  .add('Read Only', () => {
    const Wrapper = withRoot(() => (
      <ManyToManyWrapper
        name="company"
        title="Company"
        onChange={action('onChange')}
        fetchAPI={fetchCompany}
        optionValueKey="id"
        optionLabelKey="name"
        isMulti={true}
        isSearchable={true}
        readOnly={true}
      />
    ));
    return <Wrapper />;
  });
