package com.mulesoft.ps.util

import groovy.json.JsonOutput
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import org.mule.DefaultMuleEvent
import org.mule.DefaultMuleMessage
import org.mule.MessageExchangePattern
import org.mule.api.MuleContext
import org.mule.api.MuleEvent
import org.mule.api.transport.PropertyScope
import org.mule.config.spring.SpringXmlConfigurationBuilder
import org.mule.construct.Flow
import org.mule.context.DefaultMuleContextFactory
import org.mule.module.apikit.exception.BadRequestException

import static groovy.test.GroovyAssert.shouldFail
import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat

class ApikitBadRequestDetectorTest {
    private static Flow flow
    private static MuleContext muleContext

    @BeforeClass
    static void setup() {
        def factory = new DefaultMuleContextFactory()
        def configBuilder = new SpringXmlConfigurationBuilder('stuff.xml')
        muleContext = factory.createMuleContext(configBuilder)
        muleContext.start()
        flow = muleContext.registry.get('api-main') as Flow
    }

    @AfterClass
    static void cleanup() {
        muleContext.stop()
        muleContext.dispose()
    }

    static MuleEvent getEvent(Map payload) {
        def message = new DefaultMuleMessage(JsonOutput.toJson(payload), muleContext)
        def inboundProp = { String prop, String value ->
            message.setProperty(prop,
                                value,
                                PropertyScope.INBOUND)
        }
        def httpProp = { String prop, String value ->
            inboundProp("http.${prop}",
                        value)
        }
        httpProp('listener.path', '/')
        httpProp('relative.path', '/')
        httpProp('request.path', '/test')
        httpProp('request.uri', '/test')
        httpProp('method', 'POST')
        inboundProp('host', 'localhost')
        inboundProp('Content-Type', 'application/json')
        new DefaultMuleEvent(message,
                             MessageExchangePattern.REQUEST_RESPONSE,
                             flow)
    }

    @Test
    void missing_field() {
        // arrange
        def inputEvent = getEvent([howdy: 123])
        def messageException = shouldFail {
            flow.process(inputEvent)
        }
        def badRequestException = messageException.cause as BadRequestException
        def connector = new ApikitBadRequestDetector()

        // act
        def errors = connector.parse(badRequestException)

        // assert
        assert errors.size() == 1
        def error = errors[0]
        assertThat error.fieldName,
                   is(equalTo('prop1'))
        assertThat error.reason,
                   is(equalTo('field is required and is missing'))
    }

    @Test
    void field_invalid_type() {
        // arrange

        // act

        // assert
        fail 'write this'
    }

    @Test
    void non_parseable_errors() {
        // arrange

        // act

        // assert
        fail 'write this'
    }
}
