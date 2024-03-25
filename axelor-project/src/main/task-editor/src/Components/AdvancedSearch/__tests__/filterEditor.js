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
import { act } from 'react-dom/test-utils';
import { mount } from 'enzyme';

import {
  List,
  ListItem,
  Button,
  RadioGroup,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
} from '@material-ui/core';

import withRoot from '../../../withRoot';
import FilterEditor, { AddCriteria } from '../filterEditor';
import { ButtonLink, BooleanRadio, BooleanCheckBox, SimpleButton } from '../common';
import Select from '../select';
import Selection from '../../form/selection';
import TextField from '../../form/input/input';
import DateTimePicker from '../../form/input/datetime-picker';

import NumberField from '../../form/input/number';

const fields = [
  {
    name: 'confirmDate',
    type: 'DATE',
    title: 'Confirm date',
  },
  {
    defaultValue: '0',
    precision: 20,
    name: 'totalAmount',
    scale: 2,
    type: 'DECIMAL',
    title: 'Total amount',
  },
  {
    defaultValue: '0',
    precision: 20,
    name: 'orderAmount',
    scale: 4,
    type: 'INTEGER',
    title: 'Amount',
  },
  {
    name: 'notes',
    type: 'TEXT',
    title: 'Notes',
  },
  {
    sequence: true,
    readonly: true,
    name: 'name',
    type: 'STRING',
    title: 'Name',
  },
  {
    defaultValue: '0',
    precision: 20,
    name: 'taxAmount',
    scale: 4,
    type: 'DECIMAL',
    title: 'Tax amount',
  },
  {
    defaultValue: false,
    name: 'confirmed',
    type: 'BOOLEAN',
    title: 'Confirmed',
  },
  {
    name: 'orderDate',
    type: 'DATE',
    title: 'Order date',
    required: true,
  },
  {
    targetSearch: [],
    name: 'orderItems',
    type: 'ONE_TO_MANY',
    title: 'Items',
    mappedBy: 'order',
    target: 'com.axelor.sale.db.OrderLine',
  },
  {
    targetName: 'name',
    targetSearch: ['name', 'code'],
    name: 'circles',
    type: 'MANY_TO_MANY',
    title: 'Circles',
    target: 'com.axelor.contact.db.Circle',
  },
  {
    name: 'status',
    enumType: 'com.axelor.sale.db.OrderStatus',
    selectionList: [
      {
        value: 'DRAFT',
        data: {
          value: 'draft',
        },
        title: 'Draft',
      },
      {
        value: 'OPEN',
        data: {
          value: 'open',
        },
        title: 'Open',
      },
      {
        value: 'CLOSED',
        data: {
          value: 'closed',
        },
        title: 'Closed',
      },
      {
        value: 'CANCELED',
        data: {
          value: 'canceled',
        },
        title: 'Canceled',
      },
    ],
    type: 'ENUM',
    title: 'Status',
  },
  {
    targetName: 'fullName',
    targetSearch: ['fullName', 'firstName', 'lastName'],
    name: 'customer',
    type: 'MANY_TO_ONE',
    title: 'Customer',
    required: true,
    target: 'com.axelor.contact.db.Contact',
  },
];

const defaultCustomCriteria = {
  operator: 'and',
  isArchived: false,
  criteria: [
    { fieldName: 'name', operator: 'eq', value: 'john' },
    { fieldName: 'lastName', operator: 'eq', value: 'smith' },
    { fieldName: 'amount', operator: 'gt', value: '1000' },
    { fieldName: 'dob', operator: 'eq', value: '12/08/2019' },
    { fieldName: 'status', operator: 'eq', value: 'open' },
  ],
  title: 'total greater than 1k',
  isShare: false,
};

