import { Injectable } from '@angular/core';
import { Subject, Observable } from 'rxjs';
import { StatusResponse } from './status-response';

export interface Frequency {
  frequency: number;
  name: string;
  shortName: string;
}

@Injectable({
  providedIn: 'root'
})
export class UtilService {

  private subjectOverlay: Subject<boolean> = new Subject<boolean>();
  private overlay: boolean = false;

  public frequencies: number[] = [
    5865, 5845, 5825, 5805, 5785, 5765, 5745, 5725, // Band A
    5733, 5752, 5771, 5790, 5809, 5828, 5847, 5866, // Band B
    5705, 5685, 5665, 5645, 5885, 5905, 5925, 5945, // Band E
    5740, 5760, 5780, 5800, 5820, 5840, 5860, 5880, // Band F / Airwave
    5658, 5695, 5732, 5769, 5806, 5843, 5880, 5917, // Band C / Immersion Raceband
    5362, 5399, 5436, 5473, 5510, 5547, 5584, 5621  // Band D / 5.3    
  ];
  public bands: string[] = [
    'A1 5865 MHz (Boscam, TBS)', 'A2 5845 MHz (Boscam, TBS)', 'A3 5825 MHz (Boscam, TBS)', 'A4 5805 MHz (Boscam, TBS)', 'A5 5785 MHz (Boscam, TBS)', 'A6 5765 MHz (Boscam, TBS)', 'A7 5745 MHz (Boscam, TBS)', 'A8 5725 MHz (Boscam, TBS)',
    'B1 5733 MHz (Boscam)', 'B2 5752 MHz (Boscam)', 'B3 5771 MHz (Boscam)', 'B4 5790 MHz (Boscam)', 'B5 5809 MHz (Boscam)', 'B6 5828 MHz (Boscam)', 'B7 5847 MHz (Boscam)', 'B8 5866 MHz (Boscam)',
    'E1 5705 MHz (Boscam)', 'E2 5685 MHz (Boscam)', 'E3 5665 MHz (Boscam)', 'E4 5645 MHz (Boscam)', 'E5 5885 MHz (Boscam)', 'E6 5905 MHz (Boscam)', 'E7 5925 MHz (Boscam)', 'E8 5945 MHz (Boscam)',
    'F1 5740 MHz (Airwave, Fatshark, ImmersionRC)', 'F2 5760 MHz (Airwave, Fatshark, ImmersionRC)', 'F3 5780 MHz (Airwave, Fatshark, ImmersionRC)', 'F4 5800 MHz (Airwave, Fatshark, ImmersionRC)', 'F5 5820 MHz (Airwave, Fatshark, ImmersionRC)', 'F6 5840 MHz (Airwave, Fatshark, ImmersionRC)', 'F7 5860 MHz (Airwave, Fatshark, ImmersionRC)', 'F8 5880 MHz (Airwave, Fatshark, ImmersionRC)',
    'R1 5658 MHz (Raceband)', 'R2 5695 MHz (Raceband)', 'R3 5732 MHz (Raceband)', 'R4 5769 MHz (Raceband)', 'R5 5806 MHz (Raceband)', 'R6 5843 MHz (Raceband)', 'R7 5880 MHz (Raceband)', 'R8 5917 MHz (Raceband)',
    'L1 5362 MHz (Boscam)', 'L2 5399 MHz (Boscam)', 'L3 5436 MHz (Boscam)', 'L4 5473 MHz (Boscam)', 'L5 5510 MHz (Boscam)', 'L6 5547 MHz (Boscam)', 'L7 5584 MHz (Boscam)', 'L8 5621 MHz (Boscam)'
  ];
  public frequencyObjects: Frequency[] = [];

  constructor() {
    // build table of frequencies with names
    let me = this;
    this.frequencies.forEach(function (value: number, index: number) {
      let selectedBand: string = me.bands[index];
      let shortName: string = selectedBand.split(' ')[0];
      me.frequencyObjects.push({
        frequency: value,
        name: selectedBand,
        shortName: shortName
      });
    });
  }

  getOverlayObservable(): Observable<boolean> {
    return this.subjectOverlay.asObservable();
  }

  toggleOverlay(enabled: boolean = null) {
    if (enabled == null) {
      this.overlay = !this.overlay;
    } else {
      this.overlay = enabled;
    }
    this.subjectOverlay.next(this.overlay);
  }

  getFrequencyObject(frequency: number) {
    let ret: Frequency = null;
    this.frequencyObjects.forEach(function (value: Frequency) {
      if (value.frequency == frequency) {
        ret = value;
      }
    });
    if (ret != null) {
      return ret;
    }
    console.log('getValidFrequency(' + frequency + '): not valid, using default');
    return this.frequencyObjects[0];
  };

  statusResponseHandler(response: StatusResponse, fnSuccess: () => void, fnFailure: (message: string) => void) {
    if (response && !response.status || (response && response.status && response.status != 'OK' && response.status != 'OK reboot')) {
      if (!response.message) {
        response.message = 'unknown';
      }
      fnFailure && fnFailure(response.message);
    } else {
      fnSuccess && fnSuccess();
    }
}


}
