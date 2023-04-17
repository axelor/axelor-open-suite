import Service from '../../Services/index';

export function searchRecordsByField(field, searchValue, options = {}) {
  let columns = [{ name: field.targetName }];
  if (field.targetSearch) {
    columns = field.targetSearch.map(name => ({ name }));
  }

  return Service.search(field.target, {
    ...options,
    limit: 10,
    offset: 0,
    fields: columns.map(x => x.name),
    data: searchValue
      ? columns.reduce((data, field) => ({ ...data, [field.name]: searchValue }), {
          criteria: undefined,
          operator: undefined,
        })
      : {},
  });
}
