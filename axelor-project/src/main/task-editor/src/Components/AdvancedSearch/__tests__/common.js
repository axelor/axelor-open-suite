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
import { Link, RadioGroup, FormControlLabel, Checkbox, Button } from '@material-ui/core';

import withRoot from '../../../withRoot';
import { BooleanRadio, BooleanCheckBox, ButtonLink, SimpleButton } from '../common';

describe('ButtonLink', () => {
  let wrapper, ButtonLinkComponent, onClick;

  beforeEach(() => {
    onClick = jest.fn();
    ButtonLinkComponent = withRoot(props => <ButtonLink {...props} />);

    wrapper = mount(<ButtonLinkComponent title="Test" onClick={onClick} />);
  });

  it('should render Link', () => {
    expect(wrapper.find(Link).length).toBe(1);
  });

  it('should call onClick', () => {
    wrapper.find(Link).props().onClick();
    expect(onClick).toHaveBeenCalled();
  });
});

describe('BooleanRadio', () => {
  let wrapper, BooleanRadioComponent, onChange;

  beforeEach(() => {
    onChange = jest.fn();
    BooleanRadioComponent = withRoot(props => <BooleanRadio {...props} />);

    wrapper = mount(
      <BooleanRadioComponent
        name="Test"
        onChange={onChange}
        classes={{}}
        data={[
          { value: 'and', label: 'and' },
          { value: 'or', label: 'or' },
        ]}
      />,
    );
  });

  it('should render RadioGroup', () => {
    expect(wrapper.find(RadioGroup).length).toBe(1);
    expect(wrapper.find(FormControlLabel).length).toBe(2);
  });

  it('should call onChange', () => {
    wrapper.find(RadioGroup).props().onChange();
    expect(onChange).toHaveBeenCalled();
  });
});

describe('BooleanCheckBox', () => {
  let wrapper, BooleanCheckBoxComponent, onChange;

  beforeEach(() => {
    onChange = jest.fn();
    BooleanCheckBoxComponent = withRoot(props => <BooleanCheckBox {...props} />);

    wrapper = mount(<BooleanCheckBoxComponent name="test" title="Test" onChange={onChange} classes={{}} />);
  });

  it('should render Checkbox', () => {
    expect(wrapper.find(FormControlLabel).length).toBe(1);
    wrapper
      .find(Checkbox)
      .props()
      .onChange({ target: { checked: true } });

    expect(onChange).toHaveBeenCalled();
  });

  it('should render inline Checkbox', () => {
    wrapper = mount(<BooleanCheckBoxComponent name="test" title="Test" onChange={onChange} inline={true} />);
    expect(wrapper.find(Checkbox).length).toBe(1);
    wrapper
      .find(Checkbox)
      .props()
      .onChange({ target: { checked: true } });

    expect(onChange).toHaveBeenCalled();
  });
});

describe('SimpleButton', () => {
  let wrapper, SimpleButtonComponent, onClick;

  beforeEach(() => {
    onClick = jest.fn();
    SimpleButtonComponent = withRoot(props => <SimpleButton {...props} />);

    wrapper = mount(<SimpleButtonComponent title="Test" onClick={onClick} classes={{}} />);
  });

  it('should render Button', () => {
    expect(wrapper.find(Button).length).toBe(1);

    wrapper = mount(<SimpleButtonComponent title="Test" onClick={onClick} classes={{}} hide={true} />);
    expect(wrapper.find(Button).props().style).toEqual({ display: 'none' });
  });

  it('should call onClick', () => {
    wrapper.find(Button).props().onClick();
    expect(onClick).toHaveBeenCalled();
  });
});
