import { Component, OnInit } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { AlertService } from '../alert.service';
import { UtilService } from '../util.service';
import { timeout, catchError, map } from 'rxjs/operators';
import { of, Observable } from 'rxjs';
import { ConfirmDialogService } from '../confirm-dialog/confirm-dialog.service';
import { StatusResponse } from '../status-response';

interface SettingsData {
  numberOfLaps: number;
  startInterval: number;
  raceDuration: number;
  overtimeDuration: number;
  preparationDuration: number;
  timezone: string;
}

@Component({
  selector: 'app-settings',
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.css']
})
export class SettingsComponent implements OnInit {

  private _data: SettingsData = {
    numberOfLaps: 0,
    startInterval: 0,
    raceDuration: 0,
    overtimeDuration: 0,
    preparationDuration: 0,
    timezone: '-'
  };

  private _storing: boolean = false;

  constructor(private http: HttpClient, private alertService: AlertService, private utilService: UtilService, private confirmDialogService: ConfirmDialogService) { }

  ngOnInit() {
    this.loadSettings();
  }

  loadSettings() {
    this.utilService.toggleOverlay(true);
    this.http.get<SettingsData>('api/auth/settings/data', {}).subscribe(response => {
      this.utilService.toggleOverlay(false);
      this.data = response;
    }, error => {
      this.utilService.toggleOverlay(false);
      this.alertService.error('unable to load settings', 'error');
    });

  }

  storeSettings() {
    let me = this;
    this.storing = true;
    this.utilService.toggleOverlay(true);
    this.http.post<StatusResponse>('api/auth/settings/data', this.data, {})
      .pipe(timeout(5000), catchError(error => {
        this.alertService.error('error during connecting to the server.', 'saving failed');
        return of(null);
      })).subscribe(response => {
        this.utilService.statusResponseHandler(response, () => { }, (message) => {
          this.alertService.error('failed to store data on the server: ' + message, 'saving failed');
        });
        this.storing = false;
        this.utilService.toggleOverlay(false);
      });
    ;
  }

  shutdown() {
    let me = this;
    me.confirmDialogService.confirmYesNo('really shutdown?', 'do you really want to shutdown the system?', function () {
      me.utilService.toggleOverlay(true);
      me.http.get('api/auth/settings/shutdown', {}).subscribe(response => {
        me.utilService.toggleOverlay(true);
      }, error => {
        me.alertService.error('unable to shutdown the system!', 'error', true, true);
      });
    }, function () { });
  }

  public get data(): SettingsData {
    return this._data;
  }
  public set data(value: SettingsData) {
    this._data = value;
  }
  public get storing(): boolean {
    return this._storing;
  }
  public set storing(value: boolean) {
    this._storing = value;
  }
}
