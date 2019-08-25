import { Component } from '@angular/core';
import { BluetoothSerial } from '@ionic-native/bluetooth-serial/ngx';
import { ToastController } from '@ionic/angular';
import { Storage } from '@ionic/storage';
import { FltunitProvider } from '../services/fltunit/fltunit';
import { Router } from '@angular/router';

@Component({
    selector: 'page-bluetooth',
    templateUrl: 'bluetooth.html'
})
export class BluetoothPage {

    private items: object[] = [];
    private bluetoothAvailable: boolean = false;
    private bluetoothNoDevices: boolean = false;

    itemSelected(item: any) {
        this.storage.set("bluetooth.id", item.id);
        this.storage.set("bluetooth.name", item.name);
        this.fltunit.disconnect();
        this.router.navigate(['/tabs/settings']);
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
            this.toastCtrl.create({
                message: 'Bluetooth not enabled',
                duration: 3000
            }).then(toast => toast.present());
        });
    }

    constructor(private storage: Storage,
        public toastCtrl: ToastController,
        public router: Router,
        private bluetoothSerial: BluetoothSerial,
        private fltunit: FltunitProvider) {
        this.loadBluetoothDevices();
    }

}
