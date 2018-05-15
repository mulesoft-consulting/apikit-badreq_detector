package com.mulesoft.ps.util

import org.mule.module.apikit.exception.BadRequestException

import java.util.regex.Pattern

class ExceptionTransformer {
    private static final Pattern requiredPattern = Pattern.compile(/.*Missing required field "(.*?)"/)

    static List<FieldError> transform(BadRequestException badRequestException) {
        def message = badRequestException.message
        def matcher = requiredPattern.matcher(message)
        def results = []
        if (matcher.matches()) {
            results << new FieldError(matcher.group(1),
                                      'field is required and is missing')
        }
        results
    }
}
