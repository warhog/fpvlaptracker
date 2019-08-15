import { Pipe, PipeTransform } from '@angular/core';
import { RaceState } from './race-state.enum';

@Pipe({
  name: 'raceState'
})
export class RaceStatePipe implements PipeTransform {

  transform(value: string): string {
    value = value.replace('"', '');
    switch (value) {
      case RaceState.Fault:
        return 'Fault';
        break;
      case RaceState.Finished:
        return 'Finished';
        break;
      case RaceState.GetReady:
        return 'Get ready';
        break;
      case RaceState.Prepare:
        return 'Prepare';
        break;
      case RaceState.Running:
        return 'Running';
        break;
      case RaceState.Waiting:
        return 'Waiting for start';
        break;
      default:
        return 'Unknown';
    }
  }

}
