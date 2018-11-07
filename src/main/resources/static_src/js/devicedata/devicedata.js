/* global moment */
angular.module('devicedata', ['ngDialog', 'ngProgress', 'ui.bootstrap']).controller('devicedata', function (
        $scope, ngDialog, $location, ngProgressFactory, DeviceDataService, Alerts, LoginService
        ) {

    $scope.chipid = parseInt($location.search().chipid);
    $scope.alerts = Alerts;

    $scope.isLoading = false;

    $scope.progressbar = ngProgressFactory.createInstance();
    console.log("chipid", $scope.chipid);

    $scope.data = {};

    $scope.loadDeviceData = function () {
        if (!$scope.isLoading) {
            $scope.isLoading = true;
            $scope.progressbar.start();
            DeviceDataService.loadDeviceData($scope.chipid)
                    .then(function (data) {
                        $scope.data = data;
                        console.log($scope.data);
                    })
                    .catch(function (response) {
                        if (response.data === null) {
                            response.data = {message: "unable to load"};
                        }
                        ngDialog.open({template: "dataFailure", scope: $scope, data: {message: "cannot load device data: " + response.data.message}});
                    })
                    .finally(function () {
                        $scope.progressbar.complete();
                        $scope.isLoading = false;
                    });
        }
    };

    $scope.loadDeviceData();

}).factory('DeviceDataService', function ($http) {
    var factory = {};

    factory.loadDeviceData = function (chipid) {
        return $http.get("/api/participant/deviceData", {params: {chipid: chipid}, timeout: 10000}).then(function (response) {
            return response.data;
        });
    };
    return factory;
});
