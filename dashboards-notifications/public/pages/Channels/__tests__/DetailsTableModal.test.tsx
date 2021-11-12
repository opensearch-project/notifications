/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import { configure, mount } from 'enzyme';
import Adapter from 'enzyme-adapter-react-16';
import React from 'react';
import { notificationServiceMock } from '../../../../test/mocks/serviceMock';
import { DetailsTableModal } from '../components/modals/DetailsTableModal';

describe('<DetailsTableModal /> spec', () => {
  configure({ adapter: new Adapter() });

  it('renders parameters', () => {
    const onClose = jest.fn();
    const items = [
      { key: 'test key', value: 'test value' },
      { key: null, value: '' },
    ];
    const wrap = mount(
      <DetailsTableModal
        header="test header"
        isParameters={true}
        items={items}
        onClose={onClose}
        services={notificationServiceMock}
      />
    );
    expect(wrap).toMatchSnapshot();
  });

  it('renders headers', () => {
    const onClose = jest.fn();
    const items = [
      { key: 'test key', value: 'test value' },
      { key: undefined, value: '' },
    ];
    const wrap = mount(
      <DetailsTableModal
        header="test header"
        isParameters={false}
        items={items}
        onClose={onClose}
        services={notificationServiceMock}
      />
    );
    expect(wrap).toMatchSnapshot();
  });
});
