import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient, HttpParams } from '@angular/common/http';
import { NodeDeviceData } from '../nodedevicedata';
import { UtilService } from '../util.service';
import { timeout, catchError } from 'rxjs/operators';
import { of, Subscription } from 'rxjs';
import { AlertService } from '../alert.service';
import { ConfirmDialogService } from '../confirm-dialog/confirm-dialog.service';
import { StatusResponse } from '../status-response';
import { RxStompService } from '@stomp/ng2-stompjs';
import { Message } from '@stomp/stompjs';
import { NodeService } from '../node.service';
import { AuthService } from '../auth.service';

interface ProfilesRaw {
  chipId: number;
  data: string;
  name: string;
}

interface Profile {
  name: string;
  minimumLapTime: number;
  triggerThreshold: number;
  triggerThresholdCalibration: number;
  calibrationOffset: number;
  triggerValue: number;
  filterRatio: number;
  filterRatioCalibration: number;
}

@Component({
  selector: 'app-nodesetup',
  templateUrl: './nodesetup.component.html',
  styleUrls: ['./nodesetup.component.css']
})
export class NodesetupComponent implements OnInit {

  private _chipid: number = 0;
  private _node: NodeDeviceData = {
    calibrationOffset: 0,
    chipId: 0,
    defaultVref: 0,
    filterRatio: 0,
    filterRatioCalibration: 0,
    frequency: 0,
    inetAddress: '',
    loopTime: 0,
    minimumLapTime: 0,
    rssi: 0,
    state: '',
    triggerThreshold: 0,
    triggerThresholdCalibration: 0,
    triggerValue: 0,
    uptime: 0,
    version: '',
    voltage: 0,
    frequencyObj: {
      frequency: 0,
      name: '',
      shortName: ''
    }
  };
  private _cells: number = 1;
  private _loadingDeviceData: boolean = false;
  private _newProfileName: string = '';
  private _profiles: Profile[] = [];
  private _upperRssi: number = 0;
  private _lowerRssi: number = 0;
  private rssiTopicSubscription: Subscription;

  constructor(private authService: AuthService, private nodeService: NodeService, private route: ActivatedRoute, private httpClient: HttpClient, public utilService: UtilService, private alertService: AlertService, private confirmDialogService: ConfirmDialogService, private router: Router, private rxStompService: RxStompService) { }

  ngOnInit() {
    let me = this;
    this.authService.redirectIfNotAuthenticated();

    this.route.paramMap.subscribe(params => {
      this.chipid = Number(params.get('id'));
      this.loadNode();
    });

    this.rssiTopicSubscription = this.rxStompService.watch('/topic/rssi').subscribe((message: Message) => {
      console.log("got new rssi message", message);
      let rssidata = JSON.parse(message.body);
      if (rssidata.chipid == me.chipid) {
        me.node.rssi = rssidata.rssi;
      }
    });

  }

  ngOnDestroy() {
    this.rssiTopicSubscription.unsubscribe();
  }

  loadNode() {
    this.nodeService.loadNode(this.chipid, (nodeDeviceData: NodeDeviceData) => {
      this.node = nodeDeviceData;
      this.getCellCount();
      this.loadProfiles();
      this.calculateRssiLimits();
    }, (message) => {
      console.log('cannot load node: ', message);
      this.alertService.error('cannot load node: ' + message, 'load error');
    });
  }

  openScan() {
    this.router.navigate(['scan', this.node.chipId]);
  };

  updateFrequency() {
    this.node.frequency = this.node.frequencyObj.frequency;
  }

  rebootDevice() {
    let me = this;
    let params: HttpParams = new HttpParams().set('chipid', String(this.chipid));
    this.httpClient.get<StatusResponse>('/api/auth/node/reboot', { params: params }).subscribe(response => {
      this.utilService.statusResponseHandler(response, () => {
        me.router.navigateByUrl('/nodes');
      }, (message) => {
        me.alertService.error('failed to reboot device: ' + message, 'reboot failed');
      });
    }, error => {
      console.log('cannot reboot node: ', error);
      this.alertService.error('cannot reboot node: ' + error, 'reboot error');
    });
  }

