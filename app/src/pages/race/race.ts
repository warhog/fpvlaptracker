import {Component} from '@angular/core';
import {NavController, ViewController} from 'ionic-angular';
import {ToastController} from 'ionic-angular';
import {BluetoothSerial} from '@ionic-native/bluetooth-serial';
import {Storage} from '@ionic/storage';
import {BluetoothPage} from '../bluetooth/bluetooth';
import {LoadingController} from 'ionic-angular';
import {SmartAudioProvider} from '../../providers/smart-audio/smart-audio';
import {SpeechProvider} from '../../providers/speech/speech'
import {NgZone} from '@angular/core';
import {Insomnia} from '@ionic-native/insomnia';
import * as LapData from '../../models/lapdata'
import * as MessageData from '../../models/messagedata'
import {FltutilProvider} from '../../providers/fltutil/fltutil';
import {FltunitProvider} from '../../providers/fltunit/fltunit';

@Component({
    selector: 'page-race',
    templateUrl: 'race.html'
})
export class RacePage {

    private raceState: RACESTATE = RACESTATE.STOP;
    private raceStateText: string = "";
    private loader: any = null;
    private lastLapTime: number = 0;
    private lastLapRssi: number = 0;
    private lapTimes: number[] = [];
    private lapRssis: number[] = [];
    private currentLap: number = 0;
    private maxLaps: number = 10;
    private fastestLap: number = 1;
    private fastestLapTime: number = 0;
    private averageLapTime: number = 0;
    private totalTime: number = 0;


    constructor(
        public zone: NgZone,
        private storage: Storage, 
        public toastCtrl: ToastController, 
        public navCtrl: NavController, 
        private loadingCtrl: LoadingController, 
        private bluetoothSerial: BluetoothSerial, 
        private smartAudio: SmartAudioProvider, 
        private insomnia: Insomnia,
        private fltutil: FltutilProvider,
        private fltunit: FltunitProvider,
        private viewCtrl: ViewController,
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

    gotoSettings() {
        this.navCtrl.push(BluetoothPage);
    }

    showToast(errMsg: string) {
        let toast = this.toastCtrl.create({
            message: errMsg,
            duration: 5000
        });
        toast.present();
    }

    showLoader(text: string) {
        this.loader = this.loadingCtrl.create({
            content: text
        });
        this.loader.present();
    }

    hideLoader() {
        this.loader.dismiss();
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
                this.insomnia.keepAwake().then(() => {}, () => {
                    this.showToast('Cannot disable sleep mode');
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
                            this.showToast('Race ended, max. number of laps reached.');
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
            me.gotoSettings();
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

    ionViewWillEnter() {
        this.viewCtrl.showBackButton(false);
    }
}

enum RACESTATE {
    WAITING = 0,
    STOP,
    RUNNING
}
