import { Component } from '@angular/core';
import { Storage } from '@ionic/storage';

@Component({
    selector: 'page-settings',
    templateUrl: 'settings.html'
})
export class SettingsPage {

    private bluetoothDevice: string = "none";
    private numberOfLaps: number = 10;
    private keepAwakeDuringRace: boolean = false;

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

}
