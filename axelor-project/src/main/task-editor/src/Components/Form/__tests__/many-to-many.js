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
import ManyToMany from '../many-to-many';
import Selection from '../selection';
import withRoot from '../../../withRoot';

describe('ManyToMany Component', () => {
  let wrapper, ManyToManyComponent;
  beforeEach(() => {
    ManyToManyComponent = withRoot(() => <ManyToMany />);
    wrapper = mount(<ManyToManyComponent />);
  });

  it('should render ManyToOne', () => {
    expect(wrapper.find(ManyToMany).length).toBe(1);
    expect(wrapper.find(Selection).length).toBe(1);
  });
});
