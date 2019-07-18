import {Injectable} from '@angular/core';
import {BluetoothSerial} from '@ionic-native/bluetooth-serial';
import {FltutilProvider} from '../fltutil/fltutil'
import {Observable} from 'rxjs/Observable';
import {Storage} from '@ionic/storage';
import {DeviceData} from '../../models/devicedata';
import {StateData, isStateScanData, isStateRssiData, isStateCalibrationData} from '../../models/statedata'
import {RssiData} from '../../models/rssidata'
import {MessageData} from '../../models/messagedata'
import {ScanData} from '../../models/scandata';
import {LapData} from '../../models/lapdata';
import {SpeechProvider} from '../speech/speech'
import { getDataType } from '../../models/type';
import { VersionData } from '../../models/versiondata';
import { AlarmData } from '../../models/alarmdata';
import { resolveDefinition } from '@angular/core/src/view/util';

enum FLT_UNIT_STATES {
    DISCONNECTED = 0,
    CONNECTED,
    VALID_TEST,
    VALIDATED,
    CHECK_SAVE_SUCCESS
}

/*
  Generated class for the FltunitProvider provider.

  See https://angular.io/docs/ts/latest/guide/dependency-injection.html
  for more info on providers and Angular DI.
*/
@Injectable()
export class FltunitProvider {

    private version: string = "";
    private state: number = FLT_UNIT_STATES.DISCONNECTED;
    private deviceName: string = "";
    private timeout: any = null;

    private observable: any;
    private observer: any;

    constructor(
        private bluetoothSerial: BluetoothSerial,
        private fltutil: FltutilProvider,
        private storage: Storage,
        private speech: SpeechProvider
        ) {
        this.observable = Observable.create(observer => {
            this.observer = observer;
        });
    }

    getDeviceName(): string {
        return this.deviceName;
    }

    getObservable(): Observable<any> {
        return this.observable;
    }

    isWaitingForValidTest(): boolean {
        return this.state == FLT_UNIT_STATES.VALID_TEST;
    }

    isValidated(): boolean {
        return this.state == FLT_UNIT_STATES.VALIDATED;
    }

    isWaitingForSave(): boolean {
        return this.state == FLT_UNIT_STATES.CHECK_SAVE_SUCCESS;
    }

    getState(): FLT_UNIT_STATES {
        return this.state;
    }
    setState(state: FLT_UNIT_STATES) {
        this.state = state;
    }

    saveData(deviceData: DeviceData) {
        this.setState(FLT_UNIT_STATES.CHECK_SAVE_SUCCESS);
        let me = this;
        deviceData.minimumLapTime = deviceData.minimumLapTime * 1000;
        this.bluetoothSerial.write("PUT config " + JSON.stringify(deviceData) + "\n").then(function() {
            deviceData.minimumLapTime = deviceData.minimumLapTime / 1000;
        }).catch(function (msg) {
            me.observer.next({type: "message", message: "Cannot save: " + msg});
            me.state = FLT_UNIT_STATES.VALIDATED;
        });
    }

    isConnected() : Promise<any> {
        return this.bluetoothSerial.isConnected();
    }

    connectIfNotConnected() : Promise<any> {
        let me = this;
        return new Promise((resolve, reject) => {
            this.isConnected().then(() => {
                resolve();
            }).catch(() => {
                me.connect().then((data) => {
                    resolve();
                }).catch((errMsg: string) => {
                    reject(errMsg);
                })
            });
        });
    }

    connect() : Promise<string> {
        let me = this;
        this.isConnected().then(() => {
            this.disconnect();
        });
        return new Promise((resolve, reject) => {
            let bluetoothIdToConnectTo = null;
            me.storage.get("bluetooth.id").then((bluetoothId: string) => {
                if (bluetoothId === null) {
                    reject("No tracker selected.");
                }
                bluetoothIdToConnectTo = bluetoothId;
                return me.storage.get("bluetooth.name");
            }).then((name: string) => {
                me.deviceName = name;
                me.fltutil.showLoader("Connecting to " + name + ", please wait...");
                me.bluetoothSerial.connect(bluetoothIdToConnectTo).subscribe((data) => {
                    me.fltutil.hideLoader();
                    me.state = FLT_UNIT_STATES.CONNECTED;
                    me.bluetoothSerial.subscribe("\n").subscribe((data) => {
                        me.onReceive(data);
                    }, (errMsg) => {
                        me.disconnect();
                        reject(errMsg);
                    });
                    me.checkValidDevice();
                    resolve();
                }, (errMsg) => {
                    me.fltutil.hideLoader();
                    me.disconnect();
                    reject(errMsg);
                });
            }).catch(() => {
                me.disconnect();
                reject("Cannot load bluetooth data");
            });
        });
    }

    disconnect() {
        this.state = FLT_UNIT_STATES.DISCONNECTED;
        this.bluetoothSerial.disconnect();
    }

