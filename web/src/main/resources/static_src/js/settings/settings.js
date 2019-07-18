/* global ngDialog */
angular.module('settings', ['ngDialog', 'ngProgress']).controller('settings', function (
        $scope, ngDialog, ngProgressFactory, SettingsService, Alerts, Util
        ) {

    $scope.progressbar = ngProgressFactory.createInstance();
    $scope.data = {};
    $scope.data.numberOfLaps = 0;
    $scope.data.startInterval = 0;
    $scope.data.raceDuration = 0;
    $scope.data.overtimeDuration = 0;
    $scope.data.preparationDuration = 0;
    $scope.data.timezone = "-";

    $scope.shutdown = function () {
        Util.displayOverlay(true);
        SettingsService.shutdown();
    };

    $scope.progressbar.start();
    Util.displayOverlay(true);
    SettingsService.loadData()
            .then(function (data) {
                $scope.data = data;
            })
            .catch(function (response) {
                console.log(response);
                if (!response.data || !response.data.message) {
                    response.data = {message: "unable to load"};
                }
                ngDialog.open({template: 'dataFailure', scope: $scope, data: {message: response.data.message}});
            })
            .finally(function () {
                $scope.progressbar.complete();
                Util.displayOverlay(false);
            });

    $scope.storeSettings = function () {
        $scope.progressbar.start();
        Util.displayOverlay(true);
        SettingsService.storeSettings($scope.data)
                .then(function (data) {
                    Alerts.addSuccess();
                })
                .catch(function (response) {
                    if (response.data === null) {
                        response.data = {message: "unable to store settings"};
                    }
                    ngDialog.open({template: 'failedSave', scope: $scope, data: {message: response.data.message}});
                })
                .finally(function () {
                    $scope.progressbar.complete();
                    Util.displayOverlay(false);
                });

    };

}).factory('SettingsService', function ($http) {
    var factory = {};

    factory.shutdown = function () {
        $http.get("/api/auth/shutdown");
    };

    factory.loadData = function () {
        return $http.get("/api/auth/misc/loadsettings").then(function (response) {
            return response.data;
        });
    };

    factory.storeSettings = function (data) {
        console.log("storeSettings", data);
        return $http.post("/api/auth/misc/storesettings", data, {timeout: 2000}, null).then(function (response) {
            return response.data;
        });
    };

    return factory;
});
