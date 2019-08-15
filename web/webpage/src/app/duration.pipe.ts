import { Pipe, PipeTransform } from '@angular/core';
import * as moment from 'moment';

@Pipe({
  name: 'duration'
})
export class DurationPipe implements PipeTransform {

  transform(duration: string, multiplier?: number): string {
    if (duration == null) {
      return '';
    }
    let durationNumber: number = Number(duration);
    if (isNaN(durationNumber)) {
      durationNumber = moment.duration(duration).asMilliseconds();
    }
    if (multiplier != undefined) {
      durationNumber *= 1000;
    }
    return moment.duration(durationNumber).asSeconds().toFixed(2) + " s";
  }

}
