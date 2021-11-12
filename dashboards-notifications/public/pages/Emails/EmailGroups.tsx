/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import { EuiSpacer, EuiTitle } from '@elastic/eui';
import React, { useContext, useEffect } from 'react';
import { RouteComponentProps } from 'react-router-dom';
import { CoreServicesContext } from '../../components/coreServices';
import { BREADCRUMBS } from '../../utils/constants';
import { RecipientGroupsTable } from './components/tables/RecipientGroupsTable';

interface EmailGroupsProps extends RouteComponentProps {}

export function EmailGroups(props: EmailGroupsProps) {
  const coreContext = useContext(CoreServicesContext)!;
  useEffect(() => {
    coreContext.chrome.setBreadcrumbs([
      BREADCRUMBS.NOTIFICATIONS,
      BREADCRUMBS.EMAIL_GROUPS,
    ]);
    window.scrollTo(0, 0);
  }, []);

  return (
    <>
      <EuiTitle size="l">
        <h1>Email recipient groups</h1>
      </EuiTitle>

      <EuiSpacer />
      <RecipientGroupsTable coreContext={coreContext} />
    </>
  );
}
