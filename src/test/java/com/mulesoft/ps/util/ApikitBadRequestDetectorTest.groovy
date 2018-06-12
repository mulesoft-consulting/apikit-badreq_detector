package com.mulesoft.ps.util

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import org.mule.DefaultMuleEvent
import org.mule.DefaultMuleMessage
import org.mule.MessageExchangePattern
import org.mule.api.MuleContext
import org.mule.api.MuleEvent
import org.mule.api.MuleMessage
import org.mule.api.transport.PropertyScope
import org.mule.config.spring.SpringXmlConfigurationBuilder
import org.mule.construct.Flow
import org.mule.context.DefaultMuleContextFactory
import org.mule.module.apikit.exception.BadRequestException

import static groovy.test.GroovyAssert.shouldFail
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.is
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
    void integration() {
        // arrange
        def flow = muleContext.registry.get('api-main-with-strategy') as Flow
        def inputEvent = getEvent([prop2: 'howdy'])

        // act
        def responseEvent = flow.process(inputEvent)
        def message = responseEvent.message
        def errors = message.payload
        assert errors instanceof List<FieldError>

        // assert
        assert errors.size() == 1
        def error = errors[0]
        assertThat error.fieldName,
                is(equalTo('prop1'))
        assertThat error.reason,
                is(equalTo('field is required and is missing'))
        assertThat message.getProperty('http.status', PropertyScope.OUTBOUND),
                is(equalTo(400))
    }

    @Test
    void missing_single_field() {
        // arrange
        def inputEvent = getEvent([prop2: 'howdy'])
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
    void missing_multiple_fields() {
        // arrange
        def inputEvent = getEvent([:])
        def messageException = shouldFail {
            flow.process(inputEvent)
        }
        def badRequestException = messageException.cause as BadRequestException
        def connector = new ApikitBadRequestDetector()

        // act
        def errors = connector.parse(badRequestException)

        // assert
        assert errors.size() == 2
        def error = errors[0]
        assertThat error.fieldName,
                is(equalTo('prop1'))
        assertThat error.reason,
                is(equalTo('field is required and is missing'))
        error = errors[1]
        assertThat error.fieldName,
                is(equalTo('prop2'))
        assertThat error.reason,
                is(equalTo('field is required and is missing'))
    }

    @Test
    void field_invalid_type() {
        // arrange
        def inputEvent = getEvent([prop1: 99,
                                   prop2: 'howdy'])
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
                is(equalTo('(Unknown field name)'))
        assertThat error.reason,
                is(equalTo("Expected type 'String' but got 'Integer'"))
    }

    @Test
    void mixed_error_types() {
        // arrange
        def inputEvent = getEvent([prop2: 99])
        def messageException = shouldFail {
            flow.process(inputEvent)
        }
        def badRequestException = messageException.cause as BadRequestException
        def connector = new ApikitBadRequestDetector()

        // act
        def errors = connector.parse(badRequestException)

        // assert
        assert errors.size() == 2
        def error = errors[0]
        assertThat error.fieldName,
                is(equalTo('(Unknown field name)'))
        assertThat error.reason,
                is(equalTo("Expected type 'String' but got 'Integer'"))
        error = errors[1]
        assertThat error.fieldName,
                is(equalTo('prop1'))
        assertThat error.reason,
                is(equalTo('field is required and is missing'))
    }

    @Test
    void invalid_format_date() {
        // arrange
        def inputEvent = getEvent([prop1: 'howdy',
                                   prop2: 'howdy',
                                   prop3: 'howdy'])
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
                is(equalTo('(Unknown field name)'))
        assertThat error.reason,
                is(equalTo("Provided value 'howdy' is not compliant with the format date_only provided in rfc3339"))
    }

    @Test
    void invalid_format_regex() {
        // arrange
        def inputEvent = getEvent([prop1: 'howdy',
                                   prop2: 'howdy',
                                   prop5: 'howdy'])
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
                is(equalTo('(Unknown field name)'))
        assertThat error.reason,
                is(equalTo("Invalid value 'howdy'. Expected ^.+@.+\\..+\$"))
    }

    @Test
    void invalid_format_multiple() {
        // arrange
        // arrange
        def inputEvent = getEvent([prop1: 'howdy',
                                   prop2: 'howdy',
                                   prop3: 'howdy',
                                   prop4: 'howdy2'])
        def messageException = shouldFail {
            flow.process(inputEvent)
        }
        def badRequestException = messageException.cause as BadRequestException
        def connector = new ApikitBadRequestDetector()

        // act
        def errors = connector.parse(badRequestException)

        // assert
        assert errors.size() == 2
        def error = errors[0]
        assertThat error.fieldName,
                is(equalTo('(Unknown field name)'))
        assertThat error.reason,
                is(equalTo("Provided value 'howdy' is not compliant with the format date_only provided in rfc3339"))
        error = errors[1]
        assertThat error.fieldName,
                is(equalTo('(Unknown field name)'))
        assertThat error.reason,
                is(equalTo("Provided value 'howdy2' is not compliant with the format date_only provided in rfc3339"))
    }

    @Test
    void wrong_type_in_date_field() {
        // arrange
        def inputEvent = getEvent([prop1: 'howdy',
                                   prop2: 'howdy',
                                   prop3: 99])
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
                is(equalTo('(Unknown field name)'))
        assertThat error.reason,
                is(equalTo("Invalid element '99'. Did you supply the wrong type for a formatted field?"))
    }
}