  restoreFactoryDefaultsDevice() {
    let me = this;
    me.confirmDialogService.confirmYesNo('really restore factory defaults?', 'do you really want to restore the factory defaults for the node "' + String(me.chipid) + '"?<br /><br /><b>attention:</b> all data except the default voltage reference will be reset!', function () {
      let params: HttpParams = new HttpParams().set('chipid', String(me.chipid));
      me.httpClient.get<StatusResponse>('/api/auth/node/factorydefaults', { params: params }).subscribe(response => {
        me.utilService.statusResponseHandler(response, () => {
          me.router.navigateByUrl('/nodes');
        }, (message) => {
          me.alertService.error('failed to restore factory defaults for device: ' + message, 'reboot failed');
        });
      }, error => {
        console.log('cannot reboot node: ', error);
        me.alertService.error('cannot restore factory defaults node: ' + error, 'factory defaults error');
      });
    }, function () { });
  }

  calculateRssiLimits() {
    this.upperRssi = (this.node.triggerValue < 0) ? 0 : this.node.triggerValue;
    let lowerRssi = this.node.triggerValue - this.node.triggerThreshold;
    this.lowerRssi = (lowerRssi < 0) ? 0 : lowerRssi;
  };

  getCellCount() {
    if (this.node.voltage > 7 && this.node.voltage <= 10) {
      this.cells = 2;
    } else if (this.node.voltage > 10 && this.node.voltage <= 13) {
      this.cells = 3;
    } else if (this.node.voltage > 13 && this.node.voltage <= 17) {
      this.cells = 4;
    } else if (this.node.voltage > 17 && this.node.voltage <= 21.5) {
      this.cells = 5;
    } else if (this.node.voltage > 21.5 && this.node.voltage <= 26) {
      this.cells = 6;
    }
  }

  skipCalibration() {
    this.nodeService.setState(this.chipid, 'CALIBRATION_DONE', () => {
      this.loadNode();
    }, (message) => {
      this.alertService.error('cannot skip calibration state: ' + message);
    });
  }

  backToCalibration() {
    this.nodeService.setState(this.chipid, 'CALIBRATION', () => {
      this.loadNode();
    }, (message) => {
      this.alertService.error('cannot set calibration state: ' + message);
    });
  }

  restoreOldState() {
    this.nodeService.setState(this.chipid, 'RESTORE_STATE', () => {
      this.loadNode();
    }, (message) => {
      this.alertService.error('cannot restore state: ' + message);
    });
  };

  saveDeviceData() {
    this.utilService.toggleOverlay(true);
    let me = this;
    this.httpClient.post<StatusResponse>('/api/auth/node', me.node, {})
      .pipe(timeout(5000), catchError(error => {
        this.alertService.error('error during connecting to the server.', 'server error');
        return of(null);
      })).subscribe(response => {
        me.utilService.statusResponseHandler(response, () => {
          if (response.status == "OK reboot") {
            this.confirmDialogService.confirmOK('rebooting', 'the node is rebooted to apply the settings.', function () {
              me.rebootDevice();
            });
          } else {
            this.alertService.success('all values successfully saved.', undefined, false, false, 2);
            me.loadNode();
          }
        }, (message) => {
          this.alertService.error('failed to store data: ' + message, 'store failed');
        });
        this.utilService.toggleOverlay(false);
      });
  };

  createProfileTooltip(profile) {
    let tooltip = '';
    tooltip += 'trigger value: ' + profile.triggerValue + '\n';
    tooltip += 'trigger threshold: ' + profile.triggerThreshold + '\n';
    tooltip += 'trigger threshold calibration: ' + profile.triggerThresholdCalibration + '\n';
    tooltip += 'calibration offset: ' + profile.calibrationOffset + '\n';
    tooltip += 'minimum lap time: ' + profile.minimumLapTime + '\n';
    tooltip += 'filter ratio: ' + profile.filterRatio + '\n';
    tooltip += 'filter ratio calibration: ' + profile.filterRatioCalibration;
    return tooltip;
  };

  loadProfile(data: Profile, apply: boolean = false) {
    this.node.minimumLapTime = data.minimumLapTime;
    this.node.triggerThreshold = data.triggerThreshold;
    this.node.triggerThresholdCalibration = data.triggerThresholdCalibration;
    this.node.calibrationOffset = data.calibrationOffset;
    this.node.triggerValue = data.triggerValue;
    this.node.filterRatio = data.filterRatio;
    this.node.filterRatioCalibration = data.filterRatioCalibration;
    this.calculateRssiLimits();
    if (apply != undefined && apply == true) {
      this.saveDeviceData();
    }
  };

