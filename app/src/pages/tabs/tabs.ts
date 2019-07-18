import { Component } from '@angular/core';

import { SettingsPage } from '../settings/settings';
import { HomePage } from '../home/home';
import { RacePage } from '../race/race';
import { DevicePage } from '../device/device';

@Component({
  templateUrl: 'tabs.html'
})
export class TabsPage {

  tab1Root = HomePage;
  tab2Root = DevicePage;
  tab3Root = RacePage;
  tab4Root = SettingsPage;

  constructor() {

  }
}
