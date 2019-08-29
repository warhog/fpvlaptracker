import { Component, ViewChildren, QueryList } from '@angular/core';

import { SettingsPage } from '../settings/settings';
import { HomePage } from '../home/home.page';
import { RacePage } from '../race/race';
import { DevicePage } from '../device/device';
import { Platform, IonRouterOutlet } from '@ionic/angular';
import { Router } from '@angular/router';

@Component({
  selector: 'app-tabs',
  templateUrl: 'tabs.html'
})
export class TabsPage {

  @ViewChildren(IonRouterOutlet) routerOutlets: QueryList<IonRouterOutlet>;
  
  constructor(private platform: Platform, private router: Router) {
    // subscription to native back button
    this.platform.backButton.subscribe(() => {
      this.routerOutlets.forEach((outlet: IonRouterOutlet) => {
        if (outlet && outlet.canGoBack()) {
          outlet.pop();
        }
      });
    });
  }
}
