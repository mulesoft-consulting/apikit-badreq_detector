package com.mulesoft.ps.util;

import org.mule.api.MessagingException;
import org.mule.api.annotations.Connector;
import org.mule.api.annotations.Processor;
import org.mule.api.annotations.param.Default;
import org.mule.module.apikit.exception.BadRequestException;

import java.util.List;

@Connector(name = "apikit-badrequest-extractor",
        friendlyName = "Parse APIKit Bad Request Message",
        minMuleVersion = "3.8.3")
public class ApikitBadRequestDetector {
    @Processor(friendlyName = "Parse Field Details")
    public List<FieldError> parse(@Default("#[exception]") MessagingException exception) {
        Throwable cause = exception.getCause();
        assert cause instanceof BadRequestException;
        return parse((BadRequestException) cause);
    }

    public List<FieldError> parse(BadRequestException exception) {
        return ExceptionTransformer.transform(exception);
    }
}
