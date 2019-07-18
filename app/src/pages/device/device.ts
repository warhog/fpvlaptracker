import {Component} from '@angular/core';
import {NavController, AlertController, ViewController} from 'ionic-angular';
import {BluetoothPage} from '../bluetooth/bluetooth';
import {Storage} from '@ionic/storage';
import {FltutilProvider} from '../../providers/fltutil/fltutil';
import {FltunitProvider} from '../../providers/fltunit/fltunit';
import {NgZone} from '@angular/core';
import {FastrssiPage} from '../fastrssi/fastrssi';
import * as DeviceData from '../../models/devicedata'
import * as MessageData from '../../models/messagedata'
import { ScannerPage } from '../scanner/scanner';
import { HomePage } from '../home/home';
import { HelpPage } from '../help/help';
import { ProfileData } from '../../models/profiledata';

@Component({
    selector: 'page-device',
    templateUrl: 'device.html'
})

export class DevicePage {

    private deviceData: DeviceData.DeviceData = {
        type: "device",
        ssid: "",
        password: "",
        frequency: 0,
        minimumLapTime: 0,
        triggerThreshold: 0,
        triggerThresholdCalibration: 0,
        calibrationOffset: 0,
        state: "",
        triggerValue: 0,
        voltage: 0,
        uptime: 0,
        defaultVref: 0,
        wifiState: false,
        rssi: 0,
        loopTime: 0,
        filterRatio: 0.0,
        filterRatioCalibration: 0.0
    };
    private deviceName: string = "";
    private profile: string = "";
    private profiles: ProfileData[] = [];
    private cells = 1;

    constructor(
        public storage: Storage, 
        public navCtrl: NavController, 
        private fltutil: FltutilProvider, 
        private fltunit: FltunitProvider,
        private zone: NgZone,
        private alertCtrl: AlertController,
        private viewCtrl: ViewController
    ) {
        this.loadProfiles();
    }

    reboot() {
        this.fltunit.reboot();
        setTimeout(() => {
            this.navCtrl.push(HomePage);
        }, 500);
    }

    saveData() {
        this.fltunit.saveData(this.deviceData);
    }

    gotoSettings() {
        this.navCtrl.push(BluetoothPage);
    }

    gotoHelp() {
        this.navCtrl.push(HelpPage);
    }

    gotoFastRssi() {
        this.navCtrl.push(FastrssiPage);
    }
    
    requestData() {
        this.fltutil.showLoader("Loading device data...");
        let me = this;
        this.fltunit.loadDeviceData().catch(function (msg: string) {
            me.fltutil.hideLoader();
            me.fltutil.showToast("Cannot get device data: " + msg);
        });
    }

    gotoScanner() {
        this.navCtrl.push(ScannerPage);
    }

    loadProfiles() {
        let me = this;
        let defaultProfile: ProfileData = this.getDefaultProfile();
        this.profiles = [defaultProfile];
        this.storage.get("profiles").then((profilesRaw: string) => {
            let tempProfiles: ProfileData = JSON.parse(profilesRaw);
            if (tempProfiles !== null) {
                me.profiles = me.profiles.concat(tempProfiles);
            }
        });
    }

    getDefaultProfile(): ProfileData {
        return {
            "name": "Default",
            "frequency": 5865,
            "minimumLapTime": 4000,
            "triggerThreshold": 40,
            "triggerThresholdCalibration": 120,
            "calibrationOffset": 10,
            "filterRatio": 0.05,
            "filterRatioCalibration": 0.005
        };
    }

    saveProfiles() {
        let profiles = this.profiles;
        let key: ProfileData = null;
        for (let i: number = 0; i < this.profiles.length; i++) {
            if (this.profiles[i].name == "Default") {
                key = this.profiles[i];
            }
        }
        var index = profiles.indexOf(key, 0);
        if (index > -1) {
            profiles.splice(index, 1);
        }
        this.storage.set("profiles", JSON.stringify(profiles));
        this.profiles.splice(0, 0, this.getDefaultProfile());
    }

    newProfile() {
        let me = this;
        let alert = this.alertCtrl.create({
            title: "New profile name",
            inputs: [{
                name: "profile",
                placeholder: "Enter profile name"
            }],
            buttons: [{
                text: "Cancel",
                role: "cancel",
                handler: data => {}
            }, {
                text: "Create",
                handler: (data) => {
                    if (data.profile == "Default") {
                        me.fltutil.showToast("Cannot create profile with name Default");
                        return false;
                    }
                    let tempProfile: ProfileData = {
                        name: data.profile,
                        calibrationOffset: me.deviceData.calibrationOffset,
                        frequency: me.deviceData.frequency,
                        minimumLapTime: me.deviceData.minimumLapTime,
                        triggerThreshold: me.deviceData.triggerThreshold,
                        triggerThresholdCalibration: me.deviceData.triggerThresholdCalibration,
                        filterRatio: me.deviceData.filterRatio,
                        filterRatioCalibration: me.deviceData.filterRatioCalibration
                    };
                    me.profiles = me.profiles.concat(tempProfile);
                    me.profile = data.profile;
                    me.saveProfiles();
                    me.fltutil.showToast("Profile created", 2000);
                }
            }]
          });
          alert.present();
    }

