import { Component } from '@angular/core';
import { Storage } from '@ionic/storage';

@Component({
    selector: 'page-settings',
    templateUrl: 'settings.html'
})
export class SettingsPage {

    private _bluetoothDevice: string = "none";
    private _numberOfLaps: number = 10;
    private _keepAwakeDuringRace: boolean = false;

    constructor(private storage: Storage) {

    }

    ionViewDidEnter() {
        this.storage.get("bluetooth.name").then((name: string) => {
            this.bluetoothDevice = name;
        });
        this.storage.get("race.numberOfLaps").then((numberOfLaps: number) => {
            if (numberOfLaps == undefined || numberOfLaps == null || numberOfLaps == 0) {
                numberOfLaps = 10;
            }
            this.numberOfLaps = numberOfLaps;
        });
        this.storage.get("race.keepAwakeDuringRace").then((keepAwakeDuringRace: boolean) => {
            if (keepAwakeDuringRace == undefined || keepAwakeDuringRace == null) {
                keepAwakeDuringRace = false;
            }
            this.keepAwakeDuringRace = keepAwakeDuringRace;
        });
    }

    saveValues() {
        this.storage.set("race.numberOfLaps", this.numberOfLaps);
        this.storage.set("race.keepAwakeDuringRace", this.keepAwakeDuringRace);
    }

    public get bluetoothDevice(): string {
        return this._bluetoothDevice;
    }
    public set bluetoothDevice(value: string) {
        this._bluetoothDevice = value;
    }
    public get numberOfLaps(): number {
        return this._numberOfLaps;
    }
    public set numberOfLaps(value: number) {
        this._numberOfLaps = value;
    }
    public get keepAwakeDuringRace(): boolean {
        return this._keepAwakeDuringRace;
    }
    public set keepAwakeDuringRace(value: boolean) {
        this._keepAwakeDuringRace = value;
    }
}
