(function() {
  function CountriesService(ApiRoot, $http) {
    this.countries = null;

    this.get = function() {
      return $http.get(ApiRoot + 'refdata/country');
    };

    this.resolve = function() {
      return this.get();
    };
  }

  CountriesService.$inject = ['ApiRoot', '$http'];

  angular
    .module('algo.services.refdata')
    .service('CountriesService', CountriesService);
})();