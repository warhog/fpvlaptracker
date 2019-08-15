import { Component, OnInit } from '@angular/core';
import { AuthService } from '../auth.service';
import { RxStompService } from '@stomp/ng2-stompjs';
import { Message } from '@stomp/stompjs';
import { HttpClient } from '@angular/common/http';
import { AlertService } from '../alert.service';
import { Howl } from 'howler'

interface AlertMessage {
  type: string;
  headline: string;
  text: string;
  permanent: boolean;
}

interface SpeechMessage {
  text: string;
  language: string;
}

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css']
})
export class HeaderComponent implements OnInit {

  private _pilotCount: number = 0;
  private _nodeCount: number = 0;
  private _raceState: string = "-";
  private audioMap: Howl[] = [];

  constructor(private auth: AuthService, private rxStompService: RxStompService, private httpClient: HttpClient, private alertService: AlertService) { }

  private playAudioFile(file: string, repeat: number = 1) {
    this.audioMap[file].play();
    if (repeat && repeat > 1) {
      let duration = this.audioMap[file].duration();
      for (let i = 1; i < repeat; i++) {
        window.setTimeout(() => {
          this.audioMap[file].play();
        }, duration * 1250 * i);
      }
    }
  }

  ngOnInit() {
    this.rxStompService.watch('/topic/race/state').subscribe((message: Message) => {
      console.log("got new race state message", message);
      this.raceState = JSON.parse(message.body);
    });
    this.rxStompService.watch('/topic/pilots/count').subscribe((message: Message) => {
      console.log("got new pilot count message", message);
      this.pilotCount = Number(JSON.parse(message.body));
    });
    this.rxStompService.watch('/topic/nodes/count').subscribe((message: Message) => {
      console.log("got new node count message", message);
      this.nodeCount = Number(JSON.parse(message.body));
    });
    this.rxStompService.watch('/topic/alert').subscribe((message: Message) => {
      console.log("got new alert message", message);
      let alert: AlertMessage = JSON.parse(message.body);
      this.alertService.generateAlert(alert.text, alert.headline, false, alert.type, alert.permanent);
    });
    this.rxStompService.watch('/topic/audio').subscribe((message: Message) => {
      console.log("got new audio message", message);
      let me = this;
      let data = JSON.parse(message.body);
      let file = data.file;
      if (!(file in this.audioMap)) {
        this.audioMap[file] = new Howl({ src: ['assets/' + file] });
        this.audioMap[file].load();
        this.audioMap[file].on('load', () => {
          console.log('loaded');
          this.playAudioFile(data.file, data.repeat);
        });
      } else {
        this.playAudioFile(data.file, data.repeat);
      }
    });
    this.rxStompService.watch('/topic/speech').subscribe((message: Message) => {
      console.log("got new speech message", message);
      let speech: SpeechMessage = JSON.parse(message.body);
      if (typeof speechSynthesis === 'undefined') {
        return;
      }
      let utterance = new SpeechSynthesisUtterance(speech.text);
      utterance.lang = speech.language;
      // speak is blocking :(
      setTimeout(function () {
        speechSynthesis.speak(utterance);
      });
    });

    this.httpClient.get<BadgeData>('/api/badgedata', {}).subscribe(response => {
      console.log('got badgedata: ', response);
      this.pilotCount = response.pilots;
      this.raceState = response.state;
      this.nodeCount = response.nodes;
    }, error => {
      console.log('cannot get badgedata: ' + error);
    });

  }

  isAuthenticated(): boolean {
    return this.auth.isAuthenticated();
  }

  logout() {
    this.auth.logout();
  }

  public get nodeCount(): number {
    return this._nodeCount;
  }
  public set nodeCount(value: number) {
    this._nodeCount = value;
  }
  public get raceState(): string {
    return this._raceState;
  }
  public set raceState(value: string) {
    this._raceState = value;
  }
  public get pilotCount(): number {
    return this._pilotCount;
  }
  public set pilotCount(value: number) {
    this._pilotCount = value;
  }
}

interface BadgeData {
  pilots: number;
  state: string;
  nodes: number;
}
