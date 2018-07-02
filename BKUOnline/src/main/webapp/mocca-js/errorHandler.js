define(['lang'], function (lang) {
    var PARSING_ERROR = '2001';
    var UNEXPECTED_XML_PARAMETER = '2002';
    var _log = log.getInstance('errorHandler.js');

    var backendErrorToInternalErrorMap = new Map([
        ['6000', ['1003', '1005']],
        ['4500', ['1009']],
        ['1002', ['6001']],
        ['4000', ['1001']]
    ]);

    function translateInternalErrorToBackendError(internalError) {
        for (var map of backendErrorToInternalErrorMap.entries()) {
            var key = map[0];
            var value = map[1];
            for (var index in value) {
                if (value[index] == internalError) { //intentionally not identical if
                    return key;
                }
            }
        };
    }


    function getErrorAlert(message) {
        return '<div id ="erroralert" class="alert alert-danger">' +
            '<strong>' + lang.translate('error.prefix') + ': </strong>' +
            '<p id="paragraph">' + message + '</p>' +
            '</div>';   
    }

    function handleError(parameter) {
        if (parameter instanceof Error) {
            _log.error('An error occured: ' + parameter.message +'. Stack: ' + parameter.stack);
            document.getElementById('messageContainer').innerHTML = getErrorAlert(lang.translate('error.' + 1001));
        } else {
            _log.error('An error occured: ' + parameter +'.');
            var translatedMessage = lang.translate('error.' + parameter);
            var displayingMessage;
            if (translatedMessage === 'error.' + parameter) {
                displayingMessage = parameter;
            } else {
                displayingMessage = translatedMessage;
            }
            document.getElementById('messageContainer').innerHTML = getErrorAlert(displayingMessage);

            // if message was translateable a.k.a. an error code, check if QuitRequest has to be send
            if (displayingMessage !== parameter) {
                mocca_js.backend.sendErrorToBackend(mocca_js._parameters.SessionID, translateInternalErrorToBackendError(parameter), displayingMessage);
            }
        }
    }

    return {
        handleError: handleError
    };
});