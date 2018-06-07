define(function () {
    var baseUrl = '/BKUOnline/stal';
    var _log = log.getInstance('backend.js');


    function connect(successCallback, errorCallback) {
        _log.debug('sending connect request to backend');
        return $.soap({
            url: baseUrl,
            method: 'connect',
            namespaceQualifier: 'stal',
            namespaceURL: 'http://www.egiz.gv.at/stal',
            appendMethodToURL: false,
            elementName: 'SessionId',

            data: parameters.SessionID
        });
    }

    function sendCertificate(sessionId, certificate, successCallback, errorCallback) {
        _log.debug('sending certificate to backend');

        
        var infoboxReadResponse = [
            '<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:stal="http://www.egiz.gv.at/stal">',
            '<soapenv:Header/>',
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
            url: baseUrl,
            method: 'nextRequest',
            namespaceQualifier: 'stal',
            namespaceURL: 'http://www.egiz.gv.at/stal',
            appendMethodToURL: false,
            elementName: 'SessionId',
            data: infoboxReadResponse.join(''),
        });
    }

    function sendSignedData(sessionId, signedValue, successCallback, errorCallback) {
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
    }


    return {
        connect: connect,
        sendCertificate: sendCertificate,
        sendSignedData: sendSignedData
    };
});