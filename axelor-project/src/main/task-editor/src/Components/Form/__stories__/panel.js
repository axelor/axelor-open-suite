import React, { useState } from 'react';
import { storiesOf } from '@storybook/react';

import withRoot from '../../../withRoot';
import { Panel, PanelItem } from '../panel';
import TextField from '../input/input';
import ManyToOne from '../many-to-one';
import DateTimePicker from '../input/datetime-picker';
import Flex from '../../../components/flex';
import { action } from '@storybook/addon-actions';

const titles = [
  { id: 1, code: 'mr', name: 'Mr' },
  { id: 2, code: 'miss', name: 'Miss' },
  { id: 3, code: 'dr', name: 'Dr' },
];

function fetchTitle({ search }) {
  return Promise.resolve(titles.filter(i => i.name.toLowerCase().includes(search.toLowerCase())));
}

storiesOf('Form | Panel', module).add('Default', () => {
  const Wrapper = withRoot(() => (
    <Panel>
      <Flex>
        <PanelItem span="12">
          <Panel title="Personal">
            <Flex>
              <PanelItem span="4">
                <Panel>
                  <Flex>
                    <PanelItem span="12" />
                  </Flex>
                </Panel>
              </PanelItem>
              <PanelItem span="8">
                <Panel>
                  <Flex>
                    <PanelItem span="12">
                      <Panel>
                        <Flex>
                          <PanelItem span="2">
                            <ManyToOne
                              name="title"
                              title="Title"
                              placeholder="title"
                              onChange={action('onChange')}
                              fetchAPI={fetchTitle}
                              optionValueKey="id"
                              optionLabelKey="name"
                              isSearchable={true}
                            />
                          </PanelItem>
                          <PanelItem span="5">
                            <TextField name="firstName" title="First Name" onChange={action('onChange')} />
                          </PanelItem>
                          <PanelItem span="5">
                            <TextField name="lastName" title="Last Name" onChange={action('onChange')} />
                          </PanelItem>
                        </Flex>
                      </Panel>
                    </PanelItem>
                    <PanelItem>
                      <DateTimePicker name="dob" title="DOB" format={'DD/MM/YYYY'} onChange={action('onChange')} />
                    </PanelItem>
                  </Flex>
                </Panel>
              </PanelItem>
            </Flex>
          </Panel>
        </PanelItem>
      </Flex>
    </Panel>
  ));
  return <Wrapper />;
});
