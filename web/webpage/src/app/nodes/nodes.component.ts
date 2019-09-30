import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { RxStompService } from '@stomp/ng2-stompjs';
import { Message } from '@stomp/stompjs';
import { Subscription } from 'rxjs';
import { Router } from '@angular/router';
import { AlertService } from '../alert.service';
import { NodeDeviceData } from '../nodedevicedata';
import { NodeService } from '../node.service';
import { AuthService } from '../auth.service';
import { UtilService } from '../util.service';

@Component({
  selector: 'app-nodes',
  templateUrl: './nodes.component.html',
  styleUrls: ['./nodes.component.css']
})
export class NodesComponent implements OnInit {

  private _nodes: NodeDeviceData[] = [];
  private nodesCountTopicSubscription: Subscription;
  private _authenticated: boolean = false;

  constructor(private authService: AuthService, private nodeService: NodeService, private alertService: AlertService, private httpClient: HttpClient, private rxStompService: RxStompService, private router: Router, private utilService: UtilService) { }

  ngOnInit() {
    this.authenticated = this.authService.isAuthenticated();
    this.nodesCountTopicSubscription = this.rxStompService.watch('/topic/nodes/count').subscribe((message: Message) => {
      console.log("got new node count message", message);
      this.loadNodes();
    });

    this.loadNodes();

  }

  nodeSetup(chipId: number) {
    this.router.navigate(['nodesetup', chipId]);
  }

  ngOnDestroy() {
    this.nodesCountTopicSubscription.unsubscribe();
  }

  loadNodes() {
    this.nodeService.loadNodes((nodes) => {
      this.nodes = nodes;
    }, (message) => {
      console.log('cannot load nodes: ', message);
      this.alertService.error('cannot load the nodes: ' + message, 'node load error');
    }, true);
  }

  public get nodes(): NodeDeviceData[] {
    return this._nodes;
  }
  public set nodes(value: NodeDeviceData[]) {
    this._nodes = value;
  }
  public get authenticated(): boolean {
    return this._authenticated;
  }
  public set authenticated(value: boolean) {
    this._authenticated = value;
  }
}
