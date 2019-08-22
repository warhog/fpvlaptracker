import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Credentials } from './interfaces/credentials'
import { Router } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private authenticated: boolean = false;
  private admin: boolean = false;
  private principal: any = null;

  constructor(private httpClient: HttpClient, private router: Router) { }

  authenticate(credentials: Credentials, rememberMe: boolean = false, callback: Function = undefined, callbackFailure: Function = undefined) {

    let headers = new HttpHeaders(credentials ? {
      authorization: 'Basic ' + btoa(credentials.username + ':' + credentials.password)
    } : {});

    let params: HttpParams = new HttpParams().set('remember-me', (rememberMe ? 'true' : 'false'));

    this.httpClient.get<any>('user', { headers: headers, params: params }).subscribe(response => {
      this.handleAuthData(response);
      return callback && callback();
    }, error => {
      return callbackFailure && callbackFailure();
    });

  }

  private handleAuthData(response) {
    if (response['name']) {
      this.authenticated = true;
      this.principal = response['principal'];
    } else {
      this.authenticated = false;
      this.principal = null;
    }
    if (this.authenticated == true && this.principal != null) {
      if (this.principal && this.principal.authorities) {
        let entries = this.principal.authorities.filter(function (entry) {
          return entry.authority == 'ROLE_ADMIN';
        });
        this.admin = (entries.length > 0) ? true : false
      } else {
        this.admin = false;
      }
    }
  }

  logout() {
    this.httpClient.post('logout', {}).subscribe(response => {
      this.authenticated = false;
      this.principal = null;
      this.router.navigateByUrl('/');
    });
  }

  redirectIfNotAuthenticated() {
    if (!this.isAuthenticated()) {
      this.router.navigate(['login', this.router.url]);
    }
  }

  isAuthenticated(): boolean {
    return this.authenticated;
  }

  isAdmin(): boolean {
    return this.admin;
  }

}
