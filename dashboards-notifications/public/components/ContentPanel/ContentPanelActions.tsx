/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import React from 'react';
import { EuiFlexGroup, EuiFlexItem } from '@elastic/eui';

interface ContentPanelActionsProps {
  actions: {
    component: React.ReactNode;
    flexItemProps?: object;
  }[];
}

const ContentPanelActions: React.SFC<ContentPanelActionsProps> = ({
  actions,
}) => (
  <EuiFlexGroup justifyContent="spaceBetween" alignItems="center">
    {actions.map(({ component, flexItemProps = {} }, index) => {
      return (
        <EuiFlexItem {...flexItemProps} grow={false} key={index}>
          {component}
        </EuiFlexItem>
      );
    })}
  </EuiFlexGroup>
);

export default ContentPanelActions;
