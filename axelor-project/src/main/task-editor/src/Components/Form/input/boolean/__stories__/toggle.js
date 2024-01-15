/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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
