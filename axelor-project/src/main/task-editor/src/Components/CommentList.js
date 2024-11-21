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
import moment from 'moment';
import DeleteForeverIcon from '@material-ui/icons/DeleteForever';
import { Divider, Avatar } from '@material-ui/core';
import { makeStyles } from '@material-ui/core/styles';

import User from '../assets/user.jpg';
import { translate } from '../utils';

const useStyles = makeStyles(() => ({
  commentTag: {
    borderRadius: '50%',
    padding: '10px 15px 10px 15px',
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    fontWeight: 'bold',
    color: 'white',
    fontSize: 15,
    height: 25,
    width: 15,
  },
  commentContainer: {
    display: 'flex',
    lineHeight: '20px',
    padding: '15px 0 15px 0',
    fontWeight: ' normal',
    overflowX: 'auto',
  },
  commentLoad: {
    display: 'inline-block',
    '-ms-flex-pack': 'center',
    justifyContent: 'center',
    backgroundColor: 'gray',
    padding: '5px 20px 5px 20px',
    borderRadius: '20px',
    color: 'white',
    cursor: 'pointer',
  },
  cardView: {
    padding: '0px 15px',
    background: '#F6F8F9',
  },
  loadMore: {
    display: 'flex',
    justifyContent: 'center',
    padding: '10px 0px',
  },
}));

function CommentStream(props) {
  const { comments = [], limit, total, offset, userId } = props;
  const classes = useStyles();
  return (
    <div className={classes.cardView}>
      {comments &&
        comments.map((item, index) => {
          let obj = item.type === 'notification' ? JSON.parse(item.body) : '';
          return (
            <div key={index}>
              <div className={classes.commentContainer}>
                <Avatar alt="User" src={User} />
                <span
                  style={{
                    display: 'flex',
                    flex: 1,
                    flexDirection: 'column',
                    paddingLeft: 20,
                  }}
                >
                  <div style={{ display: 'flex' }}>
                    <div style={{ display: 'flex', flex: 1 }}>
                      <span>{item.$author && item.$author.fullName}</span>
                      <span style={{ color: '#D3D3D3', paddingLeft: '5px' }}>
                        - {moment(item.$eventTime).format('DD/MM HH:mm')}
                      </span>
                    </div>
                    {Number(item.$author.id) === Number(userId) && item.$canDelete && (
                      <div style={{ display: 'flex', alignItems: 'center' }}>
                        <span
                          style={{ fontSize: '14px', marginTop: '-5px', cursor: 'pointer' }}
                          onClick={() => props.removeComment(item)}
                        >
                          <DeleteForeverIcon />
                        </span>
                      </div>
                    )}
                  </div>
                  {obj ? (
                    <React.Fragment>
                      <span style={{ fontWeight: 600, textAlign: 'left' }}>{translate(obj.title)}</span>
                      <ul
                        style={{
                          paddingLeft: 20,
                          display: obj.tracks.length > 0 ? 'flex' : 'none',
                          flexDirection: 'column',
                          textAlign: 'left',
                        }}
                      >
                        {obj.tracks.map((i, ind) => (
                          <li key={ind}>
                            {i.displayValue ? (
                              <div>
                                <span style={{ fontWeight: 600 }}>{translate(i.title)} : </span>
                                <span>
                                  {i.oldDisplayValue && translate(i.oldDisplayValue) + ' » '}{' '}
                                  {translate(i.displayValue)}
                                </span>
                              </div>
                            ) : (
                              <div>
                                <span style={{ fontWeight: 600 }}>{translate(i.title)} : </span>
                                <span>
                                  {i.oldValue && translate(i.oldValue) + ' » '} {translate(i.value)}
                                </span>
                              </div>
                            )}
                          </li>
                        ))}
                      </ul>
                    </React.Fragment>
                  ) : (
                    <span
                      style={{
                        textOverflow: 'ellipsis',
                        wordBreak: 'break-word',
                        textAlign: 'left',
                      }}
                    >
                      {item.body}
                    </span>
                  )}
                </span>
              </div>
              <Divider />
            </div>
          );
        })}
      {limit + offset < total && (
        <div className={classes.loadMore}>
          <span className={classes.commentLoad} onClick={() => props.loadComment()}>
            {translate('Load more')}
          </span>
        </div>
      )}
    </div>
  );
}
export default CommentStream;
