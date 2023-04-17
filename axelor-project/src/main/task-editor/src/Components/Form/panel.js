import React from 'react';
import classNames from 'classnames';
import Typography from '@material-ui/core/Typography';
import ExpandLess from '@material-ui/icons/ExpandLess';
import ExpandMore from '@material-ui/icons/ExpandMore';

import Flex from '../../components/flex';
import { makeStyles } from '@material-ui/styles';

const useStyles = makeStyles(theme => ({
  panel: {
    marginBottom: theme.spacing(1.5),
    flexGrow: 1,
    boxShadow: 'none',
    maxWidth: '100%',
  },
  panelTitle: {
    cursor: 'pointer',
    '&.card': {
      padding: theme.spacing(1, 2),
      borderTop: '3px solid #e7e7e7',
      borderBottom: '1px solid #e7e7e7',
      display: 'flex',
      alignItems: 'center',
    },
    '& > *:first-child': {
      fontSize: 14,
      fontWeight: 600,
      flex: 1,
    },
  },
  panelCollapseIcon: {
    width: 24,
    height: 24,
  },
  panelItem: {
    padding: '0 5px',
    '.panel-item &': {
      padding: 0,
    },
  },
}));

export function PanelItem({ className, ...rest }) {
  const classes = useStyles();
  return <Flex.Item {...rest} className={classNames(classes.panelItem, 'panel-item', className)} />;
}

export function Panel({ className, title, children, isCard = false, collapse = false, isCollapsed = false, ...rest }) {
  const classes = useStyles();
  const [isCollapse, setCollapse] = React.useState(isCollapsed);

  function renderChildren() {
    if (collapse) {
      return isCollapse && children;
    }
    return children;
  }
  return (
    <div {...rest} className={classNames(classes.panel, className)}>
      {title && (
        <div
          className={classNames(classes.panelTitle, { card: isCard })}
          onClick={e => collapse && setCollapse(!isCollapse)}
        >
          <Typography variant="h5" component="h5">
            {title}
          </Typography>
          {collapse && (
            <Typography className={classes.panelCollapseIcon}>
              {isCollapse ? <ExpandLess /> : <ExpandMore />}
            </Typography>
          )}
        </div>
      )}
      {renderChildren()}
    </div>
  );
}