    updateProfile() {
        if (this.profile == "Default") {
            this.fltutil.showToast("Cannot update default profile");
            return;
        }
        for (let i: number = 0; i < this.profiles.length; i++) {
            if (this.profiles[i].name == this.profile) {
                this.profiles[i].frequency = this.deviceData.frequency;
                this.profiles[i].minimumLapTime = this.deviceData.minimumLapTime;
                this.profiles[i].triggerThreshold = this.deviceData.triggerThreshold;
                this.profiles[i].triggerThresholdCalibration = this.deviceData.triggerThresholdCalibration;
                this.profiles[i].calibrationOffset = this.deviceData.calibrationOffset;
                this.profiles[i].filterRatio = this.deviceData.filterRatio;
                this.profiles[i].filterRatioCalibration = this.deviceData.filterRatioCalibration;
            }
        }
        this.saveProfiles();
        this.fltutil.showToast("Profile updated", 2000);
    }

    removeProfile() {
        if (this.profile == "Default") {
            this.fltutil.showToast("Cannot remove default profile");
            return;
        }
        let alert = this.alertCtrl.create({
            title: "Remove profile?",
            message: "Do you really want to remove the profile '" + this.profile + "'?",
            buttons: [{
                text: "No",
                role: "cancel",
                handler: () => {}
            }, {
                text: "Yes",
                handler: () => {
                    let key: ProfileData = null;
                    for (let i: number = 0; i < this.profiles.length; i++) {
                        if (this.profiles[i].name == this.profile) {
                            key = this.profiles[i];
                        }
                    }
                    var index = this.profiles.indexOf(key, 0);
                    if (index > -1) {
                        this.profiles.splice(index, 1);
                    }
                    this.profile = "";
                    this.saveProfiles();
                    this.fltutil.showToast("Profile removed", 2000);
                }
            }]
          });
          alert.present();
    }

    changeProfile(newProfile: string) {
        let me = this;
        let alert = this.alertCtrl.create({
            title: "Overwrite values?",
            message: "You selected the profile '" + newProfile + "', this will overwrite the current data!\nContinue?",
            buttons: [{
                text: "No",
                role: "cancel",
                handler: () => {
                    me.profile = "";
                }
            }, {
                text: "Yes",
                handler: () => {
                    for (let i: number = 0; i < me.profiles.length; i++) {
                        if (me.profiles[i].name == newProfile) {
                            if (me.profiles[i].frequency !== -1) {
                                if (me.deviceData.frequency != me.profiles[i].frequency) {
                                    let alert = this.alertCtrl.create({
                                        title: "Changed frequency",
                                        subTitle: "You changed the frequency this tracker is listening to. Please power off your video transmitter and reboot unit after changing the profile!",
                                        buttons: ["OK"]
                                    });
                                    alert.present();
                                }
                                me.deviceData.frequency = me.profiles[i].frequency;
                            }
                            me.deviceData.minimumLapTime = me.profiles[i].minimumLapTime;
                            me.deviceData.triggerThreshold = me.profiles[i].triggerThreshold;
                            me.deviceData.triggerThresholdCalibration = me.profiles[i].triggerThresholdCalibration;
                            me.deviceData.calibrationOffset = me.profiles[i].calibrationOffset;
                            me.deviceData.filterRatio = me.profiles[i].filterRatio;
                            me.deviceData.filterRatioCalibration = me.profiles[i].filterRatioCalibration;
                            me.saveData();
                            break;
                        }
                    }
                }
            }]
          });
          alert.present();
    }

    ionViewDidEnter() {
        let me = this;
        this.fltunit.connectIfNotConnected().then(() => {
            me.deviceName = me.fltunit.getDeviceName();
            me.fltunit.getObservable().subscribe(data => {
                me.fltutil.hideLoader();
                if (DeviceData.isDeviceData(data)) {
                    if (data.voltage > 7 && data.voltage <= 10) {
                        me.cells = 2;
                    } else if (data.voltage > 10 && data.voltage <= 13) {
                        me.cells = 3;
                    } else if (data.voltage > 13 && data.voltage <= 17) {
                        me.cells = 4;
                    } else if (data.voltage > 17 && data.voltage <= 21.5) {
                        me.cells = 5;
                    } else if (data.voltage > 21.5 && data.voltage <= 26) {
                        me.cells = 6;
                    }
                    me.zone.run(() => {
                        me.deviceData = data;
                    });
                } else if (MessageData.isMessageData(data)) {
                    me.fltutil.showToast(data.message);
                    if (data.reboot !== undefined && data.reboot == true) {
                        me.reboot();
                    }
                }
            });
            me.requestData();
        }).catch(function (errMsg: string) {
            me.fltutil.showToast(errMsg);
            me.gotoSettings();
        });
    }

    ionViewWillLeave() {

    }

    ionViewWillEnter() {
        this.viewCtrl.showBackButton(false);
    }

}