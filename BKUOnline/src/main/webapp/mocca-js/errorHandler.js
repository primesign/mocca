define('errorHandler', function () {
    var _log = log.getInstance('errorHandler.js');

    var dialog = 
    '<div id ="erroralert" class="alert alert-danger">'+
        '<strong>Error: </strong>'+
        '<p id="paragraph"/>'+
    '</div>';
    
    document.getElementById('messageContainer').innerHTML = dialog;

    return {
        handleError: function(message) {
            $('#erroralert').show();
            $('#paragraph').text(message);
            _log.debug('handleError was called with parameter: ' + message);
        }
    };
});