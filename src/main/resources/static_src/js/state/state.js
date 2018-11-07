/* global moment */
angular.module('state', ['ngDialog', 'ngProgress', 'amChartsDirective']).controller('state', function (
        $scope, $interval, $rootScope, ngDialog, ngProgressFactory, UAUtil, StateService, WebSocketService, NotificationService, Constants, LoginService, SleepService
        ) {

    $scope.progressbar = ngProgressFactory.createInstance();
    $scope.toplist = [];
    $scope.state = {};
    $scope.lastState = null;
    $scope.authenticated = LoginService.isAuthenticated();
    $scope.sleepDisabled = false;
    $scope.isMobile = UAUtil.isMobile();
    
    WebSocketService.subscribeLapListener();
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

    $scope.convertTime = function (time) {
        return moment(time).format("HH:mm:ss, DD.MM.");
    };

    $scope.chartInitialized = false;
    $scope.loadStateData = function () {
        $scope.progressbar.start();
        StateService.loadData().then(function (ret) {
            $scope.state = ret.state;
            $scope.toplist = ret.topList;
            $scope.lastState = ret.state.state;
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

    $scope.startRace = function () {
        StateService.startRace($scope.state.maxLaps)
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
        WebSocketService.unsubscribeLapListener();
        SleepService.enableSleep();
        $scope.sleepDisabled = false;
    });

    $scope.checkRunning = function () {
        if ($scope.state.state === "RUNNING" || $scope.state.state === "GETREADY") {
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
).factory('StateService', function ($http, StateTranslation) {
    var factory = {};

    factory.startRace = function (laps) {
        return $http.get("/api/auth/race/start", {params: {laps: laps}}).then(function (response) {
            return response.data;
        });
    };

    factory.stopRace = function (laps) {
        return $http.get("/api/auth/race/stop").then(function (response) {
            return response.data;
        });
    };


    factory.loadData = function () {
        return $http.get("/api/race/state").then(function (response) {
            let toplist = [];
            Object.keys(response.data.raceData).forEach(function (value) {
                toplist.push({
                    name: value,
                    duration: moment.duration(response.data.raceData[value].totalDuration).asMilliseconds()
                });
            });

            toplist = toplist.sort(function (a, b) {
                return a.duration - b.duration;
            }).reverse();

            let state = response.data;
            state.stateText = StateTranslation.getText(response.data.state);

            return {
                topList: toplist,
                state: state
            };
        });
    };

    factory.loadChartData = function () {
        return $http.get("/api/race/chartdata").then(function (response) {
            console.log(response.data);
            return response.data;
        });
    };

    factory.setMaxLaps = function (maxLaps) {
        return $http.post("/api/auth/race/maxlaps?laps=" + maxLaps).then(function (response) {
            return response.data;
        });
    };

    return factory;
});
