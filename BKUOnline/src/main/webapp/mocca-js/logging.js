
function enhanceLoggingMessage(message, context) {
      // add component name and date to log message
      return [new Date().toISOString() + ' [' + context + '] '] + message;
  }
function isRunningInSelfServiceClient() {
    if (typeof window.external.WriteToLogFile !== 'undefined') {
        return true;
    } else {
        return false;
    }
}
if (isRunningInSelfServiceClient()) {
    log = {
        getInstance: function(context) {
            return {
                log: function(message) {
                    var enhancedMessage = enhanceLoggingMessage(message, context);
                    WorkflowExe && WorkflowExe.writeToLogFile(WorkflowExe.logINFO, enhancedMessage);
                    console.log(enhancedMessage);
                },
                info: function(message) {
                    var enhancedMessage = enhanceLoggingMessage(message, context);
                    WorkflowExe && WorkflowExe.writeToLogFile(WorkflowExe.logINFO, enhancedMessage);
                    console.info(enhancedMessage);
                },
                warn: function(message) {
                    var enhancedMessage = enhanceLoggingMessage(message, context);
                    WorkflowExe && WorkflowExe.writeToLogFile(WorkflowExe.logEXCEPT, enhancedMessage);
                    console.warn(enhancedMessage);
                },
                debug: function(message) {
                    var enhancedMessage = enhanceLoggingMessage(message, context);
                    WorkflowExe && WorkflowExe.writeToLogFile(WorkflowExe.logINFO, enhancedMessage);
                    console.info(enhancedMessage);
                },
                error: function(message) {
                    var enhancedMessage = enhanceLoggingMessage(message, context);
                    WorkflowExe && WorkflowExe.writeToLogFile(WorkflowExe.logERROR, enhancedMessage);
                    console.error(enhancedMessage);
                }
            };
        }
    }
} else {
    log = {
        getInstance: function(context) {
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

log.printXML = function(xml) {
    return new XMLSerializer().serializeToString(xml);
}