/* global moment */
angular.module('races', ['ngDialog', 'ngProgress']).controller('races', function ($scope, ngDialog, RacesService, ngProgressFactory, Util) {

    $scope.racesData = [];
    $scope.raceData = [];
    $scope.toplist = [];
    $scope.selectedRace = null;
    $scope.loaded = false;
    $scope.progressbar = ngProgressFactory.createInstance();

    $scope.progressbar.start();
    Util.displayOverlay(true);
    RacesService.loadRaces()
            .then(function (racesData) {
                $scope.racesData = racesData;
                $scope.selectedRace = $scope.racesData[$scope.racesData.length - 1];
                $scope.loadRace();
            })
            .catch(function (response) {
                if (response.data === null) {
                    response.data = {message: "unable to load"};
                }
                ngDialog.open({template: 'dataFailure', scope: $scope, data: {message: "cannot load race data: " + response.data.message}});
            })
            .finally(function () {
                $scope.progressbar.complete();
                Util.displayOverlay(false);
            });

    $scope.loadRace = function () {
        $scope.progressbar.start();
        Util.displayOverlay(true);
        RacesService.loadRace($scope.selectedRace.id)
                .then(function (data) {
                    $scope.toplist = data.toplist;
                    $scope.state = data.data;
                    $scope.loaded = true;
                })
                .catch(function (response) {
                    if (response.data === null) {
                        response.data = {message: "unable to load"};
                    }
                    ngDialog.open({template: 'dataFailure', scope: $scope, data: {message: "cannot load race data for id " + $scope.selectedRace.id + ": " + response.data.message}});
                })
                .finally(function () {
                    $scope.progressbar.complete();
                    Util.displayOverlay(false);
                });
    };

}).factory('RacesService', function ($http, Util) {
    var factory = {};

    factory.loadRaces = function () {
        return $http.get("/api/races/all").then(function (response) {
            let data = response.data;
            let raceData = [];
            Object.keys(data).forEach(function (key) {
                let starttime = "no time available";
                if (data[key] !== "-") {
                    starttime = moment(data[key]).format("DD.MM.YYYY HH:mm:ss");
                }
                raceData.push({
                    id: key,
                    starttime: starttime
                });
            });
            return raceData;
        });
    };

    factory.loadRace = function (raceId) {
        return $http.get("/api/races/load", {params: {id: raceId}}).then(function (response) {
            let toplist = [];
            let data = response.data;
            Object.keys(data.raceData).forEach(function (value) {
                toplist.push({
                    name: value,
                    duration: Util.convertDuration(data.raceData[value].totalDuration)
                });
            });

            data.startTime = Util.convertTime(data.startTime);
            Object.keys(data.raceData).forEach(function (participantName) {
                data.raceData[participantName].fastestLapDuration = Util.convertDuration(data.raceData[participantName].fastestLapDuration);
                data.raceData[participantName].averageLapDuration = Util.convertDuration(data.raceData[participantName].averageLapDuration);
                data.raceData[participantName].totalDuration = Util.convertDuration(data.raceData[participantName].totalDuration);
                Object.keys(data.raceData[participantName].laps).forEach(function (lap) {
                    data.raceData[participantName].laps[lap] = Util.convertDuration(data.raceData[participantName].laps[lap]);
                });
            });

            return {
                toplist: toplist.sort(function (a, b) {
                    return a.duration - b.duration;
                }).reverse(),
                data: data
            };

        });
    };

    return factory;
});
