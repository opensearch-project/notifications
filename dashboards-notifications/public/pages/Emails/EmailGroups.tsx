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
import { RecipientGroupsTable } from './components/tables/RecipientGroupsTable';
import { SendersTable } from './components/tables/SendersTable';

interface EmailGroupsProps extends RouteComponentProps {}

export function EmailGroups(props: EmailGroupsProps) {
  const context = useContext(CoreServicesContext)!;
  useEffect(() => {
    context.chrome.setBreadcrumbs([
      BREADCRUMBS.NOTIFICATIONS,
      BREADCRUMBS.EMAIL_GROUPS,
    ]);
    window.scrollTo(0, 0);
  }, []);

  return (
    <>
      <EuiTitle size="l">
        <h1>Email senders and groups</h1>
      </EuiTitle>

      <EuiSpacer />
      <SendersTable />

      <EuiSpacer />
      <RecipientGroupsTable />
    </>
  );
}
