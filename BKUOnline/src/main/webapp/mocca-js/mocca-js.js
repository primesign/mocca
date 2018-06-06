require.config({
    //By default load any module IDs from js/lib
    baseUrl: 'mocca-js',
    //except, if the module ID starts with "app",
    //load it from the js/app directory. paths
    //config is relative to the baseUrl, and
    //never includes a ".js" extension since
    //the paths config could be for a directory.
    paths: {
        app: '../app'
    }
    // shim: {
    //     'libs': ['libs/workflowexe']
    // }
});
var mocca_js = {};
log = {
    debug: function(message) {
        console.log(message);
    },
    error: function(message) {
        console.log(message);
    }
}
var WorkflowExe;

require(['backend', 'libs/workflowexe', 'stal'], function(backend, WorkflowExe, stal) {
    mocca_js.backend = backend;
    mocca_js.stal = stal;
    WorkflowExe = WorkflowExe; // TODO figure out why its undefined
    log.debug('mocca_js: ' + JSON.stringify(mocca_js));
    log.debug('workflowexe: ' + JSON.stringify(WorkflowExe));
    log.debug('parameters: ' + JSON.stringify(parameters));
    log.debug('inIframe: ' + inIframe);
    log.debug('mocca_js initialized!');

    run();

    function run() {
        log.debug('starting mocca-js');
        mocca_js.backend.connect(selectCertificate, log.error);
    }

    function selectCertificate() {
        mocca_js.stal.selectCertificate(function(certificate) {
            requestInfoBoxReadRequest(certificate);
        });
    }

    function requestInfoBoxReadRequest(certificate) {
        log.debug('selectedCertificate: ' + certificate);
    }
});