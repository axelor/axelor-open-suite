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
import { Page as PDFPage, Text, View, Document as DocumentView, Image, StyleSheet } from '@react-pdf/renderer';
import User from '../../../assets/user.jpg';
import { translate } from '../../../utils';

const styles = StyleSheet.create({
  page: {
    flexDirection: 'column',
    padding: 20,
    fontSize: 14,
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    fontSize: 14,
    margin: '5px 0px',
  },
  title: {
    fontSize: 30,
    fontWeight: 800,
  },
  caption: {
    color: '#8DA3A6',
    fontSize: 14,
  },
  section: {
    margin: '5px 0px',
  },
  taskName: {
    fontSize: 16,
  },
  sectionDetails: {
    margin: '5px 0px',
    fontSize: 12,
    marginLeft: 15,
  },
  avatar: {
    width: 30,
    height: 30,
    position: 'relative',
    alignItems: 'center',
    borderRadius: '50%',
    justifyContent: 'center',
  },
  img: {
    color: 'transparent',
    width: '100%',
    height: '100%',
    objectFit: 'cover',
    textAlign: 'center',
    textIndent: 10000,
  },
  commentContainer: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    marginBottom: 10,
  },
  commentList: {
    marginLeft: 20,
  },
});

export default function MyDocument({ task, comments, subTasks }) {
  return (
    <DocumentView>
      <PDFPage size="A4" style={styles.page}>
        <View style={styles.header}>
          <Text>{moment().format('DD/MM/YYYY')}</Text>
          <Text>
            • {task.project?.fullName} - {task.name}
          </Text>
        </View>
        <View style={styles.section}>
          <Text style={styles.title}>{task.name}</Text>
          <Text style={styles.caption}>{translate('Printed from Axelor open platform')}</Text>
        </View>
        <View style={styles.header}>
          <View style={{ flexDirection: 'row' }}>
            <Text>•</Text>
            <Text style={[styles.taskName, { marginLeft: 10 }]}>
              {task.assignedTo?.fullName} {task.name}
            </Text>
          </View>
          {task.taskEndDate && (
            <Text>
              {translate('Due Date')}: {moment(task.taskEndDate).format('DD/MM/YYYY')}
            </Text>
          )}
        </View>
        <View style={styles.sectionDetails}>
          {task.priority && (
            <Text>
              {translate('Priority')}: {task.priority.name}
            </Text>
          )}
          {task.status && (
            <Text>
              {translate('Status')}: {task.status.name}
            </Text>
          )}
          <Text>
            {translate('Section')}: {task.projectTaskSection && task.projectTaskSection.name}
          </Text>
        </View>
        {task.description && (
          <View style={styles.section}>
            <Text>
              {translate('Description')}: {task.description}
            </Text>
          </View>
        )}
        <View style={styles.section}>
          {subTasks &&
            subTasks.length > 0 &&
            subTasks.map((subtask, index) => (
              <View key={index} style={{ flexDirection: 'row' }}>
                <Text style={[styles.taskName, { marginLeft: 10 }]}>•</Text>
                <Text style={[styles.taskName, { marginLeft: 15 }]}>{subtask.name}</Text>
              </View>
            ))}
        </View>
        <View style={styles.section}>
          {comments &&
            comments.map((item, index) => {
              let obj = item.type === 'notification' ? JSON.parse(item.body) : '';
              return (
                <View key={index} style={styles.commentList}>
                  <View style={styles.commentContainer}>
                    <View style={styles.avatar}>
                      <Image style={styles.img} source="User" src={User} />
                    </View>
                    <View style={{ flexDirection: 'column' }}>
                      <View
                        style={{
                          flexDirection: 'column',
                          paddingLeft: 20,
                        }}
                      >
                        <View style={{ flexDirection: 'row' }}>
                          <Text>{item.$author && item.$author.fullName}</Text>
                          <Text style={{ color: '#D3D3D3', paddingLeft: '5px' }}>
                            {' '}
                            - {moment(item.$eventTime).format('DD/MM h:mm')}{' '}
                          </Text>
                        </View>
                        {obj ? (
                          <View style={{ flexDirection: 'column' }}>
                            <Text style={{ fontWeight: 600, textAlign: 'left' }}>{obj.title}</Text>
                            <View
                              style={{
                                paddingLeft: 20,
                                display: obj.tracks.length > 0 ? 'flex' : 'none',
                                flexDirection: 'column',
                                textAlign: 'left',
                              }}
                            >
                              {obj.tracks.map((i, ind) => (
                                <Text key={ind}>
                                  {i.displayValue ? (
                                    <View>
                                      <Text style={{ fontWeight: 600 }}>• {i.title} : </Text>
                                      <Text>
                                        {i.oldDisplayValue && i.oldDisplayValue + ' » '} {i.displayValue}
                                      </Text>
                                    </View>
                                  ) : (
                                    <View>
                                      <Text style={{ fontWeight: 600 }}>• {i.title} : </Text>
                                      <Text>
                                        {i.oldValue && i.oldValue + ' » '} {i.value}
                                      </Text>
                                    </View>
                                  )}
                                </Text>
                              ))}
                            </View>
                          </View>
                        ) : (
                          <Text
                            style={{
                              textOverflow: 'ellipsis',
                              wordBreak: 'break-word',
                            }}
                          >
                            {item.body}
                          </Text>
                        )}
                      </View>
                    </View>
                  </View>
                </View>
              );
            })}
        </View>
      </PDFPage>
    </DocumentView>
  );
}
