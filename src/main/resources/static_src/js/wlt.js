/* global moment, Stomp, speechSynthesis */
angular.module('wlt', ['ngRoute', 'home', 'state', 'settings', 'participants', 'navigation', 'setup', 'races', 'toplist', 'login', 'devicedata'])
        .config(function ($routeProvider, $httpProvider, $locationProvider) {

            $locationProvider.html5Mode(true);

            $routeProvider.when('/', {
                templateUrl: 'js/home/home.html',
                controller: 'home'
            }).when('/state', {
                templateUrl: 'js/state/state.html',
                controller: 'state'
            }).when('/participants', {
                templateUrl: 'js/participants/participants.html',
                controller: 'participants'
            }).when('/setup', {
                templateUrl: 'js/setup/setup.html',
                controller: 'setup'
            }).when('/devicedata', {
                templateUrl: 'js/devicedata/devicedata.html',
                controller: 'devicedata'
            }).when('/settings', {
                templateUrl: 'js/settings/settings.html',
                controller: 'settings'
            }).when('/races', {
                templateUrl: 'js/races/races.html',
                controller: 'races'
            }).when('/toplist', {
                templateUrl: 'js/toplist/toplist.html',
                controller: 'toplist'
            }).when('/login', {
                templateUrl: 'js/login/login.html',
                controller: 'login'
            }).otherwise('/');

            $httpProvider.defaults.headers.common['X-Requested-With'] = 'XMLHttpRequest';

            $httpProvider.interceptors.push('authenticationInterceptor');

        })
        .run(function (WebSocketService, LoginService) {
            // websocketservice needs to stay as argument to get initialized automatically
            LoginService.initAuth();
        })
        .directive("fltConfirmClick", [
            function ( ) {
                return {
                    priority: -1,
                    restrict: 'A',
                    scope: {confirmFunction: "&fltConfirmClick"},
                    link: function (scope, element, attrs) {
                        element.bind('click', function (e) {
                            // message defaults to "Are you sure?"
                            var message = attrs.fltConfirmClickMessage ? attrs.fltConfirmClickMessage : "Are you sure?";
                            // confirm() requires jQuery
                            if (confirm(message)) {
                                scope.confirmFunction();
                            }
                        });
                    }
                };
            }
        ])
        .constant('Constants', {
            MESSAGES: {
                newLap: 'NEW_LAP',
                newParticipant: 'NEW_PARTICIPANT',
                raceStateChanged: 'RACE_STATE_CHANGED'
            }
        })
        .factory('UAUtil', function ($window) {
            let factory = {};
            let ua = $window.navigator.userAgent;
            factory.isMobile = function () {
                return ua.includes('iPad') || ua.includes('iPhone') || ua.includes('iPod') || ua.includes('Android') || ua.includes('Windows Phone');
            };
            return factory;
        })
        .factory('LoginService', function ($http, $location) {
            let factory = {};
            let authenticated = false;
            let path = '/';

            factory.getLastPath = function () {
                console.log("last path", path);
                return path;
            };

            factory.logout = function () {
                authenticated = false;
                $http.post('/logout', {})
                        .then(
                                function () {
                                    $location.url('/');
                                },
                                function () {
                                    $location.url('/');
                                }
                        );
            };

            factory.initAuth = function () {
                console.log("init authentication");
                factory.login(undefined, undefined, undefined, undefined, undefined, true);
            };

            factory.login = function (username, password, callbackSuccess, callbackError, callbackFinally, noRoute) {
                let headers = {};
                if (username !== undefined && password !== undefined) {
                    headers = {authorization: "Basic " + btoa(username + ":" + password)};
                }
                let noRouteToLogin = noRoute || false;
                $http.get('/user', {headers: headers, noRouteToLogin: noRouteToLogin})
                        .then(function (response) {
                            if (!response.data || !response.data.name) {
                                authenticated = false;
                                callbackError && callbackError(response);
                            } else {
                                authenticated = true;
                                callbackSuccess && callbackSuccess(response);
                            }
                        })
                        .catch(function (response) {
                            authenticated = false;
                            callbackError && callbackError(response);
                        })
                        .finally(function () {
                            callbackFinally && callbackFinally();
                        });
            };

            factory.isAuthenticated = function () {
                return authenticated;
            };
            return factory;
        })
        .factory('authenticationInterceptor', ['$q', '$location', function ($q, $location) {
                return {
                    response: function (response) {
                        return response || $q.when(response);
                    },
                    responseError: function (rejection) {
                        if (rejection.status === 401 && rejection.config.noRouteToLogin !== true) {
                            console.log("401 -> login");
                            $location.url('/login?lastPath=' + encodeURIComponent($location.path()));
                            return false;
                        } else if (rejection.config.noRouteToLogin !== true) {
                            return $q.reject(rejection);
                        } else {
                            return false;
                        }
                    }
                };
            }])
        .factory('SleepService', function ($rootScope) {
            let factory = {};
            let noSleep = new NoSleep();
            factory.disableSleep = function () {
                console.log("enable nosleep");
                noSleep.enable();
            };
            factory.enableSleep = function () {
                console.log("disable nosleep");
                noSleep.disable();
            };
            return factory;
        })
        .factory('StateTranslation', function () {
            let stateMap = new Map();
            stateMap.set("RUNNING", "running");
            stateMap.set("GETREADY", "get ready");
            stateMap.set("FINISHED", "finished");
            stateMap.set("FAULT", "fault");
            stateMap.set("WAITING", "waiting");
            return {
                getFinished: function () {
                    return "FINISHED";
                },
                getText: function (state) {
                    if (stateMap.get(state)) {
                        return stateMap.get(state);
                    }
                    return "unknown";
                }
            };
        })
        .factory('Util', function () {
            let factory = {};
            factory.convertDuration = function (duration) {
                let seconds = moment.duration(duration).asSeconds();
                let minutes = 0;
                let millis = moment.duration(duration).milliseconds();
                if (seconds > 60) {
                    seconds = moment.duration(duration).seconds();
                    minutes = (moment.duration(duration).asSeconds() / 60).toFixed(0);
                    return minutes + " m " + seconds + " s " + millis + " ms";
                } else {
                    return seconds.toFixed(0) + " s " + millis.toFixed(0) + " ms";
                }
            };
            factory.convertTime = function (time) {
                return moment(time).format("HH:mm:ss, DD.MM.");
            };
            factory.displayOverlay = function (state) {
                var element = angular.element(document.querySelector('#overlay'));
                if (state) {
                    element.css('display', 'block');
                } else {
                    element.css('display', 'none');
                }
            };
            return factory;
        })
        .factory('Alerts', function ($timeout, $rootScope) {
            let alerts = [];
            let factory = {};
            factory.addGeneric = function (type, text, permanent) {
                console.log("add generic alert", type, text, permanent);
                let allowed = ['success', 'info', 'warning', 'danger'];
                if (!allowed.includes(type)) {
                    console.log("invalid alert type: " + type);
                    return;
                }
                let timeout = (permanent) ? null : moment().unix() + 2;
                alerts.push({
                    type: type,
                    msg: text,
                    timeout: timeout
                });
                $rootScope.$broadcast("alerts-were-updated");
                if (angular.isUndefined(permanent) || permanent === false) {
                    console.log("timed alert");
                    $timeout(function () {
                        console.log("close alert handler, alerts: ", alerts.length);
                        for (let i = 0; i < alerts.length; i++) {
                            console.log("process alert", i);
                            let value = alerts[i];
                            if (value.timeout !== null && value.timeout <= moment().unix()) {
                                console.log("close alert", i);
                                factory.closeAlert(i);
                                console.log("alerts", alerts.length);
                            }
                        }
                    }, 2250);
                } else {
                    console.log("permanent alert");
                }
            };
            factory.clear = function () {
                alerts = [];
            };
            factory.addSuccess = function (text) {
                if (angular.isUndefined(text)) {
                    text = "value successfully saved";
                }
                factory.addGeneric("success", text);
            };
            factory.addInfo = function (text, permanent) {
                if (angular.isUndefined(permanent)) {
                    permanent = false;
                }
                factory.addGeneric("info", text, permanent);
            };
            factory.addError = function (text) {
                console.log("add error alert", text);
                factory.addGeneric("danger", text, true);
            };
            factory.getAlerts = function () {
                return alerts;
            };
            factory.closeAlert = function (index) {
                console.log("close alert", index);
                alerts.splice(index, 1);
            };
            return factory;
        })
        .factory('NotificationService', function ($rootScope) {
            let factory = {};
            factory.send = function (type, message) {
                $rootScope.$broadcast(type, {message: message});
            };
            factory.on = function ($scope, type, handler) {
                $scope.$on(type, function (event, args) {
                    handler(args.message);
                });
            };
            return factory;
        })
        .factory('AudioService', function ($timeout) {
            let audioMap = [];
            let factory = {};
            factory.play = function (file, repeat) {
                if (!(file in audioMap)) {
                    audioMap[file] = new Howl({src: [file]});
                }
                audioMap[file].play();
                if (repeat > 1) {
                    for (let i = 1; i < repeat; i++) {
                        $timeout(function () {
                            audioMap[file].play();
                        }, i * 250);
                    }
                }
            };
            return factory;
        })
        .factory('SpeechService', function () {
            let factory = {};
            factory.speak = function (text, language) {
                if (typeof speechSynthesis === 'undefined') {
                    return;
                }
                let utterance = new SpeechSynthesisUtterance(text);
                utterance.lang = language;
                speechSynthesis.speak(utterance);
            };
            return factory;
        })
        .factory('WebSocketService', function ($timeout, NotificationService, Constants, AudioService, SpeechService) {
            let factory = {};
            let subscriberLap = null;
            let subscriberParticipant = null;
            let subscriberRaceStateChanged = null;
            let subscriberAudio = null;
            let subscriberSpeech = null;
            let stomp = null;
            let client = null;
            let connected = false;
//
//            factory.sendLapData = function (message) {
//                factory.sendMessage("/lap", message);
//            };
//
//            factory.sendMessage = function (topic, message) {
//                stomp.send("/app" + topic, {priority: 9}, JSON.stringify(message));
//            };

            let reconnect = function () {
                connected = false;
                console.log("try to reconnect in 10 seconds");
                factory.unsubscribeListeners();
                $timeout(function () {
                    initialize();
                }, 10000);
            };

            factory.unsubscribeListeners = function () {
                console.log("unsubscribe from lap");
                if (subscriberLap !== null) {
                    subscriberLap.unsubscribe();
                }
                console.log("unsubscribe from audio");
                if (subscriberAudio !== null) {
                    subscriberAudio.unsubscribe();
                }
                console.log("unsubscribe from speech");
                if (subscriberSpeech !== null) {
                    subscriberSpeech.unsubscribe();
                }
                console.log("unsubscribe from participant");
                if (subscriberParticipant !== null) {
                    subscriberParticipant.unsubscribe();
                }
                console.log("unsubscribe from race state changed");
                if (subscriberRaceStateChanged !== null) {
                    subscriberRaceStateChanged.unsubscribe();
                }
            };

            let subscribeListeners = function () {
                if (connected) {
                    console.log("subscribe to topic lap");
                    subscriberLap = stomp.subscribe("/topic/lap", function (data) {
                        console.log("got new lap websocket message", data);
                        NotificationService.send(Constants.MESSAGES["newLap"], data);
                    });

                    console.log("subscribe to topic audio");
                    subscriberAudio = stomp.subscribe("/topic/audio", function (data) {
                        console.log("got audio websocket message", data);
                        let soundData = JSON.parse(data.body);
                        AudioService.play(soundData.file, soundData.repeat);
                    });

                    console.log("subscribe to topic speech");
                    subscriberSpeech = stomp.subscribe("/topic/speech", function (data) {
                        console.log("got speech websocket message", data);
                        let textData = JSON.parse(data.body);
                        SpeechService.speak(textData.text, textData.language);
                    });

                    console.log("subscribe to participant");
                    subscriberParticipant = stomp.subscribe("/topic/participant", function (data) {
                        console.log("got new participant websocket message", data);
                        NotificationService.send(Constants.MESSAGES["newParticipant"], data);
                    });

                    console.log("subscribe to race state changed");
                    subscriberRaceStateChanged = stomp.subscribe("/topic/race/state", function (data) {
                        console.log("got new race state changed websocket message", data);
                        NotificationService.send(Constants.MESSAGES["raceStateChanged"], data);
                    });

                    console.log("subscribe to alert");
                    subscriberRaceStateChanged = stomp.subscribe("/topic/race/state", function (data) {
                        console.log("got new race state changed websocket message", data);
                        NotificationService.send(Constants.MESSAGES["raceStateChanged"], data);
                    });
                } else {
                    console.log("not connected, scheduling");
                    $timeout(function () {
                        subscribeLapListener();
                    }, 100);
                }
            };

            let initialize = function () {
                client = new SockJS("/websocketEndpoint");
                stomp = Stomp.over(client);
                stomp.connect({}, function () {
                    console.log("websocket service connected");
                    connected = true;
                    subscribeListeners();

                    console.log("subscribe to status topic");
                    stomp.subscribe("/topic/status", function (data) {
                        console.log("got new status message", data);
                        var status = JSON.parse(data.body);
                        if (status.udp !== undefined) {
                            var statusUdpDiv = angular.element(document.querySelector("#statusUdp"));
                            statusUdpDiv.html(status.udp);
                        }
                    });
                }, function () {
                    console.log("failed to connect to websocket service");
                    connected = false;
                });
                stomp.onclose = reconnect;
            };

            initialize();
            return factory;
        });
