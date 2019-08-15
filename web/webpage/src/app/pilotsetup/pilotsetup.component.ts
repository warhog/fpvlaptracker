import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpParams, HttpClient } from '@angular/common/http';
import { Pilot } from '../interfaces/pilot';
import { AlertService } from '../alert.service';
import { ConfirmDialogService } from '../confirm-dialog/confirm-dialog.service';
import { UtilService } from '../util.service';
import { StatusResponse } from '../status-response';
import { timeout, catchError } from 'rxjs/operators';
import { of, Subscription } from 'rxjs';
import { AuthService } from '../auth.service';
import { NodeDeviceData } from '../nodedevicedata';
import { NodeService } from '../node.service';
import { PilotsService } from '../pilots.service';

@Component({
  selector: 'app-pilotsetup',
  templateUrl: './pilotsetup.component.html',
  styleUrls: ['./pilotsetup.component.css']
})
export class PilotsetupComponent implements OnInit {

  private _pilot: Pilot = {
    name: ''
  };
  private _availableNodes: NodeDeviceData[] = [];

  constructor(private pilotService: PilotsService, private nodeService: NodeService, private authService: AuthService, private router: Router, private confirmDialogService: ConfirmDialogService, private utilService: UtilService, private httpClient: HttpClient, private route: ActivatedRoute, private alertService: AlertService) { }

  ngOnInit() {
    this.authService.redirectIfNotAuthenticated();

    this.route.paramMap.subscribe(params => {
      this.pilot.name = params.get('name');
      this.loadPilot();

      this.nodeService.loadNodes((nodes) => {
        this.pilotService.getChipIds((chipIds) => {
          this.availableNodes = nodes.filter((node) => this.pilot.chipId == node.chipId || chipIds.indexOf(node.chipId) == -1);
        }, (message) => {
          this.alertService.error('cannot load the used chpiids: ' + message, 'load error');
        });
      }, (message) => {
        this.alertService.error('cannot load the nodes: ' + message, 'node load error');
      });
    });
  }

  loadPilot() {
    this.pilotService.loadPilot(this.pilot.name, (pilot) => {
      this.pilot = pilot;
      this.pilot.unmodifiedName = this.pilot.name;
      if (this.pilot.chipId == null) {
        this.pilot.chipId = 0;
      }
    }, (message) => {
      this.alertService.error('cannot load pilot "' + this.pilot.name + '": ' + message, 'pilot load error');
    });
  }

  save() {
    this.utilService.toggleOverlay(true);
    let me = this;
    if (this.pilot.chipId == 0) {
      this.pilot.chipId = null;
    }

    // TODO put to service
    this.httpClient.post<StatusResponse>('/api/auth/pilot', me.pilot, {})
      .pipe(timeout(5000), catchError(error => {
        this.alertService.error('error during connecting to the server.', 'server error');
        return of(null);
      })).subscribe(response => {
        me.utilService.statusResponseHandler(response, () => {
          me.alertService.success('user updated');
          me.loadPilot();
        }, (message) => {
          this.alertService.error('failed to create new user: ' + message, 'store failed');
        });
        this.utilService.toggleOverlay(false);
      });
  }

  delete() {
    let me = this;
    this.confirmDialogService.confirmYesNo('really delete?', 'do you really want to delete the user "' + this.pilot.name + '"?', function () {
      me.utilService.toggleOverlay(true);
      // TODO put to service
      let params: HttpParams = new HttpParams().set('name', me.pilot.name);
      me.httpClient.delete<StatusResponse>('/api/auth/pilot', { params: params })
        .pipe(timeout(5000), catchError(error => {
          me.alertService.error('error during connecting to the server.', 'server error');
          return of(null);
        })).subscribe(response => {
          me.utilService.statusResponseHandler(response, () => {
            me.alertService.success('user ' + me.pilot.name + ' deleted.', undefined, false, false, 2);
            me.router.navigateByUrl('/pilots');
          }, (message) => {
            me.alertService.error('failed to delete user: ' + message, 'delete failed');
          });
          me.utilService.toggleOverlay(false);
        });
    }, function () { })
  }

  public get pilot(): Pilot {
    return this._pilot;
  }
  public set pilot(value: Pilot) {
    this._pilot = value;
  }
  public get availableNodes(): NodeDeviceData[] {
    return this._availableNodes;
  }
  public set availableNodes(value: NodeDeviceData[]) {
    this._availableNodes = value;
  }
}
