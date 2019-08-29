import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { ToastController } from '@ionic/angular';
import { Storage } from '@ionic/storage';
import { LoadingController } from '@ionic/angular';
import { SmartAudioProvider } from '../services/smart-audio/smart-audio';
import { SpeechProvider } from '../services/speech/speech'
import { NgZone } from '@angular/core';
import { Insomnia } from '@ionic-native/insomnia/ngx';
import * as LapData from '../models/lapdata'
import * as MessageData from '../models/messagedata'
import { FltutilProvider } from '../services/fltutil/fltutil';
import { FltunitProvider } from '../services/fltunit/fltunit';

@Component({
    selector: 'page-race',
    templateUrl: 'race.html'
})
export class RacePage {

    private _raceState: RACESTATE = RACESTATE.STOP;
    private _raceStateText: string = "";
    private _lastLapTime: number = 0;
    private _lastLapRssi: number = 0;
    private _lapTimes: number[] = [];
    private _lapRssis: number[] = [];
    private _currentLap: number = 0;
    private _maxLaps: number = 10;
    private _fastestLap: number = 1;
    private _fastestLapTime: number = 0;
    private _averageLapTime: number = 0;
    private _totalTime: number = 0;

    constructor(
        public zone: NgZone,
        private storage: Storage,
        public toastCtrl: ToastController,
        public router: Router,
        private loadingCtrl: LoadingController,
        private smartAudio: SmartAudioProvider,
        private insomnia: Insomnia,
        private fltutil: FltutilProvider,
        private fltunit: FltunitProvider,
        private speech: SpeechProvider
    ) {
        this.restartRace();
        this.setRaceState(RACESTATE.STOP);
    }

    restartRace() {
        this.setRaceState(RACESTATE.WAITING);

        this.storage.get("race.numberOfLaps").then((numberOfLaps: number) => {
            if (numberOfLaps == undefined || numberOfLaps == null) {
                numberOfLaps = 10;
            }
            this.maxLaps = numberOfLaps;
        });

        this.lapTimes = [];
        this.currentLap = 0;
        this.fastestLap = 1;
        this.fastestLapTime = 0;
        this.averageLapTime = 0;
        this.totalTime = 0;
        this.lastLapTime = 0;
    }

    setRaceState(state: RACESTATE) {
        this.zone.run(() => {
            this.raceState = state;
            this.raceStateText = this.getStateText();
        });
    }

    getStateText(): string {
        switch (this.raceState) {
            case RACESTATE.WAITING:
                return "Waiting";
            case RACESTATE.STOP:
                return "Stopped";
            case RACESTATE.RUNNING:
                return "Running";
            default:
                return "Unknown";
        }
    }

    convertTime(time: number): string {
        let minutes: number = 0;
        let milliseconds: number = time % 1000;
        let seconds: number = time / 1000;
        if (seconds >= 60) {
            minutes = seconds / 60;
            seconds = seconds % 60;
        }
        let minutesString: string = minutes.toFixed(0);
        let secondsString: string = seconds.toFixed(0)
        let millisecondsString: string = milliseconds.toFixed(0);
        if (milliseconds <= 99) {
            millisecondsString = "0" + millisecondsString;
        }
        if (milliseconds <= 9) {
            millisecondsString = "0" + millisecondsString;
        }
        if (seconds <= 9) {
            secondsString = "0" + secondsString;
        }

        if (minutes > 0) {
            return minutesString + "m" + secondsString + "." + millisecondsString + "s";
        }
        return secondsString + "." + millisecondsString + "s";

    }

    isRaceRunning(): boolean {
        return this.raceState == RACESTATE.RUNNING;
    }

    isRaceWaiting(): boolean {
        return this.raceState == RACESTATE.WAITING;
    }

