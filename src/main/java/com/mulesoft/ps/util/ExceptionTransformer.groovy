package com.mulesoft.ps.util

import org.mule.module.apikit.exception.BadRequestException

import java.util.regex.Matcher
import java.util.regex.Pattern

class ExceptionTransformer {
    private static final Pattern requiredPattern = Pattern.compile(/Missing required field "(.*?)"/)
    private static final Pattern invalidTypePattern = Pattern.compile(/Invalid type (\S+), expected (\S+)/)
    private static final Pattern invalidFormatPattern = Pattern.compile(
            /Provided value (.*) (is not compliant with the format.*)/)
    private static final Pattern invalidElementPattern = Pattern.compile(/Invalid element (.*)\./)
    private static final Pattern invalidValuePattern = Pattern.compile(/Invalid value .*/)
    private static final String UNKNOWN_FIELD_NAME = '(Unknown field name)'

    static List<FieldError> transform(BadRequestException badRequestException) {
        badRequestException.message.split("\n")
                .collect { String errorForField ->
            Matcher matcher = null
            def matcherCheck = { Pattern pattern ->
                matcher = pattern.matcher(errorForField)
                return matcher.find()
            }
            if (matcherCheck(requiredPattern)) {
                return new FieldError(matcher.group(1),
                                      'field is required and is missing')
            }
            if (matcherCheck(invalidTypePattern)) {
                return new FieldError(UNKNOWN_FIELD_NAME,
                                      "Expected type '${matcher.group(2)}' but got '${matcher.group(1)}'")
            }
            if (matcherCheck(invalidFormatPattern)) {
                return new FieldError(UNKNOWN_FIELD_NAME,
                                      "Provided value '${matcher.group(1)}' ${matcher.group(2)}")
            }
            if (matcherCheck(invalidElementPattern)) {
                return new FieldError(UNKNOWN_FIELD_NAME,
                                      "Invalid element '${matcher.group(1)}'. Did you supply the wrong type for a formatted field?")
            }
            if (matcherCheck(invalidValuePattern)) {
                return new FieldError(UNKNOWN_FIELD_NAME,
                                      matcher.group(0))
            }
            return new FieldError(UNKNOWN_FIELD_NAME,
                                  'Unable to parse error, raw details: ' + errorForField)
        }.sort { FieldError error ->
            error.fieldName
        }
    }
}
