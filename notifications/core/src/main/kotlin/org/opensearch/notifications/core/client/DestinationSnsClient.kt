/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.core.client

import com.amazonaws.AmazonServiceException
import com.amazonaws.SdkBaseException
import com.amazonaws.services.sns.AmazonSNS
import com.amazonaws.services.sns.model.AmazonSNSException
import com.amazonaws.services.sns.model.AuthorizationErrorException
import com.amazonaws.services.sns.model.EndpointDisabledException
import com.amazonaws.services.sns.model.InternalErrorException
import com.amazonaws.services.sns.model.InvalidParameterException
import com.amazonaws.services.sns.model.InvalidParameterValueException
import com.amazonaws.services.sns.model.InvalidSecurityException
import com.amazonaws.services.sns.model.KMSAccessDeniedException
import com.amazonaws.services.sns.model.KMSDisabledException
import com.amazonaws.services.sns.model.KMSInvalidStateException
import com.amazonaws.services.sns.model.KMSNotFoundException
import com.amazonaws.services.sns.model.KMSOptInRequiredException
import com.amazonaws.services.sns.model.KMSThrottlingException
import com.amazonaws.services.sns.model.NotFoundException
import com.amazonaws.services.sns.model.PlatformApplicationDisabledException
import com.amazonaws.services.sns.model.PublishResult
import org.opensearch.core.rest.RestStatus
import org.opensearch.notifications.core.NotificationCorePlugin.Companion.LOG_PREFIX
import org.opensearch.notifications.core.credentials.SnsClientFactory
import org.opensearch.notifications.core.utils.logger
import org.opensearch.notifications.spi.model.DestinationMessageResponse
import org.opensearch.notifications.spi.model.MessageContent
import org.opensearch.notifications.spi.model.destination.SnsDestination

/**
 * This class handles the SNS connections to the given Destination.
 */
class DestinationSnsClient(private val snsClientFactory: SnsClientFactory) {

    companion object {
        private val log by logger(DestinationSnsClient::class.java)
    }

    fun execute(destination: SnsDestination, message: MessageContent, referenceId: String): DestinationMessageResponse {
        val amazonSNS: AmazonSNS = snsClientFactory.createSnsClient(destination.region, destination.roleArn)
        return try {
            val result = sendMessage(amazonSNS, destination, message)
            DestinationMessageResponse(RestStatus.OK.status, "Success, message id: ${result.messageId}")
        } catch (exception: InvalidParameterException) {
            DestinationMessageResponse(RestStatus.BAD_REQUEST.status, getSnsExceptionText(exception))
        } catch (exception: InvalidParameterValueException) {
            DestinationMessageResponse(RestStatus.BAD_REQUEST.status, getSnsExceptionText(exception))
        } catch (exception: InternalErrorException) {
            DestinationMessageResponse(RestStatus.INTERNAL_SERVER_ERROR.status, getSnsExceptionText(exception))
        } catch (exception: NotFoundException) {
            DestinationMessageResponse(RestStatus.NOT_FOUND.status, getSnsExceptionText(exception))
        } catch (exception: EndpointDisabledException) {
            DestinationMessageResponse(RestStatus.LOCKED.status, getSnsExceptionText(exception))
        } catch (exception: PlatformApplicationDisabledException) {
            DestinationMessageResponse(RestStatus.SERVICE_UNAVAILABLE.status, getSnsExceptionText(exception))
        } catch (exception: AuthorizationErrorException) {
            DestinationMessageResponse(RestStatus.UNAUTHORIZED.status, getSnsExceptionText(exception))
        } catch (exception: KMSDisabledException) {
            DestinationMessageResponse(RestStatus.PRECONDITION_FAILED.status, getSnsExceptionText(exception))
        } catch (exception: KMSInvalidStateException) {
            DestinationMessageResponse(RestStatus.PRECONDITION_FAILED.status, getSnsExceptionText(exception))
        } catch (exception: KMSNotFoundException) {
            DestinationMessageResponse(RestStatus.PRECONDITION_FAILED.status, getSnsExceptionText(exception))
        } catch (exception: KMSOptInRequiredException) {
            DestinationMessageResponse(RestStatus.PRECONDITION_FAILED.status, getSnsExceptionText(exception))
        } catch (exception: KMSThrottlingException) {
            DestinationMessageResponse(RestStatus.TOO_MANY_REQUESTS.status, getSnsExceptionText(exception))
        } catch (exception: KMSAccessDeniedException) {
            DestinationMessageResponse(RestStatus.UNAUTHORIZED.status, getSnsExceptionText(exception))
        } catch (exception: InvalidSecurityException) {
            DestinationMessageResponse(RestStatus.UNAUTHORIZED.status, getSnsExceptionText(exception))
        } catch (exception: AmazonSNSException) {
            DestinationMessageResponse(RestStatus.FAILED_DEPENDENCY.status, getSnsExceptionText(exception))
        } catch (exception: AmazonServiceException) {
            DestinationMessageResponse(RestStatus.FAILED_DEPENDENCY.status, getServiceExceptionText(exception))
        } catch (exception: SdkBaseException) {
            DestinationMessageResponse(RestStatus.FAILED_DEPENDENCY.status, getSdkExceptionText(exception))
        }
    }

    /**
     * Create error string from Amazon SNS Exceptions
     * @param exception SNS Exception
     * @return generated error string
     */
    private fun getSnsExceptionText(exception: AmazonSNSException): String {
        log.info("$LOG_PREFIX:SnsException $exception")
        return "SNS Send Error(${exception.statusCode}), SNS status:(${exception.errorType.name})${exception.errorCode}:${exception.errorMessage}"
    }

    /**
     * Create error string from Amazon Service Exceptions
     * @param exception Amazon Service Exception
     * @return generated error string
     */
    private fun getServiceExceptionText(exception: AmazonServiceException): String {
        log.info("$LOG_PREFIX:SnsException $exception")
        return "SNS service Error(${exception.statusCode}), Service status:(${exception.errorType.name})${exception.errorCode}:${exception.errorMessage}"
    }

    /**
     * Create error string from Amazon SDK Exceptions
     * @param exception SDK Exception
     * @return generated error string
     */
    private fun getSdkExceptionText(exception: SdkBaseException): String {
        log.info("$LOG_PREFIX:SdkException $exception")
        return "SNS sdk Error, SDK status:${exception.message}"
    }

    /*
     * This method is useful for mocking the client
     */
    @Throws(Exception::class)
    fun sendMessage(amazonSNS: AmazonSNS, destination: SnsDestination, message: MessageContent): PublishResult {
        return amazonSNS.publish(destination.topicArn, message.textDescription, message.title)
    }
}
