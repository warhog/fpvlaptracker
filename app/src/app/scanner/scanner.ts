import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { Storage } from '@ionic/storage';
import { NgZone } from '@angular/core';
import { FltutilProvider } from '../services/fltutil/fltutil'
import { FltunitProvider } from '../services/fltunit/fltunit'
import * as MessageData from '../models/messagedata'
import * as ScanData from '../models/scandata'

@Component({
    selector: 'page-scanner',
    templateUrl: 'scanner.html'
})
export class ScannerPage {

    private _maxFreq: number = 0;
    private _maxRssi: number = 0;
    private _maxChannel: string = "-";
    private _channels: {
        freq: number;
        channel: string;
        rssi: number;
    }[] = [];
    private _scanRunning: boolean = false;

    constructor(public zone: NgZone, private storage: Storage, private fltutil: FltutilProvider, private fltunit: FltunitProvider, public router: Router) {
        for (let i: number = 0; i < fltutil.getFrequencyTable().length; i++) {
            let freq: number = fltutil.getFrequencyTable()[i];
            this.channels.push({
                freq: freq,
                channel: fltutil.getFrequencyName(freq),
                rssi: 0
            });
            if (i == 38) {
                // workaround for F8 and R8 have the same frequency
                this.channels[38].channel = "R7";
            }
        }
    }

    startScanChannels() {
        let me = this;
        this.fltunit.startScanChannels().then(function () {
            me.scanRunning = true;
        }).catch(function (msg: string) {
            me.fltutil.showToast("Cannot start channel scan: " + msg);
        });
    }

    stopScanChannels() {
        let me = this;
        if (this.scanRunning) {
            this.fltunit.stopScanChannels().then(function () {
                me.scanRunning = false;
            }).catch(function (msg: string) {
                me.fltutil.showToast("Cannot stop channel scan: " + msg);
            });
        }
    }

    ionViewDidEnter() {
        let me = this;
        this.fltunit.connectIfNotConnected().then(() => {
            this.fltunit.getObservable().subscribe(data => {
                me.fltutil.hideLoader();
                if (MessageData.isMessageData(data)) {
                    me.fltutil.showToast(data.message);
                } else if (ScanData.isScanData(data)) {
                    for (let i: number = 0; i < me.fltutil.getFrequencyTable().length; i++) {
                        if (me.channels[i].freq == data.frequency) {
                            me.zone.run(() => {
                                me.channels[i].rssi = data.rssi;
                            });
                            break;
                        }
                    }
                    // workaround, r7 and f8 are the same frequency
                    me.channels[38].rssi = me.channels[31].rssi;

                    let maxRssi: number = 0;
                    let maxFreq: number = 0;
                    me.channels.forEach(function (value: { freq: number; channel: string; rssi: number; }) {
                        if (value.rssi > maxRssi) {
                            maxRssi = value.rssi;
                            maxFreq = value.freq;
                        }
                    });
                    me.zone.run(() => {
                        me.maxRssi = maxRssi;
                        me.maxFreq = maxFreq;
                        me.maxChannel = me.fltutil.getFrequencyName(maxFreq);
                    });
                }
            });
        }).catch(function (errMsg: string) {
            me.fltutil.showToast(errMsg);
            me.router.navigate(['/tabs/bluetooth']);
        });
    }

    ionViewWillLeave() {
        this.stopScanChannels();
    }

    public get maxFreq(): number {
        return this._maxFreq;
    }
    public set maxFreq(value: number) {
        this._maxFreq = value;
    }
    public get maxRssi(): number {
        return this._maxRssi;
    }
    public set maxRssi(value: number) {
        this._maxRssi = value;
    }
    public get maxChannel(): string {
        return this._maxChannel;
    }
    public set maxChannel(value: string) {
        this._maxChannel = value;
    }
    public get channels(): {
        freq: number;
        channel: string;
        rssi: number;
    }[] {
        return this._channels;
    }
    public set channels(value: {
        freq: number;
        channel: string;
        rssi: number;
    }[]) {
        this._channels = value;
    }
    public get scanRunning(): boolean {
        return this._scanRunning;
    }
    public set scanRunning(value: boolean) {
        this._scanRunning = value;
    }

}