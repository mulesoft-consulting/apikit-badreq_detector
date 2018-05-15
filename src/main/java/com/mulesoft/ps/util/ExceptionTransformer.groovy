package com.mulesoft.ps.util

import org.mule.module.apikit.exception.BadRequestException

import java.util.regex.Pattern

class ExceptionTransformer {
    private static final Pattern requiredPattern = Pattern.compile(/Missing required field "(.*?)"/)
    private static final Pattern invalidTypePattern = Pattern.compile(/Invalid type (\S+), expected (\S+)/)
    private static final Pattern invalidFormatPattern = Pattern.compile(
            /Provided value (.*) (is not compliant with the format.*)/)
    private static final String UNKNOWN_FIELD_NAME = '(Unknown field name)'

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
                return new FieldError(UNKNOWN_FIELD_NAME,
                                      "Expected type '${matcher.group(2)}' but got '${matcher.group(1)}'")
            }
            matcher = invalidFormatPattern.matcher(errorForField)
            if (matcher.find()) {
                return new FieldError(UNKNOWN_FIELD_NAME,
                                      "Provided value '${matcher.group(1)}' ${matcher.group(2)}")
            }
        }.sort { FieldError error ->
            error.fieldName
        }
    }
}
