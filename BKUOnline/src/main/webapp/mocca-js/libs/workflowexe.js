/*******************************************************************************
 * <copyright> Copyright 2018 by Cryptas IT-Security GmbH, Vienna, Austria 
 * </copyright>
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
