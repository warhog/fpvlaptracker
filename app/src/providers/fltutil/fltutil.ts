import {Injectable} from '@angular/core';
import 'rxjs/add/operator/map';
import {LoadingController, NavController} from 'ionic-angular';
import {ToastController} from 'ionic-angular';
import { BluetoothPage } from '../../pages/bluetooth/bluetooth';

/*
  Generated class for the TestProvider provider.

  See https://angular.io/docs/ts/latest/guide/dependency-injection.html
  for more info on providers and Angular DI.
*/
@Injectable()
export class FltutilProvider {

    private loader: any = null;

    constructor(private loadingCtrl: LoadingController, private toastCtrl: ToastController) {
    }

    showLoader(text: string) {
        this.loader = this.loadingCtrl.create({
            content: text
        });
        this.loader.present();
    }

    hideLoader() {
        this.loader.dismiss();
    }

    showToast(errMsg: string, time: number = 5000) {
        let toast = this.toastCtrl.create({
            message: errMsg,
            duration: time
        });
        toast.present();
    }

    getFrequencyTable(): number[] {
        let frequencies: number[] = [
            5865, 5845, 5825, 5805, 5785, 5765, 5745, 5725, // Band A
            5733, 5752, 5771, 5790, 5809, 5828, 5847, 5866, // Band B
            5705, 5685, 5665, 5645, 5885, 5905, 5925, 5945, // Band E
            5740, 5760, 5780, 5800, 5820, 5840, 5860, 5880, // Band F / Airwave
            5658, 5695, 5732, 5769, 5806, 5843, 5880, 5917, // Band C / Immersion Raceband
            5362, 5399, 5436, 5473, 5510, 5547, 5584, 5621  // Band Low Raceband / D / 5.3
        ];
        return frequencies;
    }

    getFrequencyNameTable(): string[] {
        let frequencyNameTable: string[] = [
            "A1", "A2", "A3", "A4", "A5", "A6", "A7", "A8", // Band A
            "B1", "B2", "B3", "B4", "B5", "B6", "B7", "B8", // Band B
            "E1", "E2", "E3", "E4", "E5", "E6", "E7", "E8", // Band E
            "F1", "F2", "F3", "F4", "F5", "F6", "F7", "F8", // Band F
            "R1", "R2", "R3", "R4", "R5", "R6", "R7", "R8", // Band C / Immersion Raceband
            "D1", "D2", "D3", "D4", "D5", "D6", "D7", "D8", // Band Low Raceband / D / 5.3
        ];
        return frequencyNameTable;
    }

    validateFrequency(frequency: number): boolean {
        let frequencies: number[] = this.getFrequencyTable();
        return frequencies.indexOf(frequency) > -1;
    }

    getFrequencyName(frequency: number): string {
        let frequencies: number[] = this.getFrequencyTable();
        let frequencyNameTable: string[] = this.getFrequencyNameTable();
        let index = frequencies.indexOf(frequency);
        if (index >= 0) {
            return frequencyNameTable[index];
        }
        return "unknown";
    }

}
