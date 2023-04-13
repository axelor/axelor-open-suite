import React from 'react';
import { mount } from 'enzyme';
import { Panel, PanelItem } from '../panel';
import withRoot from '../../../withRoot';
import Flex from '../../../components/flex';
import Typography from '@material-ui/core/Typography';

describe('Spacer Component', () => {
  let wrapper, PanelComponent;
  beforeEach(() => {
    PanelComponent = withRoot(() => (
      <Panel title="Personal">
        <Flex>
          <PanelItem span="4">
            <Panel>
              <Flex>
                <PanelItem span="12">
                  <div />
                </PanelItem>
              </Flex>
            </Panel>
          </PanelItem>
        </Flex>
      </Panel>
    ));
    wrapper = mount(<PanelComponent />);
  });

  it('should render Panel', () => {
    expect(wrapper.find(Panel).length).toBe(2);
    expect(wrapper.find(PanelItem).length).toBe(2);
    expect(wrapper.find(Typography).props().children).toBe('Personal');
  });
});
