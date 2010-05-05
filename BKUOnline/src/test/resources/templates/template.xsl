<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:saml="urn:oasis:names:tc:SAML:1.0:assertion" xmlns:pr="http://reference.e-government.gv.at/namespace/persondata/20020228#" exclude-result-prefixes="pr saml">
    <xsl:output method="xml" xml:space="default" omit-xml-declaration="yes"/>
    <xsl:param name="givenName"/>
    <xsl:param name="familyName"/>
    <xsl:param name="dateOfBirth"/>
    <xsl:param name="url"/>
    <xsl:param name="identifierType"/>
    <xsl:param name="identifierValue"/>
    <xsl:param name="date"/>
    <xsl:param name="time"/>
    <xsl:template match="/" xmlns="http://www.w3.org/1999/xhtml">
        <html>
            <head>
                <title>Signatur der Anmeldedaten</title>
                <style type="text/css" media="screen">.boldstyle { font-weight: bold; } .italicstyle { font-style: italic; } .annotationstyle { font-size: small; } .graybground {background-color: #E0E0E0;}
							.titlestyle{ text-decoration:underline; font-weight:bold; font-family: Verdana;  font-size: x-small; } 
							.h4style{ font-family: Verdana; }
              table.parameters { font-size: x-small; }                                                                        
              </style>
            </head>
            <body>																					
                <h4 class="h4style">Anmeldedaten:</h4>																					
                
                <p class="titlestyle">Daten zur Person</p>																					
                <table class="parameters">                		    						
                    <xsl:if test="$familyName">
                        <tr>
                            <td class="italicstyle">Name:</td>
                            <td>
                                <xsl:value-of select="$givenName"/>
                                <xsl:text> </xsl:text>
                                <xsl:value-of select="$familyName"/>
                            </td>
                        </tr>
                    </xsl:if>
                    <xsl:if test="$dateOfBirth">
                        <tr>
                            <td class="italicstyle">Geburtsdatum:</td>
                            <td>
                                <xsl:value-of select="$dateOfBirth"/>
                            </td>
                        </tr>
                    </xsl:if>
                </table>
                
                <p class="titlestyle">Technische Parameter</p>     	
                <table class="parameters">														 
                    <tr>
                        <td class="italicstyle">URL:</td>
                        <td><xsl:value-of select="$url"/></td>
                    </tr>
                    <xsl:if test="starts-with($identifierType,'urn:publicid:gv.at:cdid+')">
                        <tr>
                            <td class="italicstyle">Bereich:</td>
                            <td><xsl:value-of select="substring-after($identifierType, 'urn:publicid:gv.at:cdid+')"/></td>
                        </tr>
                    </xsl:if>
                    <xsl:if test="starts-with($identifierType, 'urn:publicid:gv.at:wbpk+')">
                        <tr>
                            <td class="italicstyle"><xsl:value-of select="substring-before(substring-after($identifierType, 'urn:publicid:gv.at:wbpk+'), '+')"/></td>
                            <td><xsl:value-of select="substring-after(substring-after($identifierType, 'urn:publicid:gv.at:wbpk+'), '+')"/></td>
                        </tr>
                    </xsl:if>    
                    <xsl:if test="$identifierValue">
                        <tr>    						
                            <td class="italicstyle">Identifikator:</td>
                            <td><xsl:value-of select="$identifierValue"/>
                            </td>			      															
                        </tr>
                    </xsl:if>  
                    <tr>
                        <td class="italicstyle">Datum</td>
                        <td>
                            <xsl:value-of select="$date"/>
                        </td>
                    </tr>
                    <tr>
                        <td class="italicstyle">Uhrzeit</td>
                        <td>
                            <xsl:value-of select="$time"/>
                        </td>
                    </tr>
                </table>																																			
            </body>
        </html>
    </xsl:template>
</xsl:stylesheet>
