import React, { useState } from 'react';
import { storiesOf } from '@storybook/react';

import withRoot from '../../../../../withRoot';
import Toggle from '../toggle';

function ToggleWrapper(props) {
  const [value, setValue] = useState(true);
  return <Toggle {...props} value={value} onClick={value => setValue(value)} />;
}

storiesOf('Form | Toggle', module).add('Default', () => {
  const Wrapper = withRoot(() => <ToggleWrapper name="primary" icon="fa-star" iconActive="fas-star-half-alt" />);
  return <Wrapper />;
});
