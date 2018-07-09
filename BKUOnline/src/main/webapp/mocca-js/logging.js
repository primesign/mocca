/*******************************************************************************
 * <copyright> Copyright 2018 by PrimeSign GmbH, Graz, Austria </copyright>
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * http://www.osor.eu/eupl/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 *
 * This product combines work with different licenses. See the "NOTICE" text
 * file for details on the various modules and licenses.
 * The "NOTICE" text file is part of the distribution. Any derivative works
 * that you distribute must include a readable copy of the "NOTICE" text file.
 ******************************************************************************/

// Adds a global object for appropiate logging. If run inside selfservice portal logoutput will be written to console.* and WorkflowExe.writeToLogFile.

/**
 * Adds datetime and given context to the log output.
 * @param {string} message the message to log. Mustn't be null.
 * @param {string} context the calling context of the log output, filename for example
 */
function enhanceLoggingMessage(message, context) {
    // add component name and date to log message
    return [new Date().toISOString() + ' [' + context + '] '] + message;
}
/**
 * Tries to figure out if the javascript is running in webcontext accessible to the selfservicelibrary context.
 * @returns true if type of window.external.writeToLogFile is defined
 */
function isRunningInSelfServiceClient() {
    if (typeof window !== 'undefined' && typeof window.external !== 'undefined' && typeof window.external.writeToLogFile !== 'undefined') {
        return true;
    } else {
        return false;
    }
}
if (isRunningInSelfServiceClient()) {
    log = {
        getInstance: function (context) {
            return {
                log: function (message) {
                    var enhancedMessage = enhanceLoggingMessage(message, context);
                    WorkflowExe && WorkflowExe.writeToLogFile(WorkflowExe.logINFO, enhancedMessage);
                    console.log(enhancedMessage);
                },
                info: function (message) {
                    var enhancedMessage = enhanceLoggingMessage(message, context);
                    WorkflowExe && WorkflowExe.writeToLogFile(WorkflowExe.logINFO, enhancedMessage);
                    console.info(enhancedMessage);
                },
                warn: function (message) {
                    var enhancedMessage = enhanceLoggingMessage(message, context);
                    WorkflowExe && WorkflowExe.writeToLogFile(WorkflowExe.logEXCEPT, enhancedMessage);
                    console.warn(enhancedMessage);
                },
                debug: function (message) {
                    var enhancedMessage = enhanceLoggingMessage(message, context);
                    WorkflowExe && WorkflowExe.writeToLogFile(WorkflowExe.logDEBUG, enhancedMessage);
                    console.info(enhancedMessage);
                },
                error: function (message) {
                    var enhancedMessage = enhanceLoggingMessage(message, context);
                    WorkflowExe && WorkflowExe.writeToLogFile(WorkflowExe.logERROR, enhancedMessage);
                    console.error(enhancedMessage);
                }
            };
        }
    }
} else {
    log = {
        getInstance: function (context) {
            return {
                log: function (message) {
                    console.log(enhanceLoggingMessage(message, context));
                },
                info: function (message) {
                    console.info(enhanceLoggingMessage(message, context));
                },
                warn: function (message) {
                    console.warn(enhanceLoggingMessage(message, context));
                },
                debug: function (message) {
                    console.debug(enhanceLoggingMessage(message, context));
                },
                error: function (message) {
                    console.error(enhanceLoggingMessage(message, context));
                },
            };
        }
    }
}

/**
 * Attempts to stringify an xml object to a string for logging purpose.
 * @param {XMLDocument} xml the xml to stringify
 */
log.stringifyXML = function (xml) {
    try {
        return new XMLSerializer().serializeToString(xml);
    } catch (e) {
        return xml;
    }
}