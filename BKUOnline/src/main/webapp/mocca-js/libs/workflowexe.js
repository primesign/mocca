/*****************************************************************************
* The WorkflowExe API                                                        *
* Description  : Library for interfacing with Workflow.exe                   *
* Project Name : Self Service                                                *
* Version      : 2.3 (Workflow version.Library version)                      *
* Company	   : Cryptas IT-Security GmbH									 * 
******************************************************************************/

WorkflowExe = {

    // Logging kinds:
    "logEXCEPT": 0,
    "logERROR": 1,
    "logWARNING": 2,
    "logINFO": 3,
    "logDEBUG": 4,

    // Log File: Support log kinds EXCEPT, ERROR, WARNING and INFO
    writeToLogFile: function (iLogKind, strEntry) {
        try {
            window.external.WriteToLogFile(iLogKind, strEntry);
        } catch (e) {
            WorkflowExe.writeToEventLog(WorkflowExe.logERROR, "writeToLogFile exception: " + e.description);
        }
    },

    // EventLog: Support log kind ERROR, WARNING and INFO
    writeToEventLog: function (iLogKind, strEntry) {
        try {
            window.external.WriteToEventLog(iLogKind, strEntry);
        } catch (e) {
            // Write to eventlog was not possible...
        }
    },

    isWindows64Bit: function () {
        try {
            return window.external.IsWindows64Bit();
        } catch (e) {
            WorkflowExe.writeToLogFile(WorkflowExe.logEXCEPT, "isWindows64Bit exception: " + e.description);
            throw e;
        }
    },

    isAvailable: function () {
        try {
            return "IsWindows64Bit" in window.external;
        } catch (e) {
            return false;
        }
    },

    closeWorkflow: function (bForce) {
        try {
            return window.external.CloseWorkflow(!!bForce);
        } catch (e) {
            WorkflowExe.writeToLogFile(WorkflowExe.logEXCEPT, "closeWorkflow exception: " + e.description);
            throw e;
        }
    },

    setAuthenticatedUser: function (strUsername, strPassword) {
        try {
            return window.external.SetAuthenticatedUser(strUsername, strPassword);
        } catch (e) {
            WorkflowExe.writeToLogFile(WorkflowExe.logEXCEPT, "setAuthenticatedUser exception: " + e.description);
            throw e;
        }
    },

    setGlobalData: function (strKey, value, bSecure) {
        try {
            return window.external.SetGlobalData(strKey, JSON.stringify(value), bSecure);
        } catch (e) {
            WorkflowExe.writeToLogFile(WorkflowExe.logEXCEPT, "setGlobalData exception: " + e.description);
            throw e;
        }
    },

    getGlobalData: function (strKey) {
        // Note: Return value can be any kind of JavaScript type including object and array
        try {
            var value = window.external.GetGlobalData(strKey);
            return value ? JSON.parse(value) : null;
        } catch (e) {
            WorkflowExe.writeToLogFile(WorkflowExe.logEXCEPT, "getGlobalData exception: " + e.description);
            throw e;
        }
    },

    hasGlobalData: function (strKey) {
        try {
            return window.external.HasGlobalData(strKey);
        } catch (e) {
            WorkflowExe.writeToLogFile(WorkflowExe.logEXCEPT, "hasGlobalData exception: " + e.description);
            throw e;
        }
    },

    removeGlobalData: function (strKey) {
        try {
            return window.external.RemoveGlobalData(strKey);
        } catch (e) {
            WorkflowExe.writeToLogFile(WorkflowExe.logEXCEPT, "removeGlobalData exception: " + e.description);
            throw e;
        }
    },

    // iTimeout: Timeout in milliseconds, only apply when also bWaitForCompletion is true.
    //           If iTimeout is missing or iTimeout is zero then its a infinite wait.
    execCommand: function (strCommandWithPath, strCommandParam, bWaitForCompletion, iTimeout, bForce64bit, strStdin) {
        try {
            var value = window.external.ExecCommand(strCommandWithPath, strCommandParam, bWaitForCompletion, iTimeout, bForce64bit, strStdin);
            strStdin = null;
            return value ? JSON.parse(value) : null;
        } catch (e) {
            WorkflowExe.writeToLogFile(WorkflowExe.logEXCEPT, "execCommand exception: " + e.description);
            throw e;
        }
    },

    getUserData: function () {
        try {
            var value = window.external.GetUserDataGroup();
            return value ? JSON.parse(value) : null;
        } catch (e) {
            WorkflowExe.writeToLogFile(WorkflowExe.logEXCEPT, "getUserData exception: " + e.description);
            throw e;
        }
    },

    getCardData: function () {
        try {
            var value = window.external.GetCardDataGroup();
            return value ? JSON.parse(value) : null;
        } catch (e) {
            WorkflowExe.writeToLogFile(WorkflowExe.logEXCEPT, "getCardData exception: " + e.description);
            throw e;
        }
    },

    getSystemData: function () {
        try {
            var value = window.external.GetSystemDataGroup();
            return value ? JSON.parse(value) : null;
        } catch (e) {
            WorkflowExe.writeToLogFile(WorkflowExe.logEXCEPT, "getSystemData exception: " + e.description);
            throw e;
        }
    },

    getPrecheckData: function () {
        try {
            var value = window.external.GetPrecheckDataGroup();
            return value ? JSON.parse(value) : null;
        } catch (e) {
            WorkflowExe.writeToLogFile(WorkflowExe.logEXCEPT, "getPrecheckData exception: " + e.description);
            throw e;
        }
    },

    getCommandLineData: function () {
        try {
            var value = window.external.GetCommandLineDataGroup();
            return value ? JSON.parse(value) : null;
        } catch (e) {
            WriteToLogFile(WorkflowExe.logEXCEPT, "getCommandLineData exception: " + e.description);
            throw e;
        }
    },

    getWorkflowData: function () {
        try {
            var value = window.external.GetWorkflowDataGroup();
            return value ? JSON.parse(value) : null;
        } catch (e) {
            WorkflowExe.writeToLogFile(WorkflowExe.logEXCEPT, "getWorkflowData exception: " + e.description);
            throw e;
        }
    },

    updateCardCertificates: function () {
        try {
            return window.external.UpdateCardCertificates();
        } catch (e) {
            WorkflowExe.writeToLogFile(WorkflowExe.logEXCEPT, "updateCardCertificates exception: " + e.description);
            throw e;
        }
    },

    setCloseDialogEnabled: function (bEnableFlag) {
        try {
            return window.external.SetCloseDialogEnabled(bEnableFlag);
        } catch (e) {
            WorkflowExe.writeToLogFile(WorkflowExe.logEXCEPT, "setCloseDialogEnabled exception: " + e.description);
            throw e;
        }
    },

    setCloseDialogConfig: function (value) {
        try {
            return window.external.SetCloseDialogConfig(JSON.stringify(value));
        } catch (e) {
            WorkflowExe.writeToLogFile(WorkflowExe.logEXCEPT, "setCloseDialogConfig exception: " + e.description);
            throw e;
        }
    },

    "auditSUCCESS": "Success",
    "auditSTARTED": "Started",
    "auditERROR": "Error",
    "auditABORT": "Abort",

    createNewAudit: function (strMessage, strStatus) {
        try {
            var audit = window.external.CreateNewAudit(strMessage, strStatus);
            if (audit === -1) {
                throw new Error("Audit could not be created");
            }
            return audit;
        } catch (e) {
            WorkflowExe.writeToLogFile(WorkflowExe.logEXCEPT, "createNewAudit exception: " + e.description);
            throw e;
        }
    },

    addTraceAudit: function (iAudit, strMessage, strStatus) {
        try {
            var audit = window.external.AddTraceAudit(iAudit, strMessage, strStatus);
            if (audit === -1) {
                throw new Error("Trace audit could not be created");
            }
            return audit;
        } catch (e) {
            WorkflowExe.writeToLogFile(WorkflowExe.logEXCEPT, "addTraceAudit exception: " + e.description);
            throw e;
        }
    },

    updateAudit: function (iAudit, strStatus, items) {
        // TODO: Maybe items should be a JSON object!?
        try {
            if (!window.external.UpdateAudit(iAudit, strStatus, JSON.stringify(items))) {
                throw new Error("Audit entry could not be updated");
            }
        } catch (e) {
            WorkflowExe.writeToLogFile(WorkflowExe.logEXCEPT, "updateAudit exception: " + e.description);
            throw e;
        }
    },

    setItemsAppendAudit: function (items) {
        try {
            if (!window.external.SetItemsAppendAudit(JSON.stringify(items))) {
                throw new Error("Audit entry could not be updated");
            }
        } catch (e) {
            WorkflowExe.writeToLogFile(WorkflowExe.logEXCEPT, "setItemsAppendAudit exception: " + e.description);
            throw e;
        }
    },

    setAutoCredentialEnabled: function (bEnableFlag) {
        try {
            if (!window.external.SetAutoCredentialEnabled(bEnableFlag)) {
                throw new Error("Auto credential could not be enabled");
            }
        } catch (e) {
            WorkflowExe.writeToLogFile(WorkflowExe.logEXCEPT, "setAutoCredentialEnabled exception: " + e.description);
            throw e;
        }
    },

    setAutoCredentialConfig: function (value) {
        try {
            if (!window.external.SetAutoCredentialConfig(JSON.stringify(value))) {
                throw new Error("Auto credential config could not be set");
            }
        } catch (e) {
            WorkflowExe.writeToLogFile(WorkflowExe.logEXCEPT, "setAutoCredentialConfig exception: " + e.description);
            throw e;
        }
    },

    sleep: function (iTime) {
        try {
            return window.external.Sleep(iTime);
        } catch (e) {
            WorkflowExe.writeToLogFile(WorkflowExe.logEXCEPT, "sleep exception: " + e.description);
            throw e;
        }
    },

    addAutoRedirect: function (strTriggerPage, strTargetPage) {
        try {
            return window.external.AddAutoRedirect(strTriggerPage, strTargetPage);
        } catch (e) {
            WorkflowExe.writeToLogFile(WorkflowExe.logEXCEPT, "addAutoRedirect exception: " + e.description);
            throw e;
        }
    },

    resetAutoRedirect: function () {
        try {
            window.external.ResetAutoRedirect();
        } catch (e) {
            WorkflowExe.writeToLogFile(WorkflowExe.logEXCEPT, "resetAutoRedirect exception: " + e.description);
            throw e;
        }
    },

    addAutoAction: function (strTriggerPage, strFrameName, strIdElement, strExecuteScript) {
        try {
            return window.external.AddAutoAction(strTriggerPage, strFrameName, strIdElement, strExecuteScript);
        } catch (e) {
            WorkflowExe.writeToLogFile(WorkflowExe.logEXCEPT, "addAutoAction exception: " + e.description);
            throw e;
        }
    },

    resetAutoAction: function () {
        try {
            window.external.ResetAutoAction();
        } catch (e) {
            WorkflowExe.writeToLogFile(WorkflowExe.logEXCEPT, "resetAutoAction exception: " + e.description);
            throw e;
        }
    },

    setWorkflowQuitCode: function (iCode) {
        try {
            window.external.SetWorkflowQuitCode(iCode);
        } catch (e) {
            WorkflowExe.writeToLogFile(WorkflowExe.logEXCEPT, "setWorkflowQuitCode exception: " + e.description);
            throw e;
        }
    },
	
    // Registry access functions:

    // Possible key shortcuts for the registry:
    //  HKCR - HKEY_CLASSES_ROOT
    //  HKCU - HKEY_CURRENT_USER 
    //  HKLM - HKEY_LOCAL_MACHINE
    //  HKU  - HKEY_USERS
    //  HKCC - HKEY_CURRENT_CONFIG

    // Example: "HKLM\SOFTWARE\Microsoft\Windows\CurrentVersion\Policies\System"

    // Registry value types:
    "regNONE":       0,     // No value type
    "regSZ":         1,     // Unicode nul terminated string    
    "regBINARY":     3,     // Free form binary
    "regDWORD":      4,     // 32-bit number
    "regMULTI_SZ":   7,     // Multiple Unicode strings
    "regQWORD":     11,     // 64 bit

    hasRegKey: function (strKey, bForce64bit)
    {
        try {
            return window.external.HasRegKey(strKey, bForce64bit);
        } catch (e) {
            WorkflowExe.writeToLogFile(WorkflowExe.logEXCEPT, "hasRegKey exception: " + e.description);
            throw e;
        }
    },

    hasRegValue: function (strKey, strValue, bForce64bit)
    {
        try {
            return window.external.HasRegValue(strKey, strValue, bForce64bit);
        } catch (e) {
            WorkflowExe.writeToLogFile(WorkflowExe.logEXCEPT, "hasRegValue exception: " + e.description);
            throw e;
        }
    },

    // Return a JSON object: 
    // 
    // Examples: 
    //  { "iResult": 1, "iType": 0,  "value": null }                                  // Not found
    //  { "iResult": 0,  "iType": 0,  "value": null }                                 // Found but no value
    //  { "iResult": 0,  "iType": 4,  "value": 4294967295 }                           // DWORD (32-bit)
    //  { "iResult": 0,  "iType": 11, "value": 18446744073709550000 }                 // QWORD (64-bit)    
    //  { "iResult": 0,  "iType": 1,  "value": "Hello World" }                        // String
    //  { "iResult": 0,  "iType": 3,  "value": "TWFuIGlzIGRpc3Rpbmd1aXNoZWQsI..." }   // Binary as a base64 encoded string
    //  { "iResult": 0,  "iType": 7,  "value": ["Hello", "World"] }                   // Multi-String value    

    getRegValue: function (strKey, strValue, bForce64bit)
    {
        try {
            var value = window.external.GetRegValue(strKey, strValue, bForce64bit);
            return value ? JSON.parse(value) : null;
        } catch (e) {
            WorkflowExe.writeToLogFile(WorkflowExe.logEXCEPT, "getRegValue exception: " + e.description);
            throw e;
        }
    }

};

window.onerror = function (msg, url, lineNo, columnNo, error) 
{
  try
  {
	  // Note: columnNo and error are new in HTML 5 spec and may not be present
	  var strError = "[ONERROR] msg:" + msg + ", url: " + url + ", line: " + lineNo;
	  
	  if ( columnNo != null ) 
	  {
			strError += ", column: " +  columnNo;
	  }
	  WorkflowExe.writeToLogFile(1,strError);
	  
	  if ( ( error != null) && (typeof error == "object" ) )
	  {
		  // The error object is only present in Internet Explorer 11
		  WorkflowExe.writeToLogFile(1,"[ONERROR] " + error.toString() );
	  }
  }
  catch (e)
  {}  
  return false; // No error dialog
}
