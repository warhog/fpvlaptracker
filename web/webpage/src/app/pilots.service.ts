import { Injectable } from '@angular/core';
import { UtilService } from './util.service';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Pilot } from './interfaces/pilot';
import { StatusResponse } from './status-response';
import { timeout, catchError } from 'rxjs/operators';
import { of } from 'rxjs';
import { BigPilot } from './interfaces/bigpilot';

@Injectable({
  providedIn: 'root'
})
export class PilotsService {

  constructor(private utilService: UtilService, private httpClient: HttpClient) { }

  loadPilots(fnSuccess: (pilots: Pilot[]) => void, fnFailure: (message: string) => void) {
    this.httpClient.get<Pilot[]>('/api/pilots').subscribe(pilots => {
      fnSuccess && fnSuccess(pilots);
    }, error => {
      console.log('cannot load pilots: ', error);
      fnFailure && fnFailure(error);
    });
  }

  loadPilot(name: string, fnSuccess: (pilot: Pilot) => void, fnFailure: (message: string) => void) {
    let params: HttpParams = new HttpParams().set('name', name);
    this.httpClient.get<Pilot>('/api/pilot', { params: params }).subscribe(pilot => {
      fnSuccess && fnSuccess(pilot);
    }, error => {
      console.log('cannot load node: ', error);
      fnFailure && fnFailure(error);
    });
  }

  createPilot(pilot: Pilot, fnSuccess: () => void, fnFailure: (message: string) => void) {
    let me = this;
    this.httpClient.post<StatusResponse>('/api/auth/pilot', pilot, {})
      .pipe(timeout(5000), catchError(error => {
        fnFailure && fnFailure('error during connecting to the server.');
        return of(null);
      })).subscribe(response => {
        me.utilService.statusResponseHandler(response, () => {
          fnSuccess && fnSuccess();
        }, (message) => {
          fnFailure && fnFailure('failed to create new user: ' + message);
        });
        this.utilService.toggleOverlay(false);
      });
  }

  getChipIds(fnSuccess: (chipIds: number[]) => void, fnFailure: (message: string) => void) {
    this.loadPilots((pilots) => {
      let chipIds: number[] = [];
      pilots.filter((pilot) => {
        return pilot.chipId != null && pilot.chipId != 0;
      }).map((pilot) => {
        chipIds.push(pilot.chipId);
      });
      fnSuccess && fnSuccess(chipIds);
    }, (message) => {
      fnFailure && fnFailure(message);
    });
  }

  setPilotValid(pilot: BigPilot, valid: boolean, fnSuccess: () => void, fnFailure: (message: string) => void) {
    let params: HttpParams = new HttpParams().set('name', pilot.name).set('valid', String(valid));
    this.httpClient.get<Pilot[]>('/api/auth/race/pilot/valid', { params: params }).subscribe(response => {
      fnSuccess && fnSuccess();
    }, error => {
      console.log('cannot invalidate pilot: ', error);
      fnFailure && fnFailure(error);
    });
  }

}
