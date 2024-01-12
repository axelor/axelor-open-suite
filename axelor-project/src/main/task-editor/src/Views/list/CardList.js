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
import { Droppable } from 'react-beautiful-dnd';

import InnerCardList from './InnerCardList';

const SPACING = 8;
const SCROLLABLE_COLUMN_HEIGHT = 200;

export default function CardList({ disabled, scrollableColumn, gutterBottom, column }) {
  const { canDrop = true } = column;

  return (
    <Droppable droppableId={`${column.id}`} isDropDisabled={!canDrop || disabled}>
      {provided => (
        <div
          style={{
            display: 'flex',
            flexDirection: 'column',
            backgroundColor: column.id === -1 ? 'white' : 'inherit',
          }}
          {...provided.droppableProps}
        >
          <div
            style={{
              marginBottom: gutterBottom ? 60 : SPACING / 2,
              marginTop: SPACING / 2,
              ...(scrollableColumn
                ? {
                    overflowX: 'hidden',
                    overflowY: 'auto',
                    padding: SPACING,
                    maxHeight: `${SCROLLABLE_COLUMN_HEIGHT}px`,
                  }
                : {}),
            }}
            ref={provided.innerRef}
          >
            <InnerCardList disabled={disabled} column={column} />
            {provided.placeholder}
          </div>
        </div>
      )}
    </Droppable>
  );
}

CardList.defaultProps = {
  disabled: false,
  scrollableColumn: false,
  gutterBottom: false,
  column: {},
  CardListStyle: {},
};
