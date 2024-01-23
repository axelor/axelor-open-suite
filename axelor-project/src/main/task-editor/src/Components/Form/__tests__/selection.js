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
import { act } from 'react-dom/test-utils';
import { mount } from 'enzyme';
import { TextField, Chip, Typography, FormLabel } from '@material-ui/core';
import Select from 'react-select';

import Selection, { NoOptionsMessage, Option, SingleValue } from '../selection';
import withRoot from '../../../withRoot';
import { waitFor } from '../../../test-utils';

jest.mock('../utils.js');

const companies = [
  { id: 1, code: 'myCompany', name: 'My Company' },
  { id: 2, code: 'axelorIndia', name: 'Axelor India' },
  { id: 3, code: 'axelorSAS', name: 'Axelor SAS' },
];

function fetchCompany({ search = '' }) {
  if (search === '') {
    return Promise.resolve(companies);
  }

  return Promise.resolve(companies.filter(i => i.name.toLowerCase().includes(search.toLowerCase())));
}

describe('Selection Component', () => {
  let wrapper, onChange, SelectionComponent;
  beforeEach(() => {
    onChange = jest.fn();
    SelectionComponent = withRoot(() => (
      <Selection
        name="company"
        title="Company"
        onChange={onChange}
        fetchAPI={fetchCompany}
        isSearchable={true}
        isPortal={false}
        optionValueKey="id"
        optionLabelKey="name"
        menuIsOpen
      />
    ));
    wrapper = mount(<SelectionComponent />);
  });

  it('should render select', () => {
    expect(wrapper.find(Select).length).toBe(1);
    expect(wrapper.find(FormLabel).length).toBe(1);
    expect(wrapper.find(FormLabel).props().children).toBe('Company');

    SelectionComponent = withRoot(() => (
      <Selection
        name="company"
        title="Company"
        onChange={onChange}
        fetchAPI={fetchCompany}
        isSearchable={true}
        isPortal={false}
        optionValueKey="id"
        optionLabelKey="name"
        menuIsOpen
        inline
      />
    ));
    wrapper = mount(<SelectionComponent />);
    expect(wrapper.find(FormLabel).length).toBe(0);
  });

  it('should call onChange', () => {
    const select = wrapper.find(Select);
    select.props().onChange();
    expect(onChange).toHaveBeenCalled();
  });

  it('should render chips', () => {
    SelectionComponent = withRoot(
      () => (
        <Selection
          name="company"
          title="Company"
          onChange={onChange}
          options={companies}
          readOnly={false}
          isMulti
          value={[companies[0], companies[1]]}
        />
      ),
      { palette: { type: 'dark' } },
    );
    wrapper = mount(<SelectionComponent />);
    expect(wrapper.find(Select).length).toBe(1);

    let chip = wrapper.find(Chip);
    expect(chip.length).toBe(2);
    expect(chip.at(0).props().onDelete).toBeDefined();

    SelectionComponent = withRoot(() => (
      <Selection
        name="company"
        title="Company"
        onChange={onChange}
        options={companies}
        readOnly={true}
        isMulti
        value={[companies[0], companies[1]]}
      />
    ));
    wrapper = mount(<SelectionComponent />);
    chip = wrapper.find(Chip);
    expect(chip.length).toBe(2);
    expect(chip.at(0).props().onDelete).not.toBeDefined();
  });

  it('should render placeholder', () => {
    SelectionComponent = withRoot(() => (
      <Selection
        name="company"
        title="Company"
        onChange={onChange}
        fetchAPI={fetchCompany}
        isMulti
        isSearchable={true}
        value={[companies[0], companies[1]]}
        optionValueKey="id"
        optionLabelKey="name"
      />
    ));
    wrapper = mount(<SelectionComponent />);
    expect(wrapper.find(TextField).length).toBe(1);
    expect(wrapper.find(Select).props().placeholder).toBe('Search Company');

    SelectionComponent = withRoot(() => (
      <Selection
        name="company"
        title="Company"
        onChange={onChange}
        readOnly={true}
        isMulti
        value={[companies[0], companies[1]]}
      />
    ));
    wrapper = mount(<SelectionComponent />);
    expect(wrapper.find(Select).props().placeholder).toBe('Select Company');
  });

  it('should render NoOptionsMessage', () => {
    SelectionComponent = withRoot(() => (
      <Selection
        name="company"
        title="Company"
        onChange={onChange}
        fetchAPI={() => Promise.resolve([])}
        isSearchable={true}
        isPortal={false}
        optionValueKey="id"
        optionLabelKey="name"
        menuIsOpen
      />
    ));

    wrapper = mount(<SelectionComponent />);
    expect(wrapper.find(NoOptionsMessage).length).toBe(1);
    expect(wrapper.find(Typography).at(1).props().children).toBe('No options');
  });

  it('should render Option', async () => {
    SelectionComponent = withRoot(() => (
      <Selection
        name="company"
        title="Company"
        onChange={onChange}
        fetchAPI={fetchCompany}
        isSearchable={true}
        isPortal={false}
        optionValueKey="id"
        optionLabelKey="name"
        menuIsOpen
        onFocus={jest.fn()}
      />
    ));

    wrapper = mount(<SelectionComponent />);
    const selectProps = wrapper.find(Select).props();
    act(() => {
      selectProps.onFocus();
    });
    await waitFor(wrapper, Option);
    expect(wrapper.find(Option).length).toBe(3);

    act(() => {
      selectProps.onInputChange('axelor', { action: 'input-change' });
    });
    wrapper.update();
    await waitFor(wrapper, Option);
    expect(wrapper.find(Option).length).toBe(2);

    act(() => {
      selectProps.onInputChange('axelor', { action: 'input-blur' });
    });
    wrapper.update();
    await waitFor(wrapper, Option);
    expect(wrapper.find(Option).length).toBe(2);
  });

  it('should call onMenuClose', () => {
    let closeMenuOnScroll;
    closeMenuOnScroll = wrapper
      .find(Select)
      .props()
      .closeMenuOnScroll({
        target: { children: [{ classList: ['menu-item'] }] },
      });
    expect(closeMenuOnScroll).toBe(false);

    closeMenuOnScroll = wrapper
      .find(Select)
      .props()
      .closeMenuOnScroll({ target: { children: [{ classList: 'menu' }] } });
    expect(closeMenuOnScroll).toBe(true);

    closeMenuOnScroll = wrapper
      .find(Select)
      .props()
      .closeMenuOnScroll({ target: { children: null } });
    expect(closeMenuOnScroll).toBe(true);
  });

  it('should render SingleValue', () => {
    SelectionComponent = withRoot(
      () => (
        <Selection
          name="company"
          title="Company"
          onChange={onChange}
          options={companies}
          readOnly={false}
          value={companies[0]}
        />
      ),
      { palette: { type: 'dark' } },
    );
    wrapper = mount(<SelectionComponent />);
    wrapper.find(Select).props().styles.menuPortal();
    const singleValue = wrapper.find(SingleValue);
    expect(singleValue.length).toBe(1);
  });
});
