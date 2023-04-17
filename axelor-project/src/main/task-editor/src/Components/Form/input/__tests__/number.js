import React from 'react';
import { render, unmountComponentAtNode } from 'react-dom';
import { act } from 'react-dom/test-utils';
import { fireEvent, getByLabelText, getByText } from '@testing-library/dom';

import NumberField from '../number';

describe('NumberField Component', () => {
  let container = null,
    onChange,
    onBlur;

  let props = {
    name: 'amount',
    title: 'Amount',
    value: '100',
  };

  beforeEach(() => {
    onChange = jest.fn();
    onBlur = jest.fn();
    container = document.createElement('div');
    document.body.appendChild(container);

    act(() => {
      render(<NumberField {...props} onChange={onChange} onBlur={onBlur} />, container);
    });
  });

  afterEach(() => {
    unmountComponentAtNode(container);
    container.remove();
    container = null;
  });

  it('should render integer field', () => {
    let numberField = getByText(container, props.title);
    expect(numberField.textContent).toBe(props.title);
  });

  it('should call onChange', () => {
    let numberField = getByLabelText(container, props.title);
    act(() => {
      fireEvent.change(numberField, { target: { value: '1200' } });
      fireEvent.blur(numberField);
    });
    expect(onChange).toHaveBeenCalled();
    expect(onBlur).toHaveBeenCalled();
  });

  it('should render decimal field', () => {
    act(() => {
      render(<NumberField {...props} type="decimal" onChange={onChange} />, container);
    });
    let numberField = getByLabelText(container, props.title);
    act(() => {
      fireEvent.change(numberField, { target: { value: '12.01' } });
      fireEvent.blur(numberField);
    });
    expect(onChange).toHaveBeenCalled();
  });

  it('should render NumberField when readOnly is true', () => {
    act(() => {
      render(<NumberField {...props} onChange={onChange} readOnly={true} className="number-field" />, container);
    });
    expect(container.querySelector('p').textContent).toBe(props.value);
  });
});
