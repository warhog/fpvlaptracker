import { Component, OnInit } from '@angular/core';
import { AlertService } from '../alert.service';
import { Subscription, interval, timer } from 'rxjs';
import { Alert } from '../interfaces/alert';
import * as moment from 'moment';

@Component({
  selector: 'alert',
  templateUrl: './alert.component.html',
  styleUrls: ['./alert.component.css']
})
export class AlertComponent implements OnInit {

  private subscription: Subscription;
  private _alerts: Alert[] = [];
  
  constructor(private alertService: AlertService) { }

  ngOnInit() {
    this.subscription = this.alertService.getAlert()
      .subscribe(alert => {
        if (alert) {
          let alertClass: string = alert.type;
          if (alertClass == 'error') {
            alertClass = 'danger';
          }
          alert.cssClass = 'alert alert-' + alertClass;
          console.log('pushing alert', alert);
          this.alerts.push(alert);
        } else {
          this.alerts = [];
        }
      });

    const subscribe = interval(1000).subscribe(val => {
      this.gcAlerts()
    });

  }

  gcAlerts() {
    let me = this;
    this.alerts.forEach(function(alert, index) {
      let now: number = moment().unix();
      if (!alert.permanent && now >= alert.timeout) {
        me.closeAlert(index);
      }
    });

  }

  closeAlert(index: number) {
    console.log('closing alert', index);
    this.alerts.splice(index, 1)
  }

  ngOnDestroy() {
    this.subscription.unsubscribe();
  }
  
  public get alerts(): Alert[] {
    return this._alerts;
  }
  public set alerts(value: Alert[]) {
    this._alerts = value;
  }

}
