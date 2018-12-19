/* global moment */
angular.module('devicedata', ['ngDialog', 'ngProgress', 'ui.bootstrap']).controller('devicedata', function (
        $scope, ngDialog, $location, ngProgressFactory, DeviceDataService, Alerts
        ) {

    $scope.chipid = parseInt($location.search().chipid);
    $scope.alerts = Alerts;

    $scope.isLoading = false;
    $scope.cells = 1;

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
                        console.log("data", $scope.data);
                        if ($scope.data.voltage > 7 && $scope.data.voltage <= 10) {
                            $scope.cells = 2;
                        } else if ($scope.data.voltage > 10 && $scope.data.voltage <= 13) {
                            $scope.cells = 3;
                        } else if ($scope.data.voltage > 13 && $scope.data.voltage <= 17) {
                            $scope.cells = 4;
                        } else if ($scope.data.voltage > 17 && $scope.data.voltage <= 21.5) {
                            $scope.cells = 5;
                        } else if ($scope.data.voltage > 21.5 && $scope.data.voltage <= 26) {
                            $scope.cells = 6;
                        }
                        console.log("cells", $scope.cells);
                    })
                    .catch(function (response) {
                        if (!response.data || !response.data.message) {
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
