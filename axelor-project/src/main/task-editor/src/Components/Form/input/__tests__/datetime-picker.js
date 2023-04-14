import React from 'react';
import { render, unmountComponentAtNode } from 'react-dom';
import { act } from 'react-dom/test-utils';
import { fireEvent, getByText } from '@testing-library/dom';

import DateTimePicker from '../datetime-picker';
import moment from 'moment';

describe('Date Component', () => {
  let container = null,
    onChange;

  let props = {
    name: 'dob',
    title: 'Date of birth',
  };

  let defaultFormat = {
    date: 'DD/MM/YYYY',
  };

  beforeEach(() => {
    onChange = jest.fn();
    container = document.createElement('div');
    document.body.appendChild(container);

    act(() => {
      render(<DateTimePicker {...props} onChange={onChange} />, container);
    });
  });

  afterEach(() => {
    unmountComponentAtNode(container);
    container.remove();
    container = null;
  });

  it('should render datePickers', () => {
    let dateField = getByText(container, props.title);
    expect(dateField.textContent).toBe(props.title);
    expect(container.querySelectorAll('label').length).toBe(1);

    act(() => {
      render(<DateTimePicker {...props} onChange={onChange} inline={true} />, container);
    });
    expect(container.querySelectorAll('label').length).toBe(0);
  });

  it('should call onChange', () => {
    let dateField = container.querySelector('input');
    act(() => {
      fireEvent.change(dateField, { target: { value: moment() } });
    });
    expect(onChange).toHaveBeenCalled();
  });

  it('should render datePickers when readOnly is true', async () => {
    act(() => {
      render(<DateTimePicker {...props} onChange={onChange} readOnly={true} />, container);
    });
    expect(getByText(container, props.title).textContent).toBe(props.title);
    expect(container.querySelector('p').textContent).toBe('');

    act(() => {
      render(<DateTimePicker {...props} onChange={onChange} readOnly={true} value={new Date()} />, container);
    });
    expect(container.querySelector('p').textContent).toBe(
      moment(new Date()).format(props.format || defaultFormat['date']),
    );
  });
});

describe('Time Component', () => {
  let container = null,
    onChange;

  let props = {
    name: 'eventTime',
    title: 'Event Time',
    format: 'LT',
    type: 'time',
  };

  beforeEach(() => {
    onChange = jest.fn();
    container = document.createElement('div');
    document.body.appendChild(container);

    act(() => {
      render(<DateTimePicker {...props} onChange={onChange} />, container);
    });
  });

  afterEach(() => {
    unmountComponentAtNode(container);
    container.remove();
    container = null;
  });

  it('should render TimePickers', () => {
    let timeField = getByText(container, props.title);
    expect(timeField.textContent).toBe(props.title);
  });

  it('should render timePickers when readOnly is true', () => {
    act(() => {
      render(<DateTimePicker {...props} onChange={onChange} value={new Date()} readOnly={true} />, container);
    });
    expect(getByText(container, props.title).textContent).toBe(props.title);
    expect(container.querySelector('p').textContent).toBe(moment(new Date()).format(props.format));
  });
});

describe('DateTime Component', () => {
  let container = null,
    onChange;

  let props = {
    name: 'event',
    title: 'Event',
    type: 'datetime',
    format: 'MMMM Do h:mm a',
  };

  beforeEach(() => {
    onChange = jest.fn();
    container = document.createElement('div');
    document.body.appendChild(container);

    act(() => {
      render(<DateTimePicker {...props} onChange={onChange} />, container);
    });
  });

  afterEach(() => {
    unmountComponentAtNode(container);
    container.remove();
    container = null;
  });

  it('should render DateTimePickers', () => {
    let timeField = getByText(container, props.title);
    expect(timeField.textContent).toBe(props.title);
  });

  it('should render dateTimePickers when readOnly is true', () => {
    act(() => {
      render(<DateTimePicker {...props} onChange={onChange} value={new Date()} readOnly={true} />, container);
    });
    expect(getByText(container, props.title).textContent).toBe(props.title);
    expect(container.querySelector('p').textContent).toBe(moment(new Date()).format(props.format));
  });
});