describe('filter editor', () => {
  let wrapper, FilterEditorComponent, onSave, onDelete, clearFilters, applyFiltersClick;

  function FilterEditorWrapper(props) {
    const [customCriteria, setCustomCriteria] = React.useState(props.customCriteria || defaultCustomCriteria);
    return <FilterEditor {...props} customCriteria={customCriteria} setCustomCriteria={setCustomCriteria} />;
  }

  beforeEach(() => {
    onSave = jest.fn();
    onDelete = jest.fn();
    clearFilters = jest.fn();
    applyFiltersClick = jest.fn();

    FilterEditorComponent = withRoot(props => <FilterEditorWrapper {...props} />);

    wrapper = mount(
      <FilterEditorComponent
        onSave={onSave}
        onDelete={onDelete}
        fields={fields}
        clearFilters={clearFilters}
        applyFiltersClick={applyFiltersClick}
      />,
    );
  });

  it('should render filter editor', () => {
    expect(wrapper.find(BooleanRadio).length).toBe(1);
    expect(wrapper.find(BooleanCheckBox).length).toBe(2);
    expect(wrapper.find(List).length).toBe(1);
    expect(wrapper.find(ListItem).length).toBe(defaultCustomCriteria.criteria.length);
    expect(wrapper.find(ButtonLink).length).toBe(5);
    expect(wrapper.find(SimpleButton).length).toBe(3);
  });

  it('should open dialog', () => {
    act(() => {
      wrapper.find(SimpleButton).filter('.delete').props().onClick();
    });
    wrapper.update();
    expect(wrapper.find(Dialog).length).toBe(1);
    expect(wrapper.find(DialogTitle).length).toBe(1);
    expect(wrapper.find(DialogContent).length).toBe(1);
    expect(wrapper.find(DialogActions).length).toBe(1);

    act(() => {
      wrapper.find(Dialog).props().onClose();
    });
    wrapper.update();
    expect(wrapper.find(Dialog).props().open).toBe(false);
  });

  it('should close dialog', () => {
    act(() => {
      wrapper.find(SimpleButton).filter('.delete').props().onClick();
    });
    wrapper.update();
    expect(wrapper.find(Dialog).length).toBe(1);
    expect(wrapper.find(DialogTitle).length).toBe(1);
    expect(wrapper.find(DialogContent).length).toBe(1);
    expect(wrapper.find(DialogActions).length).toBe(1);

    act(() => {
      wrapper.find(Button).filter('.cancel').props().onClick();
    });
    wrapper.update();
    expect(wrapper.find(Dialog).props().open).toBe(false);
  });

  it('should render isShare', () => {
    expect(wrapper.find(BooleanCheckBox).filter('.share').length).toBe(1);
    act(() => {
      wrapper.find(BooleanCheckBox).filter('.share').props().onChange({ name: 'isShare', checked: true });
    });

    wrapper.update();
    expect(wrapper.find(BooleanCheckBox).filter('.share').props().value).toBe(true);
  });

  it('should render isArchived', () => {
    expect(wrapper.find(BooleanCheckBox).filter('.archived').length).toBe(1);
    act(() => {
      wrapper.find(BooleanCheckBox).filter('.archived').props().onChange({ name: 'isArchived', checked: true });
    });

    wrapper.update();
    expect(wrapper.find(BooleanCheckBox).filter('.archived').props().value).toBe(true);
  });

  it('should render title', () => {
    expect(wrapper.find(TextField).filter('.title').length).toBe(1);
    act(() => {
      wrapper.find(TextField).filter('.title').props().onChange('age > 20');
    });
    wrapper.update();
    expect(wrapper.find(TextField).filter('.title').props().value).toBe('age > 20');
  });

  it('should render operator', () => {
    expect(wrapper.find(BooleanRadio).length).toBe(1);

    act(() => {
      wrapper
        .find(RadioGroup)
        .props()
        .onChange({ target: { name: 'operator', value: 'or' } });
    });

    wrapper.update();
    expect(wrapper.find(RadioGroup).props().value).toBe('or');
  });

  it('should call applyFilterClick', () => {
    wrapper.find(ButtonLink).filter('.apply-filter').props().onClick();
    expect(applyFiltersClick).toHaveBeenCalled();
  });

  it('should render AddCriteria', () => {
    expect(wrapper.find(AddCriteria).length).toBe(5);
    act(() => {
      wrapper.find(AddCriteria).at(0).props().onChange({ name: 'value', value: 'smith' });
    });

    wrapper.update();
    expect(wrapper.find(TextField).at(0).props().value).toBe('smith');

    act(() => {
      wrapper.find(AddCriteria).at(1).props().onRemove();
    });
    wrapper.update();
    expect(wrapper.find(AddCriteria).length).toBe(4);

    wrapper = mount(
      <FilterEditorComponent
        customCriteria={{
          operator: 'and',
          isArchived: false,
          criteria: [{ fieldName: 'name', operator: 'eq', value: 'john' }],
          title: 'total greater than 1k',
          isShare: false,
        }}
        onSave={onSave}
        onDelete={onDelete}
        fields={fields}
        clearFilters={clearFilters}
        applyFiltersClick={applyFiltersClick}
      />,
    );

    act(() => {
      wrapper.find(AddCriteria).at(0).props().onRemove();
    });
    wrapper.update();
    expect(wrapper.find(AddCriteria).length).toBe(1);
    expect(
      wrapper
        .find(Select)

        .filter('.fieldName')
        .props().valaue,
    ).toBe(undefined);
  });

  it('should call onChange of SelectWrapper', () => {
    act(() => {
      wrapper.find(Select).filter('.fieldName').at(0).props().onChange('orderAmount');
    });
    wrapper.update();
    expect(wrapper.find(Select).filter('.fieldName').at(0).props().value).toBe('orderAmount');
  });

  it('should call onAddCriteria', () => {
    act(() => {
      wrapper.find(ButtonLink).filter('.add-filter').props().onClick();
    });
    wrapper.update();
    expect(wrapper.find(AddCriteria).length).toBe(6);
  });

  it('should call onDeleteFilter', () => {
    act(() => {
      wrapper.find(SimpleButton).filter('.delete').props().onClick();
    });
    wrapper.update();
    act(() => {
      wrapper.find(Button).filter('.confirm-delete').props().onClick();
    });

    expect(onDelete).toHaveBeenCalled();
  });

  it('should call onSaveFilter', () => {
    act(() => {
      wrapper.find(SimpleButton).filter('.save').props().onClick();
    });
    wrapper.update();
    expect(onSave).toHaveBeenCalled();

    wrapper = mount(
      <FilterEditorComponent
        customCriteria={{
          id: 1,
          version: 0,
          operator: 'and',
          isArchived: false,
          criteria: [{ fieldName: 'name', operator: 'eq', value: 'john' }],
          title: 'total greater than 1k',
          isShare: false,
        }}
        onSave={onSave}
        onDelete={onDelete}
        fields={fields}
        clearFilters={clearFilters}
        applyFiltersClick={applyFiltersClick}
      />,
    );
    act(() => {
      wrapper.find(SimpleButton).filter('.save').props().onClick();
    });
    wrapper.update();
    expect(wrapper.find(AddCriteria).length).toBe(1);
  });

  it('should call onClearFilter', () => {
    act(() => {
      wrapper.find(ButtonLink).filter('.clear').props().onClick();
    });
    expect(clearFilters).toHaveBeenCalled();
  });

  it('should call getDefaultValue for date field', () => {
    act(() => {
      wrapper.find(AddCriteria).at(1).props().onChange({ name: 'fieldName', value: 'confirmDate' });
      wrapper.find(AddCriteria).at(1).props().onChange({ name: 'operator', value: 'eq' });
    });
    wrapper.update();
    expect(wrapper.find(Select).filter('.fieldName').at(1).props().value).toBe('confirmDate');
    expect(wrapper.find(Select).filter('.operator').at(1).props().value).toBe('eq');

    expect(wrapper.find(AddCriteria).at(1).find(DateTimePicker).props().value).toBe(null);
  });

  it('should call getDefaultValue for number field', () => {
    act(() => {
      wrapper.find(AddCriteria).at(1).props().onChange({ name: 'fieldName', value: 'totalAmount' });
      wrapper.find(AddCriteria).at(1).props().onChange({ name: 'operator', value: 'eq' });
    });
    wrapper.update();
    expect(wrapper.find(Select).filter('.fieldName').at(1).props().value).toBe('totalAmount');
    expect(wrapper.find(Select).filter('.operator').at(1).props().value).toBe('eq');

    expect(wrapper.find(AddCriteria).at(1).find(NumberField).props().value).toBe(0);
  });

  it('should call getDefaultValue for relational field', () => {
    act(() => {
      wrapper.find(AddCriteria).at(1).props().onChange({ name: 'fieldName', value: 'customer' });
      wrapper.find(AddCriteria).at(1).props().onChange({ name: 'operator', value: 'in' });
    });
    wrapper.update();
    expect(wrapper.find(Select).filter('.fieldName').at(1).props().value).toBe('customer');
    expect(wrapper.find(Select).filter('.operator').at(1).props().value).toBe('in');
    expect(wrapper.find(AddCriteria).at(1).find(Selection).props().value).toBe(null);
  });

  it('should call getDefaultValue for string field', () => {
    act(() => {
      wrapper.find(AddCriteria).at(1).props().onChange({ name: 'fieldName', value: 'name' });
      wrapper.find(AddCriteria).at(1).props().onChange({ name: 'operator', value: 'eq' });
    });
    wrapper.update();
    expect(wrapper.find(Select).filter('.fieldName').at(1).props().value).toBe('name');
    expect(wrapper.find(Select).filter('.operator').at(1).props().value).toBe('eq');
    expect(wrapper.find(AddCriteria).at(1).find(TextField).props().value).toBe('');
  });

  it('should call onChange of Selection', () => {
    act(() => {
      wrapper.find(Select).filter('.fieldName').at(0).props().onChange('circles');
    });
    wrapper.update();
    act(() => {
      wrapper.find(Select).filter('.operator').at(0).props().onChange('in');
    });
    wrapper.update();
    act(() => {
      wrapper
        .find(Selection)
        .props()
        .onChange([
          { id: 1, title: 'friend' },
          { id: 2, title: 'business' },
        ]);
    });
    wrapper.update();
    expect(wrapper.find(Selection).props().value.length).toBe(2);

    // act(() => {
    //   wrapper
    //     .find(Select)
    //     .filter('.fieldName')
    //     .at(0)
    //     .props()
    //     .onChange('customer');
    // });
    // wrapper.update();
    // act(() => {
    //   wrapper
    //     .find(Select)
    //     .filter('.operator')
    //     .at(0)
    //     .props()
    //     .onChange('in');
    // });
    // wrapper.update();
    // act(() => {
    //   wrapper
    //     .find(Selection)
    //     .props()
    //     .onChange([{ id: 1, title: 'friend' }, { id: 2, title: 'business' }]);
    // });

    wrapper.find(Selection).props().fetchAPI({ search: 'a' });
  });

  it('should render range field', () => {
    act(() => {
      wrapper.find(Select).filter('.fieldName').at(0).props().onChange('totalAmount');
    });
    wrapper.update();
    act(() => {
      wrapper.find(Select).filter('.operator').at(0).props().onChange('between');
    });
    wrapper.update();
    expect(wrapper.find(NumberField).length).toBe(2);
    act(() => {
      wrapper.find(NumberField).at(0).props().onChange('500');
    });
    wrapper.update();
    expect(wrapper.find(NumberField).at(0).props().value).toBe('500');
    act(() => {
      wrapper.find(NumberField).at(1).props().onChange('12345');
    });
    wrapper.update();
    expect(wrapper.find(NumberField).at(1).props().value).toBe('12345');
  });

  it('should render simple field', () => {
    act(() => {
      wrapper.find(Select).filter('.fieldName').at(0).props().onChange('totalAmount');
    });
    wrapper.update();
    act(() => {
      wrapper.find(Select).filter('.operator').at(0).props().onChange('eq');
    });
    wrapper.update();
    expect(wrapper.find(NumberField).length).toBe(1);
    act(() => {
      wrapper.find(NumberField).at(0).props().onChange('500');
    });
    wrapper.update();
    expect(wrapper.find(NumberField).at(0).props().value).toBe('500');
  });

  it('should render relational field', () => {
    act(() => {
      wrapper.find(Select).filter('.fieldName').at(0).props().onChange('customer');
    });
    wrapper.update();
    act(() => {
      wrapper.find(Select).filter('.operator').at(0).props().onChange('like');
    });
    wrapper.update();
    act(() => {
      wrapper.find(TextField).at(0).props().onChange('tushar');
    });
    wrapper.update();
    expect(wrapper.find(TextField).at(0).props().value).toBe('tushar');
  });
});
