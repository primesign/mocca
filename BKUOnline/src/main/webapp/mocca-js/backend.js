define([], function () {
    var _log = log.getInstance('backend.js');
    var INFOBOX_READ_REQ = 'InfoboxReadRequest';
    var SIGN_REQ = 'SignRequest';
    var QUIT_REQ = 'QuitRequest';
    var PARSING_ERROR = 'PARSING_ERROR';
    var INVALID_XML = 'INVALID_XML';

    function setBaseUrl(baseUrl) {
        this.baseUrl = baseUrl;
    }

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

    function parseXMLResponse(xml) {
        if (xml && xml.childNodes) {
            return validateXMLChildNodes(xml.childNodes, 0);
        } else {
            _log.debug('An error occured while parsing the XML data.');
            throw INVALID_XML;
        }
    }

    function validateXMLChildNodes(xmlChildNodes, depth) {
        for (var childNodeIndex in xmlChildNodes) {
            var childNode = xmlChildNodes[childNodeIndex];
            if (childNode.nodeName === 'S:Envelope' && depth === 0) {
                return validateXMLChildNodes(childNode.childNodes, 1);
            }
            else if (childNode.nodeName === 'S:Body') {
                return validateXMLChildNodes(childNode.childNodes, 2);
            }
            else if (childNode.nodeName === 'GetNextRequestResponse') {
                return validateXMLChildNodes(childNode.childNodes, 3);
            } else if (depth === 3 && (childNode.nodeName === INFOBOX_READ_REQ ||
                childNode.nodeName === SIGN_REQ) ||
                childNode.nodeName === QUIT_REQ) {
                return childNode.nodeName;
            }
        }
        throw PARSING_ERROR;
    }

    function sendCertificate(sessionId, certificate) {
        _log.info('Sending certificate to backend. SessionID: "' + sessionId + '".');

        var infoboxReadResponse = [
            '<?xml version="1.0" encoding="UTF-8"?>',
            '<soapenv:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:stal="http://www.egiz.gv.at/stal">',
            '<soapenv:Body>',
            '<stal:GetNextRequest SessionId="',
            sessionId,
            '">',
            '<stal:InfoboxReadResponse>',
            '<stal:InfoboxValue>',
            certificate,
            '</stal:InfoboxValue>',
            '</stal:InfoboxReadResponse>',
            '</stal:GetNextRequest>',
            '</soapenv:Body>',
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

    function sendSignedData(sessionId, signedValue) {
        var signResponse = [
            '<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:stal="http://www.egiz.gv.at/stal">',
            '<soapenv:Header/>',
            '<soapenv:Body>',
            '<stal:GetNextRequest SessionId="',
            sessionId,
            '">',
            '<stal:SignResponse>',
            '<stal:SignatureValue>',
            signedValue,
            '</stal:SignatureValue>',
            '</stal:SignResponse>',
            '</stal:GetNextRequest>',
            '</soapenv:Body>',
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


    return {
        INFOBOX_READ_REQ: INFOBOX_READ_REQ,
        SIGN_REQ: SIGN_REQ,
        QUIT_REQ: QUIT_REQ,
        parseXMLResponse: parseXMLResponse,
        setBaseUrl: setBaseUrl,
        connect: connect,
        sendCertificate: sendCertificate,
        sendSignedData: sendSignedData
    };
});