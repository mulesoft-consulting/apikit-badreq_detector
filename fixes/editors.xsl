<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:tooling="http://www.mulesoft.org/schema/mule/tooling.attributes">

    <xsl:template match="tooling:modeSwitch[@description='Operation.']">
        <xsl:copy>
            <xsl:apply-templates select="@*" />
            <xsl:attribute name="defaultValue">http://www.mulesoft.org/schema/mule/apikit-badrequest-extractor/extract</xsl:attribute>
            <xsl:apply-templates select="node()" />
        </xsl:copy>
    </xsl:template>

    <xsl:template match="@* | node()">
        <xsl:copy>
            <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
    </xsl:template>
</xsl:stylesheet>