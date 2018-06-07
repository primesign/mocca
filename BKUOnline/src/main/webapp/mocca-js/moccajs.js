
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

    var algorithmId = 'rsa-sha256';
    mocca_js.backend = require('backend');
    mocca_js.stal = require('stal');
    mocca_js.errorHandler = require('errorHandler');
    var _log = log.getInstance('moccajs.js');
    _log.debug('mocca_js: ' + JSON.stringify(mocca_js));
    _log.debug('inIframe: ' + inIframe);
    _log.debug('mocca_js initialized!');
    var _parameters;

    function mockSTAL() {
        mocca_js.stal = require('stalMock');
    }

    function run(parameters, isMockSTAL) {
        if (isMockSTAL && isMockSTAL === true) {
            mockSTAL();
        }
        _log.debug('starting mocca-js with parameters: ' + JSON.stringify(parameters));
        _parameters = parameters;
        mocca_js.backend.connect()
            .then(selectCertificate)
            .then(sendCertificate)
            .then(parseDataToBeSigned)
            .then(sendSignedData)
            .then(parseSignedDataResponse)
            .then(redirectUser)
            .fail(mocca_js.errorHandler.handleError);
    }

    function selectCertificate() {
        var deferred = $.Deferred();
        var certificate = mocca_js.stal.selectCertificate();
        deferred.resolve(certificate);
        return deferred.promise();
    }

    function sendCertificate(certificate) {
        var deferred = $.Deferred();
        log.debug('selectedCertificate: ' + certificate);
        mocca_js.backend.sendCertificate(_parameters.SessionID, certificate).then(function (data, textStatus, jqXHR) {
            deferred.resolve(data, certificate);
        });
        return deferred.promise();
    }

    function parseDataToBeSigned(responseData, certificate) {
        var deferred = $.Deferred();
        log.debug('received certificate response: ' + responseData);
        var dataToBeSigned = $($.parseXML(responseData)).find('SignedInfo').text();
        var signedData = mocca_js.stal.sign(certificate, algorithmId, dataToBeSigned);
        deferred.resolve(signedData);
        return deferred.promise();
    }

    function sendSignedData(signedData) {
        log.debug("Signed data: " + signedData);
        return mocca_js.backend.sendSignedData(_parameters.SessionID, signedData);
    }

    function parseSignedDataResponse(response) {
        var deferred = $.Deferred();
        log.debug("received signed data response: " + response);
        deferred.resolve();
        return deferred.promise();
    }

    function redirectUser() {
        parent.document.location.href = _parameters.RedirectURL;
    }

    return {
        run: run
    }
});
