define(['lang'], function (lang) {
    var _log = log.getInstance('errorHandler.js');

    function getErrorAlert(message) {
        return '<div id ="erroralert" class="alert alert-danger">' +
            '<strong>' + lang.translate('error.prefix') + ': </strong>' +
            '<p id="paragraph"/>' + message + '</p>' +
            '</div>';
    }


    return {
        handleError: function (message) {
            _log.error('An error occured: ' + message +'.');
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