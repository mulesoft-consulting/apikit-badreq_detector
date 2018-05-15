package com.mulesoft.ps.util

import groovy.json.JsonOutput
import org.junit.Test
import org.mule.DefaultMuleEvent
import org.mule.DefaultMuleMessage
import org.mule.MessageExchangePattern
import org.mule.api.transport.PropertyScope
import org.mule.config.spring.SpringXmlConfigurationBuilder
import org.mule.construct.Flow
import org.mule.context.DefaultMuleContextFactory

class ApikitBadRequestDetectorTest {
    @Test
    void parse() {
        // arrange
        def factory = new DefaultMuleContextFactory()
        def configBuilder = new SpringXmlConfigurationBuilder('stuff.xml')
        def muleContext = factory.createMuleContext(configBuilder)
        muleContext.start()
        def message = new DefaultMuleMessage(JsonOutput.toJson([howdy: 123]), muleContext)
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
        def flow = muleContext.registry.get('api-main') as Flow
        def event = new DefaultMuleEvent(message,
                                         MessageExchangePattern.REQUEST_RESPONSE,
                                         flow)
        flow.process(event)
        def connector = new ApikitBadRequestDetector()

        // act
        connector.parse()

        // assert
        fail 'write this'
    }
}
