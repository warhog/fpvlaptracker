import {Component} from '@angular/core';
import {NavController} from 'ionic-angular';
import {Storage} from '@ionic/storage';
import {BluetoothPage} from '../bluetooth/bluetooth';
import {NgZone} from '@angular/core';
import {FltutilProvider} from '../../providers/fltutil/fltutil'
import {FltunitProvider} from '../../providers/fltunit/fltunit'
import * as MessageData from '../../models/messagedata'
import * as ScanData from '../../models/scandata'

@Component({
    selector: 'page-scanner',
    templateUrl: 'scanner.html'
})
export class ScannerPage {

    private maxFreq: number = 0;
    private maxRssi: number = 0;
    private maxChannel: string = "-";
    private channels: {freq: number, channel: string, rssi: number}[] = [];
    private scanRunning: boolean = false;

    constructor(public zone: NgZone, private storage: Storage, private fltutil: FltutilProvider, private fltunit: FltunitProvider, public navCtrl: NavController) {
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
        this.fltunit.startScanChannels().then(function() {
            me.scanRunning = true;
        }).catch(function (msg: string) {
            me.fltutil.showToast("Cannot start channel scan: " + msg);
        });
    }

    stopScanChannels() {
        let me = this;
        if (this.scanRunning) {
            this.fltunit.stopScanChannels().then(function() {
                me.scanRunning = false;
            }).catch(function (msg: string) {
                me.fltutil.showToast("Cannot stop channel scan: " + msg);
            });
        }
    }

    gotoSettings() {
        this.navCtrl.push(BluetoothPage);
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
                    me.channels.forEach(function(value: {freq: number; channel: string; rssi: number; }) {
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
            me.gotoSettings();
        });
    }

    ionViewWillLeave() {
        this.stopScanChannels();
    }
}