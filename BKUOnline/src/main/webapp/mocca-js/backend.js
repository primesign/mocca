define(function () {
    var baseUrl = '/BKUOnline/stal';

    function connect(successCallback, errorCallback) {
        log.debug('sending connect request to backend');
        $.soap({
            url: baseUrl,
            method: 'connect',
            namespaceQualifier: 'stal',
            namespaceURL: 'http://www.egiz.gv.at/stal',
            appendMethodToURL: false,
            elementName: 'SessionId',

            data: parameters.SessionID,

            success: successCallback,
            error: errorCallback
        });
    }


    return {
        connect: connect
    };
});