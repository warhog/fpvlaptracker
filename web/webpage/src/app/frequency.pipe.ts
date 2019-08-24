import { Pipe, PipeTransform } from '@angular/core';
import { UtilService, Frequency } from './util.service';

@Pipe({
  name: 'frequency'
})
export class FrequencyPipe implements PipeTransform {

  constructor(private utilService: UtilService) { }

  transform(frequency: number): string {
    let frequencyObj: Frequency = this.utilService.getFrequencyObject(frequency);
    if (frequencyObj != null) {
      return frequencyObj.name;
    }
    return frequency + ' MHz';
  }

}
