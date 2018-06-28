
var mocca_js = {};

var WorkflowExe;
define('moccajs', function(require) {
    mocca_js.lang = require('lang');
    mocca_js.backend = require('backend');
    mocca_js.stal = require('stal');
    mocca_js.errorHandler = require('errorHandler');
    mocca_js.data = {};

    var _log = log.getInstance('moccajs.js');
    _log.debug('Mocca-JS initializing with data: ' + JSON.stringify(mocca_js)+'.');
    _log.info('Mocca-JS initialized.');

    function mockSTAL() {
        mocca_js.stal = require('stalMock');
    }

    function run(parameters, isMockSTAL) {
        var deferred = $.Deferred();
        if (isMockSTAL && isMockSTAL === true) {
            mockSTAL();
        }
        _log.info('Running Mocca-JS with parameters: ' + JSON.stringify(parameters)+'.');
        mocca_js._parameters = parameters;
        mocca_js.backend.setBaseUrl(parameters.ContextPath);
        function callBackend() {
            if (!mocca_js.data.responseType) {
                mocca_js.backend.connect(parameters.SessionID).then(parseResponse).then(callBackend).fail(mocca_js.errorHandler.handleError);
            } else if (mocca_js.data.responseType === mocca_js.backend.INFOBOX_READ_REQ) {
                sendCertificate().then(parseResponse).then(callBackend).fail(mocca_js.errorHandler.handleError);
            } else if (mocca_js.data.responseType === mocca_js.backend.SIGN_REQ) {
                sendSignedData().then(parseResponse).then(function(){
                    if (mocca_js.data.responseType === mocca_js.backend.QUIT_REQ){
                        _log.info('Successfully signed document.');
                        deferred.resolve();
                    } else {
                        callBackend();
                    }
                }).fail(mocca_js.errorHandler.handleError);
            }
        }
        callBackend();
        return deferred.promise();
    }


    function parseResponse(responseData) {
        _log.debug('Parsing STAL response: ' + log.printXML(responseData)+'.');
        var responseType = mocca_js.backend.parseXMLResponse(responseData);
        _log.debug('Received STAL response of type: "' +responseType + '".');
        _log.debug('Mocca-JS current data: ' + JSON.stringify(mocca_js)+'.');
        if (responseType === mocca_js.backend.INFOBOX_READ_REQ && !mocca_js.data.certificate) {
            selectCertificate();
        } else if (responseType === mocca_js.backend.SIGN_REQ && !mocca_js.data.signedData) {
            getDataToBeSigned(responseData);
        } 
        mocca_js.data.responseType = responseType;
        _log.debug('Finished parsing response.');
    }

    function selectCertificate() {
        _log.info('Selecting certificate...');
        var certificate = mocca_js.stal.selectCertificate();
        _log.debug('Selected certificate: ' + certificate+ '.');
        mocca_js.data.certificate = certificate;
    }

    function sendCertificate() {
        var deferred = $.Deferred(); 
        _log.info('Sending certificate: ' + mocca_js.data.certificate+ '.');
        mocca_js.backend.sendCertificate(mocca_js._parameters.SessionID, mocca_js.data.certificate).then(function (response, textStatus, jqXHR) {
            deferred.resolve(response);
        });
        return deferred.promise();
    }

    function getDataToBeSigned(responseData) {
        var signedInfo = $(responseData).find('SignedInfo').text();
        var splitted = $(responseData).find('SignatureMethod').text().split('#');
        var signatureMethod = splitted[splitted.length - 1];
        
        _log.debug('Parse data to be signed. Length: ' + signedInfo.length + ', value: "' + signedInfo + '".');
        _log.info('Signing data: '+ signedInfo + '.');
        _log.info('Signature Method: '+ signatureMethod + '.');
        var signedData = mocca_js.stal.sign(mocca_js.data.certificate, signatureMethod, signedInfo);
        _log.debug('Finished signing data. Length: ' + signedData.length + ', value: "' + signedData+'".');
        mocca_js.data.signedData = signedData;
    }

    function sendSignedData() {
        _log.info('Sending signed data: ' + mocca_js.data.signedData +'.');
        return mocca_js.backend.sendSignedData(mocca_js._parameters.SessionID, mocca_js.data.signedData);
    }

    return {
        run: run
    }
});
