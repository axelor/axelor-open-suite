import React from 'react';
import { mount } from 'enzyme';
import Separator from '../separator';
import Flex from '../../flex';
import withRoot from '../../../withRoot';
import Typography from '@material-ui/core/Typography';
import Divider from '@material-ui/core/Divider';

describe('Spacer Component', () => {
  let wrapper, SeparatorComponent;
  beforeEach(() => {
    SeparatorComponent = withRoot(() => <Separator title="Test" />);
    wrapper = mount(<SeparatorComponent />);
  });

  it('should render flex item', () => {
    expect(wrapper.find(Flex.Item).length).toBe(1);
    expect(wrapper.find(Divider).length).toBe(1);
    expect(wrapper.find(Typography).length).toBe(1);
    expect(wrapper.find(Typography).props().children).toBe('Test');
  });
});
