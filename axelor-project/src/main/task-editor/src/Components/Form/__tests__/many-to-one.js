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
import ManyToOne from '../many-to-one';
import Selection from '../selection';
import withRoot from '../../../withRoot';

describe('ManyToOne Component', () => {
  let wrapper, ManyToOneComponent;
  beforeEach(() => {
    ManyToOneComponent = withRoot(() => <ManyToOne />);
    wrapper = mount(<ManyToOneComponent />);
  });

  it('should render ManyToOne', () => {
    expect(wrapper.find(ManyToOne).length).toBe(1);
    expect(wrapper.find(Selection).length).toBe(1);
  });
});
