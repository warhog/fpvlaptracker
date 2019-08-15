import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'raceType'
})
export class RaceTypePipe implements PipeTransform {

  transform(value: string): any {
    switch (value) {
      case 'ROUND_BASED':
        return 'Round based';
        break;
      case 'FIXED_TIME':
        return 'Fixed time';
        break;
      default:
        return 'Unknown';
    }
  }

}
