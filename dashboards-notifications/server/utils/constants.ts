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
  feature: {
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
  send_test_message: {
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
  slack: {
    send: {
      total: 0
    }
  },
  chime: {
    send: {
      total: 0
    }
  },
  email: { 
    send: {
      total: 0
    }
  }, 
  webhook: {
    send: {
      total: 0
    }
  },
  ses: {
    send: {
      total: 0
    }
  },
  sns: {
    send: {
      total: 0
    }
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
  feature: {
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
  send_test_message: {
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
  slack: {
    send: {
      count: 0,
      system_error: 0,
      user_error: 0,
    }
  },
  chime: {
    send: {
      count: 0,
      system_error: 0,
      user_error: 0,
    }
  },
  email: { 
    send: {
      count: 0,
      system_error: 0,
      user_error: 0,
    }
  }, 
  webhook: {
    send: {
      count: 0,
      system_error: 0,
      user_error: 0,
    }
  },
  ses: {
    send: {
      count: 0,
      system_error: 0,
      user_error: 0,
    }
  },
  sns: {
    send: {
      count: 0,
      system_error: 0,
      user_error: 0,
    }
  }
}