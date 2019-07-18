import {Component} from '@angular/core';
import {NavController} from 'ionic-angular';
import {BluetoothSerial} from '@ionic-native/bluetooth-serial';
import {ToastController} from 'ionic-angular';
import {DevicePage} from '../device/device';
import {Storage} from '@ionic/storage';
import { FltunitProvider } from '../../providers/fltunit/fltunit';

@Component({
    selector: 'page-bluetooth',
    templateUrl: 'bluetooth.html'
})
export class BluetoothPage {

    private items: object[] = [];
    private bluetoothAvailable: boolean = false;
    private bluetoothNoDevices: boolean = false;

    itemSelected(item) {
        this.storage.set("bluetooth.id", item.id);
        this.storage.set("bluetooth.name", item.name);
        this.fltunit.disconnect();
        this.navCtrl.pop();
    }

    reload() {
        this.loadBluetoothDevices();
    }

    loadBluetoothDevices() {
        this.bluetoothSerial.isEnabled().then((data) => {
            this.bluetoothAvailable = true;
            this.bluetoothSerial.list().then((devices) => {
                if (devices.length > 0) {
                    let list: object[] = [];
                    devices.forEach(function (dev: object) {
                        list.push(dev)
                    });
                    this.items = list;
                } else {
                    this.bluetoothNoDevices = true;
                }
            });
        }, () => {
            console.log("no bluetooth");
            let toast = this.toastCtrl.create({
                message: 'Bluetooth not enabled',
                duration: 3000
            });
            toast.present();
        });
    }

    constructor(private storage: Storage,
        public toastCtrl: ToastController,
        public navCtrl: NavController,
        private bluetoothSerial: BluetoothSerial,
        private fltunit: FltunitProvider) {
        this.loadBluetoothDevices();
    }

}
