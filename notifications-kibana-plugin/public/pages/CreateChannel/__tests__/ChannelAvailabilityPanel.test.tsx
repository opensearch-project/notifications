/*
 * Copyright 2021 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

import React from 'react';
import { fireEvent, render } from '@testing-library/react';
import { ChannelAvailabilityPanel } from '../components/ChannelAvailabilityPanel';
import { NOTIFICATION_SOURCE } from '../../../utils/constants';

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
