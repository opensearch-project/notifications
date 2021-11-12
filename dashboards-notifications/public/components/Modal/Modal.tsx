/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import React, { Component, createContext } from 'react';

const ModalContext = createContext({
  component: null,
  props: {},
  onShow: (component: any, props: object) => {},
  onClose: () => {},
});

const ModalConsumer = ModalContext.Consumer;

class ModalProvider extends Component {
  state = { component: null, props: {} };

  onShow = (component: any, props: object): void => {
    this.setState({
      component,
      props,
    });
  };

  onClose = (): void => {
    this.setState({
      component: null,
      props: {},
    });
  };

  render() {
    return (
      <ModalContext.Provider
        value={{ ...this.state, onShow: this.onShow, onClose: this.onClose }}
      >
        {this.props.children}
      </ModalContext.Provider>
    );
  }
}

export { ModalConsumer, ModalProvider };
