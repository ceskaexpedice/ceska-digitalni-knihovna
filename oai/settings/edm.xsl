<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="2.0"
                xmlns:saxon="http://saxon.sf.net/"
		xmlns:edm="http://www.europeana.eu/schemas/edm/"
		xmlns:europeana="http://www.europeana.eu/schemas/ese/"
                exclude-result-prefixes="saxon"    
		xmlns:dc="http://purl.org/dc/elements/1.1/"
		xmlns:ore="http://www.openarchives.org/ore/terms/"
		xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
		xmlns:dcterms="http://purl.org/dc/terms/"
		xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/"
		xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
		xmlns:skos="http://www.w3.org/2004/02/skos/core#"
		xmlns:rdaGr2="http://rdvocab.info/ElementsGr2/"
		xsi:schemaLocation="http://www.europeana.eu/schemas/edm/ http://purl.org/dc/elements/1.1/ http://www.dublincore.org/schemas/xmls/qdc/dc.xsd http://purl.org/dc/terms/ http://www.dublincore.org/schemas/xmls/qdc/dcterms.xsd http://www.openarchives.org/OAI/2.0/oai_dc/ http://www.openarchives.org/OAI/2.0/oai_dc.xsd"
    >

    <xsl:import href="oai.xsl"/>

    <xsl:template name="header">
        <header>
            <identifier>
		<xsl:value-of select="$doc//str[@name='PID']"/>
            </identifier>
            <datestamp>
                <xsl:call-template name="datestamp">
                    <xsl:with-param name="solrdate" select="$doc//date[@name='modified_date']"/>
                </xsl:call-template>
            </datestamp>
                <setSpec>
                    <xsl:value-of select="$doc//str[@name='fedora.model']"/>
                </setSpec>
        </header>
    </xsl:template>

    <xsl:template name="metadata">
	<xsl:variable name="pid" select="$doc//str[@name='PID']" />
	<xsl:variable name="existsDoc" select="document(concat('http://localhost:9080/oaitransform/transformed?pid=', $pid,'&amp;action=exists'))"/>
	<xsl:variable name="exists" select="$existsDoc/exists/text()"/>
	<metadata>
		<xsl:choose>
			<xsl:when test="$exists = 'NO'">
			</xsl:when>
			<xsl:otherwise>
				<xsl:param name="vc" select="$doc//arr[@name='collection']/str[1]/text()"/>
				<xsl:variable name="dc" select="document(concat('http://localhost:9080/oaitransform/transformed?pid=', $pid,'&amp;action=dc'))" />
				<xsl:variable name="agent" select="document(concat('http://localhost:9080/oaitransform/transformed?pid=', $pid,'&amp;action=agent'))" />
				<rdf:RDF>
					<edm:ProvidedCHO>
						<xsl:attribute name="rdf:about"><xsl:value-of select="$pid"/></xsl:attribute>
						<!-- commented because different transformation 		
						<xsl:copy-of select="document(concat($kramerius_url, 'api/v5.0/item/', $pid, '/streams/DC'))/oai_dc:dc/*" />
						-->
	
						<xsl:variable name="creators" select="document(concat('http://localhost:9080/oaitransform/transformed?pid=', $pid,'&amp;action=dc'))/oai_dc:dc/dc:creator"/>
						<xsl:variable name="amount" select="count($creators)" />
		
						<xsl:choose>
							<xsl:when test="$amount != 0">
								<xsl:copy-of select="$dc/oai_dc:dc/*[following-sibling::dc:creator[$amount]]"/>
								<xsl:copy-of select="$agent/edm:record/dc:creators/*"/>
								<xsl:copy-of select="$dc/oai_dc:dc/*[preceding-sibling::dc:creator[$amount]]"/>
							</xsl:when>
							<xsl:otherwise>
								<xsl:copy-of select="$agent/edm:record/dc:creators/*"/>
								<xsl:copy-of select="$dc/oai_dc:dc/*"/>
							</xsl:otherwise>
						</xsl:choose>
		
						<xsl:choose>
							<xsl:when test="$dc/oai_dc:dc/dc:type/text() = 'model:graphic' or $dc/oai_dc:dc/dc:type/text() = 'model:map'">
								<edm:type>IMAGE</edm:type>
							</xsl:when>
							<xsl:when test="$dc/oai_dc:dc/dc:type/text() = 'model:soundrecording'">
								<edm:type>SOUND</edm:type>
							</xsl:when>
							<xsl:otherwise>
								<edm:type>TEXT</edm:type>
							</xsl:otherwise>
						</xsl:choose>
					</edm:ProvidedCHO>

					<edm:WebResource>
						<xsl:attribute name="rdf:about"><xsl:value-of select="document(concat('http://localhost:9080/oaitransform/transformed?pid=',     $pid,'&amp;action=europeana'))/edm:record/edm:object/@rdf:resource"/></xsl:attribute>
					</edm:WebResource>

					<ore:Aggregation>
						<xsl:attribute name="rdf:about"><xsl:value-of select="concat($kramerius_url, 'handle/', $pid)"/></xsl:attribute>
						<edm:aggregatedCHO>
							<xsl:attribute name="rdf:resource"><xsl:value-of select="$pid"/></xsl:attribute>
						</edm:aggregatedCHO>

						<!-- Puvodni, ale podle pravidel Europeany dataProvider max:1 min:1, ale obcas se muze najit titul, ktery patri do vice knihoven viz uuid:18dc4c30-1f67-11e3-a5bb-005056827e52 -> NDK a KNAV
						<xsl:if test="$doc//arr[@name='collection']">
							<xsl:for-each select="$doc//arr[@name='collection']/str">
								<xsl:variable name="vcpid" select="text()" />
								<xsl:if test="normalize-space($providers/provider[@pid=$vcpid]/value/text()) != ''">
									<edm:dataProvider><xsl:value-of select="$providers/provider[@pid=$vcpid]/value/text()"/></edm:dataProvider>
								</xsl:if>
							</xsl:for-each>		  
						</xsl:if> -->
	
						<xsl:if test="$doc//arr[@name='collection']">
							<xsl:variable name="temp">
								<xsl:for-each select="$doc//arr[@name='collection']/str">
									<xsl:variable name="vcpid" select="text()" />
									<xsl:if test="normalize-space($providers/provider[@pid=$vcpid]/value/text()) != ''">
										<xsl:value-of select="$providers/provider[@pid=$vcpid]/value/text()"/>
										<xsl:value-of select="';'"/> 
									</xsl:if>
								</xsl:for-each>
							</xsl:variable>
							<xsl:if test="$temp != ''">
								<edm:dataProvider><xsl:value-of select="substring-before($temp, ';')" /></edm:dataProvider>
							</xsl:if>
						</xsl:if>
	      
						<!-- isShownAt, isShownBy, rights, object -->
						<xsl:copy-of select="document(concat('http://localhost:9080/oaitransform/transformed?pid=',     $pid,'&amp;action=europeana'))/edm:record/*"/>
						
						<edm:provider>Czech digital library/Česká digitální knihovna</edm:provider>
	  
					</ore:Aggregation>
	
					<xsl:copy-of select="$agent/edm:record/edm:agents/*"/>
        
				</rdf:RDF>
			</xsl:otherwise>
		</xsl:choose>
	</metadata>
    </xsl:template>

    <xsl:template name="about"/>

</xsl:stylesheet>
