/* global ngDialog */
angular.module('settings', ['ngDialog', 'ngProgress']).controller('settings', function (
        $scope, ngDialog, ngProgressFactory, SettingsService, Alerts, Util, LoginService
        ) {

    LoginService.requireAuthenticated();

    $scope.progressbar = ngProgressFactory.createInstance();
    $scope.maxLaps = 0;
    $scope.alerts = Alerts;
    $scope.timezone = "-";

    $scope.shutdown = function () {
        Util.displayOverlay(true);
        SettingsService.shutdown();
    };

    $scope.progressbar.start();
    Util.displayOverlay(true);
    SettingsService.loadData()
            .then(function (data) {
                $scope.maxLaps = data.numberOfLaps;
                $scope.timezone = data.timezone;
            })
            .catch(function (response) {
                if (response.data === null) {
                    response.data = {message: "unable to load"};
                }
                ngDialog.open({template: 'dataFailure', scope: $scope, data: {message: response.data.message}});
            })
            .finally(function () {
                $scope.progressbar.complete();
                Util.displayOverlay(false);
            });

    $scope.setMaxLaps = function () {
        $scope.progressbar.start();
        Util.displayOverlay(true);
        SettingsService.setMaxLaps($scope.maxLaps)
                .then(function (data) {
                    Alerts.addSuccess();
                })
                .catch(function (response) {
                    if (response.data === null) {
                        response.data = {message: "unable to load"};
                    }
                    ngDialog.open({template: 'failedSave', scope: $scope, data: {message: response.data.message}});
                })
                .finally(function () {
                    $scope.progressbar.complete();
                    Util.displayOverlay(false);
                });

    };

    $scope.setTimezone = function () {
        $scope.progressbar.start();
        Util.displayOverlay(true);
        SettingsService.setTimezone($scope.timezone)
                .then(function (data) {
                    Alerts.addSuccess();
                })
                .catch(function (response) {
                    if (response.data === null) {
                        response.data = {message: "unable to load"};
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
        return $http.get("/api/miscdata").then(function (response) {
            response.data.numberOfLaps = parseInt(response.data.numberOfLaps);
            return response.data;
        });
    };

    factory.setMaxLaps = function (maxLaps) {
        return $http.post("/api/auth/race/maxlaps?laps=" + maxLaps).then(function (response) {
            return response.data;
        });
    };

    factory.setTimezone = function (timezone) {
        return $http.post("/api/auth/misc/timezone?timezone=" + timezone).then(function (response) {
            return response.data;
        });
    };

    return factory;
});
