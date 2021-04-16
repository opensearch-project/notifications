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

import { fireEvent, render } from '@testing-library/react';
import React from 'react';
import { RouteComponentProps } from 'react-router-dom';
import { coreServicesMock } from '../../../../test/mocks/serviceMock';
import { CoreServicesContext } from '../../../components/coreServices';
import { ChannelDetails } from '../ChannelDetails';

describe('<ChannelDetails/> spec', () => {
  it('renders the component', () => {
    const props = { match: { params: { id: 'test' } } };
    const utils = render(
      <CoreServicesContext.Provider value={coreServicesMock}>
        <ChannelDetails {...(props as RouteComponentProps<{ id: string }>)} />
      </CoreServicesContext.Provider>
    );
    expect(utils.container.firstChild).toMatchSnapshot();
  });

  it('clicks delete button', () => {
    const props = { match: { params: { id: 'test' } } };
    const utils = render(
      <CoreServicesContext.Provider value={coreServicesMock}>
        <ChannelDetails {...(props as RouteComponentProps<{ id: string }>)} />
      </CoreServicesContext.Provider>
    );

    const button = utils.getByText('Delete');
    fireEvent.click(button);
    expect(utils.container.firstChild).toMatchSnapshot();
  });

  it('clicks mute or unmute button', () => {
    const props = { match: { params: { id: 'test' } } };
    const utils = render(
      <CoreServicesContext.Provider value={coreServicesMock}>
        <ChannelDetails {...(props as RouteComponentProps<{ id: string }>)} />
      </CoreServicesContext.Provider>
    );

    const button = utils.getByText('ute channel', { exact: false });
    fireEvent.click(button);
    expect(utils.container.firstChild).toMatchSnapshot();
  });
});
