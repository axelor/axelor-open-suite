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
