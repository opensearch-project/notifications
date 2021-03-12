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
import { CreateRecipientGroup } from '../CreateRecipientGroup';
import { CoreServicesContext } from '../../../components/coreServices';
import { coreServicesMock } from '../../../../test/mocks/serviceMock';
import { routerComponentPropsMock } from '../../../../test/mocks/routerPropsMock';

describe('<CreateRecipientGroup/> spec', () => {
  it('renders the component', () => {
    const utils = render(
      <CoreServicesContext.Provider value={coreServicesMock}>
        <CreateRecipientGroup {...routerComponentPropsMock} />
      </CoreServicesContext.Provider>
    );
    expect(utils.container.firstChild).toMatchSnapshot();
  });

  it('renders the component for editing', () => {
    const utils = render(
      <CoreServicesContext.Provider value={coreServicesMock}>
        <CreateRecipientGroup {...routerComponentPropsMock} edit={true} />
      </CoreServicesContext.Provider>
    );
    expect(utils.container.firstChild).toMatchSnapshot();
  });
});
