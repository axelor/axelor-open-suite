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
import React from 'react';
import { makeStyles } from '@material-ui/styles';
import Flex from '../flex';
import Typography from '@material-ui/core/Typography';
import Divider from '@material-ui/core/Divider';

const useStyles = makeStyles(theme => ({
  separatorContainer: {
    maxWidth: '100%',
    marginTop: '16px',
  },
}));

function Separator({ span = 6, title }) {
  const classes = useStyles();
  return (
    <Flex.Item span={span} className={classes.separatorContainer}>
      {title && <Typography>{title}</Typography>}
      <Divider />
    </Flex.Item>
  );
}
export default Separator;
