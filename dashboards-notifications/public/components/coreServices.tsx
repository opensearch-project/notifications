/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import { createContext } from 'react';
import { CoreStart } from '../../../../src/core/public';

const CoreServicesContext = createContext<CoreStart | null>(null);

const CoreServicesConsumer = CoreServicesContext.Consumer;

export { CoreServicesContext, CoreServicesConsumer };
