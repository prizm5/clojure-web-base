(function() {

  function BasicRefDataService($http, ApiRoot) {
    this.save = function(type, data) {
      return $http.post(ApiRoot + 'refdata/' + type, data);
    };

    this.get = function(type) {
      return $http.get(ApiRoot + 'refdata/' + type);
    };

  }

  BasicRefDataService.$inject = ['$http', 'ApiRoot'];

  angular
    .module('algo.services.refdata')
    .service('BasicRefDataService', BasicRefDataService);

  function BasicRefDataResolver($route, basicRefDataService) {
    this.resolve = function() {
      return basicRefDataService.get($route.current.params.type);
    };
  }

  BasicRefDataResolver.$inject = ['$route', 'BasicRefDataService'];
  angular
    .module('algo.halcyon.resolvers')
    .service('BasicRefDataResolver', BasicRefDataResolver);

  function BasicRefDataCtrl($routeParams, resolveData) {

    var self, original;
    self = this;
    self.type = $routeParams.type;

    self.items = resolveData.data;
  }

  angular
    .module('algo.halcyon.controllers.refdata')
    .controller("BasicRefDataCtrl", ['$routeParams', 'resolveData', BasicRefDataCtrl]);

  function CreateBasicRefDataCtrl($routeParams, basicRefDataService, $timeout) {

    var self, original;
    self = this;
    self.type = $routeParams.type;
    self.dataObj = {
      id: '',
      value: '',
      type: self.type
    };

    original = angular.copy(self.dataObj);

    self.save = function() {
      self.disableSubmit = true;
      basicRefDataService.save(self.type, self.dataObj)
        .then(function(data) {
          if (data.status == 200) {
            self.saveMessage = "Saved successfully.";
            self.dataObj = angular.copy(original);
            self.disableSubmit = false;
            self.form.$setPristine();
            $timeout(function() {
              self.saveMessage = null;
            }, 3000);
          }
        }, function(error) {
          self.message = error;
          self.disableSubmit = false;
        });
    };
  }
  angular
    .module('algo.halcyon.controllers.refdata')
    .controller("CreateBasicRefDataCtrl", ['$routeParams', 'BasicRefDataService', '$timeout',
      CreateBasicRefDataCtrl
    ]);

})();