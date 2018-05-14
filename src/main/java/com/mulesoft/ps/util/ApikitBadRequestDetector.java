package com.mulesoft.ps.util;

import org.mule.api.MuleEvent;
import org.mule.api.annotations.Connector;
import org.mule.api.annotations.Processor;

@Connector(name = "apikit-badrequest-extractor",
        friendlyName = "Parse APIKit Bad Request Message",
        minMuleVersion = "3.8.3")
public class ApikitBadRequestDetector {
    @Processor(friendlyName = "Extract Field Details")
    public Object extract(MuleEvent inputEvent) {
        return null;
    }
}