    ionViewDidEnter() {
        this.storage.get("race.keepAwakeDuringRace").then((keepAwakeDuringRace: boolean) => {
            if (keepAwakeDuringRace == undefined || keepAwakeDuringRace == null) {
                keepAwakeDuringRace = false;
            }
            if (keepAwakeDuringRace) {
                this.insomnia.keepAwake().then(() => { }, () => {
                    this.fltutil.showToast('Cannot disable sleep mode');
                });
            }
        });

        let me = this;
        this.fltunit.connectIfNotConnected().then(() => {
            this.fltunit.getObservable().subscribe(data => {
                me.fltutil.hideLoader();
                if (LapData.isLapData(data)) {
                    if (this.isRaceWaiting()) {
                        this.zone.run(() => {
                            this.currentLap++;
                        });
                        this.setRaceState(RACESTATE.RUNNING);
                        this.speech.speak('Race started.');
                    } else if (this.isRaceRunning()) {
                        if (this.currentLap >= this.maxLaps) {
                            this.speech.speak('Race finished.');
                            this.fltutil.showToast('Race ended, max. number of laps reached.');
                            this.setRaceState(RACESTATE.STOP);
                        }
                        this.zone.run(() => {
                            this.lastLapTime = data.lapTime;
                            this.lastLapRssi = data.rssi;
                            this.lapTimes.push(this.lastLapTime);
                            this.lapRssis.push(this.lastLapRssi);

                            let fastestLap = 0;
                            let fastestLapTime = 99999999;
                            let totalTime = 0;
                            this.lapTimes.forEach(function (lap, index) {
                                if (lap < fastestLapTime) {
                                    fastestLapTime = lap;
                                    fastestLap = index + 1;
                                }
                                totalTime += lap;
                            });
                            this.totalTime = totalTime;
                            this.averageLapTime = Number(totalTime) / this.lapTimes.length;
                            this.fastestLap = fastestLap;
                            this.fastestLapTime = fastestLapTime;
                        });
                        if (this.isRaceRunning()) {
                            this.smartAudio.play('lap');
                            this.zone.run(() => {
                                this.currentLap++;
                            });
                        }
                    }
                } else if (MessageData.isMessageData(data)) {
                    me.fltutil.showToast(data.message);
                    this.speech.speak(data.message);
                }
            })
        }).catch(function (errMsg: string) {
            me.fltutil.showToast(errMsg);
            me.router.navigate(['/tabs/settings'])
        });
    }

    ionViewWillLeave() {
        this.storage.get("race.keepAwakeDuringRace").then((keepAwakeDuringRace: boolean) => {
            if (keepAwakeDuringRace == undefined || keepAwakeDuringRace == null) {
                keepAwakeDuringRace = false;
            }
            if (keepAwakeDuringRace) {
                this.insomnia.allowSleepAgain();
            }
        });
    }

    public get raceState(): RACESTATE {
        return this._raceState;
    }
    public set raceState(value: RACESTATE) {
        this._raceState = value;
    }
    public get raceStateText(): string {
        return this._raceStateText;
    }
    public set raceStateText(value: string) {
        this._raceStateText = value;
    }
    public get lastLapTime(): number {
        return this._lastLapTime;
    }
    public set lastLapTime(value: number) {
        this._lastLapTime = value;
    }
    public get lastLapRssi(): number {
        return this._lastLapRssi;
    }
    public set lastLapRssi(value: number) {
        this._lastLapRssi = value;
    }
    public get lapTimes(): number[] {
        return this._lapTimes;
    }
    public set lapTimes(value: number[]) {
        this._lapTimes = value;
    }
    public get lapRssis(): number[] {
        return this._lapRssis;
    }
    public set lapRssis(value: number[]) {
        this._lapRssis = value;
    }
    public get currentLap(): number {
        return this._currentLap;
    }
    public set currentLap(value: number) {
        this._currentLap = value;
    }
    public get maxLaps(): number {
        return this._maxLaps;
    }
    public set maxLaps(value: number) {
        this._maxLaps = value;
    }
    public get fastestLap(): number {
        return this._fastestLap;
    }
    public set fastestLap(value: number) {
        this._fastestLap = value;
    }
    public get fastestLapTime(): number {
        return this._fastestLapTime;
    }
    public set fastestLapTime(value: number) {
        this._fastestLapTime = value;
    }
    public get averageLapTime(): number {
        return this._averageLapTime;
    }
    public set averageLapTime(value: number) {
        this._averageLapTime = value;
    }
    public get totalTime(): number {
        return this._totalTime;
    }
    public set totalTime(value: number) {
        this._totalTime = value;
    }
}

enum RACESTATE {
    WAITING = 0,
    STOP,
    RUNNING
}
