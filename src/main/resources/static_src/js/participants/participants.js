angular.module('participants', ['ngDialog']).controller('participants', function (
        $scope, ngDialog, $location, ParticipantsService, WebSocketService, NotificationService, Constants, LoginService
        ) {
    
    $scope.participants = {};
    $scope.authenticated = LoginService.isAuthenticated();

    NotificationService.on($scope, Constants.MESSAGES["newParticipant"], function(message) {
        console.log("got newParticipant message", message);
        $scope.loadParticipantData();
    });
    
    WebSocketService.subscribeParticipantListener();
    $scope.$on('$destroy', function () {
        WebSocketService.unsubscribeParticipantListener();
    });

    $scope.loadParticipantData = function () {
        ParticipantsService.loadParticipantData().then(function (data) {
            $scope.participants = data;
        }).catch(function () {
            ngDialog.open({template: "dataFailure", scope: $scope, data: "cannot load participants data"});
        });
    };

    $scope.loadParticipantData();

    $scope.clickAdd = function (chipid) {
        console.log("add " + chipid);
        ParticipantsService.addParticipantToRace(chipid)
                .then(function () {
                    $scope.loadParticipantData();
                })
                .catch(function (response) {
                    if (response.data === null) {
                        response.data = {message: "unable to load"};
                    }
                    ngDialog.open({template: "dataFailure", scope: $scope, data: {message: response.data.message}});
                });
    };

    $scope.clickRemove = function (chipid) {
        console.log("remove " + chipid);
        ParticipantsService.removeParticipantFromRace(chipid)
                .then(function () {
                    $scope.loadParticipantData();
                })
                .catch(function (response) {
                    if (response.data === null) {
                        response.data = {message: "unable to load"};
                    }
                    ngDialog.open({template: "dataFailure", scope: $scope, data: {message: response.data.message}});
                });
    };

    $scope.openSetup = function (chipid) {
        $location.url("/setup?chipid=" + chipid);
    };

}).factory('ParticipantsService', function ($http) {
    var factory = {};

    factory.loadParticipants = function () {
        return $http.get('/api/participants').then(function (response) {
            return response.data;
        });
    };

    factory.loadRaceParticipants = function () {
        return $http.get('/api/race/participants').then(function (response) {
            return response.data;
        });
    };

    factory.loadParticipantData = function () {
        return factory.loadParticipants().then(function (data) {
            return factory.loadRaceParticipants().then(function (data2) {
                data.forEach(function (value) {
                    data2.forEach(function (value2) {
                        if (value.chipId === value2.chipId) {
                            value.alreadyAdded = true;
                        }
                    });
                });
                return data;
            });
        });
    };

    factory.addParticipantToRace = function (chipid) {
        return $http.get("/api/auth/race/participants/add?chipid=" + chipid).then(function (response) {
            return response.data;
        });
    };

    factory.removeParticipantFromRace = function (chipid) {
        return $http.get("/api/auth/race/participants/remove?chipid=" + chipid).then(function (response) {
            return response.data;
        });
    };

    return factory;
});
