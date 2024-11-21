/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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
