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
import FontAwesomeIcon from '../../../Icons/FontAwesomeIcon';
import classnames from 'classnames';

const useStyles = makeStyles(theme => ({
  root: {
    color: '#ccc',
    '&.active, &:hover': {
      color: '#000',
    },
  },
}));
const mappedIcons = {
  'star-o': 'star',
};

function Toggle({ icon, iconActive, value = false, onClick, ...other }) {
  const classes = useStyles();
  icon = (value === true && iconActive ? iconActive : icon).replace(/fa-|fas-/, '');
  return (
    <div onClick={() => onClick(!value)} className="toggle">
      <FontAwesomeIcon
        icon={mappedIcons[icon] || icon}
        {...other}
        className={classnames(
          classes.root,
          {
            active: value,
          },
          'toggle-icon',
        )}
      />
    </div>
  );
}

export default Toggle;
