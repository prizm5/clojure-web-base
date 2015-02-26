(function () {
  angular
    .module('algo.services.refdata', ['halcyon.config']);

  angular
    .module('algo.halcyon.resolvers', ['algo.services.refdata']);

  angular
    .module('algo.halcyon.controllers.refdata', ['algo.services.refdata']);
})();