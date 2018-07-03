define(['lang'], function (lang) {
    var PARSING_ERROR = '2001';
    var UNEXPECTED_XML_PARAMETER = '2002';
    var WORKFLOWEXE_EMPTY_RESPONSE = '1010';
    var WORKFLOWEXE_UNEXPECTED_RESPONSE = '1011';
    var DEFAULT_ERRORCODE = '1001';
    var _log = log.getInstance('errorHandler.js');

    var backendErrorToInternalErrorMap = [
        ['6000', ['1003', '1005']],
        ['6001', ['1002', '1009']],
        ['4000', ['1001']]
    ];

    function translateInternalErrorToBackendError(internalError) {
        for (var index in backendErrorToInternalErrorMap) {
            var map = backendErrorToInternalErrorMap[index];
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

    function showDefaultError() {
        document.getElementById('messageContainer').innerHTML = getErrorAlert(lang.translate('error.' + 1001));
    }

    /**
     * Attempts to display an appropiate message to the user. If an {@link Error} is received the default error message will be shown and error will be logged to error. Otherwise attempts to translate the given parameter and displays the given translated message. If parameter was translated also attempts to send an errorResponse to the backend.
     * @param {string} parameter 
     */
    function handleError(parameter) {
        if (parameter instanceof Error) {
            _log.error('An error occured: ' + parameter.message +'. Stack: ' + parameter.stack);
            showDefaultError();
        } else if (!parameter) {
            _log.error('An error occured with unspecified parameter!');
            showDefaultError();
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
        handleError: handleError,
        PARSING_ERROR: PARSING_ERROR,
        UNEXPECTED_XML_PARAMETER: UNEXPECTED_XML_PARAMETER,
        WORKFLOWEXE_EMPTY_RESPONSE: WORKFLOWEXE_EMPTY_RESPONSE,
        DEFAULT_ERRORCODE: DEFAULT_ERRORCODE,
        WORKFLOWEXE_UNEXPECTED_RESPONSE: WORKFLOWEXE_UNEXPECTED_RESPONSE
    };
});