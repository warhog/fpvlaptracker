import { Component } from '@angular/core';
import { AuthService } from './auth.service';
import { Subscription, Observable } from 'rxjs';
import { UtilService } from './util.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {

  private subscriptionOverlay: Subscription = null;
  private overlay: boolean = false;

  getOverlay() {
    return this.overlay;
  }

  constructor(private auth: AuthService, private util: UtilService) {
    this.auth.authenticate(undefined);

    let obs: Observable<boolean> = util.getOverlayObservable();
    if (obs) {
      this.subscriptionOverlay = obs.subscribe(overlay => {
        this.overlay = overlay;
      });
    }
  }

}
