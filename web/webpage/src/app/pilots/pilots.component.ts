import { Component, OnInit } from '@angular/core';
import { Pilot } from '../interfaces/pilot';
import { RxStompService } from '@stomp/ng2-stompjs';
import { Router } from '@angular/router';
import { Message } from '@stomp/stompjs';
import { UtilService } from '../util.service';
import { Subscription, race } from 'rxjs';
import { AlertService } from '../alert.service';
import { NodeService } from '../node.service';
import { PilotsService } from '../pilots.service';
import { NodeDeviceData } from '../nodedevicedata';
import { AuthService } from '../auth.service';

@Component({
  selector: 'app-pilots',
  templateUrl: './pilots.component.html',
  styleUrls: ['./pilots.component.css']
})
export class PilotsComponent implements OnInit {

  private _pilots: Pilot[] = [];
  public addPilot: boolean = false;
  private pilotCountTopicSubscription: Subscription;
  private nodes: NodeDeviceData[] = [];
  private _newPilot: Pilot = {
    name: ''
  };
  private _authenticated: boolean = false;

  constructor(private authService: AuthService, private pilotService: PilotsService, private nodeService: NodeService, private rxStompService: RxStompService, private router: Router, private utilService: UtilService, private alertService: AlertService) { }

  ngOnInit() {
    this.authenticated = this.authService.isAuthenticated();
    this.pilotCountTopicSubscription = this.rxStompService.watch('/topic/pilots/count').subscribe((message: Message) => {
      console.log("got new pilot count message", message);
      this.loadPilots();
    });

    this.nodeService.loadNodes((nodes) => {
      this.nodes = nodes;
      this.loadPilots();
    }, (message) => {
      this.alertService.error('cannot load the nodes: ' + message, 'load error');
    });
  }

  pilotSetup(name: string) {
    this.router.navigate(['pilotsetup', name]);
  }

  ngOnDestroy() {
    this.pilotCountTopicSubscription.unsubscribe();
  }

  getFrequencyForChipId(chipId: number): string {
    let filteredNodes: NodeDeviceData[] = this.nodes.filter((node) => node.chipId == chipId);
    return (filteredNodes.length > 0) ? filteredNodes[0].frequencyObj.name : '';
  }

  loadPilots() {
    this.pilotService.loadPilots((pilots) => {
      this.pilots = pilots;
    }, (message) => {
      console.log('cannot load pilots: ', message);
      this.alertService.error('cannot load the pilots: ' + message, 'pilot load error');
    });
  }

  createNewPilot() {
    this.utilService.toggleOverlay(true);
    this.pilotService.createPilot(this.newPilot, () => {
      this.pilotSetup(this.newPilot.name);
      this.utilService.toggleOverlay(false);
    }, (message) => {
      this.alertService.error(message, 'creation failed');
      this.utilService.toggleOverlay(false);
    });
  }

  public get pilots(): Pilot[] {
    return this._pilots;
  }
  public set pilots(value: Pilot[]) {
    this._pilots = value;
  }

  public get newPilot(): Pilot {
    return this._newPilot;
  }
  public set newPilot(value: Pilot) {
    this._newPilot = value;
  }
  public get authenticated(): boolean {
    return this._authenticated;
  }
  public set authenticated(value: boolean) {
    this._authenticated = value;
  }

}
