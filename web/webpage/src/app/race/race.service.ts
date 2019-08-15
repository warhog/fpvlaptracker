import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { RaceData } from '../interfaces/racedata';
import { StatusResponse } from '../status-response';
import { UtilService } from '../util.service';

@Injectable({
  providedIn: 'root'
})
export class RaceService {

  constructor(private httpClient: HttpClient, private utilService: UtilService) { }

  loadRaceData(fnSuccess: (raceData: RaceData) => void, fnFailure: (message: string) => void) {
    this.httpClient.get<RaceData>('/api/race/data').subscribe(raceData => {
      fnSuccess && fnSuccess(raceData);
    }, error => {
      console.log('cannot load race data: ', error);
      fnFailure && fnFailure(error);
    });
  }

  loadRaceChartData(fnSuccess: (chartData: any) => void, fnFailure: (message: string) => void) {
    this.httpClient.get<any>('/api/race/chartdata').subscribe(chartData => {
      fnSuccess && fnSuccess(chartData);
    }, error => {
      console.log('cannot load race chart data: ', error);
      fnFailure && fnFailure(error);
    });
  }

  setRaceType(type: string, fnSuccess: () => void, fnFailure: (message: string) => void) {
    let params: HttpParams = new HttpParams().set('type', type);
    this.httpClient.get<StatusResponse>('/api/auth/race/type', { params: params }).subscribe(response => {
      this.utilService.statusResponseHandler(response, () => {
        fnSuccess && fnSuccess();
      }, (message) => {
        console.log('set type error: ', message);
        fnFailure && fnFailure(message);
      });
    }, error => {
      console.log('cannot set type: ', error);
      fnFailure && fnFailure(error);
    });
  }

  startRace(fnSuccess: () => void, fnFailure: (message: string) => void) {
    this.httpClient.get<StatusResponse>('/api/auth/race/start').subscribe(response => {
      this.utilService.statusResponseHandler(response, () => {
        fnSuccess && fnSuccess();
      }, (message) => {
        console.log('start race error: ', message);
        fnFailure && fnFailure(message);
      });
    }, error => {
      console.log('cannot start race: ', error);
      fnFailure && fnFailure(error);
    });
  }

  stopRace(fnSuccess: () => void, fnFailure: (message: string) => void) {
    this.httpClient.get<StatusResponse>('/api/auth/race/stop').subscribe(response => {
      this.utilService.statusResponseHandler(response, () => {
        fnSuccess && fnSuccess();
      }, (message) => {
        console.log('stop race error: ', message);
        fnFailure && fnFailure(message);
      });
    }, error => {
      console.log('cannot stop race: ', error);
      fnFailure && fnFailure(error);
    });
  }

  setLapValid(name: string, lap: number, valid: boolean, fnSuccess: () => void, fnFailure: (message: string) => void) {
    let params: HttpParams = new HttpParams().set('name', name).set('lap', String(lap)).set('valid', String(valid));
    this.httpClient.get<StatusResponse>('/api/auth/race/lap/valid', { params: params }).subscribe(response => {
      this.utilService.statusResponseHandler(response, () => {
        fnSuccess && fnSuccess();
      }, (message) => {
        console.log('stop race error: ', message);
        fnFailure && fnFailure(message);
      });
    }, error => {
      console.log('cannot stop race: ', error);
      fnFailure && fnFailure(error);
    });
  }
}
