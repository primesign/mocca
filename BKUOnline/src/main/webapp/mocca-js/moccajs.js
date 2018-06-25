
var mocca_js = {};

var WorkflowExe;
define('moccajs', function(require) {

    var algorithmId = 'rsa-sha256';
    mocca_js.backend = require('backend');
    mocca_js.stal = require('stal');
    mocca_js.errorHandler = require('errorHandler');
    mocca_js.data = {};
    mocca_js.flow = [{
        responseType: "",
        methodToCall: sendCertificate
    },{
        responseType: "",
        methodToCall: sendCertificate
    }];
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
        function callBackend() {
            if (!mocca_js.data.responseType) {
                mocca_js.backend.connect(parameters.SessionID).then(parseResponse).then(callBackend).fail(mocca_js.errorHandler.handleError);
            } else if (mocca_js.data.responseType === mocca_js.backend.INFOBOX_READ_REQ) {
                sendCertificate().then(parseResponse).then(callBackend).fail(mocca_js.errorHandler.handleError);
            } else if (mocca_js.data.responseType === mocca_js.backend.INFOBOX_SIGN_REQ) {
                sendSignedData().then(parseResponse).then(callBackend).fail(mocca_js.errorHandler.handleError);
            }
        }
        callBackend();
    }


    function parseResponse(responseData) {
        _log.debug('parseResponse responseData: ' + log.printXML(responseData));
        var responseType = mocca_js.backend.validateXMLResponse(responseData);
        _log.debug('received response of type "' +responseType + '"');
        if (responseType === mocca_js.backend.INFOBOX_READ_REQ && !mocca_js.data.certificate) {
            selectCertificate();
        } else if (responseType === mocca_js.backend.INFOBOX_SIGN_REQ && !mocca_js.data.signedData) {
            parseDataToBeSigned(responseData);
        }
        mocca_js.data.responseType = responseType;
    }

    function selectCertificate() {
        var certificate = mocca_js.stal.selectCertificate();
        _log.debug('select certificate: ' + log.printXML(certificate));
        mocca_js.data.certificate = certificate;
    }

    function sendCertificate() {
        var deferred = $.Deferred();
        _log.debug('sendCertificate: ' + mocca_js.data.certificate);
        mocca_js.backend.sendCertificate(mocca_js._parameters.SessionID, mocca_js.data.certificate).then(function (response, textStatus, jqXHR) {
            mocca_js.data.response = response;
            deferred.resolve();
        });
        return deferred.promise();
    }

    function getDataToBeSigned(responseData) {
        var deferred = $.Deferred();
        var signedInfo = $(responseData).find('SignedInfo').text();
        _log.debug('signedInfo: ' + signedInfo.length + ' value: ' + signedInfo);
        var signedData = mocca_js.stal.sign(certificate, algorithmId, signedInfo);
        mocca_js.data.signedData = signedData;
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
