/* global fetch */
var host = location.host;
var restUrl = "http://" + host + "/";
var wsUrl = "ws://" + host + ":81/";

console.log("host", host);
console.log("restUrl", restUrl);
console.log("wsUrl", wsUrl);

var audio = null;
var initAudio = function () {
    audio = new Audio(restUrl + "audio/lap.wav");
};

var playLap = function () {
    if (audio !== null) {
        console.log("play lap audio");
        audio.play();
    } else {
        console.log("lap audio is null");
    }
};

var playTriple = function () {
    if (audio !== null) {
        console.log("play triple audio");
        audio.play();
        window.setTimeout(function () {
            audio.play();
            window.setTimeout(function () {
                audio.play();
            }, 250);
        }, 250);
    } else {
        console.log("triple audio is null");
    }
};

var updateContent = function (element, content) {
    document.getElementById(element).innerHTML = content;
};

var updateField = function (element, content) {
    document.getElementById(element).value = content;
};

var readField = function (element) {
    return document.getElementById(element).value;
};

var convertLapTime = function (time) {
    let seconds = time / 1000;
    seconds = seconds.toFixed(2);
    return seconds + " s";
};

var getData = function () {
    console.log("get data");
    let p = new Promise(function (resolve, reject) {
        fetch(restUrl + "data").then(function (response) {
            return response.json().then(function (json) {
                if (response.status === 200) {
                    resolve(json);
                } else {
                    reject();
                }
            });
        }).catch(function () {
            reject();
        });
    });
    return p;
};

var getWifiData = function () {
    console.log("get wifi data");
    let p = new Promise(function (resolve, reject) {
        fetch(restUrl + "wifi").then(function (response) {
            return response.json().then(function (json) {
                if (response.status === 200) {
                    resolve(json);
                } else {
                    reject();
                }
            });
        }).catch(function () {
            reject();
        });
    });
    return p;
};

var getRssiData = function () {
    console.log("get rssi data");
    let p = new Promise(function (resolve, reject) {
        fetch(restUrl + "rssi").then(function (response) {
            return response.json().then(function (json) {
                if (response.status === 200) {
                    resolve(json);
                } else {
                    reject();
                }
            });
        }).catch(function () {
            reject();
        });
    });
    return p;
};

var setData = function (field, value, noAlertOnSuccess) {
    setDataCall(field, value).then(function (data) {
        if (noAlertOnSuccess === undefined || noAlertOnSuccess === false) {
            alert("saved");
        }
    }).catch(function () {
        alert("failed to save data");
    }).finally(function () {
        setOverlay(false);
    });
};

var setDataCall = function (field, value) {
    console.log("set data (" + field + ", " + value + ")");
    setOverlay(true);
    let p = new Promise(function (resolve, reject) {
        fetch(restUrl + "data?" + field + "=" + value, {method: "POST", body: {}}).then(function (response) {
            return response.json().then(function (json) {
                if (response.status === 200) {
                    resolve(json);
                } else {
                    reject();
                }
            });
        }).catch(function () {
            reject();
        });
    });
    return p;
};

var setOverlay = function (state) {
    if (state) {
        document.getElementById("overlay").style.display = 'block';
    } else {
        document.getElementById("overlay").style.display = 'none';
    }
};

var RaceState = {
    GET_READY: 1,
    RACE: 2,
    END: 3
};

class Race {
    constructor() {
        this.reset();
    }

    reset() {
        this.laps = [];
        this.currentLap = 0;
        this.firstLap = true;
        this.state = RaceState.END;
        this.maxLaps = 10;
        this.averageLapTime = 0;
        this.fastestLapTime = 0;
        this.fastestLap = 0;
    }

    setMaxLaps(laps) {
        this.maxLaps = laps;
    }

    getMaxLaps() {
        return this.maxLaps;
    }

    restart() {
        this.reset();
        this.start();
    }

    start() {
        this.state = RaceState.GET_READY;
    }

    stop() {
        this.state = RaceState.END;
    }

    addLap(duration) {
        console.log("got lap with laptime " + duration);
        if (this.state === RaceState.GET_READY || this.state === RaceState.RACE) {
            if (this.firstLap) {
                // is first lap, ignore
                console.log("first lap, ignore");
                this.state = RaceState.RACE;
                this.currentLap = 1;
                this.firstLap = false;
                playTriple();
            } else {
                console.log("add lap");
                this.laps.push({
                    lap: this.currentLap,
                    time: duration
                });
                this.calcSpecialLaps();
                this.currentLap++;
                if (this.currentLap > this.maxLaps) {
                    this.state = RaceState.END;
                    this.currentLap--;
                    playTriple();
                } else {
                    playLap();
                }
            }
        } else {
            console.log("try to add lap while not in race or getready state");
        }
    }

    calcSpecialLaps() {

        if (this.laps.length < 1) {
            return;
        }

        var fastestLapTime = 4294967296;
        var fastestLap = 0;

        var averageTime = 0;
        this.laps.forEach(function (value) {
            averageTime += value.time;
            if (value.time < fastestLapTime) {
                fastestLapTime = value.time;
                fastestLap = value.lap;
            }
        });

        averageTime /= this.laps.length;

        this.averageLapTime = averageTime;
        this.fastestLapTime = fastestLapTime;
        this.fastestLap = fastestLap;

    }

    getLapData() {
        return {
            currentLap: this.currentLap,
            laps: this.laps,
            state: this.state,
            fastestLap: this.fastestLap,
            fastestLapTime: this.fastestLapTime,
            averageLapTime: this.averageLapTime
        };
    }

    getState() {
        return this.state;
    }

    getStateText() {
        let ret;
        switch (this.state) {
            case RaceState.GET_READY:
                ret = "get ready";
                break;
            case RaceState.RACE:
                ret = "race";
                break;
            case RaceState.END:
                ret = "end";
                break;
            default:
                ret = "unknown";
        }
        return ret;
    }

}

/**
 * Promise.prototype.finally
 *
 * Pulled from https://github.com/domenic/promises-unwrapping/issues/18#issuecomment-57801572
 * @author @stefanpenner, @matthew-andrews
 */

(function () {
    // Get a handle on the global object
    var local;
    if (typeof global !== 'undefined')
        local = global;
    else if (typeof window !== 'undefined' && window.document)
        local = window;
    else
        local = self;

    // It's replaced unconditionally to preserve the expected behavior
    // in programs even if there's ever a native finally.
    local.Promise.prototype['finally'] = function finallyPolyfill(callback) {
        var constructor = this.constructor;

        return this.then(function (value) {
            return constructor.resolve(callback()).then(function () {
                return value;
            });
        }, function (reason) {
            return constructor.resolve(callback()).then(function () {
                throw reason;
            });
        });
    };
}());