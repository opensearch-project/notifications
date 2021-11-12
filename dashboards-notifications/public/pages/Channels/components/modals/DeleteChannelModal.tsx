/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import {
  EuiButton,
  EuiButtonEmpty,
  EuiFieldText,
  EuiFlexGroup,
  EuiFlexItem,
  EuiModal,
  EuiModalBody,
  EuiModalFooter,
  EuiModalHeader,
  EuiModalHeaderTitle,
  EuiOverlayMask,
  EuiSpacer,
  EuiText,
} from '@elastic/eui';
import React, { useContext, useState } from 'react';
import { SERVER_DELAY } from '../../../../../common';
import { ChannelItemType } from '../../../../../models/interfaces';
import { CoreServicesContext } from '../../../../components/coreServices';
import { ModalRootProps } from '../../../../components/Modal/ModalRoot';

interface DeleteChannelModalProps extends ModalRootProps {
  selected: ChannelItemType[];
  refresh?: () => void;
  href?: string;
  onClose: () => void;
}

export const DeleteChannelModal = (props: DeleteChannelModalProps) => {
  if (!props.selected.length) return null;

  const coreContext = useContext(CoreServicesContext)!;
  const [input, setInput] = useState('');
  const num = props.selected.length;
  const name = num >= 2 ? `${num} channels` : props.selected[0].name;
  const message = `Delete ${
    num >= 2 ? 'the following channels' : name
  } permanently? Any notify actions will no longer be able to send notifications using ${
    num >= 2 ? 'these channels' : 'this channel'
  }.`;

  return (
    <EuiOverlayMask>
      <EuiModal onClose={props.onClose} maxWidth={500}>
        <EuiModalHeader>
          <EuiModalHeaderTitle>{`Delete ${name}?`}</EuiModalHeaderTitle>
        </EuiModalHeader>
        <EuiModalBody>
          <EuiText>{message}</EuiText>
          {num >= 2 && (
            <>
              <EuiSpacer />
              {props.selected.map((channel, i) => (
                <EuiText
                  key={`channel-list-item-${i}`}
                  style={{ marginLeft: 20 }}
                >
                  <li>{channel.name}</li>
                </EuiText>
              ))}
            </>
          )}
          <EuiSpacer />
          <EuiText>
            To confirm delete, type <i>delete</i> in the field.
          </EuiText>
          <EuiFieldText
            placeholder="delete"
            value={input}
            onChange={(e) => setInput(e.target.value)}
          />
        </EuiModalBody>
        <EuiModalFooter>
          <EuiFlexGroup justifyContent="flexEnd">
            <EuiFlexItem grow={false}>
              <EuiButtonEmpty onClick={props.onClose}>Cancel</EuiButtonEmpty>
            </EuiFlexItem>
            <EuiFlexItem grow={false}>
              <EuiButton
                fill
                data-test-subj="delete-channel-modal-delete-button"
                color="danger"
                onClick={async () => {
                  props.services.notificationService
                    .deleteConfigs(
                      props.selected.map((channel) => channel.config_id)
                    )
                    .then((resp) => {
                      coreContext.notifications.toasts.addSuccess(
                        `${
                          props.selected.length > 1
                            ? props.selected.length + ' channels'
                            : 'Channel ' + props.selected[0].name
                        } successfully deleted.`
                      );
                      props.onClose();
                      if (props.href)
                        setTimeout(
                          () => (location.hash = props.href!),
                          SERVER_DELAY
                        );
                      else if (props.refresh)
                        setTimeout(() => props.refresh!(), SERVER_DELAY);
                    })
                    .catch((error) => {
                      coreContext.notifications.toasts.addError(error?.body || error, {
                        title: 'Failed to delete one or more channels.',
                      });
                      props.onClose();
                    });
                }}
                disabled={input !== 'delete'}
              >
                Delete
              </EuiButton>
            </EuiFlexItem>
          </EuiFlexGroup>
        </EuiModalFooter>
      </EuiModal>
    </EuiOverlayMask>
  );
};
