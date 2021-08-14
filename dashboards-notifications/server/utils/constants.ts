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

import { CountersType } from './types';

export const WINDOW = 3600;
export const INTERVAL = 60;
export const CAPACITY = (WINDOW / INTERVAL) * 2;

export const GLOBAL_BASIC_COUNTER: CountersType = {
  config: {
    create: {
      total: 0,
    },
    delete: {
      total: 0,
    },
    info: {
      total: 0,
    },
    list: {
      total: 0,
    },
    update: {
      total: 0,
    }
  },
  event: {
    info: {
      total: 0,
    },
    list: {
      total: 0,
    },
  },
  feature: {
    list: {
      total: 0,
    },
  },
  send_test_message: {
    info: {
      total: 0,
    },
  },
  slack: {
    total: 0
  },
  chime: {
    total: 0
  },
  email: { 
    total: 0
  }, 
  custom_webhook: {
    total: 0
  },
  ses: {
    total: 0
  },
  sns: {
    total: 0
  },
  smtp_account: {
    total: 0
  },
  email_group: {
    total: 0
  },
  reports: {
    total: 0
  },
  alerting: {
    total: 0
  },
  index_management: {
    total: 0
  }
}

export const DEFAULT_ROLLING_COUNTER: CountersType = {
  config: {
    create: {
      count: 0,
      system_error: 0,
      user_error: 0,
    },
    delete: {
      count: 0,
      system_error: 0,
      user_error: 0,
    },
    info: {
      count: 0,
      system_error: 0,
      user_error: 0,
    },
    list: {
      count: 0,
      system_error: 0,
      user_error: 0,
    },
    update: {
      count: 0,
      system_error: 0,
      user_error: 0,
    }
  },
  event: {
    info: {
      count: 0,
      system_error: 0,
      user_error: 0,
    },
    list: {
      count: 0,
      system_error: 0,
      user_error: 0,
    },
  },
  feature: {
    list: {
      count: 0,
      system_error: 0,
      user_error: 0,
    },
  },
  send_test_message: {
    info: {
      count: 0,
      system_error: 0,
      user_error: 0,
    },
  },
  slack: {
    count: 0,
    system_error: 0,
    user_error: 0,
  },
  chime: {
    count: 0,
    system_error: 0,
    user_error: 0,
  },
  email: { 
    count: 0,
    system_error: 0,
    user_error: 0,
  }, 
  custom_webhook: {
    count: 0,
    system_error: 0,
    user_error: 0,
  },
  ses: {
    count: 0,
    system_error: 0,
    user_error: 0,
  },
  sns: {
    count: 0,
    system_error: 0,
    user_error: 0,
  },
  smtp_account: {
    count: 0,
    system_error: 0,
    user_error: 0,
  },
  email_group: {
    count: 0,
    system_error: 0,
    user_error: 0,
  },
  reports: {
    count: 0,
    system_error: 0,
    user_error: 0,
  },
  alerting: {
    count: 0,
    system_error: 0,
    user_error: 0,
  },
  index_management: {
    count: 0,
    system_error: 0,
    user_error: 0,
  }
}