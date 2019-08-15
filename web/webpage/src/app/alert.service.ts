import { Injectable, ModuleWithComponentFactories } from '@angular/core';
import { Router, NavigationStart } from '@angular/router';
import { Subject, Observable } from 'rxjs';
import { Alert } from './interfaces/alert';
import * as moment from 'moment';

@Injectable({
  providedIn: 'root'
})
export class AlertService {

  private subject: Subject<Alert> = new Subject<Alert>();
  private keepAfterRouteChange: boolean = false;

  constructor(private router: Router) {
    // clear alert messages on route change unless 'keepAfterRouteChange' flag is true
    this.router.events.subscribe(event => {
      if (event instanceof NavigationStart) {
        if (this.keepAfterRouteChange) {
          // only keep for a single route change
          this.keepAfterRouteChange = false;
        } else {
          // clear alert message
          this.clear();
        }
      }
    });
  }

  getAlert(): Observable<Alert> {
    return this.subject.asObservable();
  }

  success(message: string, title: string = '', keepAfterRouteChange: boolean = false, permanent: boolean = false, timeToShow: number = 5) {
    this.generateAlert(message, title, keepAfterRouteChange, 'success', permanent, timeToShow);
  }

  error(message: string, title: string = '', keepAfterRouteChange: boolean = false, permanent: boolean = false, timeToShow: number = 5) {
    this.generateAlert(message, title, keepAfterRouteChange, 'error', permanent, timeToShow);
  }

  warning(message: string, title: string = '', keepAfterRouteChange: boolean = false, permanent: boolean = false, timeToShow: number = 5) {
    this.generateAlert(message, title, keepAfterRouteChange, 'warning', permanent, timeToShow);
  }

  generateAlert(message: string, title: string = '', keepAfterRouteChange: boolean = false, type: string = 'success', permanent: boolean = false, timeToShow: number = 5) {
    let alert: Alert = {
      type: type,
      message: message,
      title: title,
      permanent: permanent,
      timeout: moment().unix() + timeToShow
    };
    this.keepAfterRouteChange = keepAfterRouteChange;
    this.subject.next(alert);
  }

  clear() {
    // clear by calling subject.next() without parameters
    this.subject.next();
  }
}
