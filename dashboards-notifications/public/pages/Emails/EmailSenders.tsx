/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 *
 * Modifications Copyright OpenSearch Contributors. See
 * GitHub history for details.
 */

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

import { EuiSpacer, EuiTitle } from '@elastic/eui';
import React, { useContext, useEffect } from 'react';
import { RouteComponentProps } from 'react-router-dom';
import { CoreServicesContext } from '../../components/coreServices';
import { BREADCRUMBS } from '../../utils/constants';
import { MainContext } from '../Main/Main';
import { SendersTable } from './components/tables/SendersTable';
import { SESSendersTable } from './components/tables/SESSendersTable';

interface EmailSendersProps extends RouteComponentProps {}

export function EmailSenders(props: EmailSendersProps) {
  const coreContext = useContext(CoreServicesContext)!;
  const mainStateContext = useContext(MainContext)!;

  useEffect(() => {
    coreContext.chrome.setBreadcrumbs([
      BREADCRUMBS.NOTIFICATIONS,
      BREADCRUMBS.EMAIL_SENDERS,
    ]);
    window.scrollTo(0, 0);
  }, []);

  return (
    <>
      <EuiTitle size="l">
        <h1>Email senders</h1>
      </EuiTitle>

      {mainStateContext.availableConfigTypes.includes('smtp_account') && (
        <>
          <EuiSpacer />
          <SendersTable coreContext={coreContext} />
        </>
      )}

      {/* UI currently does not fully handle this condition, adding it just to avoid flashing */}
      {mainStateContext.availableConfigTypes.includes('ses_account') && (
        <>
          <EuiSpacer />
          <SESSendersTable coreContext={coreContext} />
        </>
      )}
    </>
  );
}
