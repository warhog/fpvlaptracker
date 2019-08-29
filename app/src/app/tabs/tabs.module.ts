import { IonicModule } from '@ionic/angular';
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { TabsPageRoutingModule } from './tabs.router.module';

import { TabsPage } from './tabs';
import { DevicePage } from '../device/device';
import { HomePage } from '../home/home.page';
import { SettingsPage } from '../settings/settings';
import { RacePage } from '../race/race';
import { BluetoothPage } from '../bluetooth/bluetooth';
import { HelpPage } from '../help/help';
import { ScannerPage } from '../scanner/scanner';

@NgModule({
  imports: [
    IonicModule,
    CommonModule,
    FormsModule,
    TabsPageRoutingModule
  ],
  declarations: [
    TabsPage,
    DevicePage,
    HomePage,
    SettingsPage,
    RacePage,
    BluetoothPage,
    HelpPage,
    ScannerPage
  ]
})
export class TabsPageModule {}