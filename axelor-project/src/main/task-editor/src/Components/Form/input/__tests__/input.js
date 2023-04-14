import React from 'react';
import { render, unmountComponentAtNode } from 'react-dom';
import { act } from 'react-dom/test-utils';
import { fireEvent, getByLabelText, getByText } from '@testing-library/dom';

import InputField from '../input';

describe('TextField Component', () => {
  let container = null,
    onChange;
  let props = {
    name: 'firstName',
    value: 'john',
    title: 'First Name',
  };

  beforeEach(() => {
    onChange = jest.fn();
    container = document.createElement('div');
    document.body.appendChild(container);

    act(() => {
      render(<InputField {...props} onChange={onChange} />, container);
    });
  });

  afterEach(() => {
    unmountComponentAtNode(container);
    container.remove();
    container = null;
  });

  it('should render TextField', () => {
    let input = getByText(container, props.title);
    expect(input.textContent).toBe(props.title);

    input = getByLabelText(container, props.title);
    expect(input.value).toBe(props.value);

    act(() => {
      render(<InputField name="firstName" autoTitle="name" onChange={onChange} />, container);
    });
    input = getByText(container, 'name');
    expect(input.textContent).toBe('name');

    act(() => {
      render(<InputField name="firstName" title="First Name" onChange={onChange} />, container);
    });
    input = getByLabelText(container, props.title);
    expect(input.value).toBe('');
  });

  it('should call onChange', () => {
    let input = container.querySelector('input');
    act(() => {
      fireEvent.change(input, { target: { value: 'tushar' } });
    });
    expect(onChange).toHaveBeenCalled();
  });

  it('should render textField when readOnly is true', () => {
    act(() => {
      render(<InputField {...props} readOnly={true} />, container);
    });
    expect(getByText(container, props.title).textContent).toBe(props.title);
    expect(container.querySelector('p').textContent).toBe(props.value);
  });
});

describe('InputField Component', () => {
  let container = null,
    onChange;

  let props = {
    name: 'firstName',
    value: 'john',
    inline: true,
  };
  beforeEach(() => {
    onChange = jest.fn();
    container = document.createElement('div');
    document.body.appendChild(container);

    act(() => {
      render(<InputField {...props} onChange={onChange} />, container);
    });
  });

  afterEach(() => {
    unmountComponentAtNode(container);
    container.remove();
    container = null;
  });

  it('should render input field', () => {
    let input = container.querySelector('input');
    expect(input.value).toBe(props.value);
  });

  it('should call onChange', () => {
    const input = container.querySelector('input');
    act(() => {
      fireEvent.change(input, { target: { value: 'tushar' } });
    });
    expect(onChange).toHaveBeenCalled();
  });

  it('should render input fields when readOnly is true', () => {
    act(() => {
      render(<InputField {...props} readOnly={true} />, container);
    });
    expect(container.querySelector('p').textContent).toBe(props.value);

    act(() => {
      render(<InputField name="firstName" inline={true} readOnly={true} />, container);
    });
    expect(container.querySelector('p').textContent).toBe('');
  });
});
