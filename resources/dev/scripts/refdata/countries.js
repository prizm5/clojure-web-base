(function () {
  function CountriesCtrl(countriesService, resolveData) {
    var self = this;

    self.countries = resolveData.data;
  }

  angular
    .module('algo.halcyon.controllers.refdata')
    .controller("CountriesCtrl", ['CountriesService', 'resolveData', CountriesCtrl]);
})();