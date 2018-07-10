/* global moment */
angular.module('setup', ['ngDialog', 'ngProgress', 'ui.bootstrap']).controller('setup', function (
        $scope, ngDialog, $interval, $location, ngProgressFactory, SetupService, Alerts, Util, LoginService
        ) {

    LoginService.requireAuthenticated();

    $scope.chipid = parseInt($location.search().chipid);
    $scope.ipAddress = "0.0.0.0";

    $scope.frequencies = [
        5865, 5845, 5825, 5805, 5785, 5765, 5745, 5725, // Band A
        5733, 5752, 5771, 5790, 5809, 5828, 5847, 5866, // Band B
        5705, 5685, 5665, 5645, 5885, 5905, 5925, 5945, // Band E
        5740, 5760, 5780, 5800, 5820, 5840, 5860, 5880, // Band F / Airwave
        5658, 5695, 5732, 5769, 5806, 5843, 5880, 5917, // Band C / Immersion Raceband
        5362, 5399, 5436, 5473, 5510, 5547, 5584, 5621  // Band D / 5.3    
    ];
    $scope.bands = [
        "A1 5865 MHz (Boscam, TBS)", "A2 5845 MHz (Boscam, TBS)", "A3 5825 MHz (Boscam, TBS)", "A4 5805 MHz (Boscam, TBS)", "A5 5785 MHz (Boscam, TBS)", "A6 5765 MHz (Boscam, TBS)", "A7 5745 MHz (Boscam, TBS)", "A8 5725 MHz (Boscam, TBS)",
        "B1 5733 MHz (Boscam)", "B2 5752 MHz (Boscam)", "B3 5771 MHz (Boscam)", "B4 5790 MHz (Boscam)", "B5 5809 MHz (Boscam)", "B6 5828 MHz (Boscam)", "B7 5847 MHz (Boscam)", "B8 5866 MHz (Boscam)",
        "E1 5705 MHz (Boscam)", "E2 5685 MHz (Boscam)", "E3 5665 MHz (Boscam)", "E4 5645 MHz (Boscam)", "E5 5885 MHz (Boscam)", "E6 5905 MHz (Boscam)", "E7 5925 MHz (Boscam)", "E8 5945 MHz (Boscam)",
        "F1 5740 MHz (Airwave, Fatshark, ImmersionRC)", "F2 5760 MHz (Airwave, Fatshark, ImmersionRC)", "F3 5780 MHz (Airwave, Fatshark, ImmersionRC)", "F4 5800 MHz (Airwave, Fatshark, ImmersionRC)", "F5 5820 MHz (Airwave, Fatshark, ImmersionRC)", "F6 MHz 5840 (Airwave, Fatshark, ImmersionRC)", "F7 MHz 5860 (Airwave, Fatshark, ImmersionRC)", "F8 5880 MHz (Airwave, Fatshark, ImmersionRC)",
        "R1 5658 MHz (Raceband)", "R2 5695 MHz (Raceband)", "R3 5732 MHz (Raceband)", "R4 5769 MHz (Raceband)", "R5 5806 MHz (Raceband)", "R6 5843 MHz (Raceband)", "R7 5880 MHz (Raceband)", "R8 5917 MHz (Raceband)",
        "D1 5362 MHz (Boscam)", "D2 5399 MHz (Boscam)", "D3 5436 MHz (Boscam)", "D4 5473 MHz (Boscam)", "D5 5510 MHz (Boscam)", "D6 5547 MHz (Boscam)", "D7 5584 MHz (Boscam)", "D8 5621 MHz (Boscam)"
    ];

    $scope.selectedFrequency = 0;
    $scope.frequencyTable = [];
    $scope.promise = null;
    $scope.rssi = "-";
    $scope.thresholdHigh = 0;
    $scope.thresholdLow = 0;
    $scope.minLapTime = 0;
    $scope.loadingRssi = false;
    $scope.participantName = "-";
    $scope.alerts = Alerts;
    $scope.isAllowConfiguration = false;

    let getInitialValue = function (frequency) {
        frequency = parseInt(frequency);
        let ret = null;
        $scope.frequencyTable.forEach(function (value) {
            if (value.frequency === frequency) {
                ret = value;
            }
        });
        if (ret !== null) {
            return ret;
        }
        console.log("using default");
        return $scope.frequencyTable[0];
    };

    // build table of frequencies with names
    $scope.frequencies.forEach(function (value) {
        let index = $scope.frequencies.indexOf(value);
        let selectedBand = $scope.bands[index];
        $scope.frequencyTable.push({
            frequency: value,
            name: selectedBand
        });
    });

    $scope.progressbar = ngProgressFactory.createInstance();
    console.log("chipid", $scope.chipid);

    $scope.loadRssi = function (overlay) {
        if (overlay === undefined || overlay === null) {
            overlay = true;
        }
        if (!$scope.loadingRssi) {
            $scope.progressbar.start();
            if (overlay) {
                Util.displayOverlay(true);
            }
            $scope.loadingRssi = true;
            SetupService.loadRssi($scope.chipid)
                    .then(function (response) {
                        $scope.rssi = response.rssi;
                    })
                    .catch(function (response) {
                        ngDialog.open({template: 'dataFailure', scope: $scope, data: {message: "failed to load rssi"}});
                        $scope.stopMeasuring();
                        $scope.rssi = "-";
                    })
                    .finally(function () {
                        $scope.loadingRssi = false;
                        $scope.progressbar.complete();
                        Util.displayOverlay(false);
                    });
        }
    };

    $scope.startMeasuring = function () {
        $scope.promise = $interval(function () {
            $scope.loadRssi(false);
        }, 1000);
    };

    $scope.stopMeasuring = function () {
        if ($scope.promise !== null) {
            $interval.cancel($scope.promise);
            $scope.promise = null;
        }
    };

    $scope.$on('$destroy', function () {
        if (angular.isDefined($scope.promise)) {
            $interval.cancel($scope.promise);
            $scope.promise = undefined;
        }
    });

    $scope.measureRssi = function () {
        if (!$scope.loadingRssi) {
            $scope.progressbar.start();
            Util.displayOverlay(true);
            $scope.loadingRssi = true;
            SetupService.measureRssi($scope.chipid)
                    .then(function (response) {
                        response.channels.forEach(function (channel) {
                            channel.band = $scope.bands[$scope.frequencies.indexOf(channel.freq)];
                        });
                        ngDialog.open({template: "rssiMeasureResult", scope: $scope, data: {response: response}, className: 'ngdialog-theme-plain custom-width'});
                    })
                    .catch(function (response) {
                        if (response.data === null) {
                            response.data = {message: "unable to load"};
                        }
                        ngDialog.open({template: "dataFailure", scope: $scope, data: {message: "cannot measure rssi: " + response.data.message}});
                    })
                    .finally(function () {
                        $scope.loadingRssi = false;
                        $scope.progressbar.complete();
                        Util.displayOverlay(false);
                    });
        }
    };

    $scope.loadSetupData = function () {
        if (!$scope.loadingRssi) {
            $scope.progressbar.start();

            $scope.loadingRssi = true;
            SetupService.loadSetupData($scope.chipid)
                    .then(function (data) {
                        if (data.isAllowConfiguration) {
                            $scope.thresholdHigh = parseInt(data.thresholdHigh);
                            $scope.thresholdLow = parseInt(data.thresholdLow);
                            $scope.minLapTime = parseInt(data.minLapTime);
                            $scope.rssi = data.rssi;
                            $scope.selectedFrequency = getInitialValue(data.frequency);
                        }
                        if (data.isAllowConfiguration || data.isAllowConfigureName) {
                            $scope.participantName = data.name;
                        }
                        $scope.ipAddress = data.ipAddress;
                        console.log($scope.selectedFrequency);
                    })
                    .catch(function (response) {
                        $scope.thresholdHigh = "-";
                        $scope.thresholdLow = "-";
                        $scope.minLapTime = "-";
                        $scope.rssi = "-";
                        $scope.selectedFrequency = 0;
                        $scope.participantName = "";
                        if (response.data === null) {
                            response.data = {message: "unable to load"};
                        }
                        ngDialog.open({template: "dataFailure", scope: $scope, data: {message: "cannot load setup data: " + response.data.message}});
                    })
                    .finally(function () {
                        $scope.loadingRssi = false;
                        $scope.progressbar.complete();
                    });
        }
    };

    $scope.setMinLapTime = function () {
        $scope.progressbar.start();
        Util.displayOverlay(true);
        SetupService.setMinLapTime($scope.chipid, $scope.minLapTime)
                .then(function (response) {
                    Alerts.addSuccess();
                })
                .catch(function (response) {
                    ngDialog.open({template: 'failedSave', scope: $scope});
                })
                .finally(function () {
                    $scope.progressbar.complete();
                    Util.displayOverlay(false);
                });
    };

    $scope.setThreshold = function () {
        if ($scope.thresholdLow >= $scope.thresholdHigh || $scope.thresholdHigh <= 0 || $scope.thresholdLow <= 0 || $scope.thresholdHigh >= 1024 || $scope.thresholdLow >= 1024) {
            ngDialog.open({template: 'dataFailure', scope: $scope, data: {message: "rssi threshold error.<br />low limit has to be below high limit<br />limits have to be bigger than zero and smaller than 1024"}});
            return;
        }
        $scope.progressbar.start();
        Util.displayOverlay(true);
        SetupService.setThreshold($scope.chipid, $scope.thresholdLow, $scope.thresholdHigh)
                .then(function (response) {
                    Alerts.addSuccess();
                })
                .catch(function (response) {
                    ngDialog.open({template: 'failedSave', scope: $scope});
                })
                .finally(function () {
                    $scope.progressbar.complete();
                    Util.displayOverlay(false);
                });
    };

    $scope.setName = function () {
        $scope.progressbar.start();
        Util.displayOverlay(true);
        SetupService.setName($scope.chipid, $scope.participantName)
                .then(function (response) {
                    $scope.loadSetupData();
                    Alerts.addSuccess();
                })
                .catch(function (response) {
                    ngDialog.open({template: 'failedSave', scope: $scope});
                })
                .finally(function () {
                    $scope.progressbar.complete();
                    Util.displayOverlay(false);
                });
    };

    $scope.setFrequency = function () {
        $scope.progressbar.start();
        Util.displayOverlay(true);
        SetupService.setFrequency($scope.chipid, $scope.selectedFrequency.frequency)
                .then(function (response) {
                    Alerts.addSuccess();
                })
                .catch(function (response) {
                    ngDialog.open({template: 'failedSave', scope: $scope});
                })
                .finally(function () {
                    $scope.progressbar.complete();
                    Util.displayOverlay(false);
                });
    };

    if (1) {
        $scope.onlyLocalData = true;
    }
    $scope.loadSetupData();

}).factory('SetupService', function ($http) {
    var factory = {};

    factory.loadRssi = function (chipid) {
        return $http.get("/api/participant/rssi", {params: {chipid: chipid}}).then(function (response) {
            return response.data;
        });
    };

    factory.loadSetupData = function (chipid, onlylocal) {
        return $http.get("/api/participant/setupData", {params: {chipid: chipid, onlylocal: onlylocal}, timeout: 5000}).then(function (response) {
            return response.data;
        });
    };

    factory.measureRssi = function (chipid) {
        return $http.get("/api/participant/measure", {params: {chipid: chipid}, timeout: 10000}).then(function (response) {
            return response.data;
        });
    };

    factory.setMinLapTime = function (chipid, minLapTime) {
        return $http.post("/api/auth/participant/minlaptime", {chipid: chipid, minlaptime: minLapTime}, {timeout: 5000}, null).then(function (response) {
            return response.data;
        });
    };

    factory.setThreshold = function (chipid, thresholdLow, thresholdHigh) {
        return $http.post("/api/auth/participant/threshold", {chipid: chipid, thresholdLow: thresholdLow, thresholdHigh: thresholdHigh}, {timeout: 5000}, null).then(function (response) {
            return response.data;
        });
    };

    factory.setName = function (chipid, name) {
        return $http.post("/api/auth/participant/name", {chipid: chipid, name: name}, {timeout: 5000}, null).then(function (response) {
            return response.data;
        });
    };

    factory.setFrequency = function (chipid, frequency) {
        return $http.post("/api/auth/participant/frequency", {chipid: chipid, frequency: frequency}, {timeout: 5000}).then(function (response) {
            return response.data;
        });
    };

    return factory;
});
