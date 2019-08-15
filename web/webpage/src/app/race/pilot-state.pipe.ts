import { Pipe, PipeTransform } from '@angular/core';
import { SafeHtml, DomSanitizer } from '@angular/platform-browser';

// TODO this is not available to the transform method when imported?
enum PilotState {
  WaitingForStart = 'WAITING_FOR_START',
  WaitingForFirstPass = 'WAITING_FOR_FIRST_PASS',
  Started = 'STARTED',
  LastLap = 'LAST_LAP',
  Finished = 'FINISHED',
  Invalid = 'INVALID'
}

@Pipe({
  name: 'pilotState'
})
export class PilotStatePipe implements PipeTransform {

  constructor(private sanitizer: DomSanitizer) {
  }

  transform(value: string): SafeHtml {
    let result: string = '';
    switch (value) {
      case PilotState.Finished:
        result = 'Finished';
        break;
      case PilotState.Invalid:
        result = '<b>Invalid</b>';
        break;
      case PilotState.LastLap:
        result = 'Last lap';
        break;
      case PilotState.Started:
        result = 'Started';
        break;
      case PilotState.WaitingForFirstPass:
        result = 'Waiting for first pass';
        break;
      case PilotState.WaitingForStart:
        result = 'Waiting for start';
        break;
      default:
        result = 'Unknown';
    }
    return this.sanitizer.bypassSecurityTrustHtml(result);
  }
}