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
import Separator from '../separator';
import Flex from '../../flex';
import withRoot from '../../../withRoot';
import Typography from '@material-ui/core/Typography';
import Divider from '@material-ui/core/Divider';

describe('Spacer Component', () => {
  let wrapper, SeparatorComponent;
  beforeEach(() => {
    SeparatorComponent = withRoot(() => <Separator title="Test" />);
    wrapper = mount(<SeparatorComponent />);
  });

  it('should render flex item', () => {
    expect(wrapper.find(Flex.Item).length).toBe(1);
    expect(wrapper.find(Divider).length).toBe(1);
    expect(wrapper.find(Typography).length).toBe(1);
    expect(wrapper.find(Typography).props().children).toBe('Test');
  });
});
