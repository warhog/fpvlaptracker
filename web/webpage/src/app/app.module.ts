import { BrowserModule } from '@angular/platform-browser';
import { NgModule, Injectable } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClientModule, HttpInterceptor, HttpHandler, HttpRequest, HTTP_INTERCEPTORS, HttpEvent, HttpEventType } from '@angular/common/http';
import { InjectableRxStompConfig, RxStompService, rxStompServiceFactory } from '@stomp/ng2-stompjs';

import { myRxStompConfig } from './my-rx-stomp.config';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { HomeComponent } from './home/home.component';
import { LoginComponent } from './login/login.component';
import { RaceComponent } from './race/race.component';
import { NodesetupComponent } from './nodesetup/nodesetup.component';
import { HeaderComponent } from './header/header.component';
import { FooterComponent } from './footer/footer.component';
import { AuthService } from './auth.service';
import { SettingsComponent } from './settings/settings.component';
import { PilotsComponent } from './pilots/pilots.component';
import { AlertComponent } from './alert/alert.component';
import { Observable, throwError, never, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Router, ActivatedRoute } from '@angular/router';
import { UtilService } from './util.service';
import { NodesComponent } from './nodes/nodes.component';
import { ConfirmDialogComponent } from './confirm-dialog/confirm-dialog.component';
import { ScanComponent } from './scan/scan.component';
import { PilotsetupComponent } from './pilotsetup/pilotsetup.component';
import { RaceFixedTimeComponent } from './race/race-fixed-time/race-fixed-time.component';
import { RaceRoundBasedComponent } from './race/race-round-based/race-round-based.component';
import { PilotStatsComponent } from './race/pilot-stats/pilot-stats.component';
import { RaceStatePipe } from './race/racestate.pipe';
import { DeviceDetectorModule } from 'ngx-device-detector';
import { PilotStatePipe } from './race/pilot-state.pipe';
import { RaceTypePipe } from './race/race-type.pipe';
import { DurationPipe } from './duration.pipe';
import { AutofocusDirective } from './autofocus.directive';
import { FrequencyPipe } from './frequency.pipe';

@Injectable()
export class XhrInterceptor implements HttpInterceptor {
  constructor(private authService: AuthService, private router: Router, private utilService: UtilService, private route: ActivatedRoute) { }

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const xhr = request.clone({
      headers: request.headers.set('X-Requested-With', 'XMLHttpRequest')
    });

    return next.handle(xhr).pipe(catchError(err => {
      if (err.status === 401) {
        console.log('XhrInterceptor 401 triggered');
        if (request.url == 'user') {
          console.log('is /user, do not redirect');
        } else {
          this.router.navigate(['login', this.router.url]);
          this.utilService.toggleOverlay(false);
          return new Observable<HttpEvent<any>>();
        }
        const error = err.error.message || err.statusText;
        return throwError(error);
      }
      const error = err.error.message || err.statusText;
      return throwError(error);
    }))
  }
}

@NgModule({
  declarations: [
    AppComponent,
    HomeComponent,
    LoginComponent,
    RaceComponent,
    NodesetupComponent,
    HeaderComponent,
    FooterComponent,
    SettingsComponent,
    PilotsComponent,
    AlertComponent,
    NodesComponent,
    ConfirmDialogComponent,
    ScanComponent,
    PilotsetupComponent,
    RaceFixedTimeComponent,
    RaceRoundBasedComponent,
    PilotStatsComponent,
    RaceStatePipe,
    PilotStatePipe,
    RaceTypePipe,
    DurationPipe,
    AutofocusDirective,
    FrequencyPipe
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    FormsModule,
    HttpClientModule,
    DeviceDetectorModule.forRoot()
  ],
  providers: [AuthService,
    {
      provide: HTTP_INTERCEPTORS,
      useClass: XhrInterceptor,
      multi: true
    }, {
      provide: InjectableRxStompConfig,
      useValue: myRxStompConfig
    }, {
      provide: RxStompService,
      useFactory: rxStompServiceFactory,
      deps: [InjectableRxStompConfig]
    }
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
