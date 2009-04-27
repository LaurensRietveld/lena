<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="2.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:f="http://www.w3.org/2004/09/fresnel-tree"
xmlns:fn="http://www.w3.org/2005/xpath-functions"
xmlns:xs="http://www.w3.org/2001/XMLSchema"
xmlns:functx="http://www.functx.com"
xmlns="http://www.w3.org/1999/xhtml"
exclude-result-prefixes="f">

<xsl:function name="functx:escape-for-regex" as="xs:string">
  <xsl:param name="arg" as="xs:string?"/>  
  <xsl:sequence select="fn:replace($arg, '(\.|\[|\]|\\|\||\-|\^|\$|\?|\*|\+|\{|\}|\(|\))','\\$1')"/>
</xsl:function>

<xsl:function name="functx:substring-after-last" as="xs:string">
  <xsl:param name="arg" as="xs:string?"/> 
  <xsl:param name="delim" as="xs:string"/> 
  <xsl:sequence select="fn:replace($arg,fn:concat('^.*',functx:escape-for-regex($delim)),'')"/>
</xsl:function>

<xsl:template match="/">
  <xsl:apply-templates/>
</xsl:template>

<xsl:template match="f:results">		
	<xsl:apply-templates select="f:resource"/>
</xsl:template>


<!-- Set resource -->

<xsl:template match="f:resource">
	<div class="resource-div">
		<xsl:attribute name="style">
    		<xsl:value-of select="@class" />
  		</xsl:attribute>
  		<div class="resource-header" style="padding: 5px 0px 5px 0px; font-weight: bold;">			
			<!-- Set variables of current resource and lens -->
			<xsl:variable name="resource" select="if (fn:exists(@uri)) then @uri else f:title" />
			<xsl:variable name="fresnelLens" select="@lens" />			
			<!-- Selection of resource title --> 	
			<span style="cursor: help;" class="tooltip" title="Resource: {$resource}">
				<xsl:value-of select="f:title" />
			</span>
			<!-- Link to the current resource keeping the lens -->
			<xsl:if test="fn:not(fn:codepoint-equal($fresnelLens, 'http://www.example.org#defaultInstanceLens') or fn:codepoint-equal($fresnelLens, 'http://www.example.org#defaultClassLens'))">			
				<a onclick='setHrefs()' href='?resource={fn:encode-for-uri($resource)}&amp;lens={fn:encode-for-uri($fresnelLens)}' class='tooltip' title='Lens: {fn:substring-after($fresnelLens, "#")} :: Resource: {$resource}'><img src='public/images/link_icon.png' style='border: 0px; margin: 0px 2px;' alt='Icon.' /></a>
			</xsl:if>			
			<!-- Link to default lens -->
			<a onclick='setHrefs()' href='?resource={fn:encode-for-uri($resource)}' class='tooltip' title='Lens: DEFAULT :: Resource: {$resource}'><img src='public/images/rdf_icon.gif' style='width: 16px; border: 0px; margin: 0px 2px;' alt='Icon.' /></a>
			<!-- Link to linked data 
			<xsl:if test="fn:codepoint-equal(functx:substring-after-last($resource, '.'), 'rdf') or fn:codepoint-equal(functx:substring-after-last($resource, '.'), 'xrdf')">
				<a onclick='setHrefs()' href='?uri={fn:encode-for-uri($resource)}' class='tooltip' title=':: Add data from URI: {$resource}'><img src='public/images/sw_cube.png' style='width: 16px; border: 0px; margin: 0px 2px;' alt='W3C Semantic Web Logo' /></a>
			</xsl:if>
			-->			
			<xsl:apply-templates select="f:lens"/>		
		</div>		
		<xsl:apply-templates select="f:property"/>		
	</div>
</xsl:template>


<!-- Check for links to other lenses -->

<xsl:template match="f:lens">
	<xsl:variable name="resource" select="../@uri" />
	<xsl:variable name="fresnelLens" select="." />
	<a onclick='setHrefs()' href='?resource={fn:encode-for-uri($resource)}&amp;lens={fn:encode-for-uri($fresnelLens)}' class='tooltip' title='Lens: {fn:substring-after($fresnelLens, "#")} :: Resource: {$resource}'><img src='public/images/lens_icon.png' style='border: 0px; margin: 0px 2px;' alt='Icon.' /></a>
