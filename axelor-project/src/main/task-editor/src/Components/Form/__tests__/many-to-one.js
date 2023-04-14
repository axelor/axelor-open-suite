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
