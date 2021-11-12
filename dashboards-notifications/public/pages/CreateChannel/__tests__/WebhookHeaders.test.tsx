/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import { fireEvent, render } from '@testing-library/react';
import { configure } from 'enzyme';
import Adapter from 'enzyme-adapter-react-16';
import React from 'react';
import { WebhookHeaders } from '../components/WebhookHeaders';

describe('<WebhookHeaders /> spec', () => {
  configure({ adapter: new Adapter() });

  it('renders the empty component', () => {
    const headerUtils = render(
      <WebhookHeaders headers={[]} setHeaders={() => {}} type="header" />
    );
    expect(headerUtils.container.firstChild).toMatchSnapshot();

    const parameterUtils = render(
      <WebhookHeaders headers={[]} setHeaders={() => {}} type="header" />
    );
    expect(parameterUtils.container.firstChild).toMatchSnapshot();
  });

  it('renders the component', () => {
    const headers = [
      { key: 'Content-Type', value: 'application/json' },
      { key: 'key2', value: 'value2' },
      { key: 'key3', value: '' },
      { key: '', value: 'value4' },
      { key: '', value: '' },
      { key: 'key6', value: 'value6' },
    ];
    const headerUtils = render(
      <WebhookHeaders headers={headers} setHeaders={() => {}} type="header" />
    );
    expect(headerUtils.container.firstChild).toMatchSnapshot();

    const parameterUtils = render(
      <WebhookHeaders
        headers={headers}
        setHeaders={() => {}}
        type="parameter"
      />
    );
    expect(parameterUtils.container.firstChild).toMatchSnapshot();
  });

  it('changes input', () => {
    const headers = [{ key: 'key1', value: 'value1' }];
    const setHeaders = jest.fn();
    const parameterUtils = render(
      <WebhookHeaders
        headers={headers}
        setHeaders={setHeaders}
        type="parameter"
      />
    );

    fireEvent.change(parameterUtils.getByDisplayValue('key1'), {
      target: { value: 'test-key' },
    });
    expect(setHeaders).toBeCalledWith([{ key: 'test-key', value: 'value1' }]);

    fireEvent.change(parameterUtils.getByDisplayValue('value1'), {
      target: { value: 'test-value' },
    });
    expect(setHeaders).toBeCalledWith([
      { key: 'test-key', value: 'test-value' },
    ]);

    parameterUtils.getByText('Remove parameter').click();
    parameterUtils.getByText('Add parameter').click();
  });
});
