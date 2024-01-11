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
import React, { useState, useRef } from 'react';
import PropTypes from 'prop-types';
import classNames from 'classnames';
import { Paper, Fade, Popper, ClickAwayListener } from '@material-ui/core';
import MenuItem from '@material-ui/core/MenuItem';
import { makeStyles } from '@material-ui/styles';
import ResponsiveList from '../responsive-list';
import Icon from '../Icons/FontAwesomeIcon';

const NAV_HEIGHT = 30;

const useStyles = makeStyles(() => ({
  container: {
    display: 'flex',
    flex: 1,
    overflow: 'hidden',
    zIndex: 0,
  },
  inner: {
    height: NAV_HEIGHT,
  },
  navStep: {
    display: 'inline-block',
    background: '#e2e2e2',
    cursor: 'pointer',
    outline: 'none',
    '&.active': {
      color: '#fff',
      background: '#0073D4',
      '&.focus:focus': {
        backgroundColor: '#0d47a1',
      },
    },
    '&.focus:focus': {
      backgroundColor: '#9e9e9e',
    },
  },
  navLabel: {
    display: 'flex',
    justifyContent: 'center',
    position: 'relative',
    lineHeight: `${NAV_HEIGHT}px`,
    padding: '0 30px 0 30px',
    whiteSpace: 'nowrap',
    background: 'inherit',
    fontSize: 14,
    '& span': {
      width: NAV_HEIGHT,
      height: NAV_HEIGHT,
      right: -15,
      transform: 'scale(0.72) rotate(45deg)',
      position: 'absolute',
      zIndex: 1,
      background: 'inherit',
      boxShadow: '2px -2px 0 1px rgba(255, 255, 255, 1)',
    },
  },
  moreContainer: {
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    position: 'relative',
    height: `${NAV_HEIGHT}px`,
    minWidth: '45px',
    padding: '0px 10px',
  },
  moreLabel: {
    padding: '0px 5px 0px 15px',
  },
  moreIcon: {
    position: 'relative',
    top: '-2px',
    left: '5px',
  },
}));

function NavItem(props) {
  const {
    title,
    autoTitle,
    className,
    active,
    value,
    onSelect,
    icon = '',
    readOnly = false,
    tabIndex,
    onClick,
  } = props;
  const isActive = `${active}` === `${value}`;
  const classes = useStyles();

  function selectItem(item) {
    onSelect && onSelect(item);
  }

  function selectOnEnterPress({ key }, value) {
    if (key === 'Enter') {
      onClick && onClick();
      if (!readOnly) {
        selectItem(value);
      }
    }
  }
  return (
    <React.Fragment>
      <li
        className={classNames(
          classes.navStep,
          className,
          isActive ? 'active' : '',
          readOnly ? '' : 'focus',
          'nav-item',
        )}
        onClick={() => !readOnly && selectItem(value)}
        onKeyDown={e => selectOnEnterPress(e, value)}
        tabIndex={tabIndex}
      >
        {icon === '' ? (
          <span className={classNames(classes.navLabel)}>
            {title || autoTitle}
            <span />
          </span>
        ) : (
          <span className={classNames(classes.moreContainer)}>
            {title && <span className={classes.moreLabel}>{title}</span>}
            <Icon icon={icon} className={classes.moreIcon} />
          </span>
        )}
      </li>
    </React.Fragment>
  );
}

function MoreNavItem(props) {
  const { active, onSelect, items, className, readOnly, tabIndex, ...other } = props;
  const [isOpen, setOpen] = useState(false);
  const classes = useStyles();
  let anchorEl = useRef();
  const activeItem = items.find(i => `${i.value}` === `${active}`);

  function onOpen() {
    !readOnly && setOpen(true);
  }

  function onClose() {
    setOpen(false);
  }

  function selectItem(item) {
    !readOnly && onSelect(item.value);
    onClose();
  }

  return (
    <div onClick={() => (isOpen ? onClose() : onOpen())} ref={anchorEl} className={className}>
      <NavItem
        icon="sort-down"
        active={active}
        {...activeItem}
        tabIndex={tabIndex}
        readOnly={readOnly}
        onClick={() => (isOpen ? onClose() : !readOnly && onOpen())}
      />
      {items && (
        <Popper disablePortal={true} open={isOpen} anchorEl={anchorEl.current} transition placement={'bottom'}>
          {({ TransitionProps }) => (
            <ClickAwayListener onClickAway={() => onClose()}>
              <Fade {...TransitionProps} timeout={350}>
                <Paper>
                  <div style={{ flex: 1 }} onKeyDown={e => other.handleNavSelectKeyPress(e)}>
                    {items.map((item, index) => (
                      <MenuItem
                        key={index}
                        onClick={() => selectItem(item)}
                        className={classNames(classes.menuItem, 'nav-item')}
                      >
                        {item.title}
                      </MenuItem>
                    ))}
                  </div>
                </Paper>
              </Fade>
            </ClickAwayListener>
          )}
        </Popper>
      )}
    </div>
  );
}

MoreNavItem.propTypes = {
  items: PropTypes.arrayOf(PropTypes.object),
  visibility: PropTypes.bool,
  isOpen: PropTypes.bool,
  onOpen: PropTypes.func,
  onClose: PropTypes.func,
};

export default function NavStepper({ data, active, onSelect, readOnly }) {
  const classes = useStyles();

  function handleNavSelectKeyPress({ target, key }) {
    const navItems = Array.from(document.getElementsByClassName('nav-item')).filter(element =>
      element.hasAttribute('tabindex'),
    );
    const focusItem = index => navItems[index] && navItems[index].focus();

    for (let i = 0; i < navItems.length; i++) {
      const child = navItems[i];
      if (child === target) {
        if (key === 'ArrowDown' || key === 'ArrowRight') {
          focusItem(i + 1);
        } else if (key === 'ArrowUp' || key === 'ArrowLeft') {
          focusItem(i - 1);
        }
      }
    }
  }
  return (
    <div className={classes.container} onKeyDown={handleNavSelectKeyPress}>
      <ResponsiveList className={classes.inner} items={data} active={active}>
        {(item, props) =>
          item.name === 'more' ? (
            <MoreNavItem
              {...item}
              {...props}
              classes={classes}
              icon="sort-down"
              active={active}
              onSelect={onSelect}
              readOnly={readOnly}
              tabIndex={0}
              handleNavSelectKeyPress={handleNavSelectKeyPress}
            />
          ) : (
            <NavItem
              {...item}
              {...props}
              classes={classes}
              active={active}
              onSelect={onSelect}
              readOnly={readOnly}
              {...(props.visibility && { tabIndex: 0 })}
            />
          )
        }
      </ResponsiveList>
    </div>
  );
}
