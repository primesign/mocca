define('errorHandler', function () {
    var _log = log.getInstance('errorHandler.js');

    return {
        handleError: function(message) {
            _log.debug('handleError was called with parameter: ' + message);
        }
    };
});