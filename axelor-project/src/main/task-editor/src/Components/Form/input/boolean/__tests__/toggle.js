import React from 'react';
import { render, unmountComponentAtNode } from 'react-dom';
import { act } from 'react-dom/test-utils';
import { fireEvent } from '@testing-library/dom';

import Toggle from '../toggle';

describe('Toggle Component', () => {
  let container = null;
  let props = {
    name: 'primary',
    icon: 'star',
    iconActive: 'star-half-alt',
  };

  function ToggleWrapper(props) {
    const [value, setValue] = React.useState();
    return <Toggle {...props} value={value} onClick={() => setValue(!value)} />;
  }

  beforeEach(() => {
    container = document.createElement('div');
    document.body.appendChild(container);

    act(() => {
      render(<ToggleWrapper {...props} />, container);
    });
  });

  afterEach(() => {
    unmountComponentAtNode(container);
    container.remove();
    container = null;
  });

  it('should render switch', () => {
    let toggle = container.querySelector('.toggle');

    act(() => {
      fireEvent.click(toggle);
    });

    let toggleIcon = container.querySelector('.toggle-icon');
    expect(toggleIcon.getAttribute('data-icon')).toBe(props.iconActive);

    act(() => {
      fireEvent.click(toggle);
    });

    toggleIcon = container.querySelector('.toggle-icon');
    expect(toggleIcon.getAttribute('data-icon')).toBe(props.icon);
  });
});
