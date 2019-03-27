/* global moment */
angular.module('state', ['ngDialog', 'ngProgress', 'amChartsDirective']).controller('state', function (
        $scope, $interval, $rootScope, ngDialog, ngProgressFactory, UAUtil, StateService, NotificationService, Constants, LoginService, SleepService
        ) {

    $scope.progressbar = ngProgressFactory.createInstance();
    $scope.raceData = {};
    $scope.authenticated = LoginService.isAuthenticated();
    $scope.sleepDisabled = false;
    $scope.isMobile = UAUtil.isMobile();

    $scope.raceData.raceType = 'ROUND_BASED';

    NotificationService.on($scope, Constants.MESSAGES["raceStateChanged"], function (message) {
        console.log("got raceStateChanged message", message);
        $scope.loadStateData();
    });

    NotificationService.on($scope, Constants.MESSAGES["newLap"], function (message) {
        console.log("got newParticipant message", message);
        $scope.loadStateData();
    });

    $scope.sleepEnable = function () {
        SleepService.enableSleep();
        $scope.sleepDisabled = false;
    };
    $scope.sleepDisable = function () {
        SleepService.disableSleep();
        $scope.sleepDisabled = true;
    };

    $scope.convertDuration = function (duration) {
        return moment.duration(duration).asSeconds().toFixed(2) + " s";
    };

    $scope.convertJavaDurationToUnixSeconds = function (duration) {
        return moment.duration(duration).asMilliseconds();
    };

    $scope.getPilotState = function (raceData, chipId) {
        if (raceData.participantExtraData[chipId] !== undefined) {
            return raceData.participantExtraData[chipId].state;
        }
        return "";
    };

    $scope.getPilotDuration = function (raceData, chipId) {
        if (raceData.participantExtraData[chipId] !== undefined) {
            return raceData.participantExtraData[chipId].duration;
        }
        return 0;
    };

    $scope.nonEmptyToplist = function () {
        if ($scope.raceData.toplist !== undefined) {
            return Object.keys($scope.raceData.toplist).length > 0;
        }
        return false;
    };

    $scope.isInvalidLap = function (lapValidity, lap) {
        return lapValidity[lap] !== undefined && lapValidity[lap] === true;
    };

    $scope.updateRaceType = function (newType) {
        console.log("update race type to ", newType);
        $scope.raceData.raceType = newType;
        StateService.setRaceType($scope.raceData.raceType)
                .then(function () {
                    $scope.loadStateData();
                })
                .catch(function (response) {
                    if (response.data === null) {
                        response.data = {message: "unable to set race type"};
                    }
                    ngDialog.open({template: "raceFailure", scope: $scope, data: {message: response.data.message}});
                    $scope.progressbar.complete();
                });
    };

    $scope.convertTime = function (time) {
        return moment(time).format("HH:mm:ss, DD.MM.");
    };

    $scope.chartInitialized = false;
    $scope.loadStateData = function () {
        $scope.progressbar.start();
        StateService.loadData().then(function (ret) {
            $scope.raceData = ret;
            console.log("raceData", $scope.raceData);

            StateService.loadChartData().then(function (ret) {
                if (!$scope.chartInitialized) {
                    $scope.chartInitialized = true;
                    $scope.constructGraphs(ret.participants);
                    $scope.amChartOptions.graphs = $scope.graphs;
                    console.log("render chart");
                    $scope.amChartOptions.data = ret.lapTimes;
                    $rootScope.$broadcast("amCharts.renderChart", $scope.amChartOptions);
                } else {
                    console.log("update chart data");
                    $rootScope.$broadcast("amCharts.updateData", ret.lapTimes);
                }
            }).catch(function (response) {
                if (response.data === null) {
                    response.data = {message: "unable to load"};
                }
                ngDialog.open({template: "dataFailure", scope: $scope, data: {message: "cannot load race data: " + response.data.message}});
            });
        }).catch(function (response) {
            if (response.data === null) {
                response.data = {message: "unable to load"};
            }
            ngDialog.open({template: "dataFailure", scope: $scope, data: {message: "cannot load race data: " + response.data.message}});
        }).finally(function () {
            $scope.progressbar.complete();
        });
    };

    $scope.invalidateLap = function (chipid, lap) {
        console.log('invalidate lap ', lap, ' for ', chipid);
        StateService.invalidateLap(chipid, lap)
                .then(function () {
                    $scope.loadStateData();
                })
                .catch(function (response) {
                    if (response.data === null) {
                        response.data = {message: "unable to invalidate lap"};
                    }
                    ngDialog.open({template: "raceFailure", scope: $scope, data: {message: response.data.message}});
                    $scope.progressbar.complete();
                });
    };

    $scope.startRace = function () {
        StateService.startRace()
                .then(function () {
                    $scope.loadStateData();
                })
                .catch(function (response) {
                    if (response.data === null) {
                        response.data = {message: "unable to load"};
                    }
                    ngDialog.open({template: "raceFailure", scope: $scope, data: {message: response.data.message}});
                    $scope.progressbar.complete();
                });
    };

    $scope.stopRace = function () {
        $scope.progressbar.start();
        StateService.stopRace()
                .then(function () {
                    $scope.loadStateData();
                })
                .catch(function (response) {
                    if (response.data === null) {
                        response.data = {message: "unable to load"};
                    }
                    ngDialog.open({template: "raceFailure", scope: $scope, data: {message: response.data.message}});
                    $scope.progressbar.complete();
                });
    };

    var countdownInterval = $interval(function () {
        var results = document.getElementsByClassName("duration");
        if (results.length > 0) {
            for (var i = 0; i < results.length; i++) {
                let entry = results[i];
                if (entry.attributes['data-state'] !== undefined && entry.attributes['data-state'].value !== undefined) {
                    if (entry.attributes['data-state'].value === 'started' || entry.attributes['data-state'].value === 'last lap') {
                        if (entry.attributes['data-duration'] !== undefined && entry.attributes['data-duration'].value !== undefined) {
                            entry.attributes['data-duration'].value -= 100;
                            entry.innerText = $scope.convertDuration(parseInt(entry.attributes['data-duration'].value));
                        }
                    }
                }
            }
        }
    }, 100);

    var promise = $interval(function () {
        $scope.loadStateData();
    }, 60000);

    $scope.progressbar.start();
    $scope.loadStateData();

    $scope.$on('$destroy', function () {
        if (angular.isDefined(promise)) {
            $interval.cancel(promise);
            promise = undefined;
        }
        if (angular.isDefined(countdownInterval)) {
            $interval.cancel(countdownInterval);
            countdownInterval = undefined;
        }
        SleepService.enableSleep();
        $scope.sleepDisabled = false;
    });

    $scope.checkRunning = function () {
        if ($scope.raceData.state === "RUNNING" || $scope.raceData.state === "GETREADY" || $scope.raceData.state === "PREPARE") {
            return true;
        }
        return false;
    };

    $scope.constructGraphs = function (participants) {
        console.log("constructing graphs");
        $scope.graphs = [];
        participants.forEach(function (participant) {
            let graph = {};
            graph.title = participant.name;
            graph.valueField = participant.chipid.toString();
            graph.balloonText = participant.name + ': [[value]] s';
            graph.balloonFunction = function (item, graph) {
                var result = graph.balloonText;
                result = result.replace("[[value]]", (item.values.value / 1000).toFixed(3));
                return result;
            };
            $scope.graphs.push(graph);
        });
    };

    $scope.amChartOptions = {
        data: [],
        type: 'serial',
        categoryField: 'lap',
        pathToImages: '/images/amcharts/',
        startDuration: 0,
        legend: {
            enabled: true,
            valueText: ''
        },
        chartScrollbar: {
            enabled: true
        },
        categoryAxis: {
            gridPosition: 'start',
            parseDates: false,
            labelFunction: function (valueText, serialDataItem, categoryAxis) {
                return "lap " + valueText;
            }
        },
        valueAxes: [{
                id: 'time',
                title: 'time [s]',
                labelFunction: function (value, valueText, valueAxis) {
                    return (value / 1000).toFixed(2);
                }
            }]
    };

}
).factory('StateService', function ($http, StateTranslation, RaceTypeTranslation) {
    var factory = {};

    factory.startRace = function () {
        return $http.get("/api/auth/race/start").then(function (response) {
            return response.data;
        });
    };

    factory.stopRace = function () {
        return $http.get("/api/auth/race/stop").then(function (response) {
            return response.data;
        });
    };

    factory.setRaceType = function (raceType) {
        return $http.get("/api/auth/race/type", {params: {type: raceType}}).then(function (response) {
            return response.data;
        });
    };

    factory.loadData = function () {
        return $http.get("/api/race/data").then(function (response) {
            response.data.stateText = StateTranslation.getText(response.data.state);
            response.data.raceTypeText = RaceTypeTranslation.getText(response.data.raceType);
            return response.data;
        });
    };

    factory.loadChartData = function () {
        return $http.get("/api/race/chartdata").then(function (response) {
            console.log(response.data);
            return response.data;
        });
    };

    factory.invalidateLap = function (chipid, lap) {
        return $http.get("/api/auth/race/invalidatelap?chipid=" + chipid + "&lap=" + lap).then(function (response) {
            return response.data;
        });
    };

    return factory;
});
