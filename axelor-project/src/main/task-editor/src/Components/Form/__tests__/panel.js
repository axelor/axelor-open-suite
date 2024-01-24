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
import React from 'react';
import { mount } from 'enzyme';
import { Panel, PanelItem } from '../panel';
import withRoot from '../../../withRoot';
import Flex from '../../../components/flex';
import Typography from '@material-ui/core/Typography';

describe('Spacer Component', () => {
  let wrapper, PanelComponent;
  beforeEach(() => {
    PanelComponent = withRoot(() => (
      <Panel title="Personal">
        <Flex>
          <PanelItem span="4">
            <Panel>
              <Flex>
                <PanelItem span="12">
                  <div />
                </PanelItem>
              </Flex>
            </Panel>
          </PanelItem>
        </Flex>
      </Panel>
    ));
    wrapper = mount(<PanelComponent />);
  });

  it('should render Panel', () => {
    expect(wrapper.find(Panel).length).toBe(2);
    expect(wrapper.find(PanelItem).length).toBe(2);
    expect(wrapper.find(Typography).props().children).toBe('Personal');
  });
});
