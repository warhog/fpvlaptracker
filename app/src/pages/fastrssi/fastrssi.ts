import {Component} from '@angular/core';
import {NavController} from 'ionic-angular';
import {Storage} from '@ionic/storage';
import {BluetoothPage} from '../bluetooth/bluetooth';
import {NgZone} from '@angular/core';
import {FltutilProvider} from '../../providers/fltutil/fltutil'
import {FltunitProvider} from '../../providers/fltunit/fltunit'
import * as RssiData from '../../models/rssidata'
import * as MessageData from '../../models/messagedata'

@Component({
    selector: 'page-fastrssi',
    templateUrl: 'fastrssi.html'
})
export class FastrssiPage {

    private rssi: number = 0;
    private fastrssiRunning: boolean = false;

    constructor(public zone: NgZone, private storage: Storage, private fltutil: FltutilProvider, private fltunit: FltunitProvider, public navCtrl: NavController) {

    }

    startFastRssi() {
        let me = this;
        this.fltunit.startFastRssi().then(function() {
            me.fastrssiRunning = true;
        }).catch(function (msg: string) {
            me.fltutil.showToast("Cannot start fastrssi: " + msg);
        });
    }

    stopFastRssi() {
        let me = this;
        if (this.fastrssiRunning) {
            this.fltunit.stopFastRssi().then(function() {
                me.fastrssiRunning = false;
            }).catch(function (msg: string) {
                me.fltutil.showToast("Cannot stop fastrssi: " + msg);
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
                } else if (RssiData.isRssiData(data)) {
                    me.zone.run(() => {
                        if (data.rssi == NaN) {
                            data.rssi = 0;
                        }
                        me.rssi = data.rssi;
                    });
                }
            });
        }).catch(function (errMsg: string) {
            me.fltutil.showToast(errMsg);
            me.gotoSettings();
        });    }

    ionViewWillLeave() {
        this.stopFastRssi();
    }
}