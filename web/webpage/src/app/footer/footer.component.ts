import { Component, OnInit } from '@angular/core';
import { RxStompService } from '@stomp/ng2-stompjs';
import { Message } from '@stomp/stompjs';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-footer',
  templateUrl: './footer.component.html',
  styleUrls: ['./footer.component.css']
})
export class FooterComponent implements OnInit {

  constructor(private rxStompService: RxStompService, private httpClient: HttpClient) { }

  private _udpStatus: string = 'unavailable';
  private _udpStatusClasses: string = '';
  private _version: string = '-';

  ngOnInit() {
    this.rxStompService.watch('/topic/status').subscribe((message: Message) => {
      console.log("got new status message", message);
      let status = JSON.parse(message.body);
      if (status.udp !== undefined) {
        this.udpStatus = status.udp;
        if (status.udp == "down") {
          this.udpStatusClasses = 'blink bold';
        } else {
          this.udpStatusClasses = '';
        }
      } else {
        this.udpStatusClasses = 'blink bold';
        this.udpStatus = 'unavailable';
      }
    });

    this.httpClient.get<VersionInfo>('/api/version', {}).subscribe(response => {
      console.log("version response", response);
      this.version = response.version;
    }, error => {
      console.log('version error', error);
    });

  }

  public get udpStatus(): string {
    return this._udpStatus;
  }
  public set udpStatus(value: string) {
    this._udpStatus = value;
  }
  public get udpStatusClasses(): string {
    return this._udpStatusClasses;
  }
  public set udpStatusClasses(value: string) {
    this._udpStatusClasses = value;
  }
  public get version(): string {
    return this._version;
  }
  public set version(value: string) {
    this._version = value;
  }

}

interface VersionInfo {
  version: string;
}