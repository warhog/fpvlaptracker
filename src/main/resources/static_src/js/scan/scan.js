/* global moment */
angular.module('scan', ['ngDialog', 'ngProgress', 'ui.bootstrap', 'amChartsDirective']).controller('scan', function (
        $scope, ngDialog, $interval, $rootScope, $timeout, $location, ngProgressFactory, Alerts, Util, NotificationService, Constants, ScanService
        ) {

    $scope.chipid = parseInt($location.search().chipid);
    $scope.state = '-';
    $scope.chartInitialized = false;
    $scope.chartData = [];
    $scope.maxFreq = '-';
    $scope.maxRssi = 0;


    $scope.frequencies = [
        5865, 5845, 5825, 5805, 5785, 5765, 5745, 5725, // Band A
        5733, 5752, 5771, 5790, 5809, 5828, 5847, 5866, // Band B
        5705, 5685, 5665, 5645, 5885, 5905, 5925, 5945, // Band E
        5740, 5760, 5780, 5800, 5820, 5840, 5860, 5880, // Band F / Airwave
        5658, 5695, 5732, 5769, 5806, 5843, 5880, 5917, // Band C / Immersion Raceband
        5362, 5399, 5436, 5473, 5510, 5547, 5584, 5621  // Band D / 5.3    
    ];
    $scope.bands = [
        "A1 5865 MHz", "A2 5845 MHz", "A3 5825 MHz", "A4 5805 MHz", "A5 5785 MHz", "A6 5765 MHz", "A7 5745 MHz", "A8 5725 MHz",
        "B1 5733 MHz", "B2 5752 MHz", "B3 5771 MHz", "B4 5790 MHz", "B5 5809 MHz", "B6 5828 MHz", "B7 5847 MHz", "B8 5866 MHz",
        "E1 5705 MHz", "E2 5685 MHz", "E3 5665 MHz", "E4 5645 MHz", "E5 5885 MHz", "E6 5905 MHz", "E7 5925 MHz", "E8 5945 MHz",
        "F1 5740 MHz", "F2 5760 MHz", "F3 5780 MHz", "F4 5800 MHz", "F5 5820 MHz", "F6 MHz 5840", "F7 MHz 5860", "F8 5880 MHz",
        "R1 5658 MHz", "R2 5695 MHz", "R3 5732 MHz", "R4 5769 MHz", "R5 5806 MHz", "R6 5843 MHz", "R7 5880 MHz", "R8 5917 MHz",
        "L1 5362 MHz", "L2 5399 MHz", "L3 5436 MHz", "L4 5473 MHz", "L5 5510 MHz", "L6 5547 MHz", "L7 5584 MHz", "L8 5621 MHz"
    ];

    for (let i = 5362; i <= 5945; i++) {
        let text = i + " MHz";
        if ($scope.frequencies.indexOf(i) !== -1) {
            text = $scope.bands[$scope.frequencies.indexOf(i)];
        }
        $scope.chartData.push({
            frequency: i,
            frequencyText: text,
            rssi: 0
        });
    }

    NotificationService.on($scope, Constants.MESSAGES['scan'], function (message) {
        let scandata = JSON.parse(message.body);
        if (scandata.chipid === $scope.chipid) {
//            console.log(scandata);
            $scope.chartData.forEach(function (entry) {
                if (entry.frequency === scandata.frequency) {
                    entry.rssi = scandata.rssi;
                    if (scandata.rssi > $scope.maxRssi) {
                        $scope.maxRssi = scandata.rssi;
                        $scope.maxFreq = scandata.frequency + ' MHz';
                        if ($scope.frequencies.indexOf(scandata.frequency) !== -1) {
                            $scope.maxFreq = $scope.bands[$scope.frequencies.indexOf(scandata.frequency)];
                        }
                    }
                }
            });
            $rootScope.$broadcast("amCharts.updateData", $scope.chartData);
        }
    });

    $scope.promise = null;
    $scope.loading = false;
    $scope.progressbar = ngProgressFactory.createInstance();
    console.log("chipid", $scope.chipid);

    $scope.scan = function () {
        ScanService.setState($scope.chipid, 'SCAN');
        $scope.minFreq = '-';
        $scope.minRssi = 0;
        $timeout(function () {
            $scope.loadDeviceData(false);
        }, 500);
    };

    $scope.deepscan = function () {
        ScanService.setState($scope.chipid, 'DEEPSCAN');
        $scope.minFreq = '-';
        $scope.minRssi = 0;
        $timeout(function () {
            $scope.loadDeviceData(false);
        }, 500);
    };

    $scope.restoreOldState = function () {
        ScanService.setState($scope.chipid, 'RESTORE_STATE');
        $timeout(function () {
            $scope.loadDeviceData(false);
        }, 500);
    };

    $scope.loadDeviceData = function (overlay) {
        if (overlay === undefined || overlay === null) {
            overlay = true;
        }
        if (!$scope.loading) {
            $scope.progressbar.start();
            if (overlay) {
                Util.displayOverlay(true);
            }
            $scope.loading = true;
            ScanService.loadDeviceData($scope.chipid)
                    .then(function (response) {
                        $scope.state = response.state;
                        if (!$scope.chartInitialized) {
                            $scope.chartInitialized = true;
                            console.log("render chart");
                            $scope.amChartOptions.data = [];
                            $rootScope.$broadcast("amCharts.renderChart", $scope.amChartOptions);
                            $rootScope.$broadcast("amCharts.updateData", $scope.chartData);
                        }
                    })
                    .catch(function (response) {
                        ngDialog.open({template: 'dataFailure', scope: $scope, data: {message: "failed to load device data"}});
                        $scope.state = 'unknown';
                    })
                    .finally(function () {
                        $scope.loading = false;
                        $scope.progressbar.complete();
                        Util.displayOverlay(false);
                    });
        }
    };

    $scope.$on('$destroy', function () {
        if (angular.isDefined($scope.promise)) {
            $interval.cancel($scope.promise);
            $scope.promise = undefined;
        }
    });

    $scope.amChartOptions = {
        data: [],
        type: 'serial',
        categoryField: 'frequencyText',
        pathToImages: '/images/amcharts/',
        startDuration: 0,
        sortColumns: true,
        legend: {
            enabled: false
        },
        chartScrollbar: {
            enabled: true
        },
        categoryAxis: {
            gridPosition: 'start',
            parseDates: false,
            labelRotation: 45
        },
        graphs: [{
                type: 'column',
                valueField: 'rssi',
                fillAlphas: 0.9
            }],
        valueAxes: [{
                id: 'rssi',
                title: 'RSSI',
                minimum: 0,
                maximum: 1000
            }]
    };

    $scope.loadDeviceData();

}).factory('ScanService', function ($http) {
    var factory = {};

    factory.loadDeviceData = function (chipid) {
        return $http.get("/api/participant/deviceData", {params: {chipid: chipid}, timeout: 10000}).then(function (response) {
            return response.data;
        });
    };

    factory.setState = function (chipid, state) {
        return $http.get("/api/auth/participant/setstate", {params: {chipid: chipid, state: state}, timeout: 2000}).then(function (response) {
            return response.data;
        });
    };

    return factory;
});
