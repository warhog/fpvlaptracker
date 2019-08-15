import { Component, OnInit } from '@angular/core';
import { AuthService } from '../auth.service';
// import { HttpClient } from 'selenium-webdriver/http';
import { Router, ActivatedRoute } from '@angular/router';
import { Credentials } from '../interfaces/credentials';
import { HttpClient } from '@angular/common/http';
import { AlertService } from '../alert.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {

  private _credentials: Credentials = { username: '', password: '' };
  private _rememberMe: boolean = false;
  private _loading: boolean = false;
  private path: string = '/';

  constructor(private auth: AuthService, private router: Router, private http: HttpClient, private alertService: AlertService, private route: ActivatedRoute) { }

  ngOnInit() {
    this.route.paramMap.subscribe(params => {
      this.path = params.get('path');
    });
  }

  login() {
    this.loading = true;
    this.alertService.clear();
    this.auth.authenticate(this.credentials, this.rememberMe, () => {
      this.loading = false;
      this.router.navigateByUrl(this.path);
    }, () => {
      this.alertService.error('unable to login, maybe wrong credentials?', 'login failed');
      this.loading = false;
    });
    return false;
  }

  public get credentials(): Credentials {
    return this._credentials;
  }
  public set credentials(value: Credentials) {
    this._credentials = value;
  }
  public get rememberMe(): boolean {
    return this._rememberMe;
  }
  public set rememberMe(value: boolean) {
    this._rememberMe = value;
  }
  public get loading(): boolean {
    return this._loading;
  }
  public set loading(value: boolean) {
    this._loading = value;
  }
}
