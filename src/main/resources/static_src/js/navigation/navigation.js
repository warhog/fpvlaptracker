angular.module('navigation', ['ngRoute']).controller('navigation', function (
        $scope, $route, StateTranslation, BadgeService, WebSocketService, NotificationService, Constants, $interval, LoginService, Alerts
        ) {

    $scope.isActive = function (route) {
        return $route.current && route === $route.current.controller;
    };

    $scope.participants = 0;
    $scope.alerts = [];
    
    $scope.$on("alerts-were-updated", function() {
        console.log("received: alerts were updated",);
        $scope.alerts = Alerts.getAlerts();
    });
    
    $scope.closeAlert = function(index) {
        Alerts.closeAlert(index);
    };
    
    $scope.isAuthenticated = function() {
        return LoginService.isAuthenticated();
    };

    $scope.loadBadgeData = function () {
        BadgeService.loadBadgeData()
                .then(function (data) {
                    $scope.participants = parseInt(data.participants);
                    $scope.state = StateTranslation.getText(data.state);
                })
                .catch(function () {
                    console.log("cannot load badge data");
                });
    };
    $scope.loadBadgeData();

    WebSocketService.subscribeParticipantListener();
    WebSocketService.subscribeRaceStateChangedListener();
    $scope.$on('$destroy', function () {
        WebSocketService.unsubscribeParticipantListener();
        WebSocketService.unsubscribeLapListener();
        WebSocketService.unsubscribeAudioListener();
        WebSocketService.unsubscribeSpeechListener();
        WebSocketService.unsubscribeRaceStateChangedListener();
    });

    NotificationService.on($scope, Constants.MESSAGES["newParticipant"], function (message) {
        console.log("got newParticipant message", message);
        $scope.loadBadgeData();
    });

    NotificationService.on($scope, Constants.MESSAGES["raceStateChanged"], function (message) {
        console.log("got raceStateChanged message", message);
        $scope.loadBadgeData();
    });

    NotificationService.on($scope, Constants.MESSAGES["newLap"], function (message) {
        console.log("got newLap message", message);
        $scope.loadBadgeData();
    });

    WebSocketService.subscribeAudioListener();
    WebSocketService.subscribeSpeechListener();

    $interval(function () {
        $scope.loadBadgeData();
    }, 60000);

}).factory('BadgeService', function ($http) {
    let factory = {};
    factory.loadBadgeData = function () {
        return $http.get("/api/badgedata").then(function (response) {
            return response.data;
        });
    };
    return factory;
});
