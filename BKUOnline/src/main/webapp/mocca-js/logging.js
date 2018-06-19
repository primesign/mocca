
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
                    WorkflowExe.writeToLogFile(WorkflowExe.logINFO, enhancedMessage);
                    console.log(enhancedMessage);
                },
                info: function(message) {
                    var enhancedMessage = enhanceLoggingMessage(message, context);
                    WorkflowExe.writeToLogFile(WorkflowExe.logINFO, enhancedMessage);
                    console.info(enhancedMessage);
                },
                warn: function(message) {
                    var enhancedMessage = enhanceLoggingMessage(message, context);
                    WorkflowExe.writeToLogFile(WorkflowExe.logEXCEPT, enhancedMessage);
                    console.warn(enhancedMessage);
                },
                debug: function(message) {
                    var enhancedMessage = enhanceLoggingMessage(message, context);
                    WorkflowExe.writeToLogFile(WorkflowExe.logINFO, enhancedMessage);
                    console.debug(enhancedMessage);
                },
                error: function(message) {
                    var enhancedMessage = enhanceLoggingMessage(message, context);
                    WorkflowExe.writeToLogFile(WorkflowExe.logERROR, enhancedMessage);
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
                    console.log(enhanceLogging(message, context));
                },
                info: function (message) {
                    console.info(enhanceLogging(message, context));
                },
                warn: function (message) {
                    console.warn(enhanceLogging(message, context));
                },
                debug: function (message) {
                    console.debug(enhanceLogging(message, context));
                },
                error: function (message) {
                    console.error(enhanceLogging(message, context));
                },
            };
        }
    }
}