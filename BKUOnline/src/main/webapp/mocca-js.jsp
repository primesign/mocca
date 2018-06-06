<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%--

  This JSP page loads a prototypical implementation of a JavaScript version
  of the Mocca applet.
  
 --%>
 
<%-- Images --%>
<c:set var="defaultButtonWidth" value="16"/>
<c:set var="defaultButtonHeight" value="22"/>
<%-- URLs --%>
<c:set value="./applet" var="codebase"/>
<c:url value="/help/" var="helpUrl"/>
<c:url value="/stal?wsdl" var="wsdlUrl"/>
<c:url value="/result" var="resultUrl"/>
<%-- Messages --%>
<fmt:bundle basename="web">

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
  <head>
    <META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=UTF-8">
    <META HTTP-EQUIV="CACHE-CONTROL" CONTENT="NO-CACHE">
    <META HTTP-EQUIV="EXPIRES" CONTENT="Mon, 22 Jul 2002 11:12:01 GMT">
    <META HTTP-EQUIV="PRAGMA" CONTENT="NO-CACHE">
    <title><fmt:message key="title"/></title>
    
    <script type="text/javascript" src="js/jquery-3.3.1.min.js"></script>
    <script type="text/javascript" src="js/jquery.soap-1.7.2.js"></script>
    <script type="text/javascript" src="js/xml2json.js"></script>
    
    <script type="text/javascript" src="mocca-js/libs/require.js"></script>
    <script type="text/javascript" src="mocca-js/mocca-js.js"></script>
    
    <script>
    	inIframe = false;
    	try{
	        if (top.location != window.location) {
	            inIframe = true;
	        }
    	} catch(e) {
            inIframe = true;
    	}
        document.write('<base href="' + document.location.pathname + '" />');
        document.write("<script type='text/javascript' src='mocca-js/libs/require.js?t=" + Date.now() + "'><\/script>");
        document.write("<script type='text/javascript' src='mocca-js/mocca-js.js?t=" + Date.now() + "'><\/script>");
    </script>
    
    </head>
    <body>
    	  <h1>Mocca-JS DEMO</h1>     
        
        <script type="text/javascript">
          
          parameters = {
            GuiStyle : '<c:out value="${requestScope.moccaParam.guiStyle}" default="simple"/>',
            Locale : '<c:out value="${requestScope.moccaParam.locale}" default=""/>',
            Background : '<c:out value="${requestScope.moccaParam.appletBackground}" default=""/>',
            BackgroundColor : '<c:out value="${requestScope.moccaParam.appletBackgroundColor}" default="#eeeeee"/>',
            WSDL_URL : '<c:out value="${wsdlUrl}"/>',
            HelpURL : '<c:out value="${helpUrl}"/>',
            SessionID : '<c:out value="${requestScope.id}"/>',
            RedirectURL : '<c:out value="${resultUrl}"/>',
            RedirectTarget : '<c:out value="${requestScope.moccaParam.redirectTarget}" default="_parent"/>'
          };
          
          var demoCertBase64 = "MIIEFTCCAv2gAwIBAgIJAMtFZnr7TIzkMA0GCSqGSIb3DQEBBQUAMGQxCzAJBgNV"+
	          "BAYTAkFUMRMwEQYDVQQIEwpTb21lLVN0YXRlMQ0wCwYDVQQHEwRHcmF6MRcwFQYD"+
	          "VQQKEw5QcmltZVNpZ24gR21iSDEYMBYGA1UEAxMPREVNTyBaRVJUSUZJS0FUMB4X"+
	          "DTE4MDUwODEzMDAzNFoXDTIwMDUwNzEzMDAzNFowZDELMAkGA1UEBhMCQVQxEzAR"+
	          "BgNVBAgTClNvbWUtU3RhdGUxDTALBgNVBAcTBEdyYXoxFzAVBgNVBAoTDlByaW1l"+
	          "U2lnbiBHbWJIMRgwFgYDVQQDEw9ERU1PIFpFUlRJRklLQVQwggEiMA0GCSqGSIb3"+
	          "DQEBAQUAA4IBDwAwggEKAoIBAQDcpCJ/y+UeDI9XwzDcFXUxgYBrMvNC0OymGUnV"+
	          "ue+jzsQ43PQ2h0wlvJbzyKOLHJUk+koN6WrfecmBgKIoc/ZI5IKdAf4GLVLsMJy1"+
	          "0O/SFfpnHID42Io4C7WAMJ1PKPlShZlC/LPPkGAChJsxZNeBKzv9Axtf3636ykIb"+
	          "gSYMjZdHPMeJnVBzS5NTsxU4ixamj9lslS5m4XZcPu4c0DC/9rVIEGK0DM4wylb2"+
	          "dCQ4xi/wMtGpZoDHr3jt1JtYjLDrmactdhPAiYYczN+kjEc3k2sUbyvNwmVauagi"+
	          "3kbCGH+Y+lsUrlwnFfkGnIwNIw3zkgrjZZnocJGGfq+cF2rPAgMBAAGjgckwgcYw"+
	          "HQYDVR0OBBYEFNNjRt03b9uqJr0QTSGXdOm0XjIFMIGWBgNVHSMEgY4wgYuAFNNj"+
	          "Rt03b9uqJr0QTSGXdOm0XjIFoWikZjBkMQswCQYDVQQGEwJBVDETMBEGA1UECBMK"+
	          "U29tZS1TdGF0ZTENMAsGA1UEBxMER3JhejEXMBUGA1UEChMOUHJpbWVTaWduIEdt"+
	          "YkgxGDAWBgNVBAMTD0RFTU8gWkVSVElGSUtBVIIJAMtFZnr7TIzkMAwGA1UdEwQF"+
	          "MAMBAf8wDQYJKoZIhvcNAQEFBQADggEBAEfQhpUq81vyNyohqW48+D1te5JnFQnX"+
	          "6We9O2cVy117P4a9rTuPRt7+Q3icxbV5bhgg57TgBtrz9Z/GRBiYDZpIe+DcN7zl"+
	          "kxYJxc7B06xia+3NZL0bEDTjFuuIOpUM59vZASyQQDLWhploDWcXALrqrmpUqHJj"+
	          "F38fpGLSEWKPAX+jhutRLHCR1TjNpBuxYWccvnKX/uRRvrTzTuXX4KLXZ+CmwbyI"+
	          "LR8sr/Ed92T6+TMzwOkcCbD3FqkBme/cqAnQglUauhcD/TMVXrGQ0t4wn7HHubVC"+
	          "Z3A1/PSKQzBcU8k6Lw8fI93FaBowiCvHraPVuM+ZFjq16rk7A04YjrU=";
          
          
          var sendConnectRequest = function () {
			//            var connectRequest = ['<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:stal="http://www.egiz.gv.at/stal">',
			//				'<soapenv:Header/>',
			//					'<soapenv:Body>',
			//					'<stal:SessionId>',
			//						'<c:out value="${requestScope.id}"/>',
			//					'</stal:SessionId>',
			//					'</soapenv:Body>',
			//				'</soapenv:Envelope>'];

			$.soap({
				url: '/BKUOnline/stal',
				method: 'connect',
				namespaceQualifier: 'stal',
				namespaceURL: 'http://www.egiz.gv.at/stal',
				appendMethodToURL: false,
				elementName: 'SessionId',
			
				data: parameters.SessionID,            
				//data: connectRequest.join(''), when using xml string array above
				
				success: function (soapResponse) {
					console.log('BKUOnline response: ' + soapResponse.toString());
					console.log(soapResponse.toJSON());
					// TODO check, which type of request (State Diagram!)
					// TODO get certificate via Cryptas JS library
					// TODO find a nicer way to handle callbacks (e.g. wrapping the soap API to support promises?)
					sendInfoboxReadResponse(demoCertBase64);
				},
				error: function (soapResponse) {
					// show error
					console.log('BKUOnline error: ' + soapResponse.toString());
				}
			});
          };
          
		  var sendInfoboxReadResponse = function (certificate) {
			  var infoboxReadResponse = [
				'<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:stal="http://www.egiz.gv.at/stal">',
				   '<soapenv:Header/>',
				   '<soapenv:Body>',
				      '<stal:GetNextRequest SessionId="',
				      	parameters.SessionID,
				      '">',
				         '<stal:InfoboxReadResponse>',
				            '<stal:InfoboxValue>',
				            		certificate,
							'</stal:InfoboxValue>',
				         '</stal:InfoboxReadResponse>',
				      '</stal:GetNextRequest>',
				   '</soapenv:Body>',
				'</soapenv:Envelope>'];
			  
			  $.soap({
	              url: '/BKUOnline/stal',
	              method: 'nextRequest',
	              appendMethodToURL: false,
	              
	              data: infoboxReadResponse.join(''),
	              
	              success: function (soapResponse) {
	                  console.log('BKUOnline response: ' + soapResponse.toString());
	                  var soapResponseJSON = soapResponse.toJSON();
	                  console.log(soapResponseJSON);
	                  // TODO check, which type of request (State Diagram!)
	                  // TODO correctly parse data to be signed (error handling, stability!)
	                  var dataToBeSigned = soapResponseJSON['#document']['S:Envelope']['S:Body']['GetNextRequestResponse']['SignRequest']['SignedInfo']['_'];
	                  console.log("Data to be signed: " + dataToBeSigned);
	                  // TODO sign with the Cryptas JS Library
	                  var signedValue = dataToBeSigned;
	                  sendSignResponse(signedValue);
	                  
	              },
	              error: function (soapResponse) {
	                  // show error
	                  console.log('BKUOnline error: ' + soapResponse.toString());
	              }
	          });
        	  
          };
          
		  var sendSignResponse = function (signedValue) {
			  var signResponse = [
				'<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:stal="http://www.egiz.gv.at/stal">',
				   '<soapenv:Header/>',
				   '<soapenv:Body>',
				      '<stal:GetNextRequest SessionId="',
				      	parameters.SessionID,
				      '">',
				         '<stal:SignResponse>',
				            '<stal:SignatureValue>',
				            		signedValue,
							'</stal:SignatureValue>',
				         '</stal:SignResponse>',
				      '</stal:GetNextRequest>',
				   '</soapenv:Body>',
				'</soapenv:Envelope>'];
			  
			  $.soap({
	              url: '/BKUOnline/stal',
	              method: 'nextRequest',
	              appendMethodToURL: false,
	              
	              data: signResponse.join(''),
	              
	              success: function (soapResponse) {
	                  console.log('BKUOnline response: ' + soapResponse.toString());
	                  console.log(soapResponse.toJSON());
	                  // TODO handle response
	                  // TODO handle redirect targets correctly
	                  parent.document.location.href = parameters.RedirectURL;
	              },
	              error: function (soapResponse) {
	                  // show error
	                  console.log('BKUOnline error: ' + soapResponse.toString());
	              }
	          });
        	  
          };   
          
          // start signatur process with BKUOnline
          sendConnectRequest();
          
      </script>

  </body>
</html>
</fmt:bundle>
