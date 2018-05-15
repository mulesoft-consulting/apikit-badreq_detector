# Purpose

APIKit router (and the RAML libraries it depends on) does a decent job of validating incoming request payload. It does not do a great job of returning which fields failed validation in a parseable format. This connector attempts to parse the `BadRequestException` message and provide those details.

# Usage

1. Install the connector from Exchange.
2. Drop the connector in the HTTP 400 section of the apikit exception handler catch strategy.
3. Transform the connector's output as appropriate.
