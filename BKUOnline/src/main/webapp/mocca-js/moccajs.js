
var mocca_js = {};

function enhanceLogging(loggingFunc, context) {
    return function () {
      var modifiedArguments = [].slice.call(arguments);
      // add component name and date to log message
      modifiedArguments[0] = [new Date().toISOString() + ' [' + context + '] '] + modifiedArguments[0];
      loggingFunc.apply(null, modifiedArguments);
    };
  }
log = {
    debug: function(message) {
        console.log(message);
    },
    error: function(message) {
        console.log(message);
    },
    getInstance: function(context) {
        return {
            log: enhanceLogging(console.log, context),
            info: enhanceLogging(console.log, context),
            warn: enhanceLogging(console.log, context),
            debug: enhanceLogging(console.debug, context),
            error: enhanceLogging(console.error, context)
          };
    }
}
var WorkflowExe;
define('moccajs', function(require) {
    mocca_js.backend = require('backend');
    mocca_js.stal = require('stal');
    var _log = log.getInstance('moccajs.js');
    _log.debug('mocca_js: ' + JSON.stringify(mocca_js));
    _log.debug('inIframe: ' + inIframe);
    _log.debug('mocca_js initialized!');
    var _parameters;

    function run(parameters) {
        _log.debug('starting mocca-js with parameters: ' + JSON.stringify(parameters));
        _parameters = parameters;
        mocca_js.backend.connect(selectCertificate, log.error);
    }

    function selectCertificate() {
        mocca_js.stal.selectCertificate(function(certificate) {
            requestInfoBoxReadRequest(certificate);
        });
    }

    function requestInfoBoxReadRequest(certificate) {
        log.debug('selectedCertificate: ' + certificate);
    }
    return {
        run: run
    }
});
