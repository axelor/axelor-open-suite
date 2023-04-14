import React from 'react';
import { render, unmountComponentAtNode } from 'react-dom';
import { act } from 'react-dom/test-utils';
import { getByText } from '@testing-library/dom';

import Switch from '../switch';

describe('Switch Component', () => {
  let container = null;
  let props = {
    name: 'isCustome',
    title: 'Customer',
  };

  function SwitchWrapper(props) {
    const [value, setValue] = React.useState(false);
    return <Switch {...props} value={value} onChange={() => setValue(!value)} />;
  }

  beforeEach(() => {
    container = document.createElement('div');
    document.body.appendChild(container);

    act(() => {
      render(<SwitchWrapper {...props} />, container);
    });
  });

  afterEach(() => {
    unmountComponentAtNode(container);
    container.remove();
    container = null;
  });

  it('should render switch', () => {
    let input = getByText(container, props.title);
    expect(input.textContent).toBe(props.title);
  });
});
