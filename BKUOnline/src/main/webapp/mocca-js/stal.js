
define(function () {
    var TIMEOUT_SIGN = 180000;
    var TIMEOUT_SELECTCERTIFICATE = 30000;

    var _log = log.getInstance('stal.js');
    _log.debug('WorkflowExe: ' + JSON.stringify(WorkflowExe));
    // Function   : selectCertificate
    // Description: Allows the user to select a signing certificate and returns the selected certificate (DER-Encoding). 
    // Parameters :
    //		guiOptions: Optional parameter if GUI should be shown even if there is only certificate available.
    // Return: In case of success the selected certificate as Base64-encoded string, otherwise an exception is thrown with the error code.
    // Example1: selectCertificate();
    // Example2: selectCertificate("gui"); // Force a GUI selection box even though there is only one certificate.
    function selectCertificate(guiOptions) {
        var strCertificate = null;
        var iError = -1;

        var strGuiOptions = "";

        // Check if function was called with parameter:
        if (guiOptions != null) {
            strGuiOptions = guiOptions;
        }

        try {
            // Execute command and wait for command completion:
            var jsonResult = WorkflowExe.execCommand("Signer.exe", "-select " + strGuiOptions, true, TIMEOUT_SELECTCERTIFICATE);

            // Check first if the program Signer.exe could be executed:
            if (jsonResult.strErrorExecution == "") {
                // Command completed execution:				
                if (jsonResult.iExitCode == 0) {
                    // Execution success:

                    // Obtain selected certificate:
                    strCertificate = JSON.parse(jsonResult.strConsoleOutput).cert;
                }
                else {
                    // Execution Error:

                    // Obtain error json:
                    var jsonErrorOutput = JSON.parse(jsonResult.strErrorOutput);

                    WorkflowExe.writeToLogFile(WorkflowExe.logERROR, "Error (selectCertificate): errcode=" + jsonErrorOutput.errcode +
                        ", message=" + jsonErrorOutput.message + ", details=" + jsonErrorOutput.details);

                    // 	1001 Undefined error 
                    //  1002 Cancelled by user
                    //  1003 No (suitable) certificate found - In case no certificate is found											   
                    iError = jsonErrorOutput.errcode;

                    // Next two lines are just for testing purposes, they are not to remain in selectCertificate function:
                    strLastExceptionMessage = jsonErrorOutput.message; // Just for testing, not normally part of selectCertificate
                    strLastExceptionDetails = jsonErrorOutput.details; // Just for testing, not normally part of selectCertificate
                }
            }
            else if (jsonResult.iExitCode === 1 && jsonResult.bTimeout === true) {
                WorkflowExe.writeToLogFile(WorkflowExe.logERROR, "Program could not be executed (selectCertificate) before timeout");
                iError = 1009;
            } else {
                // Program could not be executed, this could be a signature check that failed or the command is not allowed:
                WorkflowExe.writeToLogFile(WorkflowExe.logERROR, "Program could not be executed (selectCertificate): " + jsonResult.strErrorExecution);

                // Next two lines are just for testing purposes, they are not to remain in selectCertificate function:
                strLastExceptionMessage = "Signer program could not be executed"; // Just for testing, not normally part of selectCertificate
                strLastExceptionDetails = jsonResult.strErrorExecution; 		  // Just for testing, not normally part of selectCertificate

                // 	1001 Undefined error 
                iError = 1001;
            }
        }
        catch (e) {
            // Exception:
            WorkflowExe.writeToLogFile(WorkflowExe.logEXCEPT, "Exception (selectCertificate): " + e.message);

            // Next two lines are just for testing purposes, they are not to remain in selectCertificate function:
            strLastExceptionMessage = "Javascript Exception"; 					  // Just for testing, not normally part of selectCertificate
            strLastExceptionDetails = e.message; 		  						  // Just for testing, not normally part of selectCertificate

            // 	1001 Undefined error 
            iError = 1001;
        }

        // Check for an error:
        if (iError != -1) {
            // Throw exception
            throw iError;
        }

        // Return the obtained certificate
        return strCertificate;
    }

    // Function   : sign
    // Description: Applies an electronic signature with certificate and algorithmId on the given dataToBeSigned and returns the resulting signature.
    // Parameters :
    //		certificate	  : The signing certificate (DER encoded) as Base64-encoded string.
    //  	algorithmId	  : A signature algorithm identifier the specifies the signature algorithm to be applied. 
    //  	dataToBeSigned:	The data to-be signed as Base64-encoded string.
    // Return: The resulting signature string in case of a successful signing, otherwise an exception is thrown with the error code.
    // Example: sign( "MIIGTTCCBTWgAwIBAgIKPi...", "rsa-sha256", "V2VyIHJlaXRldCBzbyBzc...")
    function sign(certificate, algorithmId, dataToBeSigned) {
        var strSignature = null;
        var iError = -1;

        try {
            // Build first the json request:			
            var jsonSignRequest = { "algo": algorithmId, "cert": certificate, "data": dataToBeSigned };

            // Execute command and wait for command completion:
            var jsonResult = WorkflowExe.execCommand("Signer.exe", "-sign", true, TIMEOUT_SIGN, false, JSON.stringify(jsonSignRequest));

            // Check first if the program Signer.exe could be executed:
            if (jsonResult.strErrorExecution == "") {
                // Command completed execution:
                if (jsonResult.iExitCode == 0) {
                    // Execution success:

                    // Obtain signature:
                    strSignature = JSON.parse(jsonResult.strConsoleOutput).signature;
                }
                else {
                    // Execution Error:

                    // Obtain error json:
                    var jsonErrorOutput = JSON.parse(jsonResult.strErrorOutput);

                    WorkflowExe.writeToLogFile(WorkflowExe.logERROR, "Error (sign): errcode=" + jsonErrorOutput.errcode +
                        ", message=" + jsonErrorOutput.message + ", details=" + jsonErrorOutput.details);

                    //  1001 Undefined error - e.g. sign.exe is not available, call cannot be performed …​
                    //  1002 Cancelled by user
                    //  1005 No key found for the requested certificate -  E.g. smart card is not inserted
                    //  1006 Unsupported signature algorithm
                    //  1007 Invalid Argument - e.g. certificate cannot be parsed due to invalid encoding
                    //  1008 Missing Argument - e.g. missing data to be signed, missing algorithm…​
                    iError = jsonErrorOutput.errcode;

                    // Next two lines are just for testing purposes, they are not to remain in sign function:
                    strLastExceptionMessage = jsonErrorOutput.message; // Just for testing, not normally part of sign
                    strLastExceptionDetails = jsonErrorOutput.details; // Just for testing, not normally part of sign					
                }
            }
            else if (jsonResult.iExitCode === 1 && jsonResult.bTimeout === true) {
                WorkflowExe.writeToLogFile(WorkflowExe.logERROR, "Program could not be executed (sign) before timeout");
                iError = 1009;
            } else {
                // Program could not be executed, this could be a signature check that failed or the command is not allowed:
                WorkflowExe.writeToLogFile(WorkflowExe.logERROR, "Program could not be executed (sign): " + jsonResult.strErrorExecution);

                // Next two lines are just for testing purposes, they are not to remain in sign function:
                strLastExceptionMessage = "Signer program could not be executed"; // Just for testing, not normally part of sign
                strLastExceptionDetails = jsonResult.strErrorExecution; 		  // Just for testing, not normally part of sign

                // 	1001 Undefined error 
                iError = 1001;
            }
        }
        catch (e) {
            // Exception:
            WorkflowExe.writeToLogFile(WorkflowExe.logEXCEPT, "Exception (sign): " + e.message);

            // Next two lines are just for testing purposes, they are not to remain in sign function:
            strLastExceptionMessage = "Javascript Exception"; 					  // Just for testing, not normally part of sign
            strLastExceptionDetails = e.message; 		  						  // Just for testing, not normally part of sign

            // 	1001 Undefined error 
            iError = 1001;
        }

        // Check for an error:
        if (iError != -1) {
            // Throw exception
            throw iError;
        }

        // Return the obtained signature
        return strSignature;
    }

    return {
        selectCertificate: selectCertificate,
        sign: sign
    };
});