define(function () {

    var _log = log.getInstance('backend.js');

    function setBaseUrl(baseUrl) {
        this.baseUrl = baseUrl;
    }

    function connect(sessionId) {
        _log.debug('sending connect request to backend');
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

    function sendCertificate(sessionId, certificate) {
        _log.debug('sending certificate to backend');

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
        setBaseUrl: setBaseUrl,
        connect: connect,
        sendCertificate: sendCertificate,
        sendSignedData: sendSignedData
    };
});