import React from 'react';
import { mount } from 'enzyme';
import Spacer from '../spacer';
import Flex from '../../flex';
import withRoot from '../../../withRoot';

describe('Spacer Component', () => {
  let wrapper, SpacerComponent;
  beforeEach(() => {
    SpacerComponent = withRoot(() => <Spacer />);
    wrapper = mount(<SpacerComponent />);
  });

  it('should render flex item', () => {
    expect(wrapper.find(Flex.Item).length).toBe(1);
  });
});
