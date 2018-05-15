package com.mulesoft.ps.util

import org.mule.module.apikit.exception.BadRequestException

import java.util.regex.Pattern

class ExceptionTransformer {
    private static final Pattern requiredPattern = Pattern.compile(/Missing required field "(.*?)"/)
    private static final Pattern invalidTypePattern = Pattern.compile(/Invalid type (\S+), expected (\S+)/)

    static List<FieldError> transform(BadRequestException badRequestException) {
        badRequestException.message.split("\n")
                .collect { String errorForField ->
            def matcher = requiredPattern.matcher(errorForField)
            if (matcher.find()) {
                return new FieldError(matcher.group(1),
                                      'field is required and is missing')
            }
            matcher = invalidTypePattern.matcher(errorForField)
            if (matcher.find()) {
                return new FieldError('(Unknown field name)',
                                      "Expected type '${matcher.group(2)}' but got '${matcher.group(1)}'")
            }
        }.sort { FieldError error ->
            error.fieldName
        }
    }
}
