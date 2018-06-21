
require.config({
  //By default load any module IDs from mocca-js/*
  baseUrl: '/src/main/webapp/mocca-js',
  paths: {
    app: '../app',
    libs: 'libs/'
  }
});
define(['libs/workflowexe', 'moccajs', 'backend', 'stal', 'stalMock', 'errorHandler'], function (workflowexe, moccajs) {

  describe('The class selfServiceLibraryWrapper ', function () {


    // instantiate the class
    //   beforeEach(() => {
    //     selfServiceLibraryWrapper = new SelfServiceLibraryWrapper({ workflowExe: { mockLibrary: true }}, mockSelfServiceLibraryService, $log);
    //   });
    it('moccajs should have a run method defined', function () {
      expect(moccajs).toBeDefined();
      expect(moccajs.run).toBeDefined();
    });
  })
});