</xsl:template>


<!-- Set property -->

<xsl:template match="f:property">
	<div>
		<xsl:attribute name="style">
    		overflow: hidden;<xsl:value-of select="@class" />
  		</xsl:attribute>
  	
	  	<!-- Content before property -->
	  	
	  	<span style='vertical-align: top; margin: 0px; padding: 0px;'>
	  		<xsl:value-of select="f:content/f:before"/>
	  	</span>
		
		<table style="border: 0px; width: 100%; padding: 0px; margin: 0px; border-spacing: 0px;">
			<tr style="padding: 0px; margin: 0px; border: 0px;">			
				<!-- If a label exists -->				
				<xsl:if test="f:label">
					<td style="width: 35%; vertical-align: top; padding: 0px; margin: 0px; border: 0px;">
						<div>
							<xsl:attribute name="style">
							  	cursor: help;<xsl:value-of select="f:label/@class" />
							</xsl:attribute>
							<xsl:attribute name="class">
							  	tooltip
							</xsl:attribute>
							<xsl:attribute name="title">
							  	Property: <xsl:value-of select="@uri" />
							</xsl:attribute>
						
							<xsl:if test="fn:normalize-space(f:label/f:title)">							
								<xsl:value-of select="f:label/f:content/f:before"/>
								<xsl:value-of select="f:content/f:before"/>								
								<xsl:value-of select="f:label/f:title"/>			
								<xsl:value-of select="f:content/f:after"/>
								<xsl:value-of select="f:label/f:content/f:after"/>						
							</xsl:if>
							<xsl:if test="fn:not(fn:normalize-space(f:label/f:title))">		
								<xsl:value-of select="f:label/f:content/f:before"/>
								<xsl:value-of select="f:content/f:before"/>							
								<xsl:value-of select="if (fn:not(fn:contains(@uri, '#'))) then (functx:substring-after-last(@uri, '/')) else (functx:substring-after-last(@uri, '#'))" />							
								<xsl:value-of select="f:content/f:after"/>
								<xsl:value-of select="f:label/f:content/f:after"/>						
							</xsl:if>
						</div>
					</td>
				</xsl:if>				
				<!-- If no label exists -->				
				<xsl:if test="fn:not(f:label)">
					<td style="width: 0%; vertical-align: top; padding: 0px; margin: 0px; border: 0px;">
					</td>
				</xsl:if>						
				<td style="width: *; vertical-align: top; padding: 0px; margin: 0px; border: 0px;">
					<xsl:apply-templates select="f:values"/>
				</td>
			</tr>
		</table>		
	</div>
</xsl:template>

<xsl:template match="f:values">
	<div>
		<xsl:attribute name="style">
    		<xsl:value-of select="f:value/@class" />
  		</xsl:attribute>		
		<xsl:apply-templates select="f:value"/>			
	</div>
</xsl:template>

