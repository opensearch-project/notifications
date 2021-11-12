/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import { EuiTableSortingType } from '@elastic/eui';
import { Pagination } from '@elastic/eui/src/components/basic_table/pagination_bar';
import { render } from '@testing-library/react';
import { configure } from 'enzyme';
import Adapter from 'enzyme-adapter-react-16';
import React from 'react';
import { NotificationItem } from '../../../../models/interfaces';
import { MOCK_DATA } from '../../../../test/mocks/mockData';
import { NotificationsTable } from '../components/NotificationsTable/NotificationsTable';
import { DEFAULT_PAGE_SIZE_OPTIONS } from '../utils/constants';

describe('<NotificationsTable /> spec', () => {
  configure({ adapter: new Adapter() });

  const sorting: EuiTableSortingType<NotificationItem> = {
    sort: {
      direction: 'asc',
      field: 'last_updated_time_ms',
    },
  };

  it('renders the component', () => {
  const pagination: Pagination = {
    pageIndex: 0,
    pageSize: 5,
    pageSizeOptions: DEFAULT_PAGE_SIZE_OPTIONS,
    totalItemCount: 0,
  };

    const utils = render(
      <NotificationsTable
        items={[]}
        onTableChange={() => {}}
        pagination={pagination}
        sorting={sorting}
        loading={false}
      />
    );
    expect(utils.container.firstChild).toMatchSnapshot();
  });

  it('renders the component with items', () => {
  const pagination: Pagination = {
    pageIndex: 0,
    pageSize: 5,
    pageSizeOptions: DEFAULT_PAGE_SIZE_OPTIONS,
    totalItemCount: 20,
  };

    const utils = render(
      <NotificationsTable
        items={MOCK_DATA.notifications.items}
        onTableChange={() => {}}
        pagination={pagination}
        sorting={sorting}
        loading={false}
      />
    );
    expect(utils.container.firstChild).toMatchSnapshot();
  });

});
