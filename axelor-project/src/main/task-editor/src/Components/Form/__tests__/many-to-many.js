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