<xsl:template match="f:value">
	<xsl:value-of select="../f:content/f:before"/>
	<!-- The value is a sublens -->	
	<xsl:if test="fn:exists(f:resource/f:property)">
		<xsl:apply-templates select="f:resource"/>
	</xsl:if>
	<!-- The value is NO sublens -->	
	<xsl:if test="fn:not(fn:exists(f:resource/f:property))">	
		<xsl:variable name="resource" select="if (fn:exists(f:resource/@uri)) then f:resource/@uri else f:resource/f:title" />
		<xsl:variable name="fresnelLens" select="f:resource/@lens" />
	
		<!-- The value is defined by fresnel:value -->
		
		<xsl:if test="@output-type">
		
			<xsl:variable name="outputTypeValue" select="@output-type" />
			
			<xsl:choose>
			
				<!-- URI -->
			
	    	<xsl:when test="fn:codepoint-equal($outputTypeValue, 'uri')">
	    		<xsl:if test="fn:exists(f:title)">
	    			<xsl:value-of select="f:title" />
	    		</xsl:if>
	    		<xsl:if test="fn:exists(f:resource/f:title)">
						<span style="cursor: help;" class="tooltip" title="Resource: {$resource}">
	    				<xsl:value-of select="f:resource/f:title" />
	    			</span>
	    			<xsl:if test="f:resource/@lens">
							<xsl:if test="fn:not(fn:codepoint-equal($fresnelLens, 'http://www.example.org#defaultInstanceLens') or fn:codepoint-equal($fresnelLens, 'http://www.example.org#defaultClassLens'))">
	    					<a onclick='setHrefs()' href='?resource={fn:encode-for-uri($resource)}&amp;lens={fn:encode-for-uri($fresnelLens)}' class='tooltip' title='Lens: {fn:substring-after($fresnelLens, "#")} :: Resource: {$resource}'><img src='public/images/link_icon.png' style='border: 0px; margin: 0px 2px;' alt='Icon.' /></a>
	    				</xsl:if>
	    			</xsl:if>
	    			<a onclick='setHrefs()' href='?resource={fn:encode-for-uri($resource)}' class='tooltip' title='Lens: DEFAULT :: Resource: {$resource}'><img src='public/images/rdf_icon.gif' style='width: 15px; border: 0px; margin: 0px 2px;' alt='Icon.' /></a>
	    			
	    			<!-- Link to linked data
						<xsl:if test="fn:codepoint-equal(functx:substring-after-last($resource, '.'), 'rdf') or fn:codepoint-equal(functx:substring-after-last($resource, '.'), 'xrdf')">
							<a onclick='setHrefs()' href='?uri={fn:encode-for-uri($resource)}' class='tooltip' title=':: Add data from URI: {$resource}'><img src='public/images/sw_cube.png' style='width: 16px; border: 0px; margin: 0px 2px;' alt='W3C Semantic Web Logo' /></a>
						</xsl:if>
	    			-->
	    			
	    			<xsl:apply-templates select="f:resource/f:lens"/>	    				    			
	    		</xsl:if>
	    	</xsl:when>
	    	
	    	<!-- IMAGE -->
	    	
	    	<xsl:when test="fn:codepoint-equal($outputTypeValue, 'image')">
	    		<xsl:variable name="outputImage" select="f:resource/f:title" />
	    		<span style="cursor: help;" class="tooltip" title="Resource: {$resource}">
						<img src='{$outputImage}' alt='Can not render Image, might not exist anymore!' />
					</span>
	    	</xsl:when>
	    	
	    	<!-- LINK -->
	    	
	    	<xsl:when test="fn:codepoint-equal($outputTypeValue, 'link')">
	    		<xsl:if test="fn:exists(f:title)">
	    			<xsl:variable name="outputLink" select="f:title" />
	    			<a onclick='setHrefs()' href='{$outputLink}'><xsl:value-of select="f:title"/></a>
	    		</xsl:if>
	    		<xsl:if test="fn:exists(f:resource/f:title)">
	    			<xsl:variable name="outputLink" select="f:resource/f:title" />
	    			<span style="cursor: help;" class="tooltip" title="Resource: {$resource}">
	    				<a onclick='setHrefs()' href='{$outputLink}'><xsl:value-of select="f:resource/f:title"/></a>
	    			</span>
	    		</xsl:if>	
	    	</xsl:when>
	    	
	    </xsl:choose>
	    
	  </xsl:if>
	  
	  <!-- The value is NOT defined by fresnel:value -->
	  
		<xsl:if test="fn:not(@output-type)">
			
			<!-- The value is a literal as f:title -->
			<xsl:if test="fn:exists(f:title)">
				<xsl:value-of select="f:title" disable-output-escaping="yes" />
			</xsl:if>
			
			<!-- The value is a resource as f:resource/f:title -->
			<xsl:if test="fn:exists(f:resource/f:title)">
	    	
	    	<!-- Show image? -->
	    	<xsl:choose>
	    	
	    		<!-- Yes! -->
  				<xsl:when test="fn:codepoint-equal(functx:substring-after-last($resource, '.'), 'jpg') or fn:codepoint-equal(functx:substring-after-last($resource, '.'), 'JPG') or fn:codepoint-equal(functx:substring-after-last($resource, '.'), 'png') or fn:codepoint-equal(functx:substring-after-last($resource, '.'), 'gif')">
    				<img src='{$resource}' style='width: 100px; border: 0px;' alt='{$resource}' />
  				</xsl:when>
  				
  				<!-- No! -->
  				<xsl:otherwise>	
   					
   					<!-- If title has substring http:// or mailto: then set a link else set title -->
   					<xsl:choose>
	    				<xsl:when test="fn:starts-with($resource, 'http://') or fn:starts-with($resource, 'mailto:')">
	    					<a onclick='setHrefs()' href='{$resource}' class='tooltip' title='Link to :: {$resource}' target='_blank'><xsl:value-of select="if (fn:not(fn:contains(f:resource/f:title, '#'))) then (functx:substring-after-last(f:resource/f:title, '/')) else (functx:substring-after-last(f:resource/f:title, '#'))" /></a>
	    				</xsl:when>
	    				<xsl:otherwise>
	    					<span style="cursor: help;" class="tooltip" title="Resource: {$resource}">
	    						<xsl:value-of select="if (fn:not(fn:contains(f:resource/f:title, '#'))) then (functx:substring-after-last(f:resource/f:title, '/')) else (functx:substring-after-last(f:resource/f:title, '#'))" />
	    					</span>		
	    				</xsl:otherwise>
	    			</xsl:choose>
	    			
  				</xsl:otherwise>
				
				</xsl:choose>
				
				<!--
				<xsl:if test="fn:codepoint-equal(functx:substring-after-last($resource, '.'), 'jpg') or fn:codepoint-equal(functx:substring-after-last($resource, '.'), 'JPG')">
					<img src='{$resource}' style='width: 100px; border: 0px;' alt='{$resource}' />
				</xsl:if>
				<xsl:if test="fn:codepoint-equal(functx:substring-after-last($resource, '.'), 'png')">
					<img src='{$resource}' style='width: 100px; border: 0px;' alt='{$resource}' />
				</xsl:if>
				<xsl:if test="fn:codepoint-equal(functx:substring-after-last($resource, '.'), 'gif')">
					<img src='{$resource}' style='width: 100px; border: 0px;' alt='{$resource}' />
				</xsl:if>
				-->
				
				<!-- No image - show title! -->
				<!--
				<xsl:if test="fn:not(fn:codepoint-equal(functx:substring-after-last($resource, '.'), 'jpg') or fn:codepoint-equal(functx:substring-after-last($resource, '.'), 'png') or fn:codepoint-equal(functx:substring-after-last($resource, '.'), 'gif'))">
					<span style="cursor: help;" class="tooltip" title="Resource: {$resource}">
	    			<xsl:value-of select="if (fn:not(fn:contains(f:resource/f:title, '#'))) then (functx:substring-after-last(f:resource/f:title, '/')) else (functx:substring-after-last(f:resource/f:title, '#'))" />
	    		</span>
	    	</xsl:if>
	    	-->
	    	
	    </xsl:if>
			
			<!-- The value is NOT a literal as f:title -->
			
			<xsl:if test="fn:not(fn:exists(f:title))">
				<xsl:variable name="resource" select="if (fn:exists(f:resource/@uri)) then f:resource/@uri else f:resource/f:title" />
				<xsl:variable name="fresnelLens" select="f:resource/@lens" />		
				
				<!-- LINK TO RESOURCE USING SAME LENS -->
				<xsl:if test="f:resource/@lens">
					<xsl:if test="fn:not(fn:codepoint-equal($fresnelLens, 'http://www.example.org#defaultInstanceLens') or fn:codepoint-equal($fresnelLens, 'http://www.example.org#defaultClassLens'))">
		    		<a onclick='setHrefs()' href='?resource={fn:encode-for-uri($resource)}&amp;lens={fn:encode-for-uri($fresnelLens)}' class='tooltip' title='Lens: {fn:substring-after($fresnelLens, "#")} :: Resource {$resource}'><img src='public/images/link_icon.png' style='border: 0px; margin: 0px 2px;' alt='Icon.' /></a>
	    		</xsl:if>
	    	</xsl:if>
				
				<!-- LINK TO RESOURCE USING THE DEFAULT LENS -->
				<a onclick='setHrefs()' href='?resource={fn:encode-for-uri($resource)}' class='tooltip' title='Lens: DEFAULT :: Resource: {$resource}'><img src='public/images/rdf_icon.gif' style='width: 15px; border: 0px; margin: 0px 2px;' alt='Icon.' /></a>
				
				<!-- LINK TO RESOURCE USING A LENS -->
				<xsl:apply-templates select="f:resource/f:lens"/>
			</xsl:if>
			
		</xsl:if>
	
	</xsl:if>
	
	<xsl:value-of select="../f:content/f:after"/>
	
</xsl:template>

</xsl:stylesheet>