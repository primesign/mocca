
var mocca_js = {};

var WorkflowExe;
define('moccajs', function(require) {

    var algorithmId = 'rsa-sha256';
    mocca_js.lang = require('lang');
    mocca_js.backend = require('backend');
    mocca_js.stal = require('stal');
    mocca_js.errorHandler = require('errorHandler');
    mocca_js.data = {};

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
        _log.debug('Starting mocca-js with parameters: ' + JSON.stringify(parameters));
        mocca_js._parameters = parameters;
        mocca_js.lang.setLocale(parameters.Locale);
        mocca_js.backend.setBaseUrl(parameters.ContextPath);
        function callBackend() {
            if (!mocca_js.data.responseType) {
                mocca_js.backend.connect(parameters.SessionID).then(parseResponse).then(callBackend).fail(mocca_js.errorHandler.handleError);
            } else if (mocca_js.data.responseType === mocca_js.backend.INFOBOX_READ_REQ) {
                sendCertificate().then(parseResponse).then(callBackend).fail(mocca_js.errorHandler.handleError);
            } else if (mocca_js.data.responseType === mocca_js.backend.INFOBOX_SIGN_REQ) {
                sendSignedData().then(parseResponse).then(function(){
                    if (mocca_js.data.responseType === mocca_js.backend.INFOBOX_QUIT_REQ){
                        _log.debug('finished signing document!!!!!!!!!!!!!!!!!!!!!!!!!!!!');
                    } else {
                        callBackend();
                    }
                }).fail(mocca_js.errorHandler.handleError);
            }
        }
        callBackend();
    }


    function parseResponse(responseData) {
        _log.debug('ParseResponse responseData: ' + log.printXML(responseData));
        var responseType = mocca_js.backend.validateXMLResponse(responseData);
        _log.debug('Received response of type "' +responseType + '"');
        if (responseType === mocca_js.backend.INFOBOX_READ_REQ && !mocca_js.data.certificate) {
            selectCertificate();
        } else if (responseType === mocca_js.backend.INFOBOX_SIGN_REQ && !mocca_js.data.signedData) {
            getDataToBeSigned(responseData);
        }
        mocca_js.data.responseType = responseType;
        _log.debug('Finished parsing response');
    }

    function selectCertificate() {
        _log.debug('Starting selectCertificate');
        var certificate = mocca_js.stal.selectCertificate();
        _log.debug('Finished select certificate: ' + certificate);
        mocca_js.data.certificate = certificate;
    }

    function sendCertificate() {
        var deferred = $.Deferred();
        _log.debug('SendCertificate: ' + mocca_js.data.certificate);
        mocca_js.backend.sendCertificate(mocca_js._parameters.SessionID, mocca_js.data.certificate).then(function (response, textStatus, jqXHR) {
            deferred.resolve(response);
        });
        return deferred.promise();
    }

    function getDataToBeSigned(responseData) {
        var signedInfo = $(responseData).find('SignedInfo').text();
        _log.debug('getDataToBeSigned info length: ' + signedInfo.length + ' value: "' + signedInfo + '"');
        var signedData = mocca_js.stal.sign(mocca_js.data.certificate, algorithmId, signedInfo);
        _log.debug('getDataToBeSigned finished signedData: ' + signedData.length + ' value: ' + signedData);
        mocca_js.data.signedData = signedData;
    }

    function sendSignedData() {
        _log.debug('Signed data: ' + mocca_js.data.signedData);
        return mocca_js.backend.sendSignedData(mocca_js._parameters.SessionID, mocca_js.data.signedData);
    }

    return {
        run: run
    }
});
