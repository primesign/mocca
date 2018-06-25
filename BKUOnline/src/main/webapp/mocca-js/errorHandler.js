define(['lang'], function (lang) {
    var _log = log.getInstance('errorHandler.js');
    var errorPrefix = lang.translate('error.prefix');

    function getErrorAlert(message) {
        return '<div id ="erroralert" class="alert alert-danger">' +
            '<strong>' + errorPrefix + ': </strong>' +
            '<p id="paragraph"/>' + message + '</p>' +
            '</div>';
    }


    return {
        handleError: function (message) {
            _log.debug('handleError was called with parameter: ' + message);
            var translatedMessage = lang.translate('error.' + message);
            var displayingMessage;
            if (translatedMessage === 'error.' + message) {
                displayingMessage = message;
            } else {
                displayingMessage = translatedMessage;
            }
            document.getElementById('messageContainer').innerHTML = getErrorAlert(displayingMessage);
        }
    };
});