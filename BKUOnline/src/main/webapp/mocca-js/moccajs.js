
var mocca_js = {};

var WorkflowExe;
define('moccajs', function(require) {

    var algorithmId = 'rsa-sha256';
    mocca_js.backend = require('backend');
    mocca_js.stal = require('stal');
    mocca_js.errorHandler = require('errorHandler');
    var _log = log.getInstance('moccajs.js');
    _log.debug('mocca_js: ' + JSON.stringify(mocca_js));
    _log.debug('mocca_js initialized!');

    function mockSTAL() {
        mocca_js.stal = require('stalMock');
    }

    function run(parameters, isMockSTAL) {
        if (isMockSTAL && isMockSTAL === true) {
            mockSTAL();
        }
        _log.debug('starting mocca-js with parameters: ' + JSON.stringify(parameters));
        mocca_js._parameters = parameters;
        mocca_js.backend.setBaseUrl(parameters.ContextPath);
        mocca_js.backend.connect(parameters.SessionID)
            .then(selectCertificate)
            .then(sendCertificate)
            .then(parseDataToBeSigned)
            .then(sendSignedData)
            .then(parseSignedDataResponse)
            .then(redirectUser)
            .fail(mocca_js.errorHandler.handleError);
    }

    function selectCertificate(responseData) {
        var deferred = $.Deferred();
        _log.debug('connect responseData: ' + log.printXML(responseData));
        var certificate = mocca_js.stal.selectCertificate();
        deferred.resolve(certificate);
        return deferred.promise();
    }

    function sendCertificate(certificate) {
        var deferred = $.Deferred();
        _log.debug('selectedCertificate: ' + certificate);
        mocca_js.backend.sendCertificate(mocca_js._parameters.SessionID, certificate).then(function (data, textStatus, jqXHR) {
            deferred.resolve(data, certificate);
        });
        return deferred.promise();
    }

    function parseDataToBeSigned(responseData, certificate) {
        var deferred = $.Deferred();
        _log.debug('received certificate response: ' + log.printXML(responseData));
        var signedInfo = $(responseData).find('SignedInfo').text();
        if (signedInfo.length === undefined || signedInfo.length == 0){
            _log.debug('if signedInfo: ' + signedInfo + ' length: ' + signedInfo.length);
            sendCertificate(certificate).then(parseDataToBeSigned).then(function(signedData) {
                deferred.resolve(signedData);
            });
        } else {
            _log.debug('else signedInfo: ' + signedInfo + ' length: ' + signedInfo.length);
            _log.debug('signedInfo: ' + signedInfo.length + ' value: ' + signedInfo);
            var signedData = mocca_js.stal.sign(certificate, algorithmId, signedInfo);
            deferred.resolve(signedData);
        }
        return deferred.promise();
    }

    function sendSignedData(signedData) {
        _log.debug('Signed data: ' + signedData);
        return mocca_js.backend.sendSignedData(mocca_js._parameters.SessionID, signedData);
    }

    function parseSignedDataResponse(response) {
        _log.debug('received signed data response: ' + response);
    }

    function redirectUser() {
        _log.debug('finished signing document!!!!!!!!!!!!!!!!!!!!!!!!!!!!');
    }

    return {
        run: run
    }
});
