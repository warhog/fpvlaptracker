/* global moment */
angular.module('login', ['ngProgress', 'ui.bootstrap']).controller('login', function (
        $scope, LoginService, Alerts, Util, $location
        ) {

    if ($location.search().logout) {
        console.log("logout found");
        LoginService.logout();
        $location.url('/');
    }

    $scope.username = '';
    $scope.password = '';
    $scope.alerts = Alerts;

    // should be replaced using directive
    angular.element('#name').focus();

    $scope.login = function () {
        Util.displayOverlay(true);
        LoginService.login($scope.username, $scope.password,
                function (response) {
                    console.log("login successful");
                    Alerts.clear();
                    $location.url(decodeURIComponent($location.search().lastPath));
                },
                function (response) {
                    console.log("cannot login", response);
                    Alerts.addError("login unsuccessful");
                },
                function () {
                    Util.displayOverlay(false);
                });
    };

});
