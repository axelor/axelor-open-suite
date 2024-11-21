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
import { Draggable } from 'react-beautiful-dnd';

import CardItem from './CardItem';
import { useTaskEditor } from './Context';

const InnerCardList = React.memo(function InnerCardList({ disabled, column }) {
  const { onCardEdit, onCardDelete, tasksToBeDeleted } = useTaskEditor();
  const { canMove = true, canDelete = true, canEdit = true } = column;
  const filteredRecords = column.records?.filter(record => !tasksToBeDeleted.some(v => v.id === record.id));
  return filteredRecords?.length ? (
    filteredRecords.map((record, index) => (
      <Draggable
        key={`${record.id}`}
        draggableId={`task_${record.id}`}
        index={index}
        isDragDisabled={!canMove || disabled}
      >
        {provided => (
          <CardItem
            key={record.id}
            ref={provided.innerRef}
            {...provided.draggableProps}
            dragHandleProps={provided.dragHandleProps}
            canDelete={canDelete}
            canEdit={canEdit}
            record={record}
            disabled={disabled}
            style={{
              ...provided.draggableProps.style,
              width: '100%',
            }}
            onDelete={() => onCardDelete(record.id)}
            onEdit={record => onCardEdit(record)}
          />
        )}
      </Draggable>
    ))
  ) : (
    <React.Fragment />
  );
});

InnerCardList.defaultProps = {
  onCardDelete: () => {},
  disabled: false,
  onCardEdit: () => {},
  column: {},
};

export default InnerCardList;