    timeoutHandler() {
        this.fltutil.hideLoader();
        this.fltutil.showToast("Timeout, please retry");
        this.disconnect();
    }

    simpleRequest(requestString: string, timeout: number = 0) : Promise<string> {
        let me = this;
        return new Promise((resolve, reject) => {
            this.isConnected().then(() => {
                return this.bluetoothSerial.write(requestString + '\n');
            }).then(() => {
                if (timeout > 0 && me.timeout === null) {
                    me.timeout = setTimeout(function() {
                        me.timeoutHandler();
                    }, timeout);
                }
                resolve();
            }).catch((msg: string) => {
                me.clearTimeout();
                reject("Request error" + (msg ? ": " + msg : ""));
            });
        });
    }

    startScanChannels() : Promise<string> {
        return this.simpleRequest("START scan", 1000);
    }

    stopScanChannels() : Promise<string> {
        return this.simpleRequest("STOP scan", 1000);
    }

    startFastRssi() : Promise<string> {
        return this.simpleRequest("START rssi", 1000);
    }

    stopFastRssi() : Promise<string> {
        return this.simpleRequest("STOP rssi", 1000);
    }

    loadDeviceData() : Promise<string> {
        return this.simpleRequest("GET device", 5000);
    }

    checkValidDevice() {
        let me = this;
        this.state = FLT_UNIT_STATES.VALID_TEST;
        this.bluetoothSerial.write("GET version\n")
            .catch(function () {
                me.fltutil.showToast("Cannot validate device.");
                me.disconnect();
            });
    }

    clearTimeout() {
        if (this.timeout !== null) {
            clearTimeout(this.timeout);
        }
    }

    reboot() {
        this.clearTimeout();
        let me = this;
        this.simpleRequest("REBOOT").then(function() {
            me.disconnect();
            me.fltutil.showToast("Rebooting unit, this may take up to 30 seconds.", 3000);
        }).catch(function (errMsg) {
            me.fltutil.showToast("Cannot reboot unit: " + errMsg);
        });
    }

    onReceive(data: string) {
        this.clearTimeout();
        if (this.isWaitingForValidTest()) {
            this.state = FLT_UNIT_STATES.VALID_TEST;

            if (getDataType(data) == "version") {
                this.state = FLT_UNIT_STATES.VALIDATED;
                let versionData: VersionData = JSON.parse(data);
                this.version = versionData.version;
                this.loadDeviceData();
            }
        } else if (this.isValidated()) {
            let dataType: string = getDataType(data);
            if (dataType == "device") {
                let deviceData: DeviceData = JSON.parse(data);
                deviceData.minimumLapTime = deviceData.minimumLapTime / 1000;
                this.observer.next(deviceData);
            } else if (dataType == "rssi") {
                let rssiData: RssiData = JSON.parse(data);
                this.observer.next(rssiData);
            } else if (dataType == "state") {
                let stateData: StateData = JSON.parse(data);
                // TODO strange behavior, if everything in one if/elseif construct, stateData.rssi is not found?
                if (isStateScanData(stateData)) {
                    this.fltutil.showToast("Scan " + stateData.scan, 2000);
                }
                if (isStateRssiData(stateData)) {
                    this.fltutil.showToast("Fast RSSI " + stateData.rssi, 2000);
                }
                if (isStateCalibrationData(stateData)) {
                    this.fltutil.showToast("Calibration done", 3000);
                    this.speech.speak('Calibration done.');
                    this.loadDeviceData();
                }
                if (!isStateScanData(stateData) && !isStateRssiData(stateData) && !isStateCalibrationData(stateData)) {
                    this.observer.next(stateData);
                }
            } else if (dataType == "lap") {
                let lapData: LapData = JSON.parse(data);
                this.observer.next(lapData);
            } else if (dataType == "scan") {
                let scanData: ScanData = JSON.parse(data);
                this.observer.next(scanData);
            } else if (dataType == "alarm") {
                let alarmData: AlarmData = JSON.parse(data);
                this.fltutil.showToast(alarmData.msg, 10000);
            // } else {
            //     this.fltutil.showToast("unknown data: " + data);
            }
        } else if (this.isWaitingForSave()) {
            this.state = FLT_UNIT_STATES.VALIDATED;
            if (data.startsWith("SETCONFIG: ")) {
                let result: string = data.substring(11);
                if (result.trim() == "OK" || result.trim() == "OK reboot") {
                    this.loadDeviceData();
                    let messageData: MessageData = { type: "message", message: "Successfully saved" };
                    if (result.trim() == "OK reboot") {
                        messageData.reboot = true;
                    }
                    this.observer.next(messageData);
                } else {
                    let messageData: MessageData = { type: "message", message: "Cannot save to device!" };
                    this.observer.next(messageData);
                }
            }
        }
    }

}
