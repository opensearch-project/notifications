/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import { fireEvent, render } from '@testing-library/react';
import React from 'react';
import { NOTIFICATION_SOURCE } from '../../../utils/constants';
import { ChannelAvailabilityPanel } from '../components/ChannelAvailabilityPanel';

describe('<ChannelAvailabilityPanel/> spec', () => {
  it('renders the component', () => {
    const setSourceCheckboxIdToSelectedMap = jest.fn() as any;
    const utils = render(
      <ChannelAvailabilityPanel
        sourceCheckboxIdToSelectedMap={{}}
        setSourceCheckboxIdToSelectedMap={setSourceCheckboxIdToSelectedMap}
      />
    );
    expect(utils.container.firstChild).toMatchSnapshot();
  });

  it('selects options', () => {
    const setSourceCheckboxIdToSelectedMap = jest.fn() as any;
    const utils = render(
      <ChannelAvailabilityPanel
        sourceCheckboxIdToSelectedMap={{}}
        setSourceCheckboxIdToSelectedMap={setSourceCheckboxIdToSelectedMap}
      />
    );

    const [key, value] = Object.entries(NOTIFICATION_SOURCE).slice(0, 1)[0];
    const checkbox = utils.getByText(value);
    fireEvent.click(checkbox);
    expect(setSourceCheckboxIdToSelectedMap).toBeCalledWith({ [key]: true });
  });
});
