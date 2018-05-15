package com.mulesoft.ps.util

import org.mule.module.apikit.exception.BadRequestException

import java.util.regex.Pattern

class ExceptionTransformer {
    private static final Pattern requiredPattern = Pattern.compile(/Missing required field "(.*?)"/,
                                                                   Pattern.DOTALL)
    private static final Pattern invalidTypePattern = Pattern.compile(/Invalid type (\S+), expected (\S+)/,
                                                                      Pattern.DOTALL)

    static List<FieldError> transform(BadRequestException badRequestException) {
        def message = badRequestException.message
        def matcher = requiredPattern.matcher(message)
        def results = []
        while (matcher.find()) {
            results << new FieldError(matcher.group(1),
                                      'field is required and is missing')
        }
        matcher = invalidTypePattern.matcher(message)
        while (matcher.find()) {
            results << new FieldError('(Unknown field name)',
                                      "Expected type '${matcher.group(2)}' but got '${matcher.group(1)}'")
        }
        results
    }
}
