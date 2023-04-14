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
