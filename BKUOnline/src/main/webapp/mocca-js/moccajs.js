
var mocca_js = {};

var WorkflowExe;
define('moccajs', function (require) {
    mocca_js.lang = require('lang');
    mocca_js.backend = require('backend');
    mocca_js.stal = require('stal');
    mocca_js.errorHandler = require('errorHandler');
    mocca_js.data = {};

    var _log = log.getInstance('moccajs.js');
    _log.debug('Mocca-JS initializing with data: ' + JSON.stringify(mocca_js) + '.');
    _log.info('Mocca-JS initialized.');

    function mockSTAL() {
        mocca_js.stal = require('stalMock');
    }

    /**
     * Attempts to run a signature process with the given parameters. Sends soap requests to backend and interacts with the global object WorkflowExe and log.
     * @param {object} parameters parameters containing neccessary information for this method to be successful (e.g. sessionId). Mustn't be null.
     * @param {boolean} isMockSTAL flag for mocking the Stal interaction with the WorkflowExe.
     * @requires WorkflowExe global object
     * @requires log global object
     */
    function run(parameters, isMockSTAL) {
        var deferred = $.Deferred();
        if (isMockSTAL && isMockSTAL === true) {
            mockSTAL();
        }
        _log.info('Running Mocca-JS with parameters: ' + JSON.stringify(parameters) + '.');
        mocca_js._parameters = parameters;
        mocca_js.backend.setBaseUrl(parameters.ContextPath);
        function callBackend() {
            _log.debug('CallBackend called with responsetype: "' + mocca_js.data.responseType + '"');
            if (!mocca_js.data.responseType) {
                mocca_js.backend.connect(parameters.SessionID).then(parseResponse).then(callBackend).fail(mocca_js.errorHandler.handleError);
            } else if (mocca_js.data.responseType === mocca_js.backend.INFOBOX_READ_REQ) {
                sendCertificate().then(parseResponse).then(callBackend).fail(mocca_js.errorHandler.handleError);
            } else if (mocca_js.data.responseType === mocca_js.backend.SIGN_REQ) {
                sendSignedData().then(parseResponse).then(function () {
                    if (mocca_js.data.responseType === mocca_js.backend.QUIT_REQ) {
                        _log.info('Successfully signed document.');
                        deferred.resolve();
                    } else {
                        callBackend();
                    }
                }).fail(mocca_js.errorHandler.handleError);
            } else if (mocca_js.data.responseType === mocca_js.backend.QUIT_REQ) {
                _log.info('Processing quit request although document is not yet signed.');
                mocca_js.errorHandler.handleError(mocca_js.errorHandler.DEFAULT_ERRORCODE);
                deferred.reject({});
            }
        }
        callBackend();
        return deferred.promise();
    }

    /**
     * Attempts to parse the given {@link XMLDocument} and stores the type and content of the response type inside the mocca_js-object, the certificate and the dataToBeSigned to be specifically.
     * @param {XMLDocument} responseData the response data to parse. Mustn't be null.
     */
    function parseResponse(responseData) {
        _log.debug('Parsing STAL response: ' + log.stringifyXML(responseData) + '.');
        var responseType = mocca_js.backend.parseXMLResponse(responseData);
        _log.debug('Received STAL response of type: "' + responseType + '".');
        _log.debug('Mocca-JS current data: ' + JSON.stringify(mocca_js) + '.');
        if (responseType === mocca_js.backend.INFOBOX_READ_REQ && !mocca_js.data.certificate) {
            selectCertificate();
        } else if (responseType === mocca_js.backend.SIGN_REQ && !mocca_js.data.signedData) {
            getDataToBeSigned(responseData);
        }
        mocca_js.data.responseType = responseType;
        _log.debug('Finished parsing response.');
    }

    /**
     * Calls the stal method to select a certificate.
     */
    function selectCertificate() {
        _log.info('Selecting certificate...');
        var certificate = mocca_js.stal.selectCertificate();
        _log.debug('Selected certificate: ' + certificate + '.');
        if ($.type(certificate) === 'string' && certificate.trim().length) {
            mocca_js.data.certificate = certificate;
        } else if ($.type(certificate) === 'string') {
            _log.debug('Stal.selectCertificate retrieved empty string as certificate!');
            throw mocca_js.errorHandler.WORKFLOWEXE_EMPTY_RESPONSE;
        } else {
            _log.debug('Stal.selectCertificate was not of type "string", found "' + $.type(certificate) + '" instead, parameter was: "' + JSON.stringify(certificate) + '"');
            throw mocca_js.errorHandler.WORKFLOWEXE_UNEXPECTED_RESPONSE;
        }
    }

    /**
     * Sends the certificate stored in the mocca_js.data and returns a promise containing only the backend response.
     */
    function sendCertificate() {
        var deferred = $.Deferred();
        _log.info('Sending certificate: ' + mocca_js.data.certificate + '.');
        mocca_js.backend.sendCertificate(mocca_js._parameters.SessionID, mocca_js.data.certificate).then(function (response, textStatus, jqXHR) {
            deferred.resolve(response);
        });
        return deferred.promise();
    }

    /**
     * Attempts to parse the dataToBeSigned out of the given responseData
     * @param {XMLDocument} responseData the response data to parse. Mustn't be null.
     */
    function getDataToBeSigned(responseData) {
        _log.debug('getDataToBeSigned started with "' + log.stringifyXML(responseData) + '"')
        var signedInfo = $(responseData).find('SignedInfo').text();
        var splitted = $(responseData).find('SignatureMethod').text().split('#');
        var signatureMethod = splitted[splitted.length - 1];

        _log.debug('Parse data to be signed. Length: ' + signedInfo.length + ', value: "' + signedInfo + '".');
        _log.info('Signing data: ' + signedInfo + '.');
        _log.info('Signature Method: ' + signatureMethod + '.');
        var signedData = mocca_js.stal.sign(mocca_js.data.certificate, signatureMethod, signedInfo);
        _log.debug('Finished signing data. Length: ' + signedData.length + ', value: "' + signedData + '".');
        if ($.type(signedData) === 'string' && signedData.trim().length) {
            mocca_js.data.signedData = signedData;
        } else if ($.type(signedData) === 'string') {
            _log.debug('GetDataToBeSigned retrieved empty string as signedData!');
            throw mocca_js.errorHandler.WORKFLOWEXE_EMPTY_RESPONSE;
        } else {
            _log.debug('signedData was not of type "string", found "' + $.type(signedData) + '" instead');
            throw mocca_js.errorHandler.WORKFLOWEXE_UNEXPECTED_RESPONSE;
        }
    }

    /**
     * Adds logoutput before calling the appropriate backend method.
     */
    function sendSignedData() {
        _log.info('Sending signed data: ' + mocca_js.data.signedData + '.');
        return mocca_js.backend.sendSignedData(mocca_js._parameters.SessionID, mocca_js.data.signedData);
    }

    return {
        run: run
    }
});
