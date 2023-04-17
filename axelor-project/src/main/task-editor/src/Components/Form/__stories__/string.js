import React from 'react';
import { storiesOf } from '@storybook/react';
import StringWidget from '../string';
import withRoot from '../../../withRoot';

storiesOf('Form | String', module).add('Default', () => {
  const Wrapper = withRoot(() => <StringWidget title="Full Name" value="Tushar Bodara" />);
  return <Wrapper />;
});
