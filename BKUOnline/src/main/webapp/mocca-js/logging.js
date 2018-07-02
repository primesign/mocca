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