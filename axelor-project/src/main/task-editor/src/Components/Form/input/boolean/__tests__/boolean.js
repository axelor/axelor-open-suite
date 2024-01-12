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
import { render, unmountComponentAtNode } from 'react-dom';
import { act } from 'react-dom/test-utils';
import { fireEvent, getByLabelText, getByText } from '@testing-library/dom';

import DefaultBoolean from '../boolean';

describe('Default boolean Component', () => {
  let container = null;
  let props = {
    name: 'isCustomer',
    value: false,
    title: 'Customer',
  };

  function DefaultBooleanWrapper(props) {
    const [value, setValue] = React.useState(false);
    return (
      <DefaultBoolean
        {...props}
        value={value}
        onChange={() => setValue(value === false ? null : value === null ? true : false)}
      />
    );
  }

  beforeEach(() => {
    container = document.createElement('div');
    document.body.appendChild(container);

    act(() => {
      render(<DefaultBooleanWrapper {...props} />, container);
    });
  });

  afterEach(() => {
    unmountComponentAtNode(container);
    container.remove();
    container = null;
  });

  it('should render checkBox', () => {
    let input = getByText(container, props.title);
    expect(input.textContent).toBe(props.title);
  });

  it('should call onChange', () => {
    let checkBoxField = getByLabelText(container, props.title);
    act(() => {
      fireEvent.click(checkBoxField);
    });
    expect(checkBoxField.checked).toBe(false);

    act(() => {
      fireEvent.click(checkBoxField);
    });
    expect(checkBoxField.checked).toBe(true);

    act(() => {
      fireEvent.click(checkBoxField);
    });
    expect(checkBoxField.checked).toBe(false);
  });
});
