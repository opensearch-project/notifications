package org.opensearch.notifications.model

import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.io.stream.StreamOutput
import org.opensearch.common.io.stream.Writeable
import org.opensearch.common.xcontent.ToXContent
import org.opensearch.common.xcontent.XContentBuilder
import org.opensearch.common.xcontent.XContentParser
import org.opensearch.commons.notifications.action.BaseResponse
import org.opensearch.commons.notifications.action.SendNotificationResponse
import java.io.IOException

class SendTestNotificationResponse : BaseResponse  {
    private val sendNotificationResponse: SendNotificationResponse

    companion object {

        /**
         * reader to create instance of class from writable.
         */
        val reader = Writeable.Reader { SendTestNotificationResponse(it) }

        /**
         * Creator used in REST communication.
         * @param parser XContentParser to deserialize data from.
         */
        @JvmStatic
        @Throws(IOException::class)
        fun parse(parser: XContentParser): SendTestNotificationResponse {
            return SendTestNotificationResponse(SendNotificationResponse.parse(parser))
        }
    }

    constructor(sendNotificationResponse: SendNotificationResponse) : super() {
        this.sendNotificationResponse = sendNotificationResponse
    }

    /**
     * {@inheritDoc}
     */
    @Throws(IOException::class)
    constructor(input: StreamInput) : super(input) {
        this.sendNotificationResponse = SendNotificationResponse(input)
    }


    override fun toXContent(p0: XContentBuilder?, p1: ToXContent.Params?): XContentBuilder {
        return sendNotificationResponse.toXContent(p0, p1)
    }

    override fun writeTo(p0: StreamOutput) {
        sendNotificationResponse.writeTo(p0)
    }
}