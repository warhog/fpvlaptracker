import { Component } from '@angular/core';
import { AlertController } from '@ionic/angular';
import { Router } from '@angular/router';
import { Storage } from '@ionic/storage';
import { FltutilProvider } from '../services/fltutil/fltutil';
import { FltunitProvider } from '../services/fltunit/fltunit';
import { NgZone } from '@angular/core';
import * as DeviceData from '../models/devicedata'
import { ProfileData } from '../models/profiledata';
import * as MessageData from '../models/messagedata'
import * as RssiData from '../models/rssidata'

@Component({
    selector: 'page-device',
    templateUrl: 'device.html'
})

export class DevicePage {

    private deviceData: DeviceData.DeviceData = {
        type: "devicedata",
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

    private freqArr: any[] = [
        { frequency: 5865, name: "Band A 1" },
        { frequency: 5845, name: "Band A 2" },
        { frequency: 5825, name: "Band A 3" },
        { frequency: 5805, name: "Band A 4" },
        { frequency: 5785, name: "Band A 5" },
        { frequency: 5765, name: "Band A 6" },
        { frequency: 5745, name: "Band A 7" },
        { frequency: 5725, name: "Band A 8" },

        { frequency: 5733, name: "Band B 1" },
        { frequency: 5752, name: "Band B 2" },
        { frequency: 5771, name: "Band B 3" },
        { frequency: 5790, name: "Band B 4" },
        { frequency: 5809, name: "Band B 5" },
        { frequency: 5828, name: "Band B 6" },
        { frequency: 5847, name: "Band B 7" },
        { frequency: 5866, name: "Band B 8" },

        { frequency: 5705, name: "Band E 1" },
        { frequency: 5685, name: "Band E 2" },
        { frequency: 5665, name: "Band E 3" },
        { frequency: 5645, name: "Band E 4" },
        { frequency: 5885, name: "Band E 5" },
        { frequency: 5905, name: "Band E 6" },
        { frequency: 5925, name: "Band E 7" },
        { frequency: 5945, name: "Band E 8" },

        { frequency: 5740, name: "Band F 1" },
        { frequency: 5760, name: "Band F 2" },
        { frequency: 5780, name: "Band F 3" },
        { frequency: 5800, name: "Band F 4" },
        { frequency: 5820, name: "Band F 5" },
        { frequency: 5840, name: "Band F 6" },
        { frequency: 5860, name: "Band F 7" },
        { frequency: 5880, name: "Band F 8" },

        { frequency: 5658, name: "Band R 1" },
        { frequency: 5695, name: "Band R 2" },
        { frequency: 5732, name: "Band R 3" },
        { frequency: 5769, name: "Band R 4" },
        { frequency: 5806, name: "Band R 5" },
        { frequency: 5843, name: "Band R 6" },
        { frequency: 5880, name: "Band R 7" },
        { frequency: 5917, name: "Band R 8" },

        { frequency: 5362, name: "Band D 1" },
        { frequency: 5399, name: "Band D 2" },
        { frequency: 5436, name: "Band D 3" },
        { frequency: 5473, name: "Band D 4" },
        { frequency: 5510, name: "Band D 5" },
        { frequency: 5547, name: "Band D 6" },
        { frequency: 5584, name: "Band D 7" },
        { frequency: 5621, name: "Band D 8" }
    ];

    constructor(
        public storage: Storage,
        public router: Router,
        private fltutil: FltutilProvider,
        private fltunit: FltunitProvider,
        private zone: NgZone,
        private alertCtrl: AlertController
    ) {
        this.loadProfiles();
    }

    reboot() {
        this.fltunit.reboot();
        setTimeout(() => {
            this.router.navigate(['/tabs/home']);
        }, 500);
    }

    saveData() {
        this.fltunit.saveData(this.deviceData);
    }

    requestData() {
        this.fltutil.showLoader("Loading device data...");
        let me = this;
        this.fltunit.loadDeviceData().catch(function (msg: string) {
            me.fltutil.hideLoader();
            me.fltutil.showToast("Cannot get device data: " + msg);
        });
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
            header: "New profile name",
            inputs: [{
                name: "profile",
                placeholder: "Enter profile name"
            }],
            buttons: [{
                text: "Cancel",
                role: "cancel",
                handler: data => { }
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
        }).then(alert => alert.present());
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
            header: "Remove profile?",
            message: "Do you really want to remove the profile '" + this.profile + "'?",
            buttons: [{
                text: "No",
                role: "cancel",
                handler: () => { }
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
        }).then(alert => alert.present());
    }

    changeProfile(newProfile: string) {
        let me = this;
        let alert = this.alertCtrl.create({
            header: "Overwrite values?",
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
                                        header: "Changed frequency",
                                        subHeader: "You changed the frequency this tracker is listening to. Please power off your video transmitter and reboot unit after changing the profile!",
                                        buttons: ["OK"]
                                    }).then(alert => alert.present());
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
        }).then(alert => alert.present());
    }


    ionViewDidEnter() {
        let me = this;
        this.fltunit.connectIfNotConnected().then(() => {
            me.deviceName = me.fltunit.getDeviceName();
            me.fltunit.getObservable().subscribe(data => {
                me.fltutil.hideLoader();
                if (DeviceData.isDeviceData(data)) {
                    me.cells = this.fltutil.getCellCount(data.voltage);
                    me.zone.run(() => {
                        me.deviceData = data;
                    });
                } else if (RssiData.isRssiData(data)) {
                    console.log('got rssidata', data);
                    me.zone.run(() => {
                        me.deviceData.rssi = data.rssi;
                    });
                } else if (MessageData.isMessageData(data)) {
                    me.fltutil.showToast(data.message);
                    if (data.reboot !== undefined && data.reboot == true) {
                        me.reboot();
                    } else {
                        me.requestData();
                    }
                }
            });
            me.requestData();
        }).catch(function (errMsg: string) {
            me.fltutil.showToast(errMsg);
            me.router.navigate(['/tabs/settings']);
        });
        // this.deviceData = {
        //     calibrationOffset: 25,
        //     defaultVref: 1050,
        //     filterRatio: 0.05,
        //     filterRatioCalibration: 0.05,
        //     frequency: 5765,
        //     loopTime: 112,
        //     minimumLapTime: 8,
        //     password: "123",
        //     rssi: 123,
        //     ssid: "wlan",
        //     state: "RACE",
        //     triggerThreshold: 150,
        //     triggerThresholdCalibration: 250,
        //     triggerValue: 300,
        //     type: "devicedata",
        //     uptime: 12,
        //     voltage: 11.5,
        //     wifiState: false
        // };
    }

    ionViewWillLeave() {

    }

}