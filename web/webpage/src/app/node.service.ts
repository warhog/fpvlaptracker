import { Injectable } from '@angular/core';
import { HttpParams, HttpClient } from '@angular/common/http';
import { StatusResponse } from './status-response';
import { UtilService } from './util.service';
import { NodeDeviceData } from './nodedevicedata';

@Injectable({
  providedIn: 'root'
})
export class NodeService {

  constructor(private utilService: UtilService, private httpClient: HttpClient) { }

  loadNodes(fnSuccess: (nodes: NodeDeviceData[]) => void, fnFailure: (message: string) => void, update: boolean = false) {
    let me = this;
    let params: HttpParams = new HttpParams();
    if (update) {
      params = new HttpParams().set('update', String(update));
    }
    this.httpClient.get<NodeDeviceData[]>('/api/nodes', { params: params }).subscribe(nodes => {
      nodes.forEach(function(node) {
        node.frequencyObj = me.utilService.getFrequencyObject(node.frequency);
      });
      fnSuccess && fnSuccess(nodes);
    }, error => {
      console.log('cannot load nodes: ', error);
      fnFailure && fnFailure(error);
    });
  }

  setState(chipId: number, state: string, fnSuccess: () => void, fnFailure: (message: string) => void) {
    let params: HttpParams = new HttpParams().set('chipid', String(chipId)).set('state', state);
    this.httpClient.get<StatusResponse>('/api/auth/node/setstate', { params: params }).subscribe(response => {
      this.utilService.statusResponseHandler(response, () => {
        fnSuccess && fnSuccess();
      }, (message) => {
        fnFailure && fnFailure(message);
      });
    }, error => {
      console.log('cannot reboot node: ', error);
      fnFailure && fnFailure(error);
    });
  }

  loadNode(chipId: number, fnSuccess: (nodedevicedata: NodeDeviceData) => void, fnFailure: (message: string) => void) {
    let params: HttpParams = new HttpParams().set('chipid', String(chipId));
    this.httpClient.get<NodeDeviceData>('/api/node', { params: params }).subscribe(node => {
      node.frequencyObj = this.utilService.getFrequencyObject(node.frequency);
      fnSuccess && fnSuccess(node);
    }, error => {
      console.log('cannot load node: ', error);
      fnFailure && fnFailure(error);
    });
  }
  
}
