<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:sl12="http://www.buergerkarte.at/namespaces/securitylayer/1.2#">
  <xsl:output media-type="text/html"/>
  <xsl:template match="/">
    <html>
      <body>
        <xsl:apply-templates/>
      </body>
    </html>
  </xsl:template>
  <xsl:template match="ok">
  Alles Roger
  </xsl:template>
  <xsl:template match="/sl12:NullOperationResponse">
  NullKommaJosef
  </xsl:template>
</xsl:stylesheet>