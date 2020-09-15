/*
 * Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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
 *
 */

package com.amazon.opendistroforelasticsearch.notification.channel

import com.amazon.opendistroforelasticsearch.notification.core.ChannelMessage
import com.amazon.opendistroforelasticsearch.notification.core.ChannelMessageResponse
import org.elasticsearch.rest.RestStatus
import java.io.IOException
import javax.mail.MessagingException
import javax.mail.internet.AddressException

object SesChannel : NotificationChannel {
    private const val FROM_ADDRESS = "from@email.com" // TODO: Get from configuration
    override fun sendMessage(refTag: String, recipient: String, channelMessage: ChannelMessage): ChannelMessageResponse {
        try {
            val mimeMessage = EmailMimeProvider.prepareMimeMessage(FROM_ADDRESS, recipient, channelMessage)
        } catch (addressException: AddressException) {
            return ChannelMessageResponse(RestStatus.BAD_REQUEST, "recipient parsing failed with status:${addressException.message}")
        } catch (messagingException: MessagingException) {
            return ChannelMessageResponse(RestStatus.FAILED_DEPENDENCY, "Email message creation failed with status:${messagingException.message}")
        } catch (ioException: IOException) {
            return ChannelMessageResponse(RestStatus.FAILED_DEPENDENCY, "Email message creation failed with status:${ioException.message}")
        }
        return ChannelMessageResponse(RestStatus.OK, "Success")
    }
}