  loadProfiles() {
    this.profiles = [];
    let params: HttpParams = new HttpParams().set('chipid', String(this.chipid));
    let me = this;
    this.httpClient.get<ProfilesRaw[]>('/api/auth/pilot/profiles', { params: params }).subscribe(response => {
      response.forEach(function (profile, index) {
        let profileParsed: Profile = JSON.parse(profile.data);
        profileParsed.name = profile.name;
        me.profiles.push(profileParsed);
      });
    }, error => {
      console.log('cannot load profiles: ', error);
      this.alertService.warning('cannot load profiles');
    });
  }

  deleteProfile(name: string) {
    let me = this;
    this.confirmDialogService.confirmYesNo('really delete?', 'do you really want to delete the profile "' + name + '"?', function () {
      me.utilService.toggleOverlay(true);
      let params: HttpParams = new HttpParams().set('chipid', String(me.chipid)).set('name', name);
      me.httpClient.delete<StatusResponse>('/api/auth/pilot/profile', { params: params })
        .pipe(timeout(5000), catchError(error => {
          me.alertService.error('error during connecting to the server.', 'server error');
          return of(null);
        })).subscribe(response => {
          me.utilService.statusResponseHandler(response, () => {
            me.alertService.success('profile ' + name + ' deleted.', undefined, false, false, 2);
          }, (message) => {
            me.alertService.error('failed to delete profile: ' + message, 'delete failed');
          });
          me.loadProfiles();
          me.utilService.toggleOverlay(false);
        });
    }, function () { })
  };

  createOrUpdateProfile(name: string = undefined) {
    let me = this;
    this.utilService.toggleOverlay(true);
    let data = {
      minimumLapTime: this.node.minimumLapTime,
      triggerThreshold: this.node.triggerThreshold,
      triggerThresholdCalibration: this.node.triggerThresholdCalibration,
      calibrationOffset: this.node.calibrationOffset,
      triggerValue: this.node.triggerValue,
      filterRatio: this.node.filterRatio,
      filterRatioCalibration: this.node.filterRatioCalibration
    };
    let useName = this.newProfileName;
    if (name !== undefined) {
      console.log('update using name:' + name);
      useName = name;
    }
    this.httpClient.post<StatusResponse>('/api/auth/pilot/profile', {
      data: JSON.stringify(data),
      chipId: this.chipid,
      name: useName
    }, {})
      .pipe(timeout(5000), catchError(error => {
        this.alertService.error('error during connecting to the server.', 'server error');
        return of(null);
      })).subscribe(response => {
        me.utilService.statusResponseHandler(response, () => {
          this.alertService.success('profile ' + useName + ' created / updated.', undefined, false, false, 2);
        }, (message) => {
          this.alertService.error('failed to create profile: ' + message, 'failed');

        });
        this.loadProfiles();
        this.utilService.toggleOverlay(false);
      });

  }

  public get cells(): number {
    return this._cells;
  }
  public set cells(value: number) {
    this._cells = value;
  }
  public get loadingDeviceData(): boolean {
    return this._loadingDeviceData;
  }
  public set loadingDeviceData(value: boolean) {
    this._loadingDeviceData = value;
  }
  public get newProfileName(): string {
    return this._newProfileName;
  }
  public set newProfileName(value: string) {
    this._newProfileName = value;
  }
  public get profiles(): Profile[] {
    return this._profiles;
  }
  public set profiles(value: Profile[]) {
    this._profiles = value;
  }
  public get upperRssi(): number {
    return this._upperRssi;
  }
  public set upperRssi(value: number) {
    this._upperRssi = value;
  }
  public get lowerRssi(): number {
    return this._lowerRssi;
  }
  public set lowerRssi(value: number) {
    this._lowerRssi = value;
  }
  public get chipid(): number {
    return this._chipid;
  }
  public set chipid(value: number) {
    this._chipid = value;
  }
  public get node(): NodeDeviceData {
    return this._node;
  }
  public set node(value: NodeDeviceData) {
    this._node = value;
  }
}
