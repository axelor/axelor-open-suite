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
import React, { useEffect } from 'react';
import { create } from 'jss';
import rtl from 'jss-rtl';
import { ThemeProvider, StylesProvider } from '@material-ui/styles';
import { jssPreset } from '@material-ui/styles';
import { createMuiTheme } from '@material-ui/core/styles';
import blue from '@material-ui/core/colors/indigo';
import pink from '@material-ui/core/colors/pink';
import CssBaseline from '@material-ui/core/CssBaseline';

// Configure JSS with RTL support
const jss = create({ plugins: [...jssPreset().plugins, rtl()] });

let originalDir;

// Create theme with the given options.
const createTheme = ({ direction = 'ltr', palette }) => {
  if (originalDir === undefined) {
    originalDir = document.body.dir || document.dir;
  }
  direction = direction || originalDir;
  return createMuiTheme({
    direction,
    layout: {
      cols: 12,
    },
    header: {
      height: 64,
    },
    palette: {
      primary: blue,
      secondary: pink,
      ...palette,
    },
    typography: {
      useNextVariants: true,
    },
  });
};

export function WithTheme({ direction, palette, children }) {
  useEffect(() => {
    direction ? (document.body.dir = direction) : document.body.removeAttribute('dir');
  });
  const theme = createTheme({ direction, palette });
  return (
    <StylesProvider jss={jss}>
      <ThemeProvider theme={theme}>
        <CssBaseline />
        {children}
      </ThemeProvider>
    </StylesProvider>
  );
}

function withRoot(Component, { direction = 'ltr', palette } = {}) {
  function WithRoot(props) {
    return (
      <WithTheme direction={direction} palette={palette}>
        <Component {...props} />
      </WithTheme>
    );
  }
  return WithRoot;
}

export default withRoot;
