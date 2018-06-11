define('errorHandler', function () {
    var _log = log.getInstance('errorHandler.js');

    var dialog = '<div id="myModal" class="modal fade" tabindex="-1" role="dialog">'+
    '<div class="modal-dialog" role="document">'+
    '  <div class="modal-content">'+
    '    <div class="modal-header">'+
    '      <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>'+
    '      <h4 class="modal-title">Modal title</h4>'+
    '    </div>'+
    '    <div class="modal-body">'+
    '      <p id="paragraph">One fine body&hellip;</p>'+
    '    </div>'+
    '    <div class="modal-footer">'+
    '      <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>'+
    '      <button type="button" class="btn btn-primary">Save changes</button>'+
    '    </div>'+
    '  </div>'+
    '</div>'+
    '</div>';
    document.body.innerHTML += dialog;

    return {
        handleError: function(message) {
            $('#myModal').modal('show');
            $('#paragraph').text(message);
            _log.debug('handleError was called with parameter: ' + message);
        }
    };
});