define(['errorHandler'], function (errorHandler) {
    var _log = log.getInstance('backend.js');
    var INFOBOX_READ_REQ = 'InfoboxReadRequest';
    var SIGN_REQ = 'SignRequest';
    var QUIT_REQ = 'QuitRequest';

    function setBaseUrl(baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * Sends a connect request to the backend.
     * @param {string} sessionId the sessionId connected to the http request. Mustn't be null.
     */
    function connect(sessionId) {
        _log.info('Connecting to backend...');
        return $.soap({
            url: this.baseUrl + '/stal',
            method: 'connect',
            namespaceQualifier: 'stal',
            namespaceURL: 'http://www.egiz.gv.at/stal',
            appendMethodToURL: false,
            elementName: 'SessionId',
            enableLogging: true,
            data: sessionId
        });
    }

    /**
    * Function checks if XML is defined and has childNodes. 
    * In case of true, it calls function {@link validateXMLChildNodes} and passes first childenode of XML and depth.
    * 
    * @param {xml} xml the xml to parse. Mustn't be null.
    * @throws errorHandler.UNEXPECTED_XML_PARAMETER
    * @returns type of the passed xml
    */
    function parseXMLResponse(xml) {
        if (xml && xml.childNodes) {
            return parseXMLChildNodes(xml.childNodes, 0);
        } else {
            if (xml) {
                _log.debug('XML Parameter has no childNodes.');
            } else {
                _log.debug('XML Parameter undefined.');
            }

            throw errorHandler.UNEXPECTED_XML_PARAMETER;
        }
    }

    /**
    * Function iterates and checks the structure of the XML. 
    * Structure: First level S:Envelope, second level S:Body, third level GetNextRequestResponse,
    *            Fourth level can be either InfoboxReadRequest, SignRequest or QuitRequest.
    * Return value depends which fourth level is found.
    * 
    * @param {xml} xmlChildNodes the xmlChildNodes to parse. Mustn't be null.
    * @param {number} depth Mustn't be null.
    * @returns childNode Name
    * @throws errorHandler.PARSING_ERROR
    */
    function parseXMLChildNodes(xmlChildNodes, depth) {
        for (var childNodeIndex in xmlChildNodes) {
            var childNode = xmlChildNodes[childNodeIndex];
            if (childNode.nodeName === 'S:Envelope' && depth === 0) {
                return parseXMLChildNodes(childNode.childNodes, 1);
            }
            else if (childNode.nodeName === 'S:Body') {
                return parseXMLChildNodes(childNode.childNodes, 2);
            }
            else if (childNode.nodeName === 'GetNextRequestResponse') {
                return parseXMLChildNodes(childNode.childNodes, 3);
            } else if (depth === 3 && (childNode.nodeName === INFOBOX_READ_REQ ||
                childNode.nodeName === SIGN_REQ) ||
                childNode.nodeName === QUIT_REQ) {
                return childNode.nodeName;
            }
        }
        throw errorHandler.PARSING_ERROR;
    }

    /**
     * Sends a soap request to backend with the certificate to use.
     * @param {string} sessionId the sessionId connected to the http request. Mustn't be null.
     * @param {string} certificate the certificate to use. Mustn't be null.
     * @returns a Promise to resolve.
     */
    function sendCertificate(sessionId, certificate) {
        _log.info('Sending certificate to backend. SessionID: "' + sessionId + '".');

        var infoboxReadResponse = [
            '<?xml version="1.0" encoding="UTF-8"?>',
            '<soapenv:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:stal="http://www.egiz.gv.at/stal">',
            '   <soapenv:Body>',
            '       <stal:GetNextRequest SessionId="' + sessionId + '">',
            '           <stal:InfoboxReadResponse>',
            '               <stal:InfoboxValue>' + certificate + '</stal:InfoboxValue>',
            '           </stal:InfoboxReadResponse>',
            '       </stal:GetNextRequest>',
            '   </soapenv:Body>',
            '</soapenv:Envelope>'];

        return $.soap({
            url: this.baseUrl + '/stal',
            method: 'nextRequest',
            namespaceQualifier: 'stal',
            namespaceURL: 'http://www.egiz.gv.at/stal',
            appendMethodToURL: false,
            enableLogging: true,
            data: infoboxReadResponse.join(''),
        });
    }

    /**
     * Sends a soap request to backend with the signed data.
     * @param {string} sessionId the sessionId connected to the http request. Mustn't be null.
     * @param {string} signedValue the signed value.
     * @returns a Promise to resolve.
     */
    function sendSignedData(sessionId, signedValue) {
        var signResponse = [
            '<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:stal="http://www.egiz.gv.at/stal">',
            '   <soapenv:Header/>',
            '   <soapenv:Body>',
            '       <stal:GetNextRequest SessionId="' + sessionId + '">',
            '           <stal:SignResponse>',
            '               <stal:SignatureValue>' + signedValue + '</stal:SignatureValue>',
            '           </stal:SignResponse>',
            '       </stal:GetNextRequest>',
            '   </soapenv:Body>',
            '</soapenv:Envelope>'];

        return $.soap({
            url: this.baseUrl + '/stal',
            method: 'nextRequest',
            namespaceQualifier: 'stal',
            namespaceURL: 'http://www.egiz.gv.at/stal',
            appendMethodToURL: false,
            enableLogging: true,
            data: signResponse.join(''),
        });
    }

    /**
     * Sends a soap request to backend with the signed data.
     * @param {string} sessionId the sessionId connected to the http request. Mustn't be null.
     * @param {string} errorCode the errorCode to send to the backend. Default value 4000.
     * @param {string} errorMessage the errorMessage connected to the errorCode. Default value 'errorMessage'
     * @returns a Promise to resolve.
     */
    function sendErrorToBackend(sessionId, errorCode, errorMessage) {
        if (!errorCode) {
            errorCode = 4000;
        }
        if (!errorMessage) {
            errorMessage = 'errorMessage';
        }
        var errorResponse = [
            '<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:stal="http://www.egiz.gv.at/stal">',
            '   <soapenv:Header/>',
            '   <soapenv:Body>',
            '       <stal:GetNextRequest SessionId="' + sessionId + '">',
            '           <stal:ErrorResponse>',
            '               <stal:ErrorCode>' + errorCode + '</stal:ErrorCode>',
            '               <stal:ErrorMessage>' + errorMessage + '</stal:ErrorMessage>',
            '           </stal:ErrorResponse>',
            '       </stal:GetNextRequest>',
            '   </soapenv:Body>',
            '</soapenv:Envelope>'];

        return $.soap({
            url: this.baseUrl + '/stal',
            method: 'nextRequest',
            namespaceQualifier: 'stal',
            namespaceURL: 'http://www.egiz.gv.at/stal',
            appendMethodToURL: false,
            enableLogging: true,
            data: errorResponse.join(''),
        });
    }

    return {
        INFOBOX_READ_REQ: INFOBOX_READ_REQ,
        SIGN_REQ: SIGN_REQ,
        QUIT_REQ: QUIT_REQ,
        parseXMLResponse: parseXMLResponse,
        setBaseUrl: setBaseUrl,
        connect: connect,
        sendCertificate: sendCertificate,
        sendSignedData: sendSignedData,
        sendErrorToBackend: sendErrorToBackend
    };
});