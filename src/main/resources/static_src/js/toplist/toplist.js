/* global moment */
angular.module('toplist', ['ngDialog', 'ngProgress']).controller('toplist', function (
        $scope, ngDialog, ngProgressFactory, ToplistService, Util
        ) {

    $scope.topListData = [];
    $scope.show = "today";

    $scope.progressbar = ngProgressFactory.createInstance();

    $scope.showToplist = function (range) {
        $scope.show = range;
        $scope.progressbar.start();
        Util.displayOverlay(true);

        ToplistService.loadToplist(range)
                .then(function (data) {
                    $scope.topListData = data;
                })
                .catch(function () {
                    ngDialog.open({template: 'dataFailure', scope: $scope, data: {message: "cannot load toplist data"}});
                })
                .finally(function () {
                    $scope.progressbar.complete();
                    Util.displayOverlay(false);
                });
    };

    $scope.isActive = function (show) {
        return show === $scope.show;
    };

    $scope.showToplist("today");

}).factory('ToplistService', function ($http, Util) {
    var factory = {};

    factory.loadToplist = function (range) {
        return $http.get("/api/races/toplist/" + range).then(function (response) {
            let data = response.data;
            data.forEach(function (value) {
                value.totalDurationText = Util.convertDuration(value.totalDuration);
            });
            return data;
        });
    };

    return factory;
});
